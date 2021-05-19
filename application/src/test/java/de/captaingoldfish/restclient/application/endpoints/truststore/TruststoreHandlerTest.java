package de.captaingoldfish.restclient.application.endpoints.truststore;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.Base64;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.captaingoldfish.restclient.application.setup.AbstractScimClientConfig;
import de.captaingoldfish.restclient.application.setup.OAuthRestClientTest;
import de.captaingoldfish.restclient.commons.keyhelper.KeyStoreSupporter;
import de.captaingoldfish.restclient.scim.resources.CertificateInfo;
import de.captaingoldfish.restclient.scim.resources.ScimTruststore;
import de.captaingoldfish.restclient.scim.resources.ScimTruststore.CertificateUpload;
import de.captaingoldfish.restclient.scim.resources.ScimTruststore.CertificateUploadResponse;
import de.captaingoldfish.restclient.scim.resources.ScimTruststore.TruststoreUpload;
import de.captaingoldfish.restclient.scim.resources.ScimTruststore.TruststoreUploadResponse;
import de.captaingoldfish.scim.sdk.client.response.ServerResponse;
import de.captaingoldfish.scim.sdk.common.response.ErrorResponse;
import de.captaingoldfish.scim.sdk.common.response.ListResponse;
import kong.unirest.HttpStatus;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


/**
 * @author Pascal Knueppel
 * @since 19.05.2021
 */
@Slf4j
@OAuthRestClientTest
public class TruststoreHandlerTest extends AbstractScimClientConfig
{

  /**
   * the scim endpoint for the application truststore
   */
  private static final String TRUSTSTORE_ENDPOINT = "/Truststore";

  /**
   * verifies that a truststore can be successfully merged and that already existing entries in the truststore
   * will be ignored without throwing an error
   */
  @SneakyThrows
  @ParameterizedTest
  @ValueSource(strings = {UNIT_TEST_KEYSTORE_JKS, UNIT_TEST_KEYSTORE_PKCS12})
  public void testMergeTruststoreFile(String keystorePath)
  {
    final byte[] truststoreBytes = readAsBytes(keystorePath);
    final String b64Truststore = Base64.getEncoder().encodeToString(truststoreBytes);
    final String filename = "unit-test.jks";

    // send upload post request
    {
      TruststoreUpload truststoreUpload = TruststoreUpload.builder()
                                                          .truststoreFile(b64Truststore)
                                                          .truststorePassword(UNIT_TEST_KEYSTORE_PASSWORD)
                                                          .truststoreFileName(filename)
                                                          .build();
      ScimTruststore scimTruststore = ScimTruststore.builder().truststoreUpload(truststoreUpload).build();

      ServerResponse<ScimTruststore> response = scimRequestBuilder.create(ScimTruststore.class, TRUSTSTORE_ENDPOINT)
                                                                  .setResource(scimTruststore)
                                                                  .sendRequest();
      Assertions.assertEquals(HttpStatus.CREATED, response.getHttpStatus());
      Assertions.assertFalse(response.getResource().getCertificateUploadResponse().isPresent());
      Assertions.assertTrue(response.getResource().getTruststoreUploadResponse().isPresent());
      TruststoreUploadResponse uploadResponse = response.getResource().getTruststoreUploadResponse().get();
      MatcherAssert.assertThat(uploadResponse.getAliases(),
                               Matchers.containsInAnyOrder("unit-test", "goldfish", "localhost"));
      MatcherAssert.assertThat(uploadResponse.getDuplicateAliases(), Matchers.emptyIterable());
      MatcherAssert.assertThat(uploadResponse.getDuplicateCertificateAliases(), Matchers.emptyIterable());
      Assertions.assertEquals(3, truststoreDao.getTruststore().getTruststore().size());
    }

    // send same request again
    {
      TruststoreUpload truststoreUpload = TruststoreUpload.builder()
                                                          .truststoreFile(b64Truststore)
                                                          .truststorePassword(UNIT_TEST_KEYSTORE_PASSWORD)
                                                          .truststoreFileName(filename)
                                                          .build();
      ScimTruststore scimTruststore = ScimTruststore.builder().truststoreUpload(truststoreUpload).build();

      ServerResponse<ScimTruststore> response = scimRequestBuilder.create(ScimTruststore.class, TRUSTSTORE_ENDPOINT)
                                                                  .setResource(scimTruststore)
                                                                  .sendRequest();
      Assertions.assertEquals(HttpStatus.CREATED, response.getHttpStatus());
      Assertions.assertFalse(response.getResource().getCertificateUploadResponse().isPresent());
      Assertions.assertTrue(response.getResource().getTruststoreUploadResponse().isPresent());
      TruststoreUploadResponse uploadResponse = response.getResource().getTruststoreUploadResponse().get();

      MatcherAssert.assertThat(uploadResponse.getDuplicateAliases(),
                               Matchers.containsInAnyOrder("unit-test", "goldfish", "localhost"));
      MatcherAssert.assertThat(uploadResponse.getAliases(), Matchers.emptyIterable());
      MatcherAssert.assertThat(uploadResponse.getDuplicateCertificateAliases(), Matchers.emptyIterable());
      Assertions.assertEquals(3, truststoreDao.getTruststore().getTruststore().size());
    }
  }

