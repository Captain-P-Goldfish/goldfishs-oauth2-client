package de.captaingoldfish.restclient.application.endpoints.keystore;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import de.captaingoldfish.restclient.application.endpoints.keystore.forms.KeystoreInfoForm;
import de.captaingoldfish.restclient.application.endpoints.models.CertificateInfo;
import de.captaingoldfish.restclient.application.setup.AbstractOAuthRestClientTest;
import de.captaingoldfish.restclient.application.setup.OAuthRestClientTest;
import de.captaingoldfish.restclient.commons.keyhelper.KeyStoreSupporter;
import de.captaingoldfish.restclient.database.entities.Keystore;
import de.captaingoldfish.restclient.database.entities.KeystoreEntry;
import kong.unirest.HttpResponse;
import kong.unirest.HttpStatus;
import kong.unirest.JsonNode;
import kong.unirest.MultipartBody;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONException;
import kong.unirest.json.JSONObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


/**
 * @author Pascal Knueppel
 * @since 31.03.2021
 */
@Slf4j
@OAuthRestClientTest
public class KeystoreControllerTest extends AbstractOAuthRestClientTest
{

  @SneakyThrows
  @BeforeEach
  public void checkApplicationKeystore()
  {
    Keystore keystore = keystoreDao.getKeystore();
    Assertions.assertEquals(0, keystore.getKeystoreEntries().size());
    Assertions.assertFalse(keystore.getKeyStore().aliases().hasMoreElements());
  }

  @SneakyThrows
  @ParameterizedTest
  @ValueSource(strings = {UNIT_TEST_KEYSTORE_JKS, UNIT_TEST_KEYSTORE_PKCS12})
  public void testSuccessfullyUploadKeystoreAndSelectEntries(String keystorePath)
  {
    // the stateId the is returned in the upload response and must be send in the select alias request
    String stateId;

    // upload keystore file
    {
      HttpResponse<JsonNode> response;
      // send upload post request
      try (InputStream inputStream = readAsInputStream(keystorePath))
      {
        MultipartBody multipartBody = Unirest.post(getApplicationUrl("/keystore/upload"))
                                             // for some reason SUN crypto provider is able to resolve PKCS12 under JKS
                                             // type
                                             .field("keystoreFile", inputStream, "unit-test.jks")
                                             .field("keystorePassword", UNIT_TEST_KEYSTORE_PASSWORD);
        response = multipartBody.asJson();
      }

      // validate upload response
      {
        log.warn(response.getBody().toPrettyString());
        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        JSONObject jsonObject = response.getBody().getObject();
        stateId = jsonObject.getString("stateId");
        MatcherAssert.assertThat(stateId, Matchers.not(Matchers.emptyOrNullString()));
        Assertions.assertThrows(JSONException.class, () -> jsonObject.getString("aliasOverride"));
        Assertions.assertThrows(JSONException.class, () -> jsonObject.getString("privateKeyPassword"));
        JSONArray aliasesArray = jsonObject.getJSONArray("aliases");
        Assertions.assertEquals(3, aliasesArray.length());
        List<String> aliases = new ArrayList<>();
        aliasesArray.forEach(alias -> aliases.add((String)alias));
        MatcherAssert.assertThat(aliases, Matchers.containsInAnyOrder("goldfish", "unit-test", "localhost"));
      }
    }


    // select keystore aliases that should be stored in the application keystore
    {
      HttpResponse<JsonNode> response;
      // send select alias request
      {
        List<String> usedAliases = new ArrayList<>();
        for ( String alias : Arrays.asList("goldfish", "unit-test", "localhost") )
        {
          KeystoreEntry keystoreEntry = getUnitTestKeystoreEntryAccess(alias);
          // send select-alias request
          {
            response = Unirest.post(getApplicationUrl("/keystore/select-alias"))
                              .field("stateId", stateId)
                              .field("aliases", Collections.singletonList(alias))
                              .field("privateKeyPassword", keystoreEntry.getPrivateKeyPassword())
                              .asJson();
            usedAliases.add(alias);
          }

          // validate select-alias response
          {
            Assertions.assertEquals(HttpStatus.CREATED, response.getStatus());
            JSONObject jsonObject = response.getBody().getObject();
            Assertions.assertEquals(alias, jsonObject.getString("alias"));
            JSONObject certificateInfo = jsonObject.getJSONObject("certificateInfo");
            Assertions.assertEquals("CN=" + alias, certificateInfo.getString("issuerDn"));
            Assertions.assertEquals("CN=" + alias, certificateInfo.getString("subjectDn"));
            Assertions.assertNotNull(certificateInfo.getString("sha256fingerprint"));
            Assertions.assertNotNull(certificateInfo.getString("validFrom"));
            Assertions.assertNotNull(certificateInfo.getString("validUntil"));
          }

          // validate application keystore
          {
            Keystore applicationKeystore = keystoreDao.getKeystore();
            Assertions.assertEquals(usedAliases.size(), applicationKeystore.getKeyStoreAliases().size());
            Assertions.assertEquals(applicationKeystore.getKeystoreEntries().size(),
                                    applicationKeystore.getKeyStoreAliases().size());
            MatcherAssert.assertThat(applicationKeystore.getKeyStoreAliases(),
                                     Matchers.containsInAnyOrder(usedAliases.stream()
                                                                            .map(Matchers::equalTo)
                                                                            .collect(Collectors.toList())));
            KeystoreEntry mountedEntry = applicationKeystore.getKeystoreEntries()
                                                            .stream()
                                                            .filter(entry -> entry.getAlias().equals(alias))
                                                            .findAny()
                                                            .get();
            // for simplicity the keystore and all its entries will be stored under the password 123456
            Assertions.assertEquals("123456", mountedEntry.getPrivateKeyPassword());
            Assertions.assertDoesNotThrow(() -> applicationKeystore.getPrivateKey(mountedEntry));
          }
        }

        Keystore applicationKeystore = keystoreDao.getKeystore();
        for ( String usedAlias : usedAliases )
        {
          Assertions.assertNotNull(applicationKeystore.getPrivateKey(getUnitTestKeystoreEntryAccess(usedAlias)));
          X509Certificate certificate = applicationKeystore.getCertificate(getUnitTestKeystoreEntryAccess(usedAlias));
          Assertions.assertNotNull(certificate);
          Assertions.assertEquals("CN=" + usedAlias, certificate.getSubjectDN().toString());
          Assertions.assertEquals("CN=" + usedAlias, certificate.getIssuerDN().toString());
        }
      }
    }
  }

