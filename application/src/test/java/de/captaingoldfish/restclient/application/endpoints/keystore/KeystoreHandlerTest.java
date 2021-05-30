package de.captaingoldfish.restclient.application.endpoints.keystore;

import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import de.captaingoldfish.restclient.application.setup.AbstractScimClientConfig;
import de.captaingoldfish.restclient.application.setup.OAuthRestClientTest;
import de.captaingoldfish.restclient.database.entities.Keystore;
import de.captaingoldfish.restclient.database.entities.KeystoreEntry;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;
import de.captaingoldfish.restclient.database.entities.OpenIdProvider;
import de.captaingoldfish.restclient.scim.resources.CertificateInfo;
import de.captaingoldfish.restclient.scim.resources.ScimKeystore;
import de.captaingoldfish.restclient.scim.resources.ScimKeystore.AliasSelection;
import de.captaingoldfish.restclient.scim.resources.ScimKeystore.FileUpload;
import de.captaingoldfish.scim.sdk.client.response.ServerResponse;
import de.captaingoldfish.scim.sdk.common.constants.HttpStatus;
import de.captaingoldfish.scim.sdk.common.response.ErrorResponse;
import de.captaingoldfish.scim.sdk.common.response.ListResponse;
import de.captaingoldfish.scim.sdk.server.schemas.ResourceType;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


/**
 * @author Pascal Knueppel
 * @since 14.05.2021
 */
@Slf4j
@OAuthRestClientTest
public class KeystoreHandlerTest extends AbstractScimClientConfig
{

  /**
   * the scim endpoint for the application keystore
   */
  private static final String KEYSTORE_ENDPOINT = "/Keystore";

  /**
   * used to check and access the uploaded keystore
   */
  @Autowired
  private KeystoreFileCache keystoreFileCache;

  @Qualifier("keystoreResourceType")
  @Autowired
  private ResourceType keystoreResourceType;

  /**
   * simply checks that the application keystore is empty to the begin of the test
   */
  @SneakyThrows
  @BeforeEach
  public void checkApplicationKeystore()
  {
    Keystore keystore = keystoreDao.getKeystore();
    Assertions.assertEquals(0, keystore.getKeystoreEntries().size());
    Assertions.assertFalse(keystore.getKeyStore().aliases().hasMoreElements());
  }

  /**
   * this will check that the aliases of the keystore can be selected even if the keystore was just uploaded
   * once
   */
  @SneakyThrows
  @TestFactory
  public List<DynamicTest> testUploadKeystoreOnceForAliases()
  {
    List<DynamicTest> dynamicTestList = new ArrayList<>();
    final AtomicReference<String> currentStateId = new AtomicReference<>();
    Executable uploadKeystoreExecutor = getUploadKeystoreExecutableTest(currentStateId);
    dynamicTestList.add(DynamicTest.dynamicTest("upload keystore file", uploadKeystoreExecutor));
    for ( KeystoreEntry unitTestKeystoreEntryAccess : getUnitTestKeystoreEntryAccess() )
    {
      addSelectAliasEntryTest(dynamicTestList, currentStateId, unitTestKeystoreEntryAccess);
    }
    return dynamicTestList;
  }

  /**
   * this test will check that it is also possible to select the single aliases if the keystore is being
   * uploaded each time
   */
  @SneakyThrows
  @TestFactory
  public List<DynamicTest> testUploadKeystoreForEachDifferentAlias()
  {
    List<DynamicTest> dynamicTestList = new ArrayList<>();
    final AtomicReference<String> currentStateId = new AtomicReference<>();
    Executable uploadKeystoreExecutor = getUploadKeystoreExecutableTest(currentStateId);
    for ( KeystoreEntry unitTestKeystoreEntryAccess : getUnitTestKeystoreEntryAccess() )
    {
      dynamicTestList.add(DynamicTest.dynamicTest("upload keystore file", uploadKeystoreExecutor));
      addSelectAliasEntryTest(dynamicTestList, currentStateId, unitTestKeystoreEntryAccess);
    }
    return dynamicTestList;
  }

  /**
   * creates an executable that will test uploading a keystore
   * 
   * @param currentStateId an atomic reference to the current stateId that is returned on a keystore upload. So
   *          the response of the upload will be added into this reference
   */
  private Executable getUploadKeystoreExecutableTest(AtomicReference<String> currentStateId)
  {
    return () -> {
      ServerResponse<ScimKeystore> response = uploadKeystore(UNIT_TEST_KEYSTORE_JKS, "123456");
      Assertions.assertEquals(HttpStatus.CREATED, response.getHttpStatus());
      ScimKeystore uploadKeystoreResponse = response.getResource();

      // check returned resource
      {
        Assertions.assertNotNull(uploadKeystoreResponse);
        Assertions.assertNotNull(uploadKeystoreResponse.getAliasSelection());
        Assertions.assertNotNull(response.getResource().getAliasSelection().getStateId());
        Assertions.assertNotNull(response.getResource().getAliasSelection().getAliases());
        MatcherAssert.assertThat(response.getResource().getAliasSelection().getAliases(),
                                 Matchers.containsInAnyOrder("goldfish", "unit-test", "localhost"));
        Assertions.assertNull(response.getResource().getAliasSelection().getAliasOverride());
        Assertions.assertNull(response.getResource().getAliasSelection().getPrivateKeyPassword());
        MatcherAssert.assertThat(response.getResource().getAliases(), Matchers.emptyIterable());
        Assertions.assertNull(response.getResource().getFileUpload());
        Assertions.assertNull(response.getResource().getCertificateInfo());
      }

      // check cache entry
      {
        final String stateId = response.getResource().getAliasSelection().getStateId();
        currentStateId.set(stateId);
        Assertions.assertNotNull(stateId);
        Keystore cachedKeystore = keystoreFileCache.getKeystoreFile(stateId);
        Assertions.assertNotNull(cachedKeystore);
        MatcherAssert.assertThat(cachedKeystore.getKeyStoreAliases(),
                                 Matchers.containsInAnyOrder("goldfish", "unit-test", "localhost"));
      }
    };
  }