  /**
   * verifies that uploading a simple cert-file works successfully
   */
  @SneakyThrows
  @Test
  public void testUploadCertificateFile()
  {

    // send upload post request
    {
      final String alias = "goldfish";
      final byte[] certificateBytes = getCertificateOfKeystore(UNIT_TEST_KEYSTORE_JKS, alias);
      final String b64Certificate = Base64.getEncoder().encodeToString(certificateBytes);
      CertificateUpload certificateUpload = CertificateUpload.builder()
                                                             .certificateFile(b64Certificate)
                                                             .alias(alias)
                                                             .build();
      ScimTruststore scimTruststore = ScimTruststore.builder().certificateUpload(certificateUpload).build();

      ServerResponse<ScimTruststore> response = scimRequestBuilder.create(ScimTruststore.class, TRUSTSTORE_ENDPOINT)
                                                                  .setResource(scimTruststore)
                                                                  .sendRequest();
      Assertions.assertEquals(HttpStatus.CREATED, response.getHttpStatus());
      Assertions.assertTrue(response.getResource().getCertificateUploadResponse().isPresent());
      Assertions.assertFalse(response.getResource().getTruststoreUploadResponse().isPresent());
      CertificateUploadResponse uploadResponse = response.getResource().getCertificateUploadResponse().get();
      Assertions.assertEquals(alias, uploadResponse.getAlias());
      Assertions.assertEquals(1, truststoreDao.getTruststore().getTruststore().size());
    }

    // upload another certificate with already used alias
    {
      final String alias = "localhost";
      final String alreadyUsedAlias = "goldfish";
      final byte[] certificateBytes = getCertificateOfKeystore(UNIT_TEST_KEYSTORE_JKS, alias);
      final String b64Certificate = Base64.getEncoder().encodeToString(certificateBytes);
      CertificateUpload certificateUpload = CertificateUpload.builder()
                                                             .certificateFile(b64Certificate)
                                                             .alias(alreadyUsedAlias)
                                                             .build();
      ScimTruststore scimTruststore = ScimTruststore.builder().certificateUpload(certificateUpload).build();

      ServerResponse<ScimTruststore> response = scimRequestBuilder.create(ScimTruststore.class, TRUSTSTORE_ENDPOINT)
                                                                  .setResource(scimTruststore)
                                                                  .sendRequest();
      Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
      ErrorResponse errorResponse = response.getErrorResponse();

      List<String> errorMessages = errorResponse.getErrorMessages();
      MatcherAssert.assertThat(errorMessages, Matchers.emptyIterable());
      Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
      Assertions.assertEquals(1, fieldErrors.size());
      final String aliasFieldName = String.format("%s.%s",
                                                  ScimTruststore.FieldNames.CERTIFICATE_UPLOAD,
                                                  ScimTruststore.FieldNames.ALIAS);
      List<String> aliasMessages = fieldErrors.get(aliasFieldName);
      Assertions.assertEquals(1, aliasMessages.size());

      String errorMessage = String.format("Cannot add certificate for alias '%s' is already taken", alreadyUsedAlias);
      MatcherAssert.assertThat(aliasMessages, Matchers.containsInAnyOrder(errorMessage));
      Assertions.assertEquals(1, truststoreDao.getTruststore().getTruststore().size());
    }

    // upload already uploaded certificate under unused alias
    {
      final String alias = "goldfish";
      final String unusedAlias = "super-mario";
      final byte[] certificateBytes = getCertificateOfKeystore(UNIT_TEST_KEYSTORE_JKS, alias);
      final String b64Certificate = Base64.getEncoder().encodeToString(certificateBytes);
      CertificateUpload certificateUpload = CertificateUpload.builder()
                                                             .certificateFile(b64Certificate)
                                                             .alias(unusedAlias)
                                                             .build();
      ScimTruststore scimTruststore = ScimTruststore.builder().certificateUpload(certificateUpload).build();

      ServerResponse<ScimTruststore> response = scimRequestBuilder.create(ScimTruststore.class, TRUSTSTORE_ENDPOINT)
                                                                  .setResource(scimTruststore)
                                                                  .sendRequest();
      Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
      ErrorResponse errorResponse = response.getErrorResponse();

      List<String> errorMessages = errorResponse.getErrorMessages();
      MatcherAssert.assertThat(errorMessages, Matchers.emptyIterable());
      Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
      Assertions.assertEquals(1, fieldErrors.size());
      final String fieldName = String.format("%s.%s",
                                             ScimTruststore.FieldNames.CERTIFICATE_UPLOAD,
                                             ScimTruststore.FieldNames.CERTIFICATE_FILE);
      List<String> fieldErrorMessages = fieldErrors.get(fieldName);
      Assertions.assertEquals(1, fieldErrorMessages.size());

      String errorMessage = String.format("Cannot add certificate for certificate is already present under alias '%s'",
                                          alias);
      MatcherAssert.assertThat(fieldErrorMessages, Matchers.containsInAnyOrder(errorMessage));
      Assertions.assertEquals(1, truststoreDao.getTruststore().getTruststore().size());
    }
  }

  @SneakyThrows
  @Test
  public void testRequestWithoutFiles()
  {
    ScimTruststore scimTruststore = ScimTruststore.builder().build();

    ServerResponse<ScimTruststore> response = scimRequestBuilder.create(ScimTruststore.class, TRUSTSTORE_ENDPOINT)
                                                                .setResource(scimTruststore)
                                                                .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();

    Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
    Assertions.assertEquals(0, fieldErrors.size());

    List<String> errorMessageList = errorResponse.getErrorMessages();
    String errorMessage = "Missing object in create request. Either one of 'truststoreUpload' or 'certificateUpload' "
                          + "is required";
    MatcherAssert.assertThat(errorMessageList, Matchers.containsInAnyOrder(errorMessage));
  }