  @SneakyThrows
  @Test
  public void testUploadKeystoreWithIllegalPassword()
  {
    final String password = "illegal-password";
    HttpResponse<JsonNode> response;
    // send upload post request
    try (InputStream inputStream = readAsInputStream(UNIT_TEST_KEYSTORE_PKCS12))
    {
      MultipartBody multipartBody = Unirest.post(getApplicationUrl("/keystore/upload"))
                                           .field("keystoreFile", inputStream, "unit-test.p12")
                                           .field("keystorePassword", password);
      response = multipartBody.asJson();
    }

    // validate response
    {
      Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
      JSONObject jsonObject = response.getBody().getObject();
      Assertions.assertThrows(JSONException.class, () -> jsonObject.getJSONArray("errorMessages"));
      JSONObject errors = jsonObject.getJSONObject("inputFieldErrors");
      Assertions.assertEquals(1, errors.length());
      List<String> keystoreFileErrors = jsonArrayToList(errors.getJSONArray("keystoreFile"));
      Assertions.assertEquals(1, keystoreFileErrors.size());

      String errorMessage = "PKCS12 key store mac invalid - wrong password or corrupted file.";
      MatcherAssert.assertThat(keystoreFileErrors, Matchers.containsInAnyOrder(errorMessage));
    }
  }

  @SneakyThrows
  @Test
  public void testUploadKeystorePKCS12WithDifferentPasswords()
  {
    final String password = UNIT_TEST_KEYSTORE_PASSWORD;
    HttpResponse<JsonNode> response;
    // send upload post request
    try (InputStream inputStream = readAsInputStream(UNIT_TEST_KEYSTORE_PKCS12))
    {
      MultipartBody multipartBody = Unirest.post(getApplicationUrl("/keystore/upload"))
                                           .field("keystoreFile", inputStream, "unit-test.p12")
                                           .field("keystorePassword", password);
      response = multipartBody.asJson();
    }

    // validate response
    {
      Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
      log.warn(response.getBody().toPrettyString());
      JSONObject jsonObject = response.getBody().getObject();
      Assertions.assertThrows(JSONException.class, () -> jsonObject.getJSONArray("errorMessages"));
      JSONObject errors = jsonObject.getJSONObject("inputFieldErrors");
      Assertions.assertEquals(1, errors.length());
      List<String> keystoreFileErrors = jsonArrayToList(errors.getJSONArray("keystoreFile"));
      Assertions.assertEquals(2, keystoreFileErrors.size());

      String errorMessage1 = "exception unwrapping private key - java.security.InvalidKeyException: pad block corrupted";
      String errorMessage2 = "key-passwords must match keystore-password. This is a restriction of the BouncyCastle "
                             + "PKCS12 implementation. As a workaround rename the file extension to '.jks' this may "
                             + "work based on the used JDK";
      MatcherAssert.assertThat(keystoreFileErrors, Matchers.containsInAnyOrder(errorMessage1, errorMessage2));
    }
  }

  @SneakyThrows
  @Test
  public void testUploadEmptyFile()
  {
    final String password = UNIT_TEST_KEYSTORE_PASSWORD;
    HttpResponse<JsonNode> response;
    // send upload post request
    try (InputStream inputStream = new ByteArrayInputStream(new byte[0]))
    {
      MultipartBody multipartBody = Unirest.post(getApplicationUrl("/keystore/upload"))
                                           .field("keystoreFile", inputStream, "unit-test.p12")
                                           .field("keystorePassword", password);
      response = multipartBody.asJson();
    }

    // validate response
    {
      Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
      log.warn(response.getBody().toPrettyString());
      JSONObject jsonObject = response.getBody().getObject();
      Assertions.assertThrows(JSONException.class, () -> jsonObject.getJSONArray("errorMessages"));
      JSONObject errors = jsonObject.getJSONObject("inputFieldErrors");
      Assertions.assertEquals(1, errors.length());
      List<String> keystoreFileErrors = jsonArrayToList(errors.getJSONArray("keystoreFile"));
      Assertions.assertEquals(1, keystoreFileErrors.size());

      String errorMessage = "Keystore file must not be empty";
      MatcherAssert.assertThat(keystoreFileErrors, Matchers.containsInAnyOrder(errorMessage));
    }
  }

