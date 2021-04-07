package de.captaingoldfish.oauthrestclient.application.endpoints.truststore;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.List;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.captaingoldfish.oauthrestclient.application.endpoints.models.CertificateInfo;
import de.captaingoldfish.oauthrestclient.application.endpoints.truststore.forms.TruststoreInfoForm;
import de.captaingoldfish.oauthrestclient.application.endpoints.truststore.forms.TruststoreUploadResponseForm;
import de.captaingoldfish.oauthrestclient.application.setup.AbstractOAuthRestClientTest;
import de.captaingoldfish.oauthrestclient.application.setup.ErrorResponseForm;
import de.captaingoldfish.oauthrestclient.application.setup.OAuthRestClientTest;
import de.captaingoldfish.oauthrestclient.commons.keyhelper.KeyStoreSupporter;
import de.captaingoldfish.oauthrestclient.database.entities.Truststore;
import kong.unirest.HttpResponse;
import kong.unirest.HttpStatus;
import kong.unirest.JsonNode;
import kong.unirest.MultipartBody;
import kong.unirest.Unirest;
import lombok.SneakyThrows;


/**
 * @author Pascal Knueppel
 * @since 04.04.2021
 */
@OAuthRestClientTest
public class TruststoreControllerTest extends AbstractOAuthRestClientTest
{

  /**
   * verifies that a truststore can be successfully merged and that already existing entries in the truststore
   * will be ignored without throwing an error
   */
  @SneakyThrows
  @ParameterizedTest
  @ValueSource(strings = {UNIT_TEST_KEYSTORE_JKS, UNIT_TEST_KEYSTORE_PKCS12})
  public void testMergeTruststoreFile(String keystorePath)
  {
    // send upload post request
    {
      HttpResponse<JsonNode> response;
      try (InputStream inputStream = readAsInputStream(keystorePath))
      {
        MultipartBody multipartBody = Unirest.post(getApplicationUrl("/truststore/add"))
                                             .field("truststoreFile", inputStream, "unit-test.jks")
                                             .field("truststorePassword", UNIT_TEST_KEYSTORE_PASSWORD);
        response = multipartBody.asJson();
      }
      Assertions.assertEquals(HttpStatus.OK, response.getStatus());
      TruststoreUploadResponseForm responseForm = getForm(response.getBody().toString(),
                                                          TruststoreUploadResponseForm.class);
      MatcherAssert.assertThat(responseForm.getAliases(),
                               Matchers.containsInAnyOrder("unit-test", "goldfish", "localhost"));
      Assertions.assertNull(responseForm.getDuplicateAliases());
      Assertions.assertNull(responseForm.getDuplicateCertificates());
      Assertions.assertEquals(3, truststoreDao.getTruststore().getTruststore().size());
    }

    // send same request again
    {
      HttpResponse<JsonNode> response;
      try (InputStream inputStream = readAsInputStream(keystorePath))
      {
        MultipartBody multipartBody = Unirest.post(getApplicationUrl("/truststore/add"))
                                             .field("truststoreFile", inputStream, "unit-test.jks")
                                             .field("truststorePassword", UNIT_TEST_KEYSTORE_PASSWORD);
        response = multipartBody.asJson();
      }
      Assertions.assertEquals(HttpStatus.OK, response.getStatus());
      TruststoreUploadResponseForm responseForm = getForm(response.getBody().toString(),
                                                          TruststoreUploadResponseForm.class);
      Assertions.assertNull(responseForm.getAliases());
      MatcherAssert.assertThat(responseForm.getDuplicateAliases(),
                               Matchers.containsInAnyOrder("unit-test", "goldfish", "localhost"));
      Assertions.assertNull(responseForm.getDuplicateCertificates());
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
      HttpResponse<JsonNode> response;
      final String alias = "goldfish";
      try (InputStream inputStream = getCertificateStreamOfKeystore(UNIT_TEST_KEYSTORE_JKS, alias))
      {
        MultipartBody multipartBody = Unirest.post(getApplicationUrl("/truststore/add"))
                                             .field("certificateFile", inputStream, alias + ".cer")
                                             .field("alias", alias);
        response = multipartBody.asJson();
      }
      Assertions.assertEquals(HttpStatus.OK, response.getStatus());
      TruststoreUploadResponseForm responseForm = getForm(response.getBody().toString(),
                                                          TruststoreUploadResponseForm.class);
      Assertions.assertEquals(1, responseForm.getAliases().size());
      Assertions.assertEquals(alias, responseForm.getAliases().get(0));
      Assertions.assertNull(responseForm.getDuplicateAliases());
      Assertions.assertNull(responseForm.getDuplicateCertificates());
      Truststore truststore = truststoreDao.getTruststore();
      Assertions.assertEquals(1, truststore.getTruststore().size());
      Assertions.assertTrue(truststore.getTruststore().containsAlias(alias));
    }

    // send request again
    {
      HttpResponse<JsonNode> response;
      final String alias = "goldfish";
      try (InputStream inputStream = getCertificateStreamOfKeystore(UNIT_TEST_KEYSTORE_JKS, alias))
      {
        MultipartBody multipartBody = Unirest.post(getApplicationUrl("/truststore/add"))
                                             .field("certificateFile", inputStream, alias + ".cer")
                                             .field("alias", alias);
        response = multipartBody.asJson();
      }
      Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
      ErrorResponseForm responseForm = getForm(response.getBody().toString(), ErrorResponseForm.class);
      List<String> truststoreFileErrors = responseForm.getInputFieldErrors().get("certificateFile");
      Assertions.assertNotNull(truststoreFileErrors);
      Assertions.assertEquals(2, truststoreFileErrors.size());
      MatcherAssert.assertThat(truststoreFileErrors,
                               Matchers.containsInAnyOrder("Cannot add certificate for alias 'goldfish' "
                                                           + "is already taken",
                                                           "Cannot add certificate for certificate is already "
                                                                                 + "present under alias 'goldfish'"));
    }

    // send request again with another alias
    {
      HttpResponse<JsonNode> response;
      final String alias = "super-mario";
      try (InputStream inputStream = getCertificateStreamOfKeystore(UNIT_TEST_KEYSTORE_JKS, "goldfish"))
      {
        MultipartBody multipartBody = Unirest.post(getApplicationUrl("/truststore/add"))
                                             .field("certificateFile", inputStream, alias + ".cer")
                                             .field("alias", alias);
        response = multipartBody.asJson();
      }
      Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
      ErrorResponseForm responseForm = getForm(response.getBody().toString(), ErrorResponseForm.class);
      List<String> truststoreFileErrors = responseForm.getInputFieldErrors().get("certificateFile");
      Assertions.assertNotNull(truststoreFileErrors);
      Assertions.assertEquals(1, truststoreFileErrors.size());
      MatcherAssert.assertThat(truststoreFileErrors,
                               Matchers.containsInAnyOrder("Cannot add certificate for certificate is already "
                                                           + "present under alias 'goldfish'"));
    }
  }