  @SneakyThrows
  @Test
  public void testRequestWithBothFiles()
  {
    final String alias = "goldfish";
    final String unusedAlias = "super-mario";
    final byte[] certificateBytes = getCertificateOfKeystore(UNIT_TEST_KEYSTORE_JKS, alias);
    final String b64Certificate = Base64.getEncoder().encodeToString(certificateBytes);
    CertificateUpload certificateUpload = CertificateUpload.builder()
                                                           .certificateFile(b64Certificate)
                                                           .alias(unusedAlias)
                                                           .build();
    final byte[] truststoreBytes = readAsBytes(UNIT_TEST_KEYSTORE_JKS);
    final String b64Truststore = Base64.getEncoder().encodeToString(truststoreBytes);
    final String filename = getFilename(UNIT_TEST_KEYSTORE_JKS);

    TruststoreUpload truststoreUpload = TruststoreUpload.builder()
                                                        .truststoreFile(b64Truststore)
                                                        .truststorePassword(UNIT_TEST_KEYSTORE_PASSWORD)
                                                        .truststoreFileName(filename)
                                                        .build();

    ScimTruststore scimTruststore = ScimTruststore.builder()
                                                  .certificateUpload(certificateUpload)
                                                  .truststoreUpload(truststoreUpload)
                                                  .build();

    ServerResponse<ScimTruststore> response = scimRequestBuilder.create(ScimTruststore.class, TRUSTSTORE_ENDPOINT)
                                                                .setResource(scimTruststore)
                                                                .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();

    Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
    Assertions.assertEquals(0, fieldErrors.size());

    List<String> errorMessageList = errorResponse.getErrorMessages();
    String errorMessage = "Cannot handle create request. Only one of these objects may be present "
                          + "['truststoreUpload', 'certificateUpload']";
    MatcherAssert.assertThat(errorMessageList, Matchers.containsInAnyOrder(errorMessage));
  }

  @SneakyThrows
  @Test
  public void testUploadPkcs12KeystoreWithoutPassword()
  {
    final byte[] truststoreBytes = getParsablePkc12Keystore();
    final String b64Truststore = Base64.getEncoder().encodeToString(truststoreBytes);
    final String filename = getFilename(UNIT_TEST_KEYSTORE_PKCS12);

    TruststoreUpload truststoreUpload = TruststoreUpload.builder()
                                                        .truststoreFile(b64Truststore)
                                                        .truststoreFileName(filename)
                                                        .build();
    ScimTruststore scimTruststore = ScimTruststore.builder().truststoreUpload(truststoreUpload).build();
    ServerResponse<ScimTruststore> response = scimRequestBuilder.create(ScimTruststore.class, TRUSTSTORE_ENDPOINT)
                                                                .setResource(scimTruststore)
                                                                .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();

    List<String> errorMessages = errorResponse.getErrorMessages();
    MatcherAssert.assertThat(errorMessages, Matchers.emptyIterable());
    Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
    Assertions.assertEquals(2, fieldErrors.size());
    {
      final String fieldName = String.format("%s.%s",
                                             ScimTruststore.FieldNames.TRUSTSTORE_UPLOAD,
                                             ScimTruststore.FieldNames.TRUSTSTORE_PASSWORD);
      List<String> fieldErrorMessages = fieldErrors.get(fieldName);
      Assertions.assertEquals(1, fieldErrorMessages.size());

      String errorMessage = "Not accepting empty passwords for PKCS12 keystore type";
      MatcherAssert.assertThat(fieldErrorMessages, Matchers.containsInAnyOrder(errorMessage));
    }
    {
      final String fieldName = String.format("%s.%s",
                                             ScimTruststore.FieldNames.TRUSTSTORE_UPLOAD,
                                             ScimTruststore.FieldNames.TRUSTSTORE_FILE);
      List<String> fieldErrorMessages = fieldErrors.get(fieldName);
      Assertions.assertEquals(1, fieldErrorMessages.size());

      String errorMessage = "PKCS12 key store mac invalid - wrong password or corrupted file.";
      MatcherAssert.assertThat(fieldErrorMessages, Matchers.containsInAnyOrder(errorMessage));
    }
  }

  @Test
  public void testUploadIllegalCertificateFile()
  {
    final String alias = "goldfish";
    final byte[] certificateBytes = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    final String b64Certificate = Base64.getEncoder().encodeToString(certificateBytes);
    CertificateUpload certificateUpload = CertificateUpload.builder()
                                                           .certificateFile(b64Certificate)
                                                           .alias(alias)
                                                           .build();

    ScimTruststore scimTruststore = ScimTruststore.builder().certificateUpload(certificateUpload).build();

    ServerResponse<ScimTruststore> response = scimRequestBuilder.create(ScimTruststore.class, TRUSTSTORE_ENDPOINT)
                                                                .setResource(scimTruststore)
                                                                .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();

    Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
    Assertions.assertEquals(1, fieldErrors.size());
    final String fieldName = String.format("%s.%s",
                                           ScimTruststore.FieldNames.CERTIFICATE_UPLOAD,
                                           ScimTruststore.FieldNames.CERTIFICATE_FILE);
    List<String> fieldErrorMessages = fieldErrors.get(fieldName);
    Assertions.assertEquals(2, fieldErrorMessages.size());

    MatcherAssert.assertThat(fieldErrorMessages,
                             Matchers.containsInAnyOrder("parsing issue: malformed PEM data: no header found",
                                                         "malformed PEM data: no header found"));
  }

