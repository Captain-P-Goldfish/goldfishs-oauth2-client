package de.captaingoldfish.restclient.application.crypto;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECParameterSpec;
import java.text.ParseException;
import java.util.Optional;

import com.nimbusds.jose.JWECryptoParts;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.ECDHDecrypter;
import com.nimbusds.jose.crypto.ECDHEncrypter;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.crypto.factories.DefaultJWSSignerFactory;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.SignedJWT;

import de.captaingoldfish.restclient.database.entities.Keystore;
import de.captaingoldfish.restclient.database.repositories.KeystoreDao;
import de.captaingoldfish.scim.sdk.common.exceptions.BadRequestException;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;


/**
 * @author Pascal Knueppel
 * @since 25.06.2021
 */
@RequiredArgsConstructor
public class JwtHandler
{

  /**
   * needed to get access to the keys of the application keystore to sign, verify, encrypt or decrypt data
   */
  private final KeystoreDao keystoreDao;

  /**
   * creates either an encrypted JWT (JWE) or a signed JWT (JWS) based on the contents of the JOSE header
   *
   * @param keyId the keyId to use. If left empty the code will look into the header of the JWT and tries to get
   *          the 'kid' value
   * @param header the JOSE header that contains the information on what needs to be done
   * @param body the body that should either be encrypted or signed
   * @return the signed or encrypted JWT
   */
  @SneakyThrows
  public String createJwt(String keyId, String header, String body, JwtAttribute... jwtAttributes)
  {
    try
    {
      JWSHeader jwsHeader = JWSHeader.parse(header);
      JWSHeader effectiveHeader = Optional.ofNullable(jwtAttributes)
                                          .map(attributes -> addAdditionalJwsHeaders(keyId, jwsHeader, attributes))
                                          .orElse(jwsHeader);
      return signJwt(keyId, effectiveHeader, body);
    }
    catch (ParseException e)
    {
      JWEHeader jweHeader = JWEHeader.parse(header);
      JWEHeader effectiveHeader = Optional.ofNullable(jwtAttributes)
                                          .map(attributes -> addAdditionalJweHeaders(keyId, jweHeader, attributes))
                                          .orElse(jweHeader);
      return encryptJwt(keyId, effectiveHeader, body);
    }
  }

  /**
   * @param kid
   * @param jwsHeader
   * @param attributes
   * @return
   */
  @SneakyThrows
  private JWSHeader addAdditionalJwsHeaders(String kid, JWSHeader jwsHeader, JwtAttribute[] attributes)
  {
    String keyId = Optional.ofNullable(kid).orElse(jwsHeader.getKeyID());
    Keystore applicationKeystore = keystoreDao.getKeystore();

    JWSHeader.Builder builder = new JWSHeader.Builder(jwsHeader);

    for ( JwtAttribute attribute : attributes )
    {
      if (attribute == JwtAttribute.X5T_SHA256)
      {
        X509Certificate certificate = applicationKeystore.getCertificate(keyId);
        Base64URL sha256Thumbprint = Base64URL.encode(MessageDigest.getInstance("SHA-256")
                                                                   .digest(certificate.getEncoded()));
        builder.x509CertSHA256Thumbprint(sha256Thumbprint);
      }
      else if (attribute == JwtAttribute.ADD_PUBLIC_KEY)
      {
        builder.jwk(toJwk(applicationKeystore.getKeyPair(keyId)).toPublicJWK());
      }
    }
    return builder.build();
  }

  /**
   * @param kid
   * @param jwsHeader
   * @param attributes
   * @return
   */
  @SneakyThrows
  private JWEHeader addAdditionalJweHeaders(String kid, JWEHeader jwsHeader, JwtAttribute[] attributes)
  {
    String keyId = Optional.ofNullable(kid).orElse(jwsHeader.getKeyID());
    Keystore applicationKeystore = keystoreDao.getKeystore();

    JWEHeader.Builder builder = new JWEHeader.Builder(jwsHeader);

    for ( JwtAttribute attribute : attributes )
    {
      if (attribute == JwtAttribute.X5T_SHA256)
      {
        X509Certificate certificate = applicationKeystore.getCertificate(keyId);
        Base64URL sha256Thumbprint = Base64URL.encode(MessageDigest.getInstance("SHA-256")
                                                                   .digest(certificate.getEncoded()));
        builder.x509CertSHA256Thumbprint(sha256Thumbprint);
      }
      else if (attribute == JwtAttribute.ADD_PUBLIC_KEY)
      {
        builder.jwk(toJwk(applicationKeystore.getKeyPair(keyId)).toPublicJWK());
      }
    }
    return builder.build();
  }

