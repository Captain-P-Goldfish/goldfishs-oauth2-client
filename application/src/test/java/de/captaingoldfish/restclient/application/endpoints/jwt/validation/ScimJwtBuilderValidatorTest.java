package de.captaingoldfish.restclient.application.endpoints.jwt.validation;

import java.io.InputStream;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.nimbusds.jose.JWSAlgorithm;

import de.captaingoldfish.restclient.application.endpoints.jwt.JwtBuilderHandler;
import de.captaingoldfish.restclient.application.setup.FileReferences;
import de.captaingoldfish.restclient.commons.keyhelper.KeyStoreSupporter;
import de.captaingoldfish.restclient.database.entities.Keystore;
import de.captaingoldfish.restclient.database.repositories.KeystoreDao;
import de.captaingoldfish.restclient.scim.endpoints.JwtBuilderEndpoint;
import de.captaingoldfish.restclient.scim.resources.ScimJwtBuilder;
import de.captaingoldfish.scim.sdk.common.resources.ServiceProvider;
import de.captaingoldfish.scim.sdk.server.endpoints.ResourceEndpoint;
import de.captaingoldfish.scim.sdk.server.endpoints.validation.ValidationContext;
import de.captaingoldfish.scim.sdk.server.schemas.ResourceType;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


/**
 * @author Pascal Knueppel
 * @since 27.06.2021
 */
@Slf4j
public class ScimJwtBuilderValidatorTest implements FileReferences
{

  private KeystoreDao keystoreDao;

  @SneakyThrows
  @BeforeEach
  public void initialize()
  {
    keystoreDao = Mockito.mock(KeystoreDao.class);

    Keystore applicationKeystore;

    try (InputStream inputStream = readAsInputStream(UNIT_TEST_KEYSTORE_JKS_EXTENDED))
    {
      applicationKeystore = new Keystore(inputStream, KeyStoreSupporter.KeyStoreType.JKS, UNIT_TEST_KEYSTORE_PASSWORD);
      applicationKeystore.setKeystoreType(KeyStoreSupporter.KeyStoreType.JKS);
      applicationKeystore.setKeystorePassword(UNIT_TEST_KEYSTORE_PASSWORD);
      getExtendedUnitTestKeystoreEntryAccess().forEach(applicationKeystore::addKeyEntry);
    }
    Mockito.doReturn(applicationKeystore).when(keystoreDao).getKeystore();
  }

  /**
   * this test will verify that an appropriate error message is returned if the header is not valid json
   */
  @Test
  public void testHeaderisInvalidJson()
  {
    final String header = "hello world";
    final String body = "{\"iss\": \"goldfish\"}";

    ScimJwtBuilder scimJwtBuilder = ScimJwtBuilder.builder().header(header).body(body).build();
    ScimJwtBuilderValidator validator = new ScimJwtBuilderValidator(keystoreDao);

    ValidationContext validationContext = getValidationContext();
    validator.validateCreate(scimJwtBuilder, validationContext, null);

    Assertions.assertTrue(validationContext.hasErrors(), "validation context should have errors");
    Assertions.assertEquals(0, validationContext.getErrors().size());
    Assertions.assertEquals(1, validationContext.getFieldErrors().size());
    List<String> headerErrors = validationContext.getFieldErrors().get(ScimJwtBuilder.FieldNames.HEADER);
    Assertions.assertEquals(1, headerErrors.size());
    final String errorMessage = "Header is not a valid JSON structure 'Invalid content, the document could not be parsed'";
    Assertions.assertEquals(errorMessage, headerErrors.get(0));
  }

  /**
   * this test verifies that a missing keyId causes an appropriate error message
   */
  @Test
  public void testHeaderisMissingKeyId()
  {
    final String algorithm = JWSAlgorithm.ES256.toString();
    final String header = String.format("{\"alg\": \"%s\"}", algorithm);
    final String body = "{\"iss\": \"goldfish\"}";

    ScimJwtBuilder scimJwtBuilder = ScimJwtBuilder.builder().header(header).body(body).build();
    ScimJwtBuilderValidator validator = new ScimJwtBuilderValidator(keystoreDao);

    ValidationContext validationContext = getValidationContext();
    validator.validateCreate(scimJwtBuilder, validationContext, null);

    Assertions.assertTrue(validationContext.hasErrors(), "validation context should have errors");
    Assertions.assertEquals(0, validationContext.getErrors().size());
    Assertions.assertEquals(1, validationContext.getFieldErrors().size());
    List<String> headerErrors = validationContext.getFieldErrors().get(ScimJwtBuilder.FieldNames.HEADER);
    Assertions.assertEquals(1, headerErrors.size());
    final String errorMessage = "keyId is required and must match an alias of the application keystore";
    Assertions.assertEquals(errorMessage, headerErrors.get(0));
  }