  @SneakyThrows
  @Test
  public void testUploadTruststoreWithIllegalBase64()
  {
    final String b64Truststore = "$%&/";
    final String filename = getFilename(UNIT_TEST_KEYSTORE_JKS);

    TruststoreUpload truststoreUpload = TruststoreUpload.builder()
                                                        .truststoreFile(b64Truststore)
                                                        .truststoreFileName(filename)
                                                        .truststorePassword(UNIT_TEST_KEYSTORE_PASSWORD)
                                                        .build();
    ScimTruststore scimTruststore = ScimTruststore.builder().truststoreUpload(truststoreUpload).build();
    ServerResponse<ScimTruststore> response = scimRequestBuilder.create(ScimTruststore.class, TRUSTSTORE_ENDPOINT)
                                                                .setResource(scimTruststore)
                                                                .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();

    List<String> errorMessages = errorResponse.getErrorMessages();
    MatcherAssert.assertThat(errorMessages, Matchers.emptyIterable());
    Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
    Assertions.assertEquals(1, fieldErrors.size());
    {
      final String fieldName = String.format("%s.%s",
                                             ScimTruststore.FieldNames.TRUSTSTORE_UPLOAD,
                                             ScimTruststore.FieldNames.TRUSTSTORE_FILE);
      List<String> fieldErrorMessages = fieldErrors.get(fieldName);
      Assertions.assertEquals(1, fieldErrorMessages.size());

      String errorMessage = "Truststore file is not Base64 encoded: Illegal base64 character 24";
      MatcherAssert.assertThat(fieldErrorMessages, Matchers.containsInAnyOrder(errorMessage));
    }
  }

  @SneakyThrows
  @Test
  public void testUploadTruststoreWithEmptyKeystore()
  {
    final String filename = getFilename(UNIT_TEST_KEYSTORE_JKS);

    TruststoreUpload truststoreUpload = TruststoreUpload.builder()
                                                        .truststoreFile("")
                                                        .truststoreFileName(filename)
                                                        .truststorePassword(UNIT_TEST_KEYSTORE_PASSWORD)
                                                        .build();
    ScimTruststore scimTruststore = ScimTruststore.builder().truststoreUpload(truststoreUpload).build();
    ServerResponse<ScimTruststore> response = scimRequestBuilder.create(ScimTruststore.class, TRUSTSTORE_ENDPOINT)
                                                                .setResource(scimTruststore)
                                                                .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();

    List<String> errorMessages = errorResponse.getErrorMessages();
    MatcherAssert.assertThat(errorMessages, Matchers.emptyIterable());
    Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
    Assertions.assertEquals(1, fieldErrors.size());
    {
      final String fieldName = String.format("%s.%s",
                                             ScimTruststore.FieldNames.TRUSTSTORE_UPLOAD,
                                             ScimTruststore.FieldNames.TRUSTSTORE_FILE);
      List<String> fieldErrorMessages = fieldErrors.get(fieldName);
      Assertions.assertEquals(1, fieldErrorMessages.size());

      String errorMessage = "Truststore file must not be empty";
      MatcherAssert.assertThat(fieldErrorMessages, Matchers.containsInAnyOrder(errorMessage));
    }
  }

  @SneakyThrows
  @Test
  public void testUploadCertificateWithIllegalBase64()
  {
    final String b64Truststore = "$%&/";

    CertificateUpload certificateUpload = CertificateUpload.builder()
                                                           .certificateFile(b64Truststore)
                                                           .alias("test")
                                                           .build();
    ScimTruststore scimTruststore = ScimTruststore.builder().certificateUpload(certificateUpload).build();
    ServerResponse<ScimTruststore> response = scimRequestBuilder.create(ScimTruststore.class, TRUSTSTORE_ENDPOINT)
                                                                .setResource(scimTruststore)
                                                                .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();

    List<String> errorMessages = errorResponse.getErrorMessages();
    MatcherAssert.assertThat(errorMessages, Matchers.emptyIterable());
    Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
    Assertions.assertEquals(1, fieldErrors.size());
    {
      final String fieldName = String.format("%s.%s",
                                             ScimTruststore.FieldNames.CERTIFICATE_UPLOAD,
                                             ScimTruststore.FieldNames.CERTIFICATE_FILE);
      List<String> fieldErrorMessages = fieldErrors.get(fieldName);
      Assertions.assertEquals(1, fieldErrorMessages.size());

      String errorMessage = "Certificate file is not Base64 encoded: Illegal base64 character 24";
      MatcherAssert.assertThat(fieldErrorMessages, Matchers.containsInAnyOrder(errorMessage));
    }
  }