  @SneakyThrows
  @Test
  public void testRequestWithoutFiles()
  {
    HttpResponse<JsonNode> response = Unirest.post(getApplicationUrl("/truststore/add"))
                                             .field("truststoreFile",
                                                    new ByteArrayInputStream(new byte[0]),
                                                    "trust.jks")
                                             .field("certificateFile", new ByteArrayInputStream(new byte[0]), "c.cer")
                                             .asJson();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
    ErrorResponseForm responseForm = getForm(response.getBody().toString(), ErrorResponseForm.class);
    Assertions.assertNull(responseForm.getErrorMessages());
    Assertions.assertEquals(2, responseForm.getInputFieldErrors().size());
    List<String> truststoreFileErrors = responseForm.getInputFieldErrors().get("truststoreFile");
    Assertions.assertNotNull(truststoreFileErrors);
    Assertions.assertEquals(1, truststoreFileErrors.size());
    MatcherAssert.assertThat(truststoreFileErrors, Matchers.containsInAnyOrder("No file was uploaded"));

    List<String> certificateFileErrors = responseForm.getInputFieldErrors().get("certificateFile");
    Assertions.assertEquals(1, certificateFileErrors.size());
    MatcherAssert.assertThat(certificateFileErrors, Matchers.containsInAnyOrder("No file was uploaded"));
  }


  @SneakyThrows
  @Test
  public void testRequestWithBothFiles()
  {
    HttpResponse<JsonNode> response = Unirest.post(getApplicationUrl("/truststore/add"))
                                             .field("truststoreFile",
                                                    new ByteArrayInputStream(new byte[1]),
                                                    "trust.jks")
                                             .field("certificateFile", new ByteArrayInputStream(new byte[1]), "c.cer")
                                             .asJson();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
    ErrorResponseForm responseForm = getForm(response.getBody().toString(), ErrorResponseForm.class);
    Assertions.assertNull(responseForm.getErrorMessages());
    Assertions.assertEquals(2, responseForm.getInputFieldErrors().size());

    List<String> truststoreFileErrors = responseForm.getInputFieldErrors().get("truststoreFile");
    Assertions.assertNotNull(truststoreFileErrors);
    Assertions.assertEquals(1, truststoreFileErrors.size());
    MatcherAssert.assertThat(truststoreFileErrors,
                             Matchers.containsInAnyOrder("Accepting only a single upload file: keystore or certificate file"));

    List<String> certificateFileErrors = responseForm.getInputFieldErrors().get("certificateFile");
    Assertions.assertEquals(1, certificateFileErrors.size());
    MatcherAssert.assertThat(certificateFileErrors,
                             Matchers.containsInAnyOrder("Accepting only a single upload file: keystore or certificate file"));
  }