  /**
   * adds a test to the list that will select one of the returned aliases from the upload
   * 
   * @param dynamicTestList the list of dynamic tests
   * @param currentStateId the stateId from the keystore upload
   * @param unitTestKeystoreEntryAccess the entry that should be selected from the keystore
   */
  private void addSelectAliasEntryTest(List<DynamicTest> dynamicTestList,
                                       AtomicReference<String> currentStateId,
                                       KeystoreEntry unitTestKeystoreEntryAccess)
  {
    String selectEntryTestName = String.format("Select entry '%s'", unitTestKeystoreEntryAccess.getAlias());
    dynamicTestList.add(DynamicTest.dynamicTest(selectEntryTestName, () -> {
      final String privateKeyPassword = unitTestKeystoreEntryAccess.getPrivateKeyPassword();
      final List<String> aliasToSelect = Collections.singletonList(unitTestKeystoreEntryAccess.getAlias());
      AliasSelection aliasSelection = AliasSelection.builder()
                                                    .stateId(currentStateId.get())
                                                    .privateKeyPassword(privateKeyPassword)
                                                    .aliases(aliasToSelect)
                                                    .build();
      ScimKeystore scimKeystore = ScimKeystore.builder().aliasSelection(aliasSelection).build();
      ServerResponse<ScimKeystore> response = scimRequestBuilder.create(ScimKeystore.class, KEYSTORE_ENDPOINT)
                                                                .setResource(scimKeystore)
                                                                .sendRequest();
      Assertions.assertEquals(HttpStatus.CREATED, response.getHttpStatus());
      ScimKeystore returnedKeystore = response.getResource();
      MatcherAssert.assertThat(response.getResource().getAliases(), Matchers.emptyIterable());
      Assertions.assertNull(returnedKeystore.getFileUpload());
      Assertions.assertNull(returnedKeystore.getAliasSelection());
      Assertions.assertNotNull(returnedKeystore.getCertificateInfo());

      X509Certificate storedCertificate = keystoreDao.getKeystore().getCertificate(unitTestKeystoreEntryAccess);
      CertificateInfo expectedInfo = new CertificateInfo(unitTestKeystoreEntryAccess.getAlias(), storedCertificate);
      Assertions.assertEquals(expectedInfo, returnedKeystore.getCertificateInfo());
      Assertions.assertEquals("CN=" + aliasToSelect.get(0), expectedInfo.getInfo().getSubjectDn());
      Assertions.assertEquals("CN=" + aliasToSelect.get(0), expectedInfo.getInfo().getIssuerDn());
    }));
  }

  /**
   * tries to load the existing aliases from the current application keystore from the server
   */
  @Test
  public void testListResources()
  {
    addDefaultEntriesToApplicationKeystore();
    Keystore keystore = keystoreDao.getKeystore();
    Assertions.assertEquals(3, keystore.getKeystoreEntries().size());

    ServerResponse<ListResponse<ScimKeystore>> response = scimRequestBuilder.list(ScimKeystore.class, KEYSTORE_ENDPOINT)
                                                                            .get()
                                                                            .sendRequest();
    ListResponse<ScimKeystore> listResponse = response.getResource();
    Assertions.assertEquals(1, listResponse.getTotalResults());
    Assertions.assertEquals(1, listResponse.getItemsPerPage());
    Assertions.assertEquals(1, listResponse.getStartIndex());
    List<ScimKeystore> returnedResources = listResponse.getListedResources();
    Assertions.assertEquals(1, returnedResources.size());
    ScimKeystore appKeystore = returnedResources.get(0);
    Assertions.assertNull(appKeystore.getFileUpload());
    Assertions.assertNull(appKeystore.getAliasSelection());
    Assertions.assertNull(appKeystore.getCertificateInfo());
    Assertions.assertNotNull(appKeystore.getAliases());
    Assertions.assertEquals(3, appKeystore.getAliases().size());
    MatcherAssert.assertThat(appKeystore.getAliases(),
                             Matchers.containsInAnyOrder("goldfish", "unit-test", "localhost"));
  }

  /**
   * tries to load a single certificate entry from the application keystore
   */
  @Test
  public void testGetSingleResource()
  {
    addDefaultEntriesToApplicationKeystore();
    Keystore keystore = keystoreDao.getKeystore();
    Assertions.assertEquals(3, keystore.getKeystoreEntries().size());

    for ( KeystoreEntry unitTestKeystoreEntryAccess : getUnitTestKeystoreEntryAccess() )
    {
      ServerResponse<ScimKeystore> response = scimRequestBuilder.get(ScimKeystore.class,
                                                                     KEYSTORE_ENDPOINT,
                                                                     unitTestKeystoreEntryAccess.getAlias())
                                                                .sendRequest();
      Assertions.assertEquals(HttpStatus.OK, response.getHttpStatus());
      ScimKeystore scimKeystore = response.getResource();
      Assertions.assertNull(scimKeystore.getFileUpload());
      Assertions.assertNull(scimKeystore.getAliasSelection());
      MatcherAssert.assertThat(scimKeystore.getAliases(), Matchers.emptyIterable());

      Assertions.assertNotNull(scimKeystore.getCertificateInfo());
      X509Certificate storedCertificate = keystoreDao.getKeystore().getCertificate(unitTestKeystoreEntryAccess);
      CertificateInfo expectedInfo = new CertificateInfo(unitTestKeystoreEntryAccess.getAlias(), storedCertificate);
      Assertions.assertEquals(expectedInfo, scimKeystore.getCertificateInfo());
    }
  }