  @SneakyThrows
  @Test
  public void testUploadCertificateWithEmptyKeystore()
  {
    CertificateUpload certificateUpload = CertificateUpload.builder().certificateFile("").alias("test").build();
    ScimTruststore scimTruststore = ScimTruststore.builder().certificateUpload(certificateUpload).build();
    ServerResponse<ScimTruststore> response = scimRequestBuilder.create(ScimTruststore.class, TRUSTSTORE_ENDPOINT)
                                                                .setResource(scimTruststore)
                                                                .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();

    List<String> errorMessages = errorResponse.getErrorMessages();
    MatcherAssert.assertThat(errorMessages, Matchers.emptyIterable());
    Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
    Assertions.assertEquals(1, fieldErrors.size());
    {
      final String fieldName = String.format("%s.%s",
                                             ScimTruststore.FieldNames.CERTIFICATE_UPLOAD,
                                             ScimTruststore.FieldNames.CERTIFICATE_FILE);
      List<String> fieldErrorMessages = fieldErrors.get(fieldName);
      Assertions.assertEquals(1, fieldErrorMessages.size());

      String errorMessage = "Certificate file must not be empty";
      MatcherAssert.assertThat(fieldErrorMessages, Matchers.containsInAnyOrder(errorMessage));
    }
  }

  /**
   * verifies that a truststore can be successfully merged and that already existing entries in the truststore
   * will be ignored without throwing an error
   */
  @SneakyThrows
  @Test
  public void testMergeTruststoreFileWithDuplicateCertificate()
  {
    final String unusedAlias = "super-mario";

    // upload certificate under unused alias
    {
      final String alias = "goldfish";
      final byte[] certificateBytes = getCertificateOfKeystore(UNIT_TEST_KEYSTORE_JKS, alias);
      final String b64Certificate = Base64.getEncoder().encodeToString(certificateBytes);
      CertificateUpload certificateUpload = CertificateUpload.builder()
                                                             .certificateFile(b64Certificate)
                                                             .alias(unusedAlias)
                                                             .build();
      ScimTruststore scimTruststore = ScimTruststore.builder().certificateUpload(certificateUpload).build();

      ServerResponse<ScimTruststore> response = scimRequestBuilder.create(ScimTruststore.class, TRUSTSTORE_ENDPOINT)
                                                                  .setResource(scimTruststore)
                                                                  .sendRequest();
      Assertions.assertEquals(HttpStatus.CREATED, response.getHttpStatus());
      Assertions.assertTrue(response.getResource().getCertificateUploadResponse().isPresent());
      Assertions.assertFalse(response.getResource().getTruststoreUploadResponse().isPresent());
      CertificateUploadResponse uploadResponse = response.getResource().getCertificateUploadResponse().get();
      Assertions.assertEquals(unusedAlias, uploadResponse.getAlias());
      Assertions.assertEquals(1, truststoreDao.getTruststore().getTruststore().size());
    }

    // upload truststore that also contains the just uploaded certificate but under another alias
    {
      final byte[] truststoreBytes = readAsBytes(UNIT_TEST_KEYSTORE_JKS);
      final String b64Truststore = Base64.getEncoder().encodeToString(truststoreBytes);
      final String filename = getFilename(UNIT_TEST_KEYSTORE_JKS);
      TruststoreUpload truststoreUpload = TruststoreUpload.builder()
                                                          .truststoreFile(b64Truststore)
                                                          .truststorePassword(UNIT_TEST_KEYSTORE_PASSWORD)
                                                          .truststoreFileName(filename)
                                                          .build();
      ScimTruststore scimTruststore = ScimTruststore.builder().truststoreUpload(truststoreUpload).build();

      ServerResponse<ScimTruststore> response = scimRequestBuilder.create(ScimTruststore.class, TRUSTSTORE_ENDPOINT)
                                                                  .setResource(scimTruststore)
                                                                  .sendRequest();
      Assertions.assertEquals(HttpStatus.CREATED, response.getHttpStatus());
      Assertions.assertFalse(response.getResource().getCertificateUploadResponse().isPresent());
      Assertions.assertTrue(response.getResource().getTruststoreUploadResponse().isPresent());
      TruststoreUploadResponse uploadResponse = response.getResource().getTruststoreUploadResponse().get();
      MatcherAssert.assertThat(uploadResponse.getAliases(), Matchers.containsInAnyOrder("unit-test", "localhost"));
      MatcherAssert.assertThat(uploadResponse.getDuplicateAliases(), Matchers.emptyIterable());
      MatcherAssert.assertThat(uploadResponse.getDuplicateCertificateAliases(),
                               Matchers.containsInAnyOrder("goldfish"));
      Assertions.assertEquals(3, truststoreDao.getTruststore().getTruststore().size());
    }
  }

  @SneakyThrows
  @Test
  public void testGetTruststoreInfos()
  {
    // upload truststore
    {
      final byte[] truststoreBytes = readAsBytes(UNIT_TEST_KEYSTORE_JKS);
      final String b64Truststore = Base64.getEncoder().encodeToString(truststoreBytes);
      final String filename = getFilename(UNIT_TEST_KEYSTORE_JKS);
      TruststoreUpload truststoreUpload = TruststoreUpload.builder()
                                                          .truststoreFile(b64Truststore)
                                                          .truststorePassword(UNIT_TEST_KEYSTORE_PASSWORD)
                                                          .truststoreFileName(filename)
                                                          .build();
      ScimTruststore scimTruststore = ScimTruststore.builder().truststoreUpload(truststoreUpload).build();

      ServerResponse<ScimTruststore> response = scimRequestBuilder.create(ScimTruststore.class, TRUSTSTORE_ENDPOINT)
                                                                  .setResource(scimTruststore)
                                                                  .sendRequest();
      Assertions.assertEquals(HttpStatus.CREATED, response.getHttpStatus());
      Assertions.assertEquals(3, truststoreDao.getTruststore().getTruststore().size());
    }

    {
      ServerResponse<ListResponse<ScimTruststore>> response = scimRequestBuilder.list(ScimTruststore.class,
                                                                                      TRUSTSTORE_ENDPOINT)
                                                                                .get()
                                                                                .sendRequest();
      Assertions.assertEquals(HttpStatus.OK, response.getHttpStatus());
      ListResponse<ScimTruststore> listResponse = response.getResource();
      Assertions.assertEquals(1, listResponse.getTotalResults());
      ScimTruststore applicationTruststore = listResponse.getListedResources().get(0);
      Assertions.assertEquals(3, applicationTruststore.getAliases().size());
      MatcherAssert.assertThat(applicationTruststore.getAliases(),
                               Matchers.containsInAnyOrder("goldfish", "localhost", "unit-test"));
    }
  }