  @SneakyThrows
  @Test
  public void testUploadWithNullPassword()
  {
    HttpResponse<JsonNode> response;
    // send upload post request
    try (InputStream inputStream = readAsInputStream(UNIT_TEST_KEYSTORE_PKCS12))
    {
      MultipartBody multipartBody = Unirest.post(getApplicationUrl("/keystore/upload"))
                                           .field("keystoreFile", inputStream, "unit-test.p12");
      response = multipartBody.asJson();
    }

    // validate response
    {
      Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
      log.warn(response.getBody().toPrettyString());
      JSONObject jsonObject = response.getBody().getObject();
      Assertions.assertThrows(JSONException.class, () -> jsonObject.getJSONArray("errorMessages"));
      JSONObject errors = jsonObject.getJSONObject("inputFieldErrors");
      Assertions.assertEquals(2, errors.length());
      {
        List<String> keystoreFileErrors = jsonArrayToList(errors.getJSONArray("keystoreFile"));
        Assertions.assertEquals(1, keystoreFileErrors.size());

        String errorMessage = "Parsing of keystore failed";
        MatcherAssert.assertThat(keystoreFileErrors, Matchers.containsInAnyOrder(errorMessage));
      }
      {
        List<String> keystorePasswordErrors = jsonArrayToList(errors.getJSONArray("keystorePassword"));
        Assertions.assertEquals(1, keystorePasswordErrors.size());

        String errorMessage = "Not accepting empty passwords";
        MatcherAssert.assertThat(keystorePasswordErrors, Matchers.containsInAnyOrder(errorMessage));
      }
    }
  }

  @SneakyThrows
  @Test
  public void testUploadWithEmptyPassword()
  {
    final String password = "";
    HttpResponse<JsonNode> response;
    // send upload post request
    try (InputStream inputStream = readAsInputStream(UNIT_TEST_KEYSTORE_PKCS12))
    {
      MultipartBody multipartBody = Unirest.post(getApplicationUrl("/keystore/upload"))
                                           .field("keystoreFile", inputStream, "unit-test.p12")
                                           .field("keystorePassword", password);
      response = multipartBody.asJson();
    }

    // validate response
    {
      Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
      log.warn(response.getBody().toPrettyString());
      JSONObject jsonObject = response.getBody().getObject();
      Assertions.assertThrows(JSONException.class, () -> jsonObject.getJSONArray("errorMessages"));
      JSONObject errors = jsonObject.getJSONObject("inputFieldErrors");
      Assertions.assertEquals(2, errors.length());
      {
        List<String> keystoreFileErrors = jsonArrayToList(errors.getJSONArray("keystoreFile"));
        Assertions.assertEquals(1, keystoreFileErrors.size());

        String errorMessage = "PKCS12 key store mac invalid - wrong password or corrupted file.";
        MatcherAssert.assertThat(keystoreFileErrors, Matchers.containsInAnyOrder(errorMessage));
      }
      {
        List<String> keystorePasswordErrors = jsonArrayToList(errors.getJSONArray("keystorePassword"));
        Assertions.assertEquals(1, keystorePasswordErrors.size());

        String errorMessage = "Not accepting empty passwords";
        MatcherAssert.assertThat(keystorePasswordErrors, Matchers.containsInAnyOrder(errorMessage));
      }
    }
  }

  @Test
  public void testUploadWrongContentType()
  {
    // send upload post request
    HttpResponse<JsonNode> response = Unirest.post(getApplicationUrl("/keystore/upload")).asJson();

    // validate response
    {
      log.warn(response.getBody().toPrettyString());
      Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
      JSONObject jsonObject = response.getBody().getObject();
      Assertions.assertThrows(JSONException.class, () -> jsonObject.getJSONArray("inputFieldErrors"));
      List<String> errorMessages = jsonArrayToList(Assertions.assertDoesNotThrow(() -> jsonObject.getJSONArray("errorMessages")));
      Assertions.assertEquals(1, errorMessages.size());
      Assertions.assertEquals("Content type '' not supported", errorMessages.get(0));
    }
  }

  @Test
  public void testUploadWithoutParameters()
  {
    // send upload post request
    HttpResponse<JsonNode> response = Unirest.post(getApplicationUrl("/keystore/upload"))
                                             .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE)
                                             .asJson();