  /**
   * verifies either the signature of a signed JWT (JWS) or tries to decrypt the given JWT (JWE)
   *
   * @param keyId used to verify the signature or to decrypt the JWT. If left empty the application will try to
   *          get the id from the 'kid' field within the header of the JWT
   * @param jwt the signed or encrypted JWT
   * @return the payload of the verified or decrypted content
   */
  @SneakyThrows
  public PlainJwtData handleJwt(String keyId, String jwt)
  {
    final int numberOfJwtParts = jwt.split("\\.").length;
    if (numberOfJwtParts == 3)
    {
      return verifySignature(keyId, jwt);
    }
    else
    {
      return decryptJwt(keyId, jwt);
    }
  }

  /**
   * signs the given body based on the data within the header
   */
  @SneakyThrows
  private String signJwt(String kid, JWSHeader jwsHeader, String body)
  {
    String keyId = Optional.ofNullable(kid).orElse(jwsHeader.getKeyID());
    Keystore applicationKeystore = keystoreDao.getKeystore();
    KeyPair keyPair = applicationKeystore.getKeyPair(keyId);
    return createSignedJwt(keyPair, jwsHeader, body);
  }

  /**
   * verifies the signature based on the data within the header
   */
  @SneakyThrows
  private PlainJwtData verifySignature(String keyId, String jws)
  {
    SignedJWT signedJwt = SignedJWT.parse(jws);
    String effectiveKeyId = Optional.ofNullable(keyId).orElse(signedJwt.getHeader().getKeyID());
    JWSVerifier jwsVerifier = getVerifier(effectiveKeyId, signedJwt.getHeader());
    boolean isValid = signedJwt.verify(jwsVerifier);
    if (!isValid)
    {
      throw new BadRequestException(String.format("Signature validation has failed with signature key '%s'",
                                                  effectiveKeyId));
    }
    return PlainJwtData.builder()
                       .header(signedJwt.getHeader().toString())
                       .body(signedJwt.getPayload().toString())
                       .build();
  }

  /**
   * encrypts the body based on the data within the header
   *
   * @return the encrypted JWT
   */
  @SneakyThrows
  private String encryptJwt(String kid, JWEHeader jweHeader, String body)
  {
    String keyId = Optional.ofNullable(kid).orElse(jweHeader.getKeyID());
    Keystore applicationKeystore = keystoreDao.getKeystore();
    PublicKey publicKey = applicationKeystore.getCertificate(keyId).getPublicKey();
    return createEncryptedJwt(publicKey, jweHeader, body);
  }

  /**
   * decrypts the JWT based on the data within the header
   *
   * @return the decrypted content of the JWT
   */
  @SneakyThrows
  private PlainJwtData decryptJwt(String kid, String jwt)
  {
    EncryptedJWT encryptedJWT = EncryptedJWT.parse(jwt);
    JWEHeader jweHeader = encryptedJWT.getHeader();
    String keyId = Optional.ofNullable(kid).orElse(jweHeader.getKeyID());
    Keystore applicationKeystore = keystoreDao.getKeystore();
    KeyPair keyPair = applicationKeystore.getKeyPair(keyId);
    String plainTextBody = decryptJwt(keyPair, encryptedJWT);
    String jweHeaderString = jweHeader.toString();
    return PlainJwtData.builder().header(jweHeaderString).body(plainTextBody).build();
  }

  /**
   * decrypts the given encrypted JWT with the given keypair
   *
   * @return the plain content of the decrypted JWT
   */
  @SneakyThrows
  private String decryptJwt(KeyPair keyPair, EncryptedJWT encryptedJWT)
  {
    JWEDecrypter jweDecrypter = getJweDecrypter(keyPair);
    encryptedJWT.decrypt(jweDecrypter);
    return encryptedJWT.getPayload().toString();
  }

  /**
   * gets fitting decrypter based on the type of key that was selected
   *
   * @param keyPair the key pair that is used to decrypt the JWT
   */

  @SneakyThrows
  private JWEDecrypter getJweDecrypter(KeyPair keyPair)
  {
    switch (keyPair.getPublic().getAlgorithm())
    {
      case "RSA":
        RSAKey rsaKey = toRsaJwk(keyPair);
        return new RSADecrypter(rsaKey);
      case "EC":
        ECKey ecKey = toEcJwk(keyPair);
        return new ECDHDecrypter(ecKey);
      default:
        throw new IllegalArgumentException(String.format("Cannot sign with key of type '%s'",
                                                         keyPair.getPublic().getAlgorithm()));
    }
  }

  /**
   * builds an encrypted JWT with the given public key based on the data within the header
   *
   * @param publicKey the key used for encrypting
   * @param jweHeader contains the algorithm to use for encryption
   * @param body the body that should be encrypted
   * @return the encrypted JWT
   */
  @SneakyThrows
  private String createEncryptedJwt(PublicKey publicKey, JWEHeader jweHeader, String body)
  {
    JWEEncrypter jweEncrypter = getJweEncrypter(publicKey);
    JWECryptoParts jweCryptoParts = jweEncrypter.encrypt(jweHeader, body.getBytes(StandardCharsets.UTF_8), null);
    EncryptedJWT encryptedJWT = new EncryptedJWT(jweCryptoParts.getHeader().toBase64URL(),
                                                 jweCryptoParts.getEncryptedKey(),
                                                 jweCryptoParts.getInitializationVector(),
                                                 jweCryptoParts.getCipherText(), jweCryptoParts.getAuthenticationTag());
    return encryptedJWT.serialize();
  }