  @SneakyThrows
  @Test
  public void testUploadCertificateWithoutAlias()
  {
    // upload certificate without alias
    {
      final String alias = "goldfish";
      final byte[] certificateBytes = getCertificateOfKeystore(UNIT_TEST_KEYSTORE_JKS, alias);
      final String b64Certificate = Base64.getEncoder().encodeToString(certificateBytes);
      CertificateUpload certificateUpload = CertificateUpload.builder().certificateFile(b64Certificate).build();
      ScimTruststore scimTruststore = ScimTruststore.builder().certificateUpload(certificateUpload).build();

      ServerResponse<ScimTruststore> response = scimRequestBuilder.create(ScimTruststore.class, TRUSTSTORE_ENDPOINT)
                                                                  .setResource(scimTruststore)
                                                                  .sendRequest();
      Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
      ErrorResponse errorResponse = response.getErrorResponse();

      List<String> errorMessages = errorResponse.getErrorMessages();
      MatcherAssert.assertThat(errorMessages, Matchers.emptyIterable());
      Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
      Assertions.assertEquals(1, fieldErrors.size());
      {
        final String fieldName = String.format("%s.%s",
                                               ScimTruststore.FieldNames.CERTIFICATE_UPLOAD,
                                               ScimTruststore.FieldNames.ALIAS);
        List<String> fieldErrorMessages = fieldErrors.get(fieldName);
        Assertions.assertEquals(1, fieldErrorMessages.size());

        String errorMessage = "Required 'WRITE_ONLY' attribute "
                              + "'urn:ietf:params:scim:schemas:captaingoldfish:2.0:Truststore:certificateUpload.alias' "
                              + "is missing";
        MatcherAssert.assertThat(fieldErrorMessages, Matchers.containsInAnyOrder(errorMessage));
      }
    }
  }

  @SneakyThrows
  @Test
  public void testLoadSingleAlias()
  {
    // upload truststore
    {
      final byte[] truststoreBytes = readAsBytes(UNIT_TEST_KEYSTORE_JKS);
      final String b64Truststore = Base64.getEncoder().encodeToString(truststoreBytes);
      final String filename = getFilename(UNIT_TEST_KEYSTORE_JKS);
      TruststoreUpload truststoreUpload = TruststoreUpload.builder()
                                                          .truststoreFile(b64Truststore)
                                                          .truststorePassword(UNIT_TEST_KEYSTORE_PASSWORD)
                                                          .truststoreFileName(filename)
                                                          .build();
      ScimTruststore scimTruststore = ScimTruststore.builder().truststoreUpload(truststoreUpload).build();

      ServerResponse<ScimTruststore> response = scimRequestBuilder.create(ScimTruststore.class, TRUSTSTORE_ENDPOINT)
                                                                  .setResource(scimTruststore)
                                                                  .sendRequest();
      Assertions.assertEquals(HttpStatus.CREATED, response.getHttpStatus());
      Assertions.assertEquals(3, truststoreDao.getTruststore().getTruststore().size());
    }

    final String alias = "goldfish";
    final String encodedAlias = URLEncoder.encode(alias, StandardCharsets.UTF_8);
    {
      ServerResponse<ScimTruststore> response = scimRequestBuilder.get(ScimTruststore.class,
                                                                       TRUSTSTORE_ENDPOINT,
                                                                       encodedAlias)
                                                                  .sendRequest();
      Assertions.assertEquals(HttpStatus.OK, response.getHttpStatus());
      ScimTruststore scimTruststore = response.getResource();
      Assertions.assertTrue(scimTruststore.getCertificateInfo().isPresent());
      CertificateInfo certificateInfo = scimTruststore.getCertificateInfo().get();
      Assertions.assertEquals("CN=goldfish", certificateInfo.getInfo().getIssuerDn());
      Assertions.assertEquals("CN=goldfish", certificateInfo.getInfo().getSubjectDn());
      Assertions.assertNotNull(certificateInfo.getInfo().getSha256Fingerprint());
      Assertions.assertNotNull(certificateInfo.getInfo().getValidFrom());
      Assertions.assertNotNull(certificateInfo.getInfo().getValidTo());
    }
  }

