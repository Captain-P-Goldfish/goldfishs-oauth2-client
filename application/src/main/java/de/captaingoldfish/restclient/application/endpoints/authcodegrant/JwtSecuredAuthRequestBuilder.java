package de.captaingoldfish.restclient.application.endpoints.authcodegrant;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECParameterSpec;
import java.util.UUID;

import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;

import de.captaingoldfish.restclient.application.crypto.JwtHandler;
import de.captaingoldfish.restclient.application.endpoints.tokenrequest.request.JwtAuthenticator;
import de.captaingoldfish.restclient.application.projectconfig.WebAppConfig;
import de.captaingoldfish.restclient.application.utils.OAuthConstants;
import de.captaingoldfish.restclient.database.entities.Keystore;
import de.captaingoldfish.restclient.database.entities.KeystoreEntry;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;
import de.captaingoldfish.restclient.database.repositories.KeystoreDao;
import de.captaingoldfish.restclient.scim.resources.ScimAuthCodeGrantRequest;
import de.captaingoldfish.restclient.scim.resources.ScimCurrentWorkflowSettings;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;


/**
 * @author Pascal Knueppel
 * @since 17.04.2026
 */
@Slf4j
public class JwtSecuredAuthRequestBuilder
{

  private final OpenIdClient openIdClient;

  private final ScimCurrentWorkflowSettings workflowSettings;

  private final UriComponents originalRequestUrl;

  private final String keyId;

  @Getter
  private final String httpContentType = "application/oauth-authz-req+jwt";

  @Getter
  private UriComponents requestUrl;

  @Getter
  private String authCodeRequestUrl;

  public JwtSecuredAuthRequestBuilder(OpenIdClient openIdClient,
                                      ScimCurrentWorkflowSettings workflowSettings,
                                      UriComponents originalRequestUrl)
  {
    this.openIdClient = openIdClient;
    this.workflowSettings = workflowSettings;
    this.originalRequestUrl = originalRequestUrl;
    this.keyId = workflowSettings.getJar().flatMap(ScimAuthCodeGrantRequest.Jar::getSignatureKey).orElseThrow();
    buildJarRequest();
  }

  private void buildJarRequest()
  {
    ObjectNode objectNode = new ObjectNode(JsonNodeFactory.instance);
    originalRequestUrl.getQueryParams().forEach((key, value) -> {
      String decodedValue = URLDecoder.decode(value.getFirst(), StandardCharsets.UTF_8);
      log.debug("adding query parameter to JWT Secured AuthorizationRequest {}={}", key, decodedValue);
      objectNode.put(key, decodedValue);
    });
    objectNode.put("nonce", UUID.randomUUID().toString());
    objectNode.put("max_age", 86400);

    KeystoreDao keystoreDao = WebAppConfig.getApplicationContext().getBean(KeystoreDao.class);
    Keystore applicationKeystore = keystoreDao.getKeystore();

    String jwsHeader = generateClientAssertionHeader(applicationKeystore);
    String jwsBody = objectNode.toString();
    JwtHandler jwtHandler = new JwtHandler(keystoreDao);
    String jwtSecuredAuthRequest = jwtHandler.createJwt(keyId, jwsHeader, jwsBody);

    requestUrl = UriComponentsBuilder.fromUri(originalRequestUrl.toUri())
                                     .replaceQuery(null)
                                     .queryParam(OAuthConstants.CLIENT_ID, openIdClient.getClientId())
                                     .queryParam(OAuthConstants.REQUEST, jwtSecuredAuthRequest)
                                     .build();
    authCodeRequestUrl = requestUrl.toUriString();
  }

  /**
   * builds a JWS header with the signature algorithm to use that is determined by the key-type
   *
   * @param applicationKeystore the application keystore with the key that must be used for signing
   * @return the generates JWS header
   */
  private String generateClientAssertionHeader(Keystore applicationKeystore)
  {
    KeystoreEntry signatureKeyEntry = applicationKeystore.getKeystoreEntry(keyId);
    JWSAlgorithm jwsAlgorithm = JwtAuthenticator.determineJwsAlgorithm(openIdClient, signatureKeyEntry);
    KeyPair keyPair = applicationKeystore.getKeyPair(keyId);
    JWK jwk = toJwk(keyPair);
    JWSHeader jwsHeader = new JWSHeader.Builder(jwsAlgorithm).jwk(jwk).build();
    return jwsHeader.toString();
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
    return builder.build();
  }

  /**
   * parses an RSA key pair into its RSA JWK representation
   */
  private RSAKey toRsaJwk(KeyPair keyPair)
  {
    RSAKey.Builder builder = new RSAKey.Builder((RSAPublicKey)keyPair.getPublic());
    return builder.build();
  }
}