    // validate response
    {
      log.warn(response.getBody().toPrettyString());
      Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
      JSONObject jsonObject = response.getBody().getObject();
      List<String> errorMessages = jsonArrayToList(Assertions.assertDoesNotThrow(() -> jsonObject.getJSONArray("errorMessages")));
      Assertions.assertEquals(2, errorMessages.size());
      String errorMessage1 = "Failed to parse multipart servlet request; nested exception is "
                             + "org.apache.commons.fileupload.FileUploadException: the request was rejected "
                             + "because no multipart boundary was found";
      String errorMessage2 = "the request was rejected because no multipart boundary was found";
      MatcherAssert.assertThat(errorMessages, Matchers.containsInAnyOrder(errorMessage1, errorMessage2));
    }
  }

  @Test
  public void testSelectAliasWithoutStateId()
  {
    // select keystore aliases that should be stored in the application keystore
    // send select alias request
    final String alias = "goldfish";
    KeystoreEntry keystoreEntry = getUnitTestKeystoreEntryAccess(alias);
    // send select-alias request
    HttpResponse<JsonNode> response = Unirest.post(getApplicationUrl("/keystore/select-alias"))
                                             .field("aliases", Collections.singletonList(alias))
                                             .field("privateKeyPassword", keystoreEntry.getPrivateKeyPassword())
                                             .asJson();
    log.warn(response.getBody().toPrettyString());
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
    JSONObject jsonObject = response.getBody().getObject();
    Assertions.assertThrows(JSONException.class, () -> jsonObject.getJSONArray("inputFieldErrors"));
    List<String> errorMessages = jsonArrayToList(Assertions.assertDoesNotThrow(() -> jsonObject.getJSONArray("errorMessages")));
    Assertions.assertEquals(1, errorMessages.size());
    String errorMessage = "Ups the required stateId parameter was missing in the request...";
    MatcherAssert.assertThat(errorMessages, Matchers.containsInAnyOrder(errorMessage));
  }

  @Test
  public void testNoAliasSelected()
  {
    final String stateId = uploadKeystoreFile();
    // select keystore aliases that should be stored in the application keystore
    // send select alias request
    final String alias = "goldfish";
    KeystoreEntry keystoreEntry = getUnitTestKeystoreEntryAccess(alias);
    // send select-alias request
    HttpResponse<JsonNode> response = Unirest.post(getApplicationUrl("/keystore/select-alias"))
                                             .field("stateId", stateId)
                                             .field("privateKeyPassword", keystoreEntry.getPrivateKeyPassword())
                                             .asJson();
    log.warn(response.getBody().toPrettyString());
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
    JSONObject jsonObject = response.getBody().getObject();
    Assertions.assertThrows(JSONException.class, () -> jsonObject.getJSONArray("errorMessages"));
    JSONObject errors = jsonObject.getJSONObject("inputFieldErrors");
    Assertions.assertEquals(1, errors.length());
    {
      List<String> aliases = jsonArrayToList(errors.getJSONArray("aliases"));
      Assertions.assertEquals(1, aliases.size());

      String errorMessage = "No alias was selected";
      MatcherAssert.assertThat(aliases, Matchers.containsInAnyOrder(errorMessage));
    }
  }

  @Test
  public void testSeveralAliasesSelected()
  {
    final String stateId = uploadKeystoreFile();
    // select keystore aliases that should be stored in the application keystore
    // send select alias request
    final String alias = "goldfish";
    KeystoreEntry keystoreEntry = getUnitTestKeystoreEntryAccess(alias);
    // send select-alias request
    HttpResponse<JsonNode> response = Unirest.post(getApplicationUrl("/keystore/select-alias"))
                                             .field("stateId", stateId)
                                             .field("aliases", Arrays.asList(alias, "localhost", "unit-test"))
                                             .field("privateKeyPassword", keystoreEntry.getPrivateKeyPassword())
                                             .asJson();
    log.warn(response.getBody().toPrettyString());
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
    JSONObject jsonObject = response.getBody().getObject();
    Assertions.assertThrows(JSONException.class, () -> jsonObject.getJSONArray("errorMessages"));
    JSONObject errors = jsonObject.getJSONObject("inputFieldErrors");
    Assertions.assertEquals(1, errors.length());
    {
      List<String> aliases = jsonArrayToList(errors.getJSONArray("aliases"));
      Assertions.assertEquals(1, aliases.size());

      String errorMessage = "Only a single alias can be selected but found: [goldfish, localhost, unit-test]";
      MatcherAssert.assertThat(aliases, Matchers.containsInAnyOrder(errorMessage));
    }
  }

  @Test
  public void testUnknownStateId()
  {
    final String stateId = "unknown-stateId";
    // select keystore aliases that should be stored in the application keystore
    // send select alias request
    final String alias = "goldfish";
    KeystoreEntry keystoreEntry = getUnitTestKeystoreEntryAccess(alias);
    // send select-alias request
    HttpResponse<JsonNode> response = Unirest.post(getApplicationUrl("/keystore/select-alias"))
                                             .field("stateId", stateId)
                                             .field("aliases", Collections.singletonList(alias))
                                             .field("privateKeyPassword", keystoreEntry.getPrivateKeyPassword())
                                             .asJson();
    log.warn(response.getBody().toPrettyString());
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
    JSONObject jsonObject = response.getBody().getObject();
    Assertions.assertThrows(JSONException.class, () -> jsonObject.getJSONArray("inputFieldErrors"));
    List<String> errorMessages = jsonArrayToList(Assertions.assertDoesNotThrow(() -> jsonObject.getJSONArray("errorMessages")));
    Assertions.assertEquals(1, errorMessages.size());
    String errorMessage = "The stateId '" + stateId + "' could not be resolved to a previously uploaded keystore";
    MatcherAssert.assertThat(errorMessages, Matchers.containsInAnyOrder(errorMessage));
  }

  @Test
  public void testUnknownAlias()
  {
    final String stateId = uploadKeystoreFile();
    // select keystore aliases that should be stored in the application keystore
    // send select alias request
    final String alias = "unknown-alias";
    // send select-alias request
    HttpResponse<JsonNode> response = Unirest.post(getApplicationUrl("/keystore/select-alias"))
                                             .field("stateId", stateId)
                                             .field("aliases", Collections.singletonList(alias))
                                             .field("privateKeyPassword", "invalid")
                                             .asJson();
    log.warn(response.getBody().toPrettyString());
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
    JSONObject jsonObject = response.getBody().getObject();
    Assertions.assertThrows(JSONException.class, () -> jsonObject.getJSONArray("errorMessages"));
    JSONObject errors = jsonObject.getJSONObject("inputFieldErrors");
    Assertions.assertEquals(2, errors.length());
    {
      List<String> aliases = jsonArrayToList(errors.getJSONArray("aliases"));
      Assertions.assertEquals(1, aliases.size());

      String errorMessage = "Unknown alias selected: " + alias;
      MatcherAssert.assertThat(aliases, Matchers.containsInAnyOrder(errorMessage));
    }
    {
      List<String> aliases = jsonArrayToList(errors.getJSONArray("privateKeyPassword"));
      Assertions.assertEquals(1, aliases.size());

      String errorMessage = "Could not access private key of alias 'unknown-alias'";
      MatcherAssert.assertThat(aliases, Matchers.containsInAnyOrder(errorMessage));
    }
  }

  @Test
  public void testWrongKeyPassword()
  {
    final String stateId = uploadKeystoreFile();
    // select keystore aliases that should be stored in the application keystore
    // send select alias request
    final String alias = "goldfish";
    // send select-alias request
    HttpResponse<JsonNode> response = Unirest.post(getApplicationUrl("/keystore/select-alias"))
                                             .field("stateId", stateId)
                                             .field("aliases", Collections.singletonList(alias))
                                             .field("privateKeyPassword", "invalid")
                                             .asJson();
    log.warn(response.getBody().toPrettyString());
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
    JSONObject jsonObject = response.getBody().getObject();
    Assertions.assertThrows(JSONException.class, () -> jsonObject.getJSONArray("errorMessages"));
    JSONObject errors = jsonObject.getJSONObject("inputFieldErrors");
    Assertions.assertEquals(2, errors.length());
    {
      List<String> aliases = jsonArrayToList(errors.getJSONArray("aliases"));
      Assertions.assertEquals(1, aliases.size());

      String errorMessage = "Cannot recover key";
      MatcherAssert.assertThat(aliases, Matchers.containsInAnyOrder(errorMessage));
    }
    {
      List<String> aliases = jsonArrayToList(errors.getJSONArray("privateKeyPassword"));
      Assertions.assertEquals(2, aliases.size());

      String errorMessage1 = "Could not access private key of alias 'goldfish'";
      String errorMessage2 = "Cannot recover key";
      MatcherAssert.assertThat(aliases, Matchers.containsInAnyOrder(errorMessage1, errorMessage2));
    }
  }

  @Test
  public void testNoPasswordSendButPasswordMatchesWithKeystorePassword()
  {
    final String stateId = uploadKeystoreFile();
    // select keystore aliases that should be stored in the application keystore
    // send select alias request
    final String alias = "goldfish";
    // send select-alias request
    HttpResponse<JsonNode> response = Unirest.post(getApplicationUrl("/keystore/select-alias"))
                                             .field("stateId", stateId)
                                             .field("aliases", Collections.singletonList(alias))
                                             .asJson();
    log.warn(response.getBody().toPrettyString());
    Assertions.assertEquals(HttpStatus.CREATED, response.getStatus());
  }

  @Test
  public void testNoPasswordSendAndPasswordDoesNotMatchKeystorePassword()
  {
    final String stateId = uploadKeystoreFile();
    // select keystore aliases that should be stored in the application keystore
    // send select alias request
    final String alias = "unit-test";
    // send select-alias request
    HttpResponse<JsonNode> response = Unirest.post(getApplicationUrl("/keystore/select-alias"))
                                             .field("stateId", stateId)
                                             .field("aliases", Collections.singletonList(alias))
                                             .asJson();
    log.warn(response.getBody().toPrettyString());
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
    JSONObject jsonObject = response.getBody().getObject();
    Assertions.assertThrows(JSONException.class, () -> jsonObject.getJSONArray("errorMessages"));
    JSONObject errors = jsonObject.getJSONObject("inputFieldErrors");
    Assertions.assertEquals(2, errors.length());
    {
      List<String> aliases = jsonArrayToList(errors.getJSONArray("aliases"));
      Assertions.assertEquals(1, aliases.size());

      String errorMessage = "Cannot recover key";
      MatcherAssert.assertThat(aliases, Matchers.containsInAnyOrder(errorMessage));
    }
    {
      List<String> aliases = jsonArrayToList(errors.getJSONArray("privateKeyPassword"));
      Assertions.assertEquals(2, aliases.size());

      String errorMessage1 = "Could not access private key of alias '" + alias + "'";
      String errorMessage2 = "Cannot recover key";
      MatcherAssert.assertThat(aliases, Matchers.containsInAnyOrder(errorMessage1, errorMessage2));
    }
  }

  @Test
  public void testAddEntryWithAlreadyTakenAliasAndUseAliasOverride()
  {
    {
      final String stateId = uploadKeystoreFile(UNIT_TEST_KEYSTORE_JKS);
      // select keystore aliases that should be stored in the application keystore
      // send select alias request
      final String alias = "goldfish";
      // send select-alias request
      HttpResponse<JsonNode> response = Unirest.post(getApplicationUrl("/keystore/select-alias"))
                                               .field("stateId", stateId)
                                               .field("aliases", Collections.singletonList(alias))
                                               .asJson();
      Assertions.assertEquals(HttpStatus.CREATED, response.getStatus());
    }

    {
      final String stateId = uploadKeystoreFile(UNIT_TEST_KEYSTORE_PKCS12);
      // select keystore aliases that should be stored in the application keystore
      // send select alias request
      final String alias = "goldfish";
      // send duplicate alias entry
      {
        // send select-alias request
        HttpResponse<JsonNode> response = Unirest.post(getApplicationUrl("/keystore/select-alias"))
                                                 .field("stateId", stateId)
                                                 .field("aliases", Collections.singletonList(alias))
                                                 .asJson();
        log.warn(response.getBody().toPrettyString());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        JSONObject jsonObject = response.getBody().getObject();
        Assertions.assertThrows(JSONException.class, () -> jsonObject.getJSONArray("errorMessages"));
        JSONObject errors = jsonObject.getJSONObject("inputFieldErrors");
        Assertions.assertEquals(1, errors.length());
        {
          List<String> aliases = jsonArrayToList(errors.getJSONArray("aliases"));
          Assertions.assertEquals(1, aliases.size());

          String errorMessage = "Alias '" + alias + "' is already used. Please override this alias with another name";
          MatcherAssert.assertThat(aliases, Matchers.containsInAnyOrder(errorMessage));
        }
      }

      // use aliasOverride attribute
      {
        final String aliasOverride = "super-mario";
        // send select-alias request
        HttpResponse<JsonNode> response = Unirest.post(getApplicationUrl("/keystore/select-alias"))
                                                 .field("stateId", stateId)
                                                 .field("aliases", Collections.singletonList(alias))
                                                 .field("aliasOverride", aliasOverride)
                                                 .asJson();
        log.warn(response.getBody().toPrettyString());
        Assertions.assertEquals(HttpStatus.CREATED, response.getStatus());
        Keystore applicationKeystore = keystoreDao.getKeystore();
        MatcherAssert.assertThat(applicationKeystore.getKeyStoreAliases(), Matchers.hasItem(aliasOverride));
      }
    }
  }

  @Test
  public void testAliasOverrideIsAlreadyTaken()
  {
    {
      final String stateId = uploadKeystoreFile(UNIT_TEST_KEYSTORE_JKS);
      // select keystore aliases that should be stored in the application keystore
      // send select alias request
      final String alias = "goldfish";
      // send select-alias request
      HttpResponse<JsonNode> response = Unirest.post(getApplicationUrl("/keystore/select-alias"))
                                               .field("stateId", stateId)
                                               .field("aliases", Collections.singletonList(alias))
                                               .asJson();
      Assertions.assertEquals(HttpStatus.CREATED, response.getStatus());
    }

    {
      final String stateId = uploadKeystoreFile(UNIT_TEST_KEYSTORE_PKCS12);
      // select keystore aliases that should be stored in the application keystore
      // send select alias request
      final String alias = "localhost";
      // send duplicate alias entry
      {
        // send select-alias request
        HttpResponse<JsonNode> response = Unirest.post(getApplicationUrl("/keystore/select-alias"))
                                                 .field("stateId", stateId)
                                                 .field("aliases", Collections.singletonList(alias))
                                                 .field("aliasOverride", "goldfish")
                                                 .asJson();
        log.warn(response.getBody().toPrettyString());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        JSONObject jsonObject = response.getBody().getObject();
        Assertions.assertThrows(JSONException.class, () -> jsonObject.getJSONArray("errorMessages"));
        JSONObject errors = jsonObject.getJSONObject("inputFieldErrors");
        Assertions.assertEquals(1, errors.length());
        {
          List<String> aliases = jsonArrayToList(errors.getJSONArray("aliasOverride"));
          Assertions.assertEquals(1, aliases.size());

          String errorMessage = "Alias 'goldfish' is already used. Please override this alias with another name";
          MatcherAssert.assertThat(aliases, Matchers.containsInAnyOrder(errorMessage));
        }
      }
    }
  }

  @Test
  public void testSendKeyTwice()
  {
    final String stateId = uploadKeystoreFile();
    final String alias = "goldfish";
    {
      HttpResponse<JsonNode> response = Unirest.post(getApplicationUrl("/keystore/select-alias"))
                                               .field("stateId", stateId)
                                               .field("aliases", Collections.singletonList(alias))
                                               .asJson();
      log.warn(response.getBody().toPrettyString());
      Assertions.assertEquals(HttpStatus.CREATED, response.getStatus());

    }

    final String aliasOverride = "super-mario";
    HttpResponse<JsonNode> response = Unirest.post(getApplicationUrl("/keystore/select-alias"))
                                             .field("stateId", stateId)
                                             .field("aliases", Collections.singletonList(alias))
                                             .field("aliasOverride", aliasOverride)
                                             .asJson();
    log.warn(response.getBody().toPrettyString());
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
    JSONObject jsonObject = response.getBody().getObject();
    Assertions.assertThrows(JSONException.class, () -> jsonObject.getJSONArray("errorMessages"));
    JSONObject errors = jsonObject.getJSONObject("inputFieldErrors");
    Assertions.assertEquals(1, errors.length());
    {
      List<String> aliases = jsonArrayToList(errors.getJSONArray("aliases"));
      Assertions.assertEquals(1, aliases.size());

      String errorMessage = "The selected Key is already present under the alias '" + alias + "'";
      MatcherAssert.assertThat(aliases, Matchers.containsInAnyOrder(errorMessage));
    }
  }

  @Test
  public void testDownloadExistingKeystoreInfos()
  {
    // preparation
    {
      final String stateId = uploadKeystoreFile(UNIT_TEST_KEYSTORE_JKS);
      for ( KeystoreEntry keystoreEntry : getUnitTestKeystoreEntryAccess() )
      {
        HttpResponse<JsonNode> response = Unirest.post(getApplicationUrl("/keystore/select-alias"))
                                                 .field("stateId", stateId)
                                                 .field("aliases", Collections.singletonList(keystoreEntry.getAlias()))
                                                 .field("privateKeyPassword", keystoreEntry.getPrivateKeyPassword())
                                                 .asJson();
        Assertions.assertEquals(HttpStatus.CREATED, response.getStatus());
      }
    }

    HttpResponse<JsonNode> response = Unirest.get(getApplicationUrl("/keystore/infos")).asJson();
    Assertions.assertEquals(HttpStatus.OK, response.getStatus());
    KeystoreInfoForm keystoreInfoForm = getForm(response.getBody().toString(), KeystoreInfoForm.class);
    Assertions.assertEquals(3, keystoreInfoForm.getNumberOfEntries());
    Assertions.assertEquals(3, keystoreInfoForm.getCertificateAliases().size());
    for ( KeystoreEntry keystoreEntry : getUnitTestKeystoreEntryAccess() )
    {
      Assertions.assertTrue(keystoreInfoForm.getCertificateAliases()
                                            .stream()
                                            .anyMatch(alias -> alias.equals(keystoreEntry.getAlias())));
    }
  }

  @Test
  public void testDeleteKeystoreEntries()
  {
    // preparation
    {
      final String stateId = uploadKeystoreFile(UNIT_TEST_KEYSTORE_JKS);
      for ( KeystoreEntry keystoreEntry : getUnitTestKeystoreEntryAccess() )
      {
        HttpResponse<JsonNode> response = Unirest.post(getApplicationUrl("/keystore/select-alias"))
                                                 .field("stateId", stateId)
                                                 .field("aliases", Collections.singletonList(keystoreEntry.getAlias()))
                                                 .field("privateKeyPassword", keystoreEntry.getPrivateKeyPassword())
                                                 .asJson();
        Assertions.assertEquals(HttpStatus.CREATED, response.getStatus());
      }
    }

    int expectedNumberOfEntriesInKeystore = getUnitTestKeystoreEntryAccess().size();
    for ( KeystoreEntry keystoreEntry : getUnitTestKeystoreEntryAccess() )
    {
      // check application keystore
      {
        Keystore keystore = keystoreDao.getKeystore();
        Assertions.assertEquals(expectedNumberOfEntriesInKeystore, keystore.getKeyStoreAliases().size());
        Assertions.assertEquals(expectedNumberOfEntriesInKeystore, keystore.getKeystoreEntries().size());
      }

      HttpResponse<String> response = Unirest.delete(getApplicationUrl("/keystore/delete-alias"))
                                             .field("alias", keystoreEntry.getAlias())
                                             .asString();

      Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getStatus());
      MatcherAssert.assertThat(response.getBody(), Matchers.emptyOrNullString());

      // check application keystore
      {
        expectedNumberOfEntriesInKeystore--;
        Keystore keystore = keystoreDao.getKeystore();
        Assertions.assertEquals(expectedNumberOfEntriesInKeystore, keystore.getKeyStoreAliases().size());
        Assertions.assertEquals(expectedNumberOfEntriesInKeystore, keystore.getKeystoreEntries().size());
      }
    }
  }

  @Test
  public void testDeleteKeystoreEntryWithUnknownAlias()
  {
    // preparation
    {
      final String stateId = uploadKeystoreFile(UNIT_TEST_KEYSTORE_JKS);
      HttpResponse<JsonNode> response = Unirest.post(getApplicationUrl("/keystore/select-alias"))
                                               .field("stateId", stateId)
                                               .field("aliases", Collections.singletonList("goldfish"))
                                               .asJson();
      Assertions.assertEquals(HttpStatus.CREATED, response.getStatus());
    }

    HttpResponse<JsonNode> response = Unirest.delete(getApplicationUrl("/keystore/delete-alias"))
                                             .field("alias", "unknown-alias")
                                             .asJson();
    log.warn(response.getBody().toPrettyString());
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
    log.warn(response.getBody().toPrettyString());
    JSONObject jsonObject = response.getBody().getObject();
    Assertions.assertThrows(JSONException.class, () -> jsonObject.getJSONArray("errorMessages"));
    JSONObject errors = jsonObject.getJSONObject("inputFieldErrors");
    Assertions.assertEquals(1, errors.length());
    List<String> aliasErrors = jsonArrayToList(errors.getJSONArray("alias"));
    Assertions.assertEquals(1, aliasErrors.size());

    String errorMessage = "Unknown alias 'unknown-alias'";
    MatcherAssert.assertThat(aliasErrors, Matchers.containsInAnyOrder(errorMessage));
  }

  @Test
  public void testDeleteKeystoreEntryWithMissingAlias()
  {
    // preparation
    {
      final String stateId = uploadKeystoreFile(UNIT_TEST_KEYSTORE_JKS);
      HttpResponse<JsonNode> response = Unirest.post(getApplicationUrl("/keystore/select-alias"))
                                               .field("stateId", stateId)
                                               .field("aliases", Collections.singletonList("goldfish"))
                                               .asJson();
      Assertions.assertEquals(HttpStatus.CREATED, response.getStatus());
    }

    HttpResponse<JsonNode> response = Unirest.delete(getApplicationUrl("/keystore/delete-alias")).asJson();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
    log.warn(response.getBody().toPrettyString());
    JSONObject jsonObject = response.getBody().getObject();
    Assertions.assertThrows(JSONException.class, () -> jsonObject.getJSONArray("errorMessages"));
    JSONObject errors = jsonObject.getJSONObject("inputFieldErrors");
    Assertions.assertEquals(1, errors.length());
    List<String> aliasErrors = jsonArrayToList(errors.getJSONArray("alias"));
    Assertions.assertEquals(1, aliasErrors.size());

    String errorMessage = "Required parameter 'alias' is missing in request";
    MatcherAssert.assertThat(aliasErrors, Matchers.containsInAnyOrder(errorMessage));
  }

  @SneakyThrows
  @Test
  public void testDownloadKeystore()
  {
    // preparation
    {
      final String stateId = uploadKeystoreFile(UNIT_TEST_KEYSTORE_JKS);
      for ( KeystoreEntry keystoreEntry : getUnitTestKeystoreEntryAccess() )
      {
        HttpResponse<JsonNode> response = Unirest.post(getApplicationUrl("/keystore/select-alias"))
                                                 .field("stateId", stateId)
                                                 .field("aliases", Collections.singletonList(keystoreEntry.getAlias()))
                                                 .field("privateKeyPassword", keystoreEntry.getPrivateKeyPassword())
                                                 .asJson();
        Assertions.assertEquals(HttpStatus.CREATED, response.getStatus());
      }
    }

    HttpResponse<byte[]> response = Unirest.get(getApplicationUrl("/keystore/download")).asBytes();
    Assertions.assertEquals(HttpStatus.OK, response.getStatus());
    KeyStore javaKeystore = KeyStoreSupporter.readKeyStore(response.getBody(),
                                                           KeyStoreSupporter.KeyStoreType.JKS,
                                                           "123456");
    Assertions.assertEquals(3, javaKeystore.size());
    for ( KeystoreEntry keystoreEntry : getUnitTestKeystoreEntryAccess() )
    {
      Assertions.assertTrue(javaKeystore.containsAlias(keystoreEntry.getAlias()));
    }
  }

  @Test
  public void testLoadAliasInfos()
  {
    // preparation
    {
      final String stateId = uploadKeystoreFile(UNIT_TEST_KEYSTORE_JKS);
      for ( KeystoreEntry keystoreEntry : getUnitTestKeystoreEntryAccess() )
      {
        HttpResponse<JsonNode> response = Unirest.post(getApplicationUrl("/keystore/select-alias"))
                                                 .field("stateId", stateId)
                                                 .field("aliases", Collections.singletonList(keystoreEntry.getAlias()))
                                                 .field("privateKeyPassword", keystoreEntry.getPrivateKeyPassword())
                                                 .asJson();
        Assertions.assertEquals(HttpStatus.CREATED, response.getStatus());
      }
    }

    for ( KeystoreEntry keystoreEntry : getUnitTestKeystoreEntryAccess() )
    {
      HttpResponse<JsonNode> response = Unirest.get(getApplicationUrl("/keystore/load-alias"))
                                               .queryString("alias", keystoreEntry.getAlias())
                                               .asJson();
      Assertions.assertEquals(HttpStatus.OK, response.getStatus());
      CertificateInfo certificateInfo = getForm(response.getBody().toString(), CertificateInfo.class);
      Assertions.assertEquals("CN=" + keystoreEntry.getAlias(), certificateInfo.getIssuerDn());
      Assertions.assertEquals("CN=" + keystoreEntry.getAlias(), certificateInfo.getSubjectDn());
      MatcherAssert.assertThat(certificateInfo.getSha256fingerprint(), Matchers.not(Matchers.emptyOrNullString()));
      MatcherAssert.assertThat(certificateInfo.getValidFrom(), Matchers.not(Matchers.nullValue()));
      MatcherAssert.assertThat(certificateInfo.getValidUntil(), Matchers.not(Matchers.nullValue()));
    }
  }


  private String uploadKeystoreFile()
  {
    return uploadKeystoreFile(UNIT_TEST_KEYSTORE_JKS);
  }

  /**
   * can be used to upload a keystore file and return the stateId
   */
  @SneakyThrows
  private String uploadKeystoreFile(String keystorePath)
  {
    // the stateId the is returned in the upload response and must be send in the select alias request
    String stateId;

    // upload keystore file
    {
      HttpResponse<JsonNode> response;
      // send upload post request
      try (InputStream inputStream = readAsInputStream(keystorePath))
      {
        MultipartBody multipartBody = Unirest.post(getApplicationUrl("/keystore/upload"))
                                             // for some reason SUN crypto provider is able to resolve PKCS12 under JKS
                                             // type
                                             .field("keystoreFile", inputStream, "unit-test.jks")
                                             .field("keystorePassword", UNIT_TEST_KEYSTORE_PASSWORD);
        response = multipartBody.asJson();
        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        JSONObject jsonObject = response.getBody().getObject();
        stateId = jsonObject.getString("stateId");
      }
    }
    return stateId;
  }
}