  @SneakyThrows
  @Test
  public void testLoadSingleAliasWithBadlyEncodedAlias()
  {
    final String badAlias = "$%& _()";
    // upload truststore
    {
      final String alias = "goldfish";
      final byte[] certificateBytes = getCertificateOfKeystore(UNIT_TEST_KEYSTORE_JKS, alias);
      final String b64Certificate = Base64.getEncoder().encodeToString(certificateBytes);
      CertificateUpload certificateUpload = CertificateUpload.builder()
                                                             .certificateFile(b64Certificate)
                                                             .alias(badAlias)
                                                             .build();
      ScimTruststore scimTruststore = ScimTruststore.builder().certificateUpload(certificateUpload).build();

      ServerResponse<ScimTruststore> response = scimRequestBuilder.create(ScimTruststore.class, TRUSTSTORE_ENDPOINT)
                                                                  .setResource(scimTruststore)
                                                                  .sendRequest();
      Assertions.assertEquals(HttpStatus.CREATED, response.getHttpStatus());
      Assertions.assertEquals(1, truststoreDao.getTruststore().getTruststoreAliases().size());
    }

    final String alias = URLEncoder.encode(badAlias, StandardCharsets.UTF_8);
    {
      ServerResponse<ScimTruststore> response = scimRequestBuilder.get(ScimTruststore.class, TRUSTSTORE_ENDPOINT, alias)
                                                                  .sendRequest();
      Assertions.assertEquals(HttpStatus.OK, response.getHttpStatus());
      ScimTruststore scimTruststore = response.getResource();
      Assertions.assertTrue(scimTruststore.getCertificateInfo().isPresent());
      CertificateInfo certificateInfo = scimTruststore.getCertificateInfo().get();
      Assertions.assertEquals("CN=goldfish", certificateInfo.getInfo().getIssuerDn());
      Assertions.assertEquals("CN=goldfish", certificateInfo.getInfo().getSubjectDn());
      Assertions.assertNotNull(certificateInfo.getInfo().getSha256Fingerprint());
      Assertions.assertNotNull(certificateInfo.getInfo().getValidFrom());
      Assertions.assertNotNull(certificateInfo.getInfo().getValidTo());
    }
  }

