package de.captaingoldfish.restclient.application.endpoints.jwt;

import java.io.InputStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;

import de.captaingoldfish.restclient.application.crypto.JwtHandler;
import de.captaingoldfish.restclient.application.setup.AbstractScimClientConfig;
import de.captaingoldfish.restclient.application.setup.OAuthRestClientTest;
import de.captaingoldfish.restclient.commons.keyhelper.KeyStoreSupporter;
import de.captaingoldfish.restclient.database.entities.Keystore;
import de.captaingoldfish.restclient.database.repositories.KeystoreDao;
import de.captaingoldfish.restclient.scim.resources.ScimJwtBuilder;
import de.captaingoldfish.scim.sdk.client.response.ServerResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


/**
 * @author Pascal Knueppel
 * @since 27.06.2021
 */
@Slf4j
@OAuthRestClientTest
public class JwtBuilderHandlerTest extends AbstractScimClientConfig
{

  /**
   * the scim endpoint for the application keystore
   */
  private static final String JWT_BUILDER_ENDPOINT = "/JwtBuilder";

  @Autowired
  private KeystoreDao keystoreDao;

  @SneakyThrows
  @BeforeEach
  public void initialize()
  {
    Keystore applicationKeystore;

    try (InputStream inputStream = readAsInputStream(UNIT_TEST_KEYSTORE_JKS_EXTENDED))
    {
      applicationKeystore = new Keystore(inputStream, KeyStoreSupporter.KeyStoreType.JKS, UNIT_TEST_KEYSTORE_PASSWORD);
      applicationKeystore.setKeystoreType(KeyStoreSupporter.KeyStoreType.JKS);
      applicationKeystore.setKeystorePassword(UNIT_TEST_KEYSTORE_PASSWORD);
      getExtendedUnitTestKeystoreEntryAccess().forEach(applicationKeystore::addKeyEntry);
    }
    keystoreDao.save(applicationKeystore);
  }

  @Test
  public void testSignJwt()
  {
    final String keyId = "goldfish-rsa";
    final String algorithm = JWSAlgorithm.RS256.toString();
    final String header = String.format("{\"kid\": \"%s\", \"alg\": \"%s\"}", keyId, algorithm);
    final String body = "{\"iss\": \"goldfish\"}";

    ScimJwtBuilder scimJwtBuilder = ScimJwtBuilder.builder().header(header).body(body).build();

    ServerResponse<ScimJwtBuilder> response = scimRequestBuilder.create(ScimJwtBuilder.class, JWT_BUILDER_ENDPOINT)
                                                                .setResource(scimJwtBuilder)
                                                                .sendRequest();
    Assertions.assertTrue(response.isSuccess(), response.getResponseBody());
    ScimJwtBuilder returnedResource = response.getResource();

    JwtHandler jwtHandler = new JwtHandler(keystoreDao);
    final String jws = returnedResource.getBody();
    String plainBody = jwtHandler.handleJwt(jws);
    Assertions.assertEquals(body, plainBody);
  }

  @Test
  public void testEncrypt()
  {
    final String keyId = "goldfish-ec";
    final String algorithm = JWEAlgorithm.ECDH_ES_A256KW.toString();
    final String contentEncryptionAlgorithm = EncryptionMethod.A128CBC_HS256.toString();
    final String header = String.format("{\"kid\": \"%s\", \"alg\": \"%s\", \"enc\": \"%s\"}",
                                        keyId,
                                        algorithm,
                                        contentEncryptionAlgorithm);
    final String body = "{\"iss\": \"goldfish\"}";

    ScimJwtBuilder scimJwtBuilder = ScimJwtBuilder.builder().header(header).body(body).build();

    ServerResponse<ScimJwtBuilder> response = scimRequestBuilder.create(ScimJwtBuilder.class, JWT_BUILDER_ENDPOINT)
                                                                .setResource(scimJwtBuilder)
                                                                .sendRequest();
    Assertions.assertTrue(response.isSuccess(), response.getResponseBody());
    ScimJwtBuilder returnedResource = response.getResource();

    JwtHandler jwtHandler = new JwtHandler(keystoreDao);
    final String jwe = returnedResource.getBody();
    log.warn(jwe);
    String plainBody = jwtHandler.handleJwt(jwe);
    Assertions.assertEquals(body, plainBody);
  }
}