  @SneakyThrows
  @Test
  public void testUploadPkcs12KeystoreWithoutPassword()
  {
    HttpResponse<JsonNode> response;
    try (InputStream inputStream = getParsablePkc12Keystore())
    {
      MultipartBody multipartBody = Unirest.post(getApplicationUrl("/truststore/add"))
                                           .field("truststoreFile", inputStream, "unit-test.p12");
      response = multipartBody.asJson();
    }
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
    ErrorResponseForm responseForm = getForm(response.getBody().toString(), ErrorResponseForm.class);
    Assertions.assertNull(responseForm.getErrorMessages());
    Assertions.assertEquals(2, responseForm.getInputFieldErrors().size());

    List<String> truststoreFileErrors = responseForm.getInputFieldErrors().get("truststoreFile");
    Assertions.assertNotNull(truststoreFileErrors);
    Assertions.assertEquals(1, truststoreFileErrors.size());
    MatcherAssert.assertThat(truststoreFileErrors,
                             Matchers.containsInAnyOrder("PKCS12 key store mac invalid - wrong password or corrupted file."));

    List<String> truststorePasswordErrors = responseForm.getInputFieldErrors().get("truststorePassword");
    Assertions.assertNotNull(truststorePasswordErrors);
    Assertions.assertEquals(1, truststorePasswordErrors.size());
    MatcherAssert.assertThat(truststorePasswordErrors,
                             Matchers.containsInAnyOrder("Not accepting empty passwords for PKCS12 keystore type"));
  }

  @Test
  public void testUploadIllegalCertificateFile()
  {
    HttpResponse<JsonNode> response = Unirest.post(getApplicationUrl("/truststore/add"))
                                             .field("certificateFile", new ByteArrayInputStream(new byte[1]), "c.cer")
                                             .field("alias", "goldfish")
                                             .asJson();

    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
    ErrorResponseForm responseForm = getForm(response.getBody().toString(), ErrorResponseForm.class);
    Assertions.assertNull(responseForm.getErrorMessages());
    Assertions.assertEquals(1, responseForm.getInputFieldErrors().size());

    List<String> truststoreFileErrors = responseForm.getInputFieldErrors().get("certificateFile");
    Assertions.assertNotNull(truststoreFileErrors);
    Assertions.assertEquals(2, truststoreFileErrors.size());
    MatcherAssert.assertThat(truststoreFileErrors,
                             Matchers.containsInAnyOrder("parsing issue: malformed PEM data: no header found",
                                                         "malformed PEM data: no header found"));
  }

  /**
   * verifies that a truststore can be successfully merged and that already existing entries in the truststore
   * will be ignored without throwing an error
   */
  @SneakyThrows
  @Test
  public void testMergeTruststoreFileWithDuplicateCertificate()
  {
    // send request again with another alias
    {
      HttpResponse<JsonNode> response;
      final String alias = "super-mario";
      try (InputStream inputStream = getCertificateStreamOfKeystore(UNIT_TEST_KEYSTORE_JKS, "goldfish"))
      {
        MultipartBody multipartBody = Unirest.post(getApplicationUrl("/truststore/add"))
                                             .field("certificateFile", inputStream, alias + ".cer")
                                             .field("alias", alias);
        response = multipartBody.asJson();
      }
      Assertions.assertEquals(HttpStatus.OK, response.getStatus());
      TruststoreUploadResponseForm responseForm = getForm(response.getBody().toString(),
                                                          TruststoreUploadResponseForm.class);
      Assertions.assertNull(responseForm.getDuplicateCertificates());
      Assertions.assertNull(responseForm.getDuplicateAliases());
      Assertions.assertEquals(1, responseForm.getAliases().size());
      Assertions.assertEquals(alias, responseForm.getAliases().get(0));
      Assertions.assertEquals(1, truststoreDao.getTruststore().getTruststore().size());
    }

    // send upload post request
    {
      HttpResponse<JsonNode> response;
      try (InputStream inputStream = readAsInputStream(UNIT_TEST_KEYSTORE_JKS))
      {
        MultipartBody multipartBody = Unirest.post(getApplicationUrl("/truststore/add"))
                                             .field("truststoreFile", inputStream, "unit-test.jks")
                                             .field("truststorePassword", UNIT_TEST_KEYSTORE_PASSWORD);
        response = multipartBody.asJson();
      }
      Assertions.assertEquals(HttpStatus.OK, response.getStatus());
      TruststoreUploadResponseForm responseForm = getForm(response.getBody().toString(),
                                                          TruststoreUploadResponseForm.class);
      MatcherAssert.assertThat(responseForm.getAliases(), Matchers.containsInAnyOrder("unit-test", "localhost"));
      Assertions.assertNull(responseForm.getDuplicateAliases());
      Assertions.assertNotNull(responseForm.getDuplicateCertificates());
      Assertions.assertEquals(1, responseForm.getDuplicateCertificates().size());
      MatcherAssert.assertThat(responseForm.getDuplicateCertificates(), Matchers.hasItem("goldfish"));
      Assertions.assertEquals(3, truststoreDao.getTruststore().getTruststore().size());
    }
  }