  /**
   * checks that entries are successfully removed from the application keystore
   */
  @Test
  public void testDeleteEntriesFromKeystore()
  {
    addDefaultEntriesToApplicationKeystore();
    Keystore keystore = keystoreDao.getKeystore();
    Assertions.assertEquals(3, keystore.getKeystoreEntries().size());

    List<KeystoreEntry> testKeystoreEntryAccess = getUnitTestKeystoreEntryAccess();
    for ( int i = 0 ; i < testKeystoreEntryAccess.size() ; i++ )
    {
      KeystoreEntry unitTestKeystoreEntryAccess = testKeystoreEntryAccess.get(i);
      ServerResponse<ScimKeystore> response = scimRequestBuilder.delete(ScimKeystore.class,
                                                                        KEYSTORE_ENDPOINT,
                                                                        unitTestKeystoreEntryAccess.getAlias())
                                                                .sendRequest();
      Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getHttpStatus());
      keystore = keystoreDao.getKeystore();
      Assertions.assertEquals(3 - (i + 1), keystore.getKeystoreEntries().size());
    }
  }

  /**
   * adds all entries from the unit-keystore of type jks into the application keystore
   */
  @SneakyThrows
  private void addDefaultEntriesToApplicationKeystore()
  {
    KeystoreHandler keystoreHandler = (KeystoreHandler)keystoreResourceType.getResourceHandlerImpl();

    String b64Keystore = Base64.getEncoder().encodeToString(readAsBytes(UNIT_TEST_KEYSTORE_JKS));
    FileUpload fileUpload = FileUpload.builder()
                                      .keystoreFile(b64Keystore)
                                      .keystoreFileName("test.jks")
                                      .keystorePassword(UNIT_TEST_KEYSTORE_PASSWORD)
                                      .build();
    ScimKeystore uploadResponse = keystoreHandler.handleKeystoreUpload(fileUpload);

    for ( KeystoreEntry unitTestKeystoreEntryAccess : getUnitTestKeystoreEntryAccess() )
    {
      AliasSelection aliasSelection = AliasSelection.builder()
                                                    .stateId(uploadResponse.getAliasSelection().getStateId())
                                                    .aliases(Collections.singletonList(unitTestKeystoreEntryAccess.getAlias()))
                                                    .privateKeyPassword(unitTestKeystoreEntryAccess.getPrivateKeyPassword())
                                                    .build();
      keystoreHandler.handleAliasSelection(aliasSelection);
    }
  }

  @Test
  public void testUploadKeystoreWithIllegalPassword()
  {
    final String password = "illegal-password";
    ServerResponse<ScimKeystore> response = uploadKeystore(UNIT_TEST_KEYSTORE_PKCS12, password);

    // validate response
    {
      Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
      ErrorResponse errorResponse = response.getErrorResponse();
      Assertions.assertTrue(errorResponse.getErrorMessages().isEmpty());

      Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
      final String fieldName = String.format("%s.%s",
                                             ScimKeystore.FieldNames.FILE_UPLOAD,
                                             ScimKeystore.FieldNames.KEYSTORE_FILE);
      Assertions.assertEquals(1, fieldErrors.size());
      Assertions.assertEquals(1, fieldErrors.get(fieldName).size());

      final String errorMessage = "PKCS12 key store mac invalid - wrong password or corrupted file.";
      MatcherAssert.assertThat(fieldErrors.get(fieldName), Matchers.containsInAnyOrder(errorMessage));
    }
  }

  @Test
  public void testUploadKeystorePKCS12WithDifferentPasswords()
  {
    final String password = UNIT_TEST_KEYSTORE_PASSWORD;
    ServerResponse<ScimKeystore> response = uploadKeystore(UNIT_TEST_KEYSTORE_PKCS12, password);

    // validate response
    {
      Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
      ErrorResponse errorResponse = response.getErrorResponse();
      Assertions.assertTrue(errorResponse.getErrorMessages().isEmpty());

      Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
      final String fieldName = String.format("%s.%s",
                                             ScimKeystore.FieldNames.FILE_UPLOAD,
                                             ScimKeystore.FieldNames.KEYSTORE_FILE);
      Assertions.assertEquals(1, fieldErrors.size());
      Assertions.assertEquals(2, fieldErrors.get(fieldName).size());

      String errorMessage1 = "exception unwrapping private key - java.security.InvalidKeyException: pad block corrupted";
      String errorMessage2 = "key-passwords must match keystore-password. This is a restriction of the BouncyCastle "
                             + "PKCS12 implementation. As a workaround rename the file extension to '.jks' this may "
                             + "work based on the used JDK";
      MatcherAssert.assertThat(fieldErrors.get(fieldName), Matchers.containsInAnyOrder(errorMessage1, errorMessage2));
    }
  }

  @Test
  public void testUploadEmptyFile()
  {
    final String password = UNIT_TEST_KEYSTORE_PASSWORD;
    FileUpload fileUpload = FileUpload.builder()
                                      .keystoreFile("")
                                      .keystoreFileName("")
                                      .keystorePassword(password)
                                      .build();
    ScimKeystore scimKeystore = ScimKeystore.builder().fileUpload(fileUpload).build();
    ServerResponse<ScimKeystore> response = scimRequestBuilder.create(ScimKeystore.class, KEYSTORE_ENDPOINT)
                                                              .setResource(scimKeystore)
                                                              .sendRequest();

    // validate response
    {
      Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
      ErrorResponse errorResponse = response.getErrorResponse();
      Assertions.assertTrue(errorResponse.getErrorMessages().isEmpty());

      Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
      final String fieldName = String.format("%s.%s",
                                             ScimKeystore.FieldNames.FILE_UPLOAD,
                                             ScimKeystore.FieldNames.KEYSTORE_FILE);
      Assertions.assertEquals(1, fieldErrors.size());
      Assertions.assertEquals(1, fieldErrors.get(fieldName).size());

      String errorMessage = "Keystore file must not be empty";
      MatcherAssert.assertThat(fieldErrors.get(fieldName), Matchers.containsInAnyOrder(errorMessage));
    }
  }

  @Test
  public void testIllegalKeystoreFile()
  {
    final String password = UNIT_TEST_KEYSTORE_PASSWORD;
    FileUpload fileUpload = FileUpload.builder()
                                      .keystoreFile("YQ==")
                                      .keystoreFileName("")
                                      .keystorePassword(password)
                                      .build();
    ScimKeystore scimKeystore = ScimKeystore.builder().fileUpload(fileUpload).build();
    ServerResponse<ScimKeystore> response = scimRequestBuilder.create(ScimKeystore.class, KEYSTORE_ENDPOINT)
                                                              .setResource(scimKeystore)
                                                              .sendRequest();

    // validate response
    {
      Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
      ErrorResponse errorResponse = response.getErrorResponse();
      Assertions.assertTrue(errorResponse.getErrorMessages().isEmpty());

      Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
      final String fieldName = String.format("%s.%s",
                                             ScimKeystore.FieldNames.FILE_UPLOAD,
                                             ScimKeystore.FieldNames.KEYSTORE_FILE);
      Assertions.assertEquals(1, fieldErrors.size());
      Assertions.assertEquals(1, fieldErrors.get(fieldName).size());

      String errorMessage = "File is not a valid keystore file";
      MatcherAssert.assertThat(fieldErrors.get(fieldName), Matchers.containsInAnyOrder(errorMessage));
    }
  }

  @Test
  public void testIllegalBase64KeystoreFile()
  {
    final String password = UNIT_TEST_KEYSTORE_PASSWORD;
    FileUpload fileUpload = FileUpload.builder()
                                      .keystoreFile("$?")
                                      .keystoreFileName("")
                                      .keystorePassword(password)
                                      .build();
    ScimKeystore scimKeystore = ScimKeystore.builder().fileUpload(fileUpload).build();
    ServerResponse<ScimKeystore> response = scimRequestBuilder.create(ScimKeystore.class, KEYSTORE_ENDPOINT)
                                                              .setResource(scimKeystore)
                                                              .sendRequest();

    // validate response
    {
      Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
      ErrorResponse errorResponse = response.getErrorResponse();
      List<String> errorMessageList = errorResponse.getErrorMessages();
      Assertions.assertEquals(0, errorMessageList.size());

      Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
      final String fieldName = String.format("%s.%s",
                                             ScimKeystore.FieldNames.FILE_UPLOAD,
                                             ScimKeystore.FieldNames.KEYSTORE_FILE);
      Assertions.assertEquals(1, fieldErrors.size());
      Assertions.assertEquals(1, fieldErrors.get(fieldName).size());

      String errorMessage = "Keystore file is not Base64 encoded: Illegal base64 character 24";
      MatcherAssert.assertThat(fieldErrors.get(fieldName), Matchers.containsInAnyOrder(errorMessage));
    }
  }

  @Test
  public void testUploadWithNullPassword()
  {
    ServerResponse<ScimKeystore> response = uploadKeystore(UNIT_TEST_KEYSTORE_PKCS12, null);

    // validate response
    {
      Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
      ErrorResponse errorResponse = response.getErrorResponse();
      Assertions.assertTrue(errorResponse.getErrorMessages().isEmpty());

      Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
      final String fieldName = String.format("%s.%s",
                                             ScimKeystore.FieldNames.FILE_UPLOAD,
                                             ScimKeystore.FieldNames.KEYSTORE_PASSWORD);
      Assertions.assertEquals(1, fieldErrors.size());
      Assertions.assertEquals(1, fieldErrors.get(fieldName).size());

      String errorMessage = "Required 'WRITE_ONLY' attribute "
                            + "'urn:ietf:params:scim:schemas:captaingoldfish:2.0:Keystore:fileUpload.keystorePassword' "
                            + "is missing";
      MatcherAssert.assertThat(fieldErrors.get(fieldName), Matchers.containsInAnyOrder(errorMessage));
    }
  }

  @Test
  public void testUploadWithEmptyPassword()
  {
    ServerResponse<ScimKeystore> response = uploadKeystore(UNIT_TEST_KEYSTORE_PKCS12, "");

    // validate response
    {
      Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
      ErrorResponse errorResponse = response.getErrorResponse();
      Assertions.assertTrue(errorResponse.getErrorMessages().isEmpty());

      Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
      final String fieldName = String.format("%s.%s",
                                             ScimKeystore.FieldNames.FILE_UPLOAD,
                                             ScimKeystore.FieldNames.KEYSTORE_PASSWORD);
      Assertions.assertEquals(1, fieldErrors.size());
      Assertions.assertEquals(1, fieldErrors.get(fieldName).size());

      String errorMessage = "The 'STRING'-attribute 'fileUpload.keystorePassword' with value '' must have a minimum "
                            + "length of '1' characters but is '0' characters long";
      MatcherAssert.assertThat(fieldErrors.get(fieldName), Matchers.containsInAnyOrder(errorMessage));
    }
  }

  @Test
  public void testUploadWithoutParameters()
  {
    ScimKeystore scimKeystore = ScimKeystore.builder().build();
    ServerResponse<ScimKeystore> response = scimRequestBuilder.create(ScimKeystore.class, KEYSTORE_ENDPOINT)
                                                              .setResource(scimKeystore)
                                                              .sendRequest();

    // validate response
    {
      Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
      ErrorResponse errorResponse = response.getErrorResponse();
      List<String> errorMessageList = errorResponse.getErrorMessages();
      Assertions.assertEquals(1, errorMessageList.size());
      String errorMessage = "Missing object in create request. Either one of 'fileUpload' "
                            + "or 'aliasSelection' is required";
      MatcherAssert.assertThat(errorMessageList, Matchers.containsInAnyOrder(errorMessage));

      Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
      Assertions.assertTrue(fieldErrors.isEmpty());
    }
  }

  @Test
  public void testUploadWithBothUploadParameters()
  {
    FileUpload fileUpload = getFileUpload(UNIT_TEST_KEYSTORE_JKS, UNIT_TEST_KEYSTORE_PASSWORD);
    AliasSelection aliasSelection = AliasSelection.builder()
                                                  .stateId(UUID.randomUUID().toString())
                                                  .aliases(Collections.singletonList("goldfish"))
                                                  .build();
    ScimKeystore scimKeystore = ScimKeystore.builder().fileUpload(fileUpload).aliasSelection(aliasSelection).build();
    ServerResponse<ScimKeystore> response = scimRequestBuilder.create(ScimKeystore.class, KEYSTORE_ENDPOINT)
                                                              .setResource(scimKeystore)
                                                              .sendRequest();

    // validate response
    {
      Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
      ErrorResponse errorResponse = response.getErrorResponse();
      List<String> errorMessageList = errorResponse.getErrorMessages();
      Assertions.assertEquals(1, errorMessageList.size());
      String errorMessage = "Cannot handle create request. Only one of these objects may be present "
                            + "['fileUpload', 'aliasSelection']";
      MatcherAssert.assertThat(errorMessageList, Matchers.containsInAnyOrder(errorMessage));

      Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
      Assertions.assertTrue(fieldErrors.isEmpty());
    }
  }

  @Test
  public void testSelectAliasWithoutStateId()
  {
    final String alias = "goldfish";
    KeystoreEntry keystoreEntry = getUnitTestKeystoreEntryAccess(alias);

    final String stateId = UUID.randomUUID().toString();
    AliasSelection aliasSelection = AliasSelection.builder()
                                                  .stateId(stateId)
                                                  .aliases(Collections.singletonList(keystoreEntry.getAlias()))
                                                  .build();
    ScimKeystore scimKeystore = ScimKeystore.builder().aliasSelection(aliasSelection).build();
    ServerResponse<ScimKeystore> response = scimRequestBuilder.create(ScimKeystore.class, KEYSTORE_ENDPOINT)
                                                              .setResource(scimKeystore)
                                                              .sendRequest();

    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());

    ErrorResponse errorResponse = response.getErrorResponse();
    List<String> errorMessageList = errorResponse.getErrorMessages();
    Assertions.assertEquals(0, errorMessageList.size());

    Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
    final String fieldName = String.format("%s.%s",
                                           ScimKeystore.FieldNames.ALIAS_SELECTION,
                                           ScimKeystore.FieldNames.STATE_ID);
    Assertions.assertEquals(1, fieldErrors.size());
    Assertions.assertEquals(1, fieldErrors.get(fieldName).size());

    String errorMessage = String.format("The stateId '%s' is not related to any previously uploaded keystore files",
                                        stateId);
    MatcherAssert.assertThat(fieldErrors.get(fieldName), Matchers.containsInAnyOrder(errorMessage));
  }

  @Test
  public void testSelectAliasWithoutAlias()
  {

    final String stateId = UUID.randomUUID().toString();
    AliasSelection aliasSelection = AliasSelection.builder().stateId(stateId).build();
    ArrayNode aliasesNode = new ArrayNode(JsonNodeFactory.instance);
    aliasSelection.set(ScimKeystore.FieldNames.ALIASES, aliasesNode);
    ScimKeystore scimKeystore = ScimKeystore.builder()
                                            .aliasSelection(aliasSelection)
                                            .aliasesList(Collections.emptyList())
                                            .build();
    ServerResponse<ScimKeystore> response = scimRequestBuilder.create(ScimKeystore.class, KEYSTORE_ENDPOINT)
                                                              .setResource(scimKeystore)
                                                              .sendRequest();

    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());

    ErrorResponse errorResponse = response.getErrorResponse();
    List<String> errorMessageList = errorResponse.getErrorMessages();
    Assertions.assertEquals(0, errorMessageList.size());

    Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
    final String fieldName = String.format("%s.%s",
                                           ScimKeystore.FieldNames.ALIAS_SELECTION,
                                           ScimKeystore.FieldNames.ALIASES);
    Assertions.assertEquals(1, fieldErrors.size());
    Assertions.assertEquals(1, fieldErrors.get(fieldName).size());

    String errorMessage = "No alias was selected";
    MatcherAssert.assertThat(fieldErrors.get(fieldName), Matchers.containsInAnyOrder(errorMessage));
  }

  @Test
  public void testSelectTooManyAliases()
  {

    final String stateId = UUID.randomUUID().toString();
    List<String> aliases = Arrays.asList("goldfish", "localhost");
    AliasSelection aliasSelection = AliasSelection.builder().stateId(stateId).aliases(aliases).build();
    ScimKeystore scimKeystore = ScimKeystore.builder().aliasSelection(aliasSelection).build();
    ServerResponse<ScimKeystore> response = scimRequestBuilder.create(ScimKeystore.class, KEYSTORE_ENDPOINT)
                                                              .setResource(scimKeystore)
                                                              .sendRequest();

    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());

    ErrorResponse errorResponse = response.getErrorResponse();
    List<String> errorMessageList = errorResponse.getErrorMessages();
    Assertions.assertEquals(0, errorMessageList.size());

    Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
    final String fieldName = String.format("%s.%s",
                                           ScimKeystore.FieldNames.ALIAS_SELECTION,
                                           ScimKeystore.FieldNames.ALIASES);
    Assertions.assertEquals(1, fieldErrors.size());
    Assertions.assertEquals(1, fieldErrors.get(fieldName).size());

    String errorMessage = String.format("Only a single alias can be selected but found: %s", aliases);
    MatcherAssert.assertThat(fieldErrors.get(fieldName), Matchers.containsInAnyOrder(errorMessage));
  }

  @Test
  public void testUnknownAliasSelected()
  {
    ServerResponse<ScimKeystore> uploadResponse = uploadKeystore(UNIT_TEST_KEYSTORE_JKS, UNIT_TEST_KEYSTORE_PASSWORD);
    Assertions.assertEquals(HttpStatus.CREATED, uploadResponse.getHttpStatus());
    final String stateId = uploadResponse.getResource().getAliasSelection().getStateId();

    List<String> aliases = Arrays.asList("unknownAlias");
    AliasSelection aliasSelection = AliasSelection.builder().stateId(stateId).aliases(aliases).build();
    ScimKeystore scimKeystore = ScimKeystore.builder().aliasSelection(aliasSelection).build();
    ServerResponse<ScimKeystore> response = scimRequestBuilder.create(ScimKeystore.class, KEYSTORE_ENDPOINT)
                                                              .setResource(scimKeystore)
                                                              .sendRequest();

    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());

    ErrorResponse errorResponse = response.getErrorResponse();
    List<String> errorMessageList = errorResponse.getErrorMessages();
    Assertions.assertEquals(0, errorMessageList.size());

    Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
    final String aliasesFieldName = String.format("%s.%s",
                                                  ScimKeystore.FieldNames.ALIAS_SELECTION,
                                                  ScimKeystore.FieldNames.ALIASES);
    Assertions.assertEquals(2, fieldErrors.size());
    Assertions.assertEquals(1, fieldErrors.get(aliasesFieldName).size());

    String errorMessage1 = String.format("Unknown alias selected: %s", aliases.get(0));
    MatcherAssert.assertThat(fieldErrors.get(aliasesFieldName), Matchers.containsInAnyOrder(errorMessage1));

    final String privateKeyFieldName = String.format("%s.%s",
                                                     ScimKeystore.FieldNames.ALIAS_SELECTION,
                                                     ScimKeystore.FieldNames.PRIVATE_KEY_PASSWORD);
    Assertions.assertEquals(1, fieldErrors.get(privateKeyFieldName).size());
    String errorMessage2 = String.format("Could not access private key of alias '%s'", aliases.get(0));
    MatcherAssert.assertThat(fieldErrors.get(privateKeyFieldName), Matchers.containsInAnyOrder(errorMessage2));
  }

  @Test
  public void testInvalidPrivateKeyPassword()
  {
    ServerResponse<ScimKeystore> uploadResponse = uploadKeystore(UNIT_TEST_KEYSTORE_JKS, UNIT_TEST_KEYSTORE_PASSWORD);
    Assertions.assertEquals(HttpStatus.CREATED, uploadResponse.getHttpStatus());
    final String stateId = uploadResponse.getResource().getAliasSelection().getStateId();

    final String password = "invalid-password";
    List<String> aliases = Arrays.asList("goldfish");
    AliasSelection aliasSelection = AliasSelection.builder()
                                                  .stateId(stateId)
                                                  .aliases(aliases)
                                                  .privateKeyPassword(password)
                                                  .build();
    ScimKeystore scimKeystore = ScimKeystore.builder().aliasSelection(aliasSelection).build();
    ServerResponse<ScimKeystore> response = scimRequestBuilder.create(ScimKeystore.class, KEYSTORE_ENDPOINT)
                                                              .setResource(scimKeystore)
                                                              .sendRequest();

    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());

    ErrorResponse errorResponse = response.getErrorResponse();
    List<String> errorMessageList = errorResponse.getErrorMessages();
    Assertions.assertEquals(0, errorMessageList.size());

    Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
    final String aliasesFieldName = String.format("%s.%s",
                                                  ScimKeystore.FieldNames.ALIAS_SELECTION,
                                                  ScimKeystore.FieldNames.ALIASES);
    Assertions.assertEquals(2, fieldErrors.size());
    Assertions.assertEquals(1, fieldErrors.get(aliasesFieldName).size());

    String errorMessage1 = "Cannot recover key";
    MatcherAssert.assertThat(fieldErrors.get(aliasesFieldName), Matchers.containsInAnyOrder(errorMessage1));

    final String privateKeyFieldName = String.format("%s.%s",
                                                     ScimKeystore.FieldNames.ALIAS_SELECTION,
                                                     ScimKeystore.FieldNames.PRIVATE_KEY_PASSWORD);
    Assertions.assertEquals(2, fieldErrors.get(privateKeyFieldName).size());
    String errorMessage2 = String.format("Could not access private key of alias '%s'", aliases.get(0));
    MatcherAssert.assertThat(fieldErrors.get(privateKeyFieldName),
                             Matchers.containsInAnyOrder(errorMessage1, errorMessage2));
  }

  @Test
  public void testInvalidAliasPattern()
  {
    ServerResponse<ScimKeystore> uploadResponse = uploadKeystore(UNIT_TEST_KEYSTORE_JKS, UNIT_TEST_KEYSTORE_PASSWORD);
    Assertions.assertEquals(HttpStatus.CREATED, uploadResponse.getHttpStatus());
    final String stateId = uploadResponse.getResource().getAliasSelection().getStateId();

    List<String> aliases = Arrays.asList("goldfish");
    String aliasOverride = "$%&";
    AliasSelection aliasSelection = AliasSelection.builder()
                                                  .stateId(stateId)
                                                  .aliases(aliases)
                                                  .aliasOverride(aliasOverride)
                                                  .build();
    ScimKeystore scimKeystore = ScimKeystore.builder().aliasSelection(aliasSelection).build();
    ServerResponse<ScimKeystore> response = scimRequestBuilder.create(ScimKeystore.class, KEYSTORE_ENDPOINT)
                                                              .setResource(scimKeystore)
                                                              .sendRequest();

    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());

    ErrorResponse errorResponse = response.getErrorResponse();
    List<String> errorMessageList = errorResponse.getErrorMessages();
    Assertions.assertEquals(0, errorMessageList.size());

    Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
    final String aliasesFieldName = String.format("%s.%s",
                                                  ScimKeystore.FieldNames.ALIAS_SELECTION,
                                                  ScimKeystore.FieldNames.ALIASES);
    Assertions.assertEquals(2, fieldErrors.size());
    Assertions.assertEquals(1, fieldErrors.get(aliasesFieldName).size());

    final String aliasPattern = "[A-Za-z0-9_-]+";
    String errorMessage1 = String.format("Invalid alias with value '%s'. The alias is used as url path-parameter "
                                         + "so please use the aliasOverride field to override the alias with a value "
                                         + "that matches the following pattern: %s",
                                         aliasOverride,
                                         aliasPattern);
    MatcherAssert.assertThat(fieldErrors.get(aliasesFieldName), Matchers.containsInAnyOrder(errorMessage1));

    final String aliasesOverrideFieldName = String.format("%s.%s",
                                                          ScimKeystore.FieldNames.ALIAS_SELECTION,
                                                          ScimKeystore.FieldNames.ALIAS_OVERRIDE);
    Assertions.assertEquals(1, fieldErrors.get(aliasesOverrideFieldName).size());
    MatcherAssert.assertThat(fieldErrors.get(aliasesOverrideFieldName), Matchers.containsInAnyOrder(errorMessage1));
  }

  @Test
  public void testAliasAlreadyTaken()
  {
    ServerResponse<ScimKeystore> uploadResponse = uploadKeystore(UNIT_TEST_KEYSTORE_JKS, UNIT_TEST_KEYSTORE_PASSWORD);
    Assertions.assertEquals(HttpStatus.CREATED, uploadResponse.getHttpStatus());
    final String stateId = uploadResponse.getResource().getAliasSelection().getStateId();

    AtomicReference<String> privateKeyPassword = new AtomicReference<>();
    BiFunction<String, String, ServerResponse<ScimKeystore>> uploadAlias = (alias, aliasOverride) -> {
      List<String> aliases = Arrays.asList(alias);
      AliasSelection aliasSelection = AliasSelection.builder()
                                                    .stateId(stateId)
                                                    .aliases(aliases)
                                                    .aliasOverride(aliasOverride)
                                                    .privateKeyPassword(privateKeyPassword.get())
                                                    .build();
      ScimKeystore scimKeystore = ScimKeystore.builder().aliasSelection(aliasSelection).build();
      return scimRequestBuilder.create(ScimKeystore.class, KEYSTORE_ENDPOINT).setResource(scimKeystore).sendRequest();
    };
    {
      ServerResponse<ScimKeystore> response = uploadAlias.apply("localhost", "goldfish");
      Assertions.assertEquals(HttpStatus.CREATED, response.getHttpStatus());
    }

    // error with duplicate alias in aliases field
    {
      ServerResponse<ScimKeystore> response = uploadAlias.apply("goldfish", null);
      Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
      ErrorResponse errorResponse = response.getErrorResponse();
      List<String> errorMessageList = errorResponse.getErrorMessages();
      Assertions.assertEquals(0, errorMessageList.size());

      Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
      final String aliasesFieldName = String.format("%s.%s",
                                                    ScimKeystore.FieldNames.ALIAS_SELECTION,
                                                    ScimKeystore.FieldNames.ALIASES);
      Assertions.assertEquals(1, fieldErrors.size());
      Assertions.assertEquals(1, fieldErrors.get(aliasesFieldName).size());

      String errorMessage1 = "Alias 'goldfish' is already used. Please override this alias with another name";
      MatcherAssert.assertThat(fieldErrors.get(aliasesFieldName), Matchers.containsInAnyOrder(errorMessage1));
    }

    // error with duplicate alias by aliasOverride field
    {
      privateKeyPassword.set("unit-test");
      ServerResponse<ScimKeystore> response = uploadAlias.apply("unit-test", "goldfish");
      Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
      ErrorResponse errorResponse = response.getErrorResponse();
      List<String> errorMessageList = errorResponse.getErrorMessages();
      Assertions.assertEquals(0, errorMessageList.size());

      Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
      final String aliasesFieldName = String.format("%s.%s",
                                                    ScimKeystore.FieldNames.ALIAS_SELECTION,
                                                    ScimKeystore.FieldNames.ALIAS_OVERRIDE);
      Assertions.assertEquals(1, fieldErrors.size());
      Assertions.assertEquals(1, fieldErrors.get(aliasesFieldName).size());

      String errorMessage1 = "Alias 'goldfish' is already used. Please override this alias with another name";
      MatcherAssert.assertThat(fieldErrors.get(aliasesFieldName), Matchers.containsInAnyOrder(errorMessage1));
    }
  }

  @Test
  public void testKeyDoesAlreadyExistUnderAnotherAlias()
  {
    ServerResponse<ScimKeystore> uploadResponse = uploadKeystore(UNIT_TEST_KEYSTORE_JKS, UNIT_TEST_KEYSTORE_PASSWORD);
    Assertions.assertEquals(HttpStatus.CREATED, uploadResponse.getHttpStatus());
    final String stateId = uploadResponse.getResource().getAliasSelection().getStateId();

    BiFunction<String, String, ServerResponse<ScimKeystore>> uploadAlias = (alias, aliasOverride) -> {
      List<String> aliases = Arrays.asList(alias);
      AliasSelection aliasSelection = AliasSelection.builder()
                                                    .stateId(stateId)
                                                    .aliases(aliases)
                                                    .aliasOverride(aliasOverride)
                                                    .build();
      ScimKeystore scimKeystore = ScimKeystore.builder().aliasSelection(aliasSelection).build();
      return scimRequestBuilder.create(ScimKeystore.class, KEYSTORE_ENDPOINT).setResource(scimKeystore).sendRequest();
    };

    final String otherAlias = "happy-day";
    {
      ServerResponse<ScimKeystore> response = uploadAlias.apply("goldfish", otherAlias);
      Assertions.assertEquals(HttpStatus.CREATED, response.getHttpStatus());
    }

    // error with duplicate alias in aliases field
    {
      ServerResponse<ScimKeystore> response = uploadAlias.apply("goldfish", null);
      Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
      ErrorResponse errorResponse = response.getErrorResponse();
      List<String> errorMessageList = errorResponse.getErrorMessages();
      Assertions.assertEquals(0, errorMessageList.size());

      Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
      final String aliasesFieldName = String.format("%s.%s",
                                                    ScimKeystore.FieldNames.ALIAS_SELECTION,
                                                    ScimKeystore.FieldNames.ALIASES);
      Assertions.assertEquals(1, fieldErrors.size());
      Assertions.assertEquals(1, fieldErrors.get(aliasesFieldName).size());

      String errorMessage1 = String.format("The selected Key is already present under the alias '%s'", otherAlias);
      MatcherAssert.assertThat(fieldErrors.get(aliasesFieldName), Matchers.containsInAnyOrder(errorMessage1));
    }
  }

  @Test
  public void testSelectAliasWithIllegalCharacter()
  {
    ServerResponse<ScimKeystore> uploadResponse = uploadKeystore(UNIT_TEST_KEYSTORE_JKS, UNIT_TEST_KEYSTORE_PASSWORD);
    Assertions.assertEquals(HttpStatus.CREATED, uploadResponse.getHttpStatus());
    final String stateId = uploadResponse.getResource().getAliasSelection().getStateId();

    List<String> aliases = Arrays.asList("goldfish");
    String aliasOverride = "bad-alias/";
    AliasSelection aliasSelection = AliasSelection.builder()
                                                  .stateId(stateId)
                                                  .aliases(aliases)
                                                  .aliasOverride(aliasOverride)
                                                  .build();
    ScimKeystore scimKeystore = ScimKeystore.builder().aliasSelection(aliasSelection).build();
    ServerResponse<ScimKeystore> response = scimRequestBuilder.create(ScimKeystore.class, KEYSTORE_ENDPOINT)
                                                              .setResource(scimKeystore)
                                                              .sendRequest();

    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());

    ErrorResponse errorResponse = response.getErrorResponse();
    List<String> errorMessageList = errorResponse.getErrorMessages();
    Assertions.assertEquals(0, errorMessageList.size());

    Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
    Assertions.assertEquals(2, fieldErrors.size());
    {
      final String aliasesFieldName = String.format("%s.%s",
                                                    ScimKeystore.FieldNames.ALIAS_SELECTION,
                                                    ScimKeystore.FieldNames.ALIAS_OVERRIDE);
      Assertions.assertEquals(1, fieldErrors.get(aliasesFieldName).size());

      String errorMessage1 = "Invalid alias with value 'bad-alias/'. The alias is used as url path-parameter so "
                             + "please use the aliasOverride field to override the alias with a value that matches "
                             + "the following pattern: [A-Za-z0-9_-]+";
      MatcherAssert.assertThat(fieldErrors.get(aliasesFieldName), Matchers.containsInAnyOrder(errorMessage1));
    }
  }

  @Test
  public void testDeleteAliasReferencedByOpenIdClient()
  {
    addDefaultEntriesToApplicationKeystore();
    Keystore keystore = keystoreDao.getKeystore();
    Assertions.assertEquals(3, keystore.getKeystoreEntries().size());
    OpenIdProvider openIdProvider = openIdProviderDao.save(OpenIdProvider.builder()
                                                                         .name(UUID.randomUUID().toString())
                                                                         .discoveryEndpoint("http://localhost:8080")
                                                                         .build());
    OpenIdClient openIdClient = openIdClientDao.save(OpenIdClient.builder()
                                                                 .openIdProvider(openIdProvider)
                                                                 .clientId(UUID.randomUUID().toString())
                                                                 .build());

    List<KeystoreEntry> testKeystoreEntryAccess = getUnitTestKeystoreEntryAccess();
    for ( int i = 0 ; i < testKeystoreEntryAccess.size() ; i++ )
    {
      KeystoreEntry unitTestKeystoreEntryAccess = testKeystoreEntryAccess.get(i);
      openIdClient.setSigningKeyRef(unitTestKeystoreEntryAccess.getAlias());
      openIdClient.setDecryptionKeyRef(unitTestKeystoreEntryAccess.getAlias());
      openIdClient.setTlsClientAuthKeyRef(unitTestKeystoreEntryAccess.getAlias());
      openIdClient = openIdClientDao.save(openIdClient);

      Assertions.assertEquals(1, openIdClientDao.count());
      openIdClient = openIdClientDao.findById(openIdClient.getId()).orElseThrow();
      Assertions.assertEquals(unitTestKeystoreEntryAccess.getAlias(), openIdClient.getSigningKeyRef());

      ServerResponse<ScimKeystore> response = scimRequestBuilder.delete(ScimKeystore.class,
                                                                        KEYSTORE_ENDPOINT,
                                                                        unitTestKeystoreEntryAccess.getAlias())
                                                                .sendRequest();
      Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getHttpStatus());
      keystore = keystoreDao.getKeystore();
      Assertions.assertEquals(3 - (i + 1), keystore.getKeystoreEntries().size());

      Assertions.assertEquals(1, openIdClientDao.count());
      openIdClient = openIdClientDao.findById(openIdClient.getId()).orElseThrow();
      Assertions.assertNull(openIdClient.getSigningKeyRef());
      Assertions.assertNull(openIdClient.getDecryptionKeyRef());
      Assertions.assertNull(openIdClient.getTlsClientAuthKeyRef());
    }
  }

  private ServerResponse<ScimKeystore> uploadKeystore(String classPathToKeystore, String password)
  {
    FileUpload fileUpload = getFileUpload(classPathToKeystore, password);
    ScimKeystore scimKeystore = ScimKeystore.builder().fileUpload(fileUpload).build();
    return scimRequestBuilder.create(ScimKeystore.class, KEYSTORE_ENDPOINT).setResource(scimKeystore).sendRequest();
  }

  @SneakyThrows
  private FileUpload getFileUpload(String classPathToKeystore, String password)
  {
    final String keystoreFileB64;
    try (InputStream inputStream = readAsInputStream(classPathToKeystore))
    {
      byte[] keystoreFile = IOUtils.toByteArray(inputStream);
      keystoreFileB64 = Base64.getEncoder().encodeToString(keystoreFile);
    }
    String[] pathParts = classPathToKeystore.split("/");
    final String filename = pathParts[pathParts.length - 1];

    FileUpload fileUpload = FileUpload.builder()
                                      .keystoreFile(keystoreFileB64)
                                      .keystoreFileName(filename)
                                      .keystorePassword(password)
                                      .build();
    return fileUpload;
  }
}
