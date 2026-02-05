package de.captaingoldfish.restclient.application.endpoints.tokenrequest.request;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;

import de.captaingoldfish.restclient.application.crypto.JwtHandler;
import de.captaingoldfish.restclient.application.projectconfig.WebAppConfig;
import de.captaingoldfish.restclient.application.utils.Utils;
import de.captaingoldfish.restclient.database.entities.Keystore;
import de.captaingoldfish.restclient.database.entities.KeystoreEntry;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;
import de.captaingoldfish.restclient.database.repositories.KeystoreDao;
import de.captaingoldfish.scim.sdk.common.exceptions.BadRequestException;
import lombok.RequiredArgsConstructor;


/**
 * @author Pascal Knueppel
 * @since 21.08.2021
 */
@RequiredArgsConstructor
public class JwtAuthenticator implements Authenticator
{

  /**
   * contains all necessary data to build a client_assertion
   */
  private final OpenIdClient openIdClient;


  /**
   * in case of jwt-profile authentication no additional request headers are required
   */
  @Override
  public Map<String, String> getRequestHeader()
  {
    return new HashMap<>();
  }

  /**
   * generates the client_assertion parameters from the JWT-profile in RFC7523
   */
  @Override
  public Map<String, String> getRequestParameter()
  {
    Map<String, String> requestParams = new HashMap<>();
    requestParams.put("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer");
    final String clientAssertion = generateClientAssertion();
    requestParams.put("client_assertion", clientAssertion);
    return requestParams;
  }

  /**
   * will generate the client assertion for the identity provider
   *
   * @return the JWS representation of the client assertion
   */
  private String generateClientAssertion()
  {
    KeystoreEntry signatureKeyEntry = determineSignatureKey();
    String jwsHeader = generateClientAssertionHeader(signatureKeyEntry);
    String jwsBody = generateClientAssertionBody();
    KeystoreDao keystoreDao = WebAppConfig.getApplicationContext().getBean(KeystoreDao.class);
    JwtHandler jwtHandler = new JwtHandler(keystoreDao);
    return jwtHandler.createJwt(openIdClient.getSigningKeyRef(), jwsHeader, jwsBody);
  }

  /**
   * retrieves the signature key from the client out of the application keystore
   */
  private KeystoreEntry determineSignatureKey()
  {
    KeystoreDao keystoreDao = WebAppConfig.getApplicationContext().getBean(KeystoreDao.class);
    String keyId = openIdClient.getSigningKeyRef();
    Keystore applicationKeystore = keystoreDao.getKeystore();
    return applicationKeystore.getKeystoreEntry(keyId);
  }

  /**
   * builds a JWS header with the signature algorithm to use that is determined by the key-type
   *
   * @param signatureKeyEntry the key entry that determines the type of signature algorithm to use
   * @return the generates JWS header
   */
  private String generateClientAssertionHeader(KeystoreEntry signatureKeyEntry)
  {
    JWSAlgorithm jwsAlgorithm = determineJwsAlgorithm(signatureKeyEntry);
    JWSHeader jwsHeader = new JWSHeader(jwsAlgorithm);
    return jwsHeader.toString();
  }

  /**
   * determines the JWS-Algorithm that should be entered into the JWS header
   *
   * @param signatureKeyEntry the key entry that determines the algorithm type
   * @return the algorithm type to be entered into the JWS header
   */
  private JWSAlgorithm determineJwsAlgorithm(KeystoreEntry signatureKeyEntry)
  {
    Optional<JWSAlgorithm> clientSignatureAlgorithm = Optional.ofNullable(openIdClient.getSignatureAlgorithm())
                                                              .map(JWSAlgorithm::parse);
    if (clientSignatureAlgorithm.isPresent())
    {
      return clientSignatureAlgorithm.get();
    }
    if ("RSA".equals(signatureKeyEntry.getKeyAlgorithm().toUpperCase(Locale.ROOT)))
    {
      return JWSAlgorithm.RS256;
    }
    else if ("EC".equals(signatureKeyEntry.getKeyAlgorithm().toUpperCase(Locale.ROOT)))
    {
      return determineEcJwsAlgorithm(signatureKeyEntry);
    }
    else
    {
      throw new IllegalStateException(String.format("Unsupported private key of type '%s' for JWS creation",
                                                    signatureKeyEntry.getKeyAlgorithm()));
    }
  }

  /**
   * determines the signature algorithm in case for elliptic curve keys
   *
   * @param signatureKeyEntry the elliptic curve key
   * @return the fitting algorithm to the given key
   */
  private JWSAlgorithm determineEcJwsAlgorithm(KeystoreEntry signatureKeyEntry)
  {
    switch (signatureKeyEntry.getKeyLength())
    {
      case 256:
        // there might also be the case for ES256K algorithm that is currently ignored for lack of necessity
        return JWSAlgorithm.ES256;
      case 384:
        return JWSAlgorithm.ES384;
      case 512:
        return JWSAlgorithm.ES512;
      default:
        throw new BadRequestException(String.format("Unsupported key for JWS signature under alias '%s'",
                                                    signatureKeyEntry.getAlias()));
    }
  }

  /**
   * generates the client assertions body that is a bearer-token claims-set
   */
  private String generateClientAssertionBody()
  {
    String audience = Optional.ofNullable(openIdClient.getAudience()).orElseGet(this::getProviderIssuer);
    JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder().issuer(openIdClient.getClientId())
                                                          .subject(openIdClient.getClientId())
                                                          .audience(audience)
                                                          .issueTime(Date.from(Instant.now()))
                                                          .expirationTime(Date.from(Instant.now().plusSeconds(60 * 5)))
                                                          .jwtID(UUID.randomUUID().toString())
                                                          .build();
    return jwtClaimsSet.toString();
  }

  /**
   * if no audience value is entered into the clients configuration this method is trying to get the audience
   * value as fallback from the metadata endpoint of the identity provider
   */
  private String getProviderIssuer()
  {
    OIDCProviderMetadata metadata = Utils.loadDiscoveryEndpointInfos(openIdClient.getOpenIdProvider());
    return metadata.getIssuer().getValue();
  }
}