  @SneakyThrows
  @Test
  public void testGetTruststoreInfos()
  {
    // send upload post request
    {
      HttpResponse<JsonNode> response;
      try (InputStream inputStream = readAsInputStream(UNIT_TEST_KEYSTORE_JKS))
      {
        MultipartBody multipartBody = Unirest.post(getApplicationUrl("/truststore/add"))
                                             .field("truststoreFile", inputStream, "unit-test.jks")
                                             .field("truststorePassword", UNIT_TEST_KEYSTORE_PASSWORD);
        response = multipartBody.asJson();
      }
      Assertions.assertEquals(HttpStatus.OK, response.getStatus());
      TruststoreUploadResponseForm responseForm = getForm(response.getBody().toString(),
                                                          TruststoreUploadResponseForm.class);
      MatcherAssert.assertThat(responseForm.getAliases(),
                               Matchers.containsInAnyOrder("unit-test", "goldfish", "localhost"));
      Assertions.assertNull(responseForm.getDuplicateAliases());
      Assertions.assertNull(responseForm.getDuplicateCertificates());
      Assertions.assertEquals(3, truststoreDao.getTruststore().getTruststore().size());
    }

    {
      HttpResponse<JsonNode> response = Unirest.get(getApplicationUrl("/truststore/infos")).asJson();
      Assertions.assertEquals(HttpStatus.OK, response.getStatus());
      TruststoreInfoForm truststoreInfoForm = getForm(response.getBody().toString(), TruststoreInfoForm.class);
      Assertions.assertEquals(3, truststoreInfoForm.getNumberOfEntries());
    }
  }

  @SneakyThrows
  @Test
  public void testDownloadTruststore()
  {
    // send upload post request
    {
      HttpResponse<JsonNode> response;
      try (InputStream inputStream = readAsInputStream(UNIT_TEST_KEYSTORE_JKS))
      {
        MultipartBody multipartBody = Unirest.post(getApplicationUrl("/truststore/add"))
                                             .field("truststoreFile", inputStream, "unit-test.jks")
                                             .field("truststorePassword", UNIT_TEST_KEYSTORE_PASSWORD);
        response = multipartBody.asJson();
      }
      Assertions.assertEquals(HttpStatus.OK, response.getStatus());
      TruststoreUploadResponseForm responseForm = getForm(response.getBody().toString(),
                                                          TruststoreUploadResponseForm.class);
      MatcherAssert.assertThat(responseForm.getAliases(),
                               Matchers.containsInAnyOrder("unit-test", "goldfish", "localhost"));
      Assertions.assertNull(responseForm.getDuplicateAliases());
      Assertions.assertNull(responseForm.getDuplicateCertificates());
      Assertions.assertEquals(3, truststoreDao.getTruststore().getTruststore().size());
    }

    {
      HttpResponse<byte[]> response = Unirest.get(getApplicationUrl("/truststore/download")).asBytes();
      Assertions.assertEquals(HttpStatus.OK, response.getStatus());
      KeyStore javaTruststore = KeyStoreSupporter.readTruststore(response.getBody(),
                                                                 KeyStoreSupporter.KeyStoreType.JKS);
      Assertions.assertEquals(3, javaTruststore.size());
    }
  }