  /**
   * this test verifies that a missing alg-parameter causes an appropriate error message
   */
  @Test
  public void testHeaderIsMissingAlg()
  {
    final String keyId = "goldfish-rsa";
    final String header = String.format("{\"kid\": \"%s\"}", keyId);
    final String body = "{\"iss\": \"goldfish\"}";

    ScimJwtBuilder scimJwtBuilder = ScimJwtBuilder.builder().header(header).body(body).build();
    ScimJwtBuilderValidator validator = new ScimJwtBuilderValidator(keystoreDao);

    ValidationContext validationContext = getValidationContext();
    validator.validateCreate(scimJwtBuilder, validationContext, null);

    Assertions.assertTrue(validationContext.hasErrors(), "validation context should have errors");
    Assertions.assertEquals(0, validationContext.getErrors().size());
    Assertions.assertEquals(1, validationContext.getFieldErrors().size());
    List<String> headerErrors = validationContext.getFieldErrors().get(ScimJwtBuilder.FieldNames.HEADER);
    Assertions.assertEquals(1, headerErrors.size());
    final String errorMessage = "Header is missing required values. JWS requires [kid, alg]. JWE requires [kid, alg, enc]";
    Assertions.assertEquals(errorMessage, headerErrors.get(0));
  }

  /**
   * this test will verify that an appropriate error is entered into the validation context if the chosen
   * algorithm does not match the chosen key type
   */
  @Test
  public void testUnmatchingRsaSignatureAlgorithm()
  {
    final String keyId = "goldfish-rsa";
    final String algorithm = JWSAlgorithm.ES256.toString();
    final String header = String.format("{\"kid\": \"%s\", \"alg\": \"%s\"}", keyId, algorithm);
    final String body = "{\"iss\": \"goldfish\"}";

    ScimJwtBuilder scimJwtBuilder = ScimJwtBuilder.builder().header(header).body(body).build();
    ScimJwtBuilderValidator validator = new ScimJwtBuilderValidator(keystoreDao);

    ValidationContext validationContext = getValidationContext();
    validator.validateCreate(scimJwtBuilder, validationContext, null);

    Assertions.assertTrue(validationContext.hasErrors(), "validation context should have errors");
    Assertions.assertEquals(0, validationContext.getErrors().size());
    Assertions.assertEquals(1, validationContext.getFieldErrors().size());
    List<String> headerErrors = validationContext.getFieldErrors().get(ScimJwtBuilder.FieldNames.HEADER);
    Assertions.assertEquals(1, headerErrors.size());
    final String errorMessage = "Invalid JWK: Must be an instance of class com.nimbusds.jose.jwk.ECKey";
    Assertions.assertEquals(errorMessage, headerErrors.get(0));
  }

  /**
   * this test will verify that an appropriate error is entered into the validation context if the chosen
   * algorithm does not match the chosen key type
   */
  @Test
  public void testUnmatchingEsSignatureAlgorithm()
  {
    final String keyId = "goldfish-ec";
    final String algorithm = JWSAlgorithm.RS256.toString();
    final String header = String.format("{\"kid\": \"%s\", \"alg\": \"%s\"}", keyId, algorithm);
    final String body = "{\"iss\": \"goldfish\"}";

    ScimJwtBuilder scimJwtBuilder = ScimJwtBuilder.builder().header(header).body(body).build();
    ScimJwtBuilderValidator validator = new ScimJwtBuilderValidator(keystoreDao);

    ValidationContext validationContext = getValidationContext();
    validator.validateCreate(scimJwtBuilder, validationContext, null);

    Assertions.assertTrue(validationContext.hasErrors(), "validation context should have errors");
    Assertions.assertEquals(0, validationContext.getErrors().size());
    Assertions.assertEquals(1, validationContext.getFieldErrors().size());
    List<String> headerErrors = validationContext.getFieldErrors().get(ScimJwtBuilder.FieldNames.HEADER);
    Assertions.assertEquals(1, headerErrors.size());
    final String errorMessage = "Invalid JWK: Must be an instance of class com.nimbusds.jose.jwk.RSAKey";
    Assertions.assertEquals(errorMessage, headerErrors.get(0));
  }

  /**
   * this test will verify that an appropriate error is entered into the validation context if the chosen
   * algorithm does not match the chosen key type
   */
  @Test
  public void testUnmatchingEsSignatureAlgorithmWithEsKey()
  {
    final String keyId = "goldfish-ec";
    final String algorithm = JWSAlgorithm.ES512.toString();
    final String header = String.format("{\"kid\": \"%s\", \"alg\": \"%s\"}", keyId, algorithm);
    final String body = "{\"iss\": \"goldfish\"}";

    ScimJwtBuilder scimJwtBuilder = ScimJwtBuilder.builder().header(header).body(body).build();
    ScimJwtBuilderValidator validator = new ScimJwtBuilderValidator(keystoreDao);

    ValidationContext validationContext = getValidationContext();
    validator.validateCreate(scimJwtBuilder, validationContext, null);

    Assertions.assertTrue(validationContext.hasErrors(), "validation context should have errors");
    Assertions.assertEquals(0, validationContext.getErrors().size());
    Assertions.assertEquals(1, validationContext.getFieldErrors().size());
    List<String> headerErrors = validationContext.getFieldErrors().get(ScimJwtBuilder.FieldNames.HEADER);
    Assertions.assertEquals(1, headerErrors.size());
    final String errorMessage = "Unsupported JWS algorithm ES512, must be ES256";
    Assertions.assertEquals(errorMessage, headerErrors.get(0));
  }

  private ValidationContext getValidationContext()
  {
    ResourceEndpoint resourceEndpoint = new ResourceEndpoint(ServiceProvider.builder().build());
    ResourceType resourceType = resourceEndpoint.registerEndpoint(new JwtBuilderEndpoint(new JwtBuilderHandler(null,
                                                                                                               null)));
    return new ValidationContext(resourceType);
  }
}