  /**
   * selects an encrypter based on the given key
   *
   * @param publicKey the key that determines the encrypter to use
   */
  @SneakyThrows
  private JWEEncrypter getJweEncrypter(PublicKey publicKey)
  {
    switch (publicKey.getAlgorithm())
    {
      case "RSA":
        RSAKey rsaKey = toRsaJwk(new KeyPair(publicKey, null));
        return new RSAEncrypter(rsaKey);
      case "EC":
        ECKey ecKey = toEcJwk(new KeyPair(publicKey, null));
        return new ECDHEncrypter(ecKey);
      default:
        throw new IllegalArgumentException(String.format("Cannot sign with key of type '%s'",
                                                         publicKey.getAlgorithm()));
    }
  }

  /**
   * builds a signature with the given key and builds the signed JWT from it
   *
   * @param keyPair the key pair used for signing
   * @param jwsHeader contains the algorithm to use for signature
   * @param body the body to sign together with the header
   * @return the signed JWT
   */
  @SneakyThrows
  private String createSignedJwt(KeyPair keyPair, JWSHeader jwsHeader, String body)
  {
    JWK jwk = toJwk(keyPair);
    JWSSigner jwsSigner = new DefaultJWSSignerFactory().createJWSSigner(jwk, jwsHeader.getAlgorithm());
    Payload payload = new Payload(body);
    String headerAndBody = jwsHeader.toBase64URL().toString() + "." + payload.toBase64URL().toString();
    Base64URL signature = jwsSigner.sign(jwsHeader, headerAndBody.getBytes(StandardCharsets.UTF_8));
    SignedJWT jws = new SignedJWT(jwsHeader.toBase64URL(), Base64URL.encode(body), signature);
    return jws.serialize();
  }

  /**
   * parsed an RSA or EC key to its JWK representation
   *
   * @param keyPair the keypair to parse
   */
  private JWK toJwk(KeyPair keyPair)
  {
    switch (keyPair.getPublic().getAlgorithm())
    {
      case "RSA":
        return toRsaJwk(keyPair);
      case "EC":
        return toEcJwk(keyPair);
      default:
        return null; // should not happen due to previous validation
    }
  }

  /**
   * parses an EC key pair into its EC JWK representation
   */
  private ECKey toEcJwk(KeyPair keyPair)
  {
    ECPublicKey ecPublicKey = (ECPublicKey)keyPair.getPublic();
    ECParameterSpec ecParameterSpec = ecPublicKey.getParams();
    Curve curve = Curve.forECParameterSpec(ecParameterSpec);
    ECKey.Builder builder = new ECKey.Builder(curve, ecPublicKey);
    Optional.ofNullable(keyPair.getPrivate()).ifPresent(builder::privateKey);
    return builder.build();
  }

  /**
   * parses an RSA key pair into its RSA JWK representation
   */
  private RSAKey toRsaJwk(KeyPair keyPair)
  {
    RSAKey.Builder builder = new RSAKey.Builder((RSAPublicKey)keyPair.getPublic());
    Optional.ofNullable(keyPair.getPrivate()).ifPresent(builder::privateKey);
    return builder.build();
  }

  /**
   * retrieves the signature verifier based on the data within the JWS header
   */
  @SneakyThrows
  private JWSVerifier getVerifier(String keyId, JWSHeader header)
  {
    Keystore applicationKeystore = keystoreDao.getKeystore();
    PublicKey publicKey = applicationKeystore.getCertificate(keyId).getPublicKey();

    boolean isRsaAlgorithm = JWSAlgorithm.Family.RSA.stream().anyMatch(rsa -> rsa.equals(header.getAlgorithm()));
    if (isRsaAlgorithm)
    {
      return new RSASSAVerifier((RSAPublicKey)publicKey);
    }

    boolean isEcAlgorithm = JWSAlgorithm.Family.EC.stream().anyMatch(ec -> ec.equals(header.getAlgorithm()));
    if (isEcAlgorithm)
    {
      return new ECDSAVerifier((ECPublicKey)publicKey);
    }

    String errorMessage = String.format("Unsupported algorithm found '%s'", header.getAlgorithm());
    throw new IllegalArgumentException(errorMessage);
  }

  /**
   * an enum that is used to indicate that additional attributes should be added to the JWTs header before it is
   * signed or encrypted
   */
  public static enum JwtAttribute
  {
    X5T_SHA256, ADD_PUBLIC_KEY
  }

  /**
   * represents the plain data of a JWT
   */
  @Getter
  @Builder
  public static class PlainJwtData
  {

    /**
     * the JWT header
     */
    private final String header;

    /**
     * the JWT body
     */
    private final String body;
  }
}