  @SneakyThrows
  @Test
  public void testUploadCertificateWithoutAlias()
  {
    HttpResponse<JsonNode> response;
    final String alias = "goldfish";
    try (InputStream inputStream = getCertificateStreamOfKeystore(UNIT_TEST_KEYSTORE_JKS, alias))
    {
      MultipartBody multipartBody = Unirest.post(getApplicationUrl("/truststore/add"))
                                           .field("certificateFile", inputStream, alias + ".cer");
      response = multipartBody.asJson();
    }
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
    ErrorResponseForm responseForm = getForm(response.getBody().toString(), ErrorResponseForm.class);
    Assertions.assertNull(responseForm.getErrorMessages());
    Assertions.assertEquals(1, responseForm.getInputFieldErrors().size());
    List<String> aliasErrors = responseForm.getInputFieldErrors().get("alias");
    Assertions.assertNotNull(aliasErrors);
    Assertions.assertEquals(1, aliasErrors.size());
    Assertions.assertEquals("Alias must not be blank", aliasErrors.get(0));
  }

  @SneakyThrows
  @Test
  public void testLoadSingleAlias()
  {
    // send upload post request
    {
      HttpResponse<JsonNode> response;
      try (InputStream inputStream = readAsInputStream(UNIT_TEST_KEYSTORE_JKS))
      {
        MultipartBody multipartBody = Unirest.post(getApplicationUrl("/truststore/add"))
                                             .field("truststoreFile", inputStream, "unit-test.jks")
                                             .field("truststorePassword", UNIT_TEST_KEYSTORE_PASSWORD);
        response = multipartBody.asJson();
      }
      Assertions.assertEquals(HttpStatus.OK, response.getStatus());
    }

    HttpResponse<JsonNode> response = Unirest.get(getApplicationUrl("/truststore/load-alias"))
                                             .queryString("alias", "goldfish")
                                             .asJson();
    Assertions.assertEquals(HttpStatus.OK, response.getStatus());
    CertificateInfo certificateInfo = getForm(response.getBody().toString(), CertificateInfo.class);
    Assertions.assertEquals("CN=goldfish", certificateInfo.getIssuerDn());
    Assertions.assertEquals("CN=goldfish", certificateInfo.getSubjectDn());
    Assertions.assertNotNull(certificateInfo.getSha256fingerprint());
    Assertions.assertNotNull(certificateInfo.getValidFrom());
    Assertions.assertNotNull(certificateInfo.getValidUntil());
  }

  @SneakyThrows
  @Test
  public void testDeleteAlias()
  {
    // send upload post request
    {
      HttpResponse<JsonNode> response;
      try (InputStream inputStream = readAsInputStream(UNIT_TEST_KEYSTORE_JKS))
      {
        MultipartBody multipartBody = Unirest.post(getApplicationUrl("/truststore/add"))
                                             .field("truststoreFile", inputStream, "unit-test.jks")
                                             .field("truststorePassword", UNIT_TEST_KEYSTORE_PASSWORD);
        response = multipartBody.asJson();
      }
      Assertions.assertEquals(HttpStatus.OK, response.getStatus());
      Assertions.assertEquals(3, truststoreDao.getTruststore().getTruststore().size());
    }

    HttpResponse<JsonNode> response = Unirest.delete(getApplicationUrl("/truststore/delete-alias"))
                                             .queryString("alias", "goldfish")
                                             .asJson();
    Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getStatus());
    Assertions.assertEquals(2, truststoreDao.getTruststore().getTruststore().size());
  }

  @SneakyThrows
  private InputStream getCertificateStreamOfKeystore(String keystorePath, String alias)
  {
    KeyStore keyStore;
    try (InputStream inputStream = readAsInputStream(keystorePath))
    {
      KeyStoreSupporter.KeyStoreType type = KeyStoreSupporter.KeyStoreType.byFileExtension(keystorePath)
                                                                          .orElse(KeyStoreSupporter.KeyStoreType.JKS);
      keyStore = KeyStoreSupporter.readKeyStore(inputStream, type, UNIT_TEST_KEYSTORE_PASSWORD);
    }

    return new ByteArrayInputStream(keyStore.getCertificate(alias).getEncoded());
  }

  @SneakyThrows
  public InputStream getParsablePkc12Keystore()
  {
    try (InputStream inputStream = readAsInputStream(UNIT_TEST_KEYSTORE_PKCS12))
    {
      KeyStore keyStore = KeyStoreSupporter.readKeyStore(inputStream,
                                                         KeyStoreSupporter.KeyStoreType.JKS,
                                                         UNIT_TEST_KEYSTORE_PASSWORD);
      keyStore.deleteEntry("unit-test");
      return new ByteArrayInputStream(KeyStoreSupporter.getBytes(keyStore, UNIT_TEST_KEYSTORE_PASSWORD));
    }
  }
}