  @SneakyThrows
  @Test
  public void testDeleteAlias()
  {
    final String alias = "goldfish";
    // upload certificate
    {
      final byte[] certificateBytes = getCertificateOfKeystore(UNIT_TEST_KEYSTORE_JKS, alias);
      final String b64Certificate = Base64.getEncoder().encodeToString(certificateBytes);
      CertificateUpload certificateUpload = CertificateUpload.builder()
                                                             .certificateFile(b64Certificate)
                                                             .alias(alias)
                                                             .build();
      ScimTruststore scimTruststore = ScimTruststore.builder().certificateUpload(certificateUpload).build();

      ServerResponse<ScimTruststore> response = scimRequestBuilder.create(ScimTruststore.class, TRUSTSTORE_ENDPOINT)
                                                                  .setResource(scimTruststore)
                                                                  .sendRequest();
      Assertions.assertEquals(HttpStatus.CREATED, response.getHttpStatus());
      Assertions.assertEquals(1, truststoreDao.getTruststore().getTruststoreAliases().size());
    }
    // delete alias
    {
      final String encodedAlias = URLEncoder.encode(alias, StandardCharsets.UTF_8);
      ServerResponse<ScimTruststore> response = scimRequestBuilder.delete(ScimTruststore.class,
                                                                          TRUSTSTORE_ENDPOINT,
                                                                          encodedAlias)
                                                                  .sendRequest();
      Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getHttpStatus());
      Assertions.assertEquals(0, truststoreDao.getTruststore().getTruststoreAliases().size());
    }
  }

  @SneakyThrows
  @Test
  public void testDeleteWithBadlyEncodedAlias()
  {
    final String badAlias = "$%&";
    // upload certificate
    {
      final String goodAlias = "goldfish";
      final byte[] certificateBytes = getCertificateOfKeystore(UNIT_TEST_KEYSTORE_JKS, goodAlias);
      final String b64Certificate = Base64.getEncoder().encodeToString(certificateBytes);
      CertificateUpload certificateUpload = CertificateUpload.builder()
                                                             .certificateFile(b64Certificate)
                                                             .alias(badAlias)
                                                             .build();
      ScimTruststore scimTruststore = ScimTruststore.builder().certificateUpload(certificateUpload).build();

      ServerResponse<ScimTruststore> response = scimRequestBuilder.create(ScimTruststore.class, TRUSTSTORE_ENDPOINT)
                                                                  .setResource(scimTruststore)
                                                                  .sendRequest();
      Assertions.assertEquals(HttpStatus.CREATED, response.getHttpStatus());
      Assertions.assertEquals(1, truststoreDao.getTruststore().getTruststoreAliases().size());
    }
    // delete alias
    {
      final String alias = URLEncoder.encode(badAlias, StandardCharsets.UTF_8);
      ServerResponse<ScimTruststore> response = scimRequestBuilder.delete(ScimTruststore.class,
                                                                          TRUSTSTORE_ENDPOINT,
                                                                          alias)
                                                                  .sendRequest();
      Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getHttpStatus());
      Assertions.assertEquals(0, truststoreDao.getTruststore().getTruststoreAliases().size());
    }
  }

  @SneakyThrows
  @Test
  public void testUploadCertificateWithIllegalCharacterInAlias()
  {
    // upload certificate without alias
    final String alias = "goldfish";
    // the '/' character causes problems in HTTP requests so we do not accept such characters
    final String illegalAlias = "test/";
    final byte[] certificateBytes = getCertificateOfKeystore(UNIT_TEST_KEYSTORE_JKS, alias);
    final String b64Certificate = Base64.getEncoder().encodeToString(certificateBytes);
    CertificateUpload certificateUpload = CertificateUpload.builder()
                                                           .certificateFile(b64Certificate)
                                                           .alias(illegalAlias)
                                                           .build();
    ScimTruststore scimTruststore = ScimTruststore.builder().certificateUpload(certificateUpload).build();

    ServerResponse<ScimTruststore> response = scimRequestBuilder.create(ScimTruststore.class, TRUSTSTORE_ENDPOINT)
                                                                .setResource(scimTruststore)
                                                                .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();

    List<String> errorMessages = errorResponse.getErrorMessages();
    MatcherAssert.assertThat(errorMessages, Matchers.emptyIterable());
    Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
    Assertions.assertEquals(1, fieldErrors.size());
    {
      final String fieldName = String.format("%s.%s",
                                             ScimTruststore.FieldNames.CERTIFICATE_UPLOAD,
                                             ScimTruststore.FieldNames.ALIAS);
      List<String> fieldErrorMessages = fieldErrors.get(fieldName);
      Assertions.assertEquals(1, fieldErrorMessages.size());

      String errorMessage = String.format("The alias '%s' must not contain '/' characters", illegalAlias);
      MatcherAssert.assertThat(fieldErrorMessages, Matchers.containsInAnyOrder(errorMessage));
    }
  }

  @SneakyThrows
  @Test
  public void testUploadTruststoreWithIllegalCharacterInAlias()
  {
    KeyStore truststore = KeyStore.getInstance("JKS");
    truststore.load(null, UNIT_TEST_KEYSTORE_PASSWORD.toCharArray());
    {
      KeyStore keyStore = KeyStore.getInstance("JKS");
      final byte[] certificateBytes = readAsBytes(UNIT_TEST_KEYSTORE_JKS);
      keyStore.load(new ByteArrayInputStream(certificateBytes), UNIT_TEST_KEYSTORE_PASSWORD.toCharArray());

      Enumeration<String> aliases = keyStore.aliases();
      while (aliases.hasMoreElements())
      {
        String keystoreAlias = aliases.nextElement();
        if (keystoreAlias.equals("localhost"))
        {
          truststore.setCertificateEntry(keystoreAlias, keyStore.getCertificate(keystoreAlias));
        }
        else
        {
          truststore.setCertificateEntry(keystoreAlias + "/", keyStore.getCertificate(keystoreAlias));
        }
      }
    }

    final byte[] truststoreBytes = KeyStoreSupporter.getBytes(truststore, UNIT_TEST_KEYSTORE_PASSWORD);
    final String b64Truststore = Base64.getEncoder().encodeToString(truststoreBytes);
    final String filename = getFilename(UNIT_TEST_KEYSTORE_JKS);

    TruststoreUpload truststoreUpload = TruststoreUpload.builder()
                                                        .truststoreFile(b64Truststore)
                                                        .truststorePassword(UNIT_TEST_KEYSTORE_PASSWORD)
                                                        .truststoreFileName(filename)
                                                        .build();

    ScimTruststore scimTruststore = ScimTruststore.builder().truststoreUpload(truststoreUpload).build();

    ServerResponse<ScimTruststore> response = scimRequestBuilder.create(ScimTruststore.class, TRUSTSTORE_ENDPOINT)
                                                                .setResource(scimTruststore)
                                                                .sendRequest();

    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();

    List<String> errorMessages = errorResponse.getErrorMessages();
    MatcherAssert.assertThat(errorMessages, Matchers.emptyIterable());
    Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
    Assertions.assertEquals(1, fieldErrors.size());
    {
      final String fieldName = String.format("%s.%s",
                                             ScimTruststore.FieldNames.TRUSTSTORE_UPLOAD,
                                             ScimTruststore.FieldNames.TRUSTSTORE_FILE);
      List<String> fieldErrorMessages = fieldErrors.get(fieldName);
      Assertions.assertEquals(3, fieldErrorMessages.size());

      String errorMessage1 = "This truststore cannot be handled for illegal character in alias";
      String errorMessage2 = String.format("The alias '%s' contains an illegal character '/'", "goldfish/");
      String errorMessage3 = String.format("The alias '%s' contains an illegal character '/'", "unit-test/");
      MatcherAssert.assertThat(fieldErrorMessages,
                               Matchers.containsInAnyOrder(errorMessage1, errorMessage2, errorMessage3));
    }
  }

  @SneakyThrows
  private byte[] getCertificateOfKeystore(String keystorePath, String alias)
  {
    KeyStore keyStore;
    try (InputStream inputStream = readAsInputStream(keystorePath))
    {
      KeyStoreSupporter.KeyStoreType type = KeyStoreSupporter.KeyStoreType.byFileExtension(keystorePath)
                                                                          .orElse(KeyStoreSupporter.KeyStoreType.JKS);
      keyStore = KeyStoreSupporter.readKeyStore(inputStream, type, UNIT_TEST_KEYSTORE_PASSWORD);
    }

    return keyStore.getCertificate(alias).getEncoded();
  }

  @SneakyThrows
  public byte[] getParsablePkc12Keystore()
  {
    try (InputStream inputStream = readAsInputStream(UNIT_TEST_KEYSTORE_PKCS12))
    {
      KeyStore keyStore = KeyStoreSupporter.readKeyStore(inputStream,
                                                         KeyStoreSupporter.KeyStoreType.JKS,
                                                         UNIT_TEST_KEYSTORE_PASSWORD);
      keyStore.deleteEntry("unit-test");
      return KeyStoreSupporter.getBytes(keyStore, UNIT_TEST_KEYSTORE_PASSWORD);
    }
  }
}
