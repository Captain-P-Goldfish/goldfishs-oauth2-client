package de.captaingoldfish.restclient.application.endpoints.openidclient;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import de.captaingoldfish.restclient.application.endpoints.keystore.KeystoreHandler;
import de.captaingoldfish.restclient.application.setup.AbstractScimClientConfig;
import de.captaingoldfish.restclient.application.setup.OAuthRestClientTest;
import de.captaingoldfish.restclient.database.entities.KeystoreEntry;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;
import de.captaingoldfish.restclient.database.entities.OpenIdProvider;
import de.captaingoldfish.restclient.scim.resources.CertificateInfo;
import de.captaingoldfish.restclient.scim.resources.ScimKeystore;
import de.captaingoldfish.restclient.scim.resources.ScimKeystore.AliasSelection;
import de.captaingoldfish.restclient.scim.resources.ScimOpenIdClient;
import de.captaingoldfish.scim.sdk.client.response.ServerResponse;
import de.captaingoldfish.scim.sdk.common.constants.HttpStatus;
import de.captaingoldfish.scim.sdk.common.response.ErrorResponse;
import de.captaingoldfish.scim.sdk.common.response.ListResponse;
import de.captaingoldfish.scim.sdk.server.schemas.ResourceType;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


/**
 * @author Pascal Knueppel
 * @since 29.05.2021
 */
@Slf4j
@OAuthRestClientTest
public class OpenIdClientHandlerTest extends AbstractScimClientConfig
{

  /**
   * the scim endpoint for the OpenID Clients
   */
  private static final String OPENID_CLIENT_ENDPOINT = "/OpenIdClient";

  @Qualifier("keystoreResourceType")
  @Autowired
  private ResourceType keystoreResourceType;

  private OpenIdProvider openIdProvider;

  private KeystoreEntry keystoreEntry;

  @BeforeEach
  public void initialize()
  {
    openIdProvider = createDefaultProvider();
    addDefaultEntriesToApplicationKeystore("localhost");
  }

  @Test
  public void testCreateOpenIdClient()
  {
    final String clientId = "goldfish";
    final String clientSecret = "blubb";
    final String audience = "pond";
    final CertificateInfo certificateInfo = CertificateInfo.builder().alias("localhost").build();

    ScimOpenIdClient scimOpenIdClient = ScimOpenIdClient.builder()
                                                        .openIdProviderId(openIdProvider.getId())
                                                        .clientId(clientId)
                                                        .clientSecret(clientSecret)
                                                        .audience(audience)
                                                        .certificateInfo(certificateInfo)
                                                        .build();
    Assertions.assertEquals(0, openIdClientDao.count());
    ServerResponse<ScimOpenIdClient> response = scimRequestBuilder.create(ScimOpenIdClient.class,
                                                                          OPENID_CLIENT_ENDPOINT)
                                                                  .setResource(scimOpenIdClient)
                                                                  .sendRequest();
    Assertions.assertEquals(HttpStatus.CREATED, response.getHttpStatus());

    ScimOpenIdClient createdResource = response.getResource();
    Assertions.assertEquals(clientId, createdResource.getClientId());
    Assertions.assertEquals(clientSecret, createdResource.getClientSecret().get());
    Assertions.assertEquals(audience, createdResource.getAudience().get());
    Assertions.assertEquals(certificateInfo.getAlias(), createdResource.getCertificateInfo().get().getAlias());

    Assertions.assertEquals(1, openIdClientDao.count());
    OpenIdClient openIdClient = openIdClientDao.findById(Long.valueOf(createdResource.getId().get())).get();
    Assertions.assertEquals(clientId, openIdClient.getClientId());
    Assertions.assertEquals(clientSecret, openIdClient.getClientSecret());
    Assertions.assertEquals(audience, openIdClient.getAudience());
    Assertions.assertEquals(certificateInfo.getAlias(), openIdClient.getSignatureKeyRef());
    Assertions.assertEquals(1, openIdClientDao.count());
  }

  @Test
  public void testCreateDuplicateClientId()
  {
    final String clientId = "goldfish";
    final String clientSecret = "blubb";
    final String audience = "pond";
    final CertificateInfo certificateInfo = CertificateInfo.builder().alias("localhost").build();

    ScimOpenIdClient scimOpenIdClient = ScimOpenIdClient.builder()
                                                        .openIdProviderId(openIdProvider.getId())
                                                        .clientId(clientId)
                                                        .clientSecret(clientSecret)
                                                        .audience(audience)
                                                        .certificateInfo(certificateInfo)
                                                        .build();

    OpenIdClient openIdClient = OpenIdClientConverter.toOpenIdClient(scimOpenIdClient);
    openIdClientDao.save(openIdClient);

    ServerResponse<ScimOpenIdClient> response = scimRequestBuilder.create(ScimOpenIdClient.class,
                                                                          OPENID_CLIENT_ENDPOINT)
                                                                  .setResource(scimOpenIdClient)
                                                                  .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();
    Assertions.assertEquals(0, errorResponse.getErrorMessages().size());

    Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
    Assertions.assertEquals(1, fieldErrors.size());
    List<String> clientIdErrors = fieldErrors.get(ScimOpenIdClient.FieldNames.CLIENT_ID);
    Assertions.assertEquals(1, clientIdErrors.size());
    String errorMessage = String.format("A client with this clientId '%s' was already registered", clientId);
    MatcherAssert.assertThat(clientIdErrors, Matchers.containsInAnyOrder(errorMessage));
  }

  @Test
  public void testCreateClientWithUnknownParent()
  {
    final String clientId = "goldfish";
    final String clientSecret = "blubb";
    final String audience = "pond";
    final CertificateInfo certificateInfo = CertificateInfo.builder().alias("localhost").build();

    final Long unknownId = 999999L;
    ScimOpenIdClient scimOpenIdClient = ScimOpenIdClient.builder()
                                                        .openIdProviderId(unknownId)
                                                        .clientId(clientId)
                                                        .clientSecret(clientSecret)
                                                        .audience(audience)
                                                        .certificateInfo(certificateInfo)
                                                        .build();

    ServerResponse<ScimOpenIdClient> response = scimRequestBuilder.create(ScimOpenIdClient.class,
                                                                          OPENID_CLIENT_ENDPOINT)
                                                                  .setResource(scimOpenIdClient)
                                                                  .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();
    Assertions.assertEquals(0, errorResponse.getErrorMessages().size());

    Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
    Assertions.assertEquals(1, fieldErrors.size());
    List<String> openIdProviderErrors = fieldErrors.get(ScimOpenIdClient.FieldNames.OPEN_ID_PROVIDER_ID);
    Assertions.assertEquals(1, openIdProviderErrors.size());
    String errorMessage = String.format("No OpenID Provider with ID '%s' does exist", unknownId);
    MatcherAssert.assertThat(openIdProviderErrors, Matchers.containsInAnyOrder(errorMessage));
  }

  @Test
  public void testCreateClientWithoutParent()
  {
    final String clientId = "goldfish";
    final String clientSecret = "blubb";
    final String audience = "pond";
    final CertificateInfo certificateInfo = CertificateInfo.builder().alias("localhost").build();

    ScimOpenIdClient scimOpenIdClient = ScimOpenIdClient.builder()
                                                        .clientId(clientId)
                                                        .clientSecret(clientSecret)
                                                        .audience(audience)
                                                        .certificateInfo(certificateInfo)
                                                        .build();

    ServerResponse<ScimOpenIdClient> response = scimRequestBuilder.create(ScimOpenIdClient.class,
                                                                          OPENID_CLIENT_ENDPOINT)
                                                                  .setResource(scimOpenIdClient)
                                                                  .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();
    Assertions.assertEquals(0, errorResponse.getErrorMessages().size());

    Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
    Assertions.assertEquals(1, fieldErrors.size());
    List<String> openIdProviderErrors = fieldErrors.get(ScimOpenIdClient.FieldNames.OPEN_ID_PROVIDER_ID);
    Assertions.assertEquals(1, openIdProviderErrors.size());
    String errorMessage = "Required 'READ_WRITE' attribute "
                          + "'urn:ietf:params:scim:schemas:captaingoldfish:2.0:OpenIdClient:openIdProviderId' is missing";
    MatcherAssert.assertThat(openIdProviderErrors, Matchers.containsInAnyOrder(errorMessage));
  }

  @Test
  public void testCreateClientWithUnknownAliasReference()
  {
    final String clientId = "goldfish";
    final String clientSecret = "blubb";
    final String audience = "pond";
    final String unknownAlias = "unknown";
    final CertificateInfo certificateInfo = CertificateInfo.builder().alias(unknownAlias).build();

    ScimOpenIdClient scimOpenIdClient = ScimOpenIdClient.builder()
                                                        .openIdProviderId(openIdProvider.getId())
                                                        .clientId(clientId)
                                                        .clientSecret(clientSecret)
                                                        .audience(audience)
                                                        .certificateInfo(certificateInfo)
                                                        .build();

    ServerResponse<ScimOpenIdClient> response = scimRequestBuilder.create(ScimOpenIdClient.class,
                                                                          OPENID_CLIENT_ENDPOINT)
                                                                  .setResource(scimOpenIdClient)
                                                                  .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();
    Assertions.assertEquals(0, errorResponse.getErrorMessages().size());

    Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
    Assertions.assertEquals(1, fieldErrors.size());
    List<String> aliasErrors = fieldErrors.get(CertificateInfo.FieldNames.ALIAS);
    Assertions.assertEquals(1, aliasErrors.size());
    String errorMessage = String.format("Alias '%s' does not exist within application keystore", unknownAlias);
    MatcherAssert.assertThat(aliasErrors, Matchers.containsInAnyOrder(errorMessage));
  }

  @Test
  public void testGetOpenIdClient()
  {
    final String clientId = "goldfish";
    final String clientSecret = "blubb";
    final String audience = "pond";
    final CertificateInfo certificateInfo = CertificateInfo.builder().alias("localhost").build();

    ScimOpenIdClient scimOpenIdClient = ScimOpenIdClient.builder()
                                                        .openIdProviderId(openIdProvider.getId())
                                                        .clientId(clientId)
                                                        .clientSecret(clientSecret)
                                                        .audience(audience)
                                                        .certificateInfo(certificateInfo)
                                                        .build();

    OpenIdClient openIdClient = OpenIdClientConverter.toOpenIdClient(scimOpenIdClient);
    openIdClient = openIdClientDao.save(openIdClient);

    ServerResponse<ScimOpenIdClient> response = scimRequestBuilder.get(ScimOpenIdClient.class,
                                                                       OPENID_CLIENT_ENDPOINT,
                                                                       String.valueOf(openIdClient.getId()))
                                                                  .sendRequest();
    Assertions.assertEquals(HttpStatus.OK, response.getHttpStatus());
    ScimOpenIdClient returnedResource = response.getResource();
    Assertions.assertEquals(clientId, returnedResource.getClientId());
    Assertions.assertEquals(clientSecret, returnedResource.getClientSecret().get());
    Assertions.assertEquals(audience, returnedResource.getAudience().get());
    Assertions.assertEquals(certificateInfo.getAlias(), returnedResource.getCertificateInfo().get().getAlias());
  }

  @Test
  public void testGetUnknownOpenIdClient()
  {
    final String unknownId = "99999999";
    ServerResponse<ScimOpenIdClient> response = scimRequestBuilder.get(ScimOpenIdClient.class,
                                                                       OPENID_CLIENT_ENDPOINT,
                                                                       unknownId)
                                                                  .sendRequest();
    Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();
    Assertions.assertEquals("OpenID Client with id '99999999' does not exist", errorResponse.getDetail().get());
  }

  @Test
  public void testListOpenIdClient()
  {
    final String clientId = "goldfish";
    final String clientSecret = "blubb";
    final String audience = "pond";
    final String alias = "localhost";

    openIdClientDao.save(OpenIdClient.builder()
                                     .openIdProvider(openIdProvider)
                                     .clientId(clientId)
                                     .clientSecret(clientSecret)
                                     .audience(audience)
                                     .signatureKeyRef(alias)
                                     .build());


    ServerResponse<ListResponse<ScimOpenIdClient>> response = scimRequestBuilder.list(ScimOpenIdClient.class,
                                                                                      OPENID_CLIENT_ENDPOINT)
                                                                                .get()
                                                                                .sendRequest();
    Assertions.assertEquals(HttpStatus.OK, response.getHttpStatus());
    ListResponse<ScimOpenIdClient> returnedResource = response.getResource();
    Assertions.assertEquals(1, returnedResource.getTotalResults());
    Assertions.assertEquals(1, returnedResource.getItemsPerPage());
    ScimOpenIdClient scimOpenIdClient = returnedResource.getListedResources().get(0);

    Assertions.assertEquals(openIdProvider.getId(), scimOpenIdClient.getOpenIdProviderId());
    Assertions.assertEquals(clientId, scimOpenIdClient.getClientId());
    Assertions.assertEquals(clientSecret, scimOpenIdClient.getClientSecret().get());
    Assertions.assertEquals(audience, scimOpenIdClient.getAudience().get());
    Assertions.assertEquals(alias, scimOpenIdClient.getCertificateInfo().get().getAlias());
  }

  @Test
  public void testDeleteOpenIdClient()
  {
    final String clientId = "goldfish";
    final String clientSecret = "blubb";
    final String audience = "pond";
    final String alias = "localhost";

    OpenIdClient openIdClient = openIdClientDao.save(OpenIdClient.builder()
                                                                 .openIdProvider(openIdProvider)
                                                                 .clientId(clientId)
                                                                 .clientSecret(clientSecret)
                                                                 .audience(audience)
                                                                 .signatureKeyRef(alias)
                                                                 .build());


    Assertions.assertEquals(1, openIdClientDao.count());
    ServerResponse<ScimOpenIdClient> response = scimRequestBuilder.delete(ScimOpenIdClient.class,
                                                                          OPENID_CLIENT_ENDPOINT,
                                                                          String.valueOf(openIdClient.getId()))
                                                                  .sendRequest();
    Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getHttpStatus());
    Assertions.assertEquals(0, openIdClientDao.count());
  }

  @Test
  public void testDeleteUnknownOpenIdClient()
  {
    final String unknownId = "999999999";
    ServerResponse<ScimOpenIdClient> response = scimRequestBuilder.delete(ScimOpenIdClient.class,
                                                                          OPENID_CLIENT_ENDPOINT,
                                                                          unknownId)
                                                                  .sendRequest();
    Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();
    Assertions.assertEquals(String.format("OpenID Client with id '%s' does not exist", unknownId),
                            errorResponse.getDetail().get());
  }

  @Test
  public void testUpdateOpenIdClient()
  {
    final String clientId = "goldfish";
    final String clientSecret = "blubb";
    final String audience = "pond";
    final String alias = "localhost";

    OpenIdClient openIdClient = openIdClientDao.save(OpenIdClient.builder()
                                                                 .openIdProvider(openIdProvider)
                                                                 .clientId(clientId)
                                                                 .clientSecret(clientSecret)
                                                                 .audience(audience)
                                                                 .signatureKeyRef(alias)
                                                                 .build());

    final String newClientId = "cup";
    final String newClientSecret = "cake";
    final String newAudience = "key";
    final String newAlias = "unit-test";
    addDefaultEntriesToApplicationKeystore(newAlias);

    ScimOpenIdClient scimOpenIdClient = ScimOpenIdClient.builder()
                                                        .openIdProviderId(openIdProvider.getId())
                                                        .clientId(newClientId)
                                                        .clientSecret(newClientSecret)
                                                        .audience(newAudience)
                                                        .certificateInfo(CertificateInfo.builder()
                                                                                        .alias(newAlias)
                                                                                        .build())
                                                        .build();
    Assertions.assertEquals(1, openIdClientDao.count());
    ServerResponse<ScimOpenIdClient> response = scimRequestBuilder.update(ScimOpenIdClient.class,
                                                                          OPENID_CLIENT_ENDPOINT,
                                                                          String.valueOf(openIdClient.getId()))
                                                                  .setResource(scimOpenIdClient)
                                                                  .sendRequest();
    Assertions.assertEquals(HttpStatus.OK, response.getHttpStatus());

    ScimOpenIdClient updatedResource = response.getResource();
    Assertions.assertEquals(newClientId, updatedResource.getClientId());
    Assertions.assertEquals(newClientSecret, updatedResource.getClientSecret().get());
    Assertions.assertEquals(newAudience, updatedResource.getAudience().get());
    Assertions.assertEquals(newAlias, updatedResource.getCertificateInfo().get().getAlias());
    Assertions.assertEquals(1, openIdClientDao.count());
  }

  @Test
  public void testUpdateClientToAlreadyExistingClientIdOnOtherClient()
  {
    final String clientId = "goldfish";
    final String clientSecret = "blubb";
    final String audience = "pond";
    final String alias = "localhost";

    OpenIdClient openIdClient = openIdClientDao.save(OpenIdClient.builder()
                                                                 .openIdProvider(openIdProvider)
                                                                 .clientId(clientId)
                                                                 .clientSecret(clientSecret)
                                                                 .audience(audience)
                                                                 .signatureKeyRef(alias)
                                                                 .build());
    final String otherClientId = "fish";

    openIdClientDao.save(OpenIdClient.builder()
                                     .openIdProvider(openIdProvider)
                                     .clientId(otherClientId)
                                     .clientSecret(clientSecret)
                                     .audience(audience)
                                     .signatureKeyRef(alias)
                                     .build());

    ScimOpenIdClient scimOpenIdClient = ScimOpenIdClient.builder()
                                                        .openIdProviderId(openIdProvider.getId())
                                                        .clientId(otherClientId)
                                                        .clientSecret(clientSecret)
                                                        .audience(audience)
                                                        .certificateInfo(CertificateInfo.builder().alias(alias).build())
                                                        .build();
    ServerResponse<ScimOpenIdClient> response = scimRequestBuilder.update(ScimOpenIdClient.class,
                                                                          OPENID_CLIENT_ENDPOINT,
                                                                          String.valueOf(openIdClient.getId()))
                                                                  .setResource(scimOpenIdClient)
                                                                  .sendRequest();
    Assertions.assertEquals(HttpStatus.CONFLICT, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();
    Assertions.assertEquals(String.format("A client with this clientId '%s' was already registered. "
                                          + "Failed to alter attribute clientId",
                                          otherClientId),
                            errorResponse.getDetail().get());
  }

  @Test
  public void testUpdateClientIdToExistingOneOnOtherProvider()
  {
    final String clientId = "goldfish";
    final String clientSecret = "blubb";
    final String audience = "pond";
    final String alias = "localhost";

    OpenIdClient openIdClient = openIdClientDao.save(OpenIdClient.builder()
                                                                 .openIdProvider(openIdProvider)
                                                                 .clientId(clientId)
                                                                 .clientSecret(clientSecret)
                                                                 .audience(audience)
                                                                 .signatureKeyRef(alias)
                                                                 .build());
    final String otherClientId = "fish";

    OpenIdProvider otherOpenIdProvider = openIdProviderDao.save(OpenIdProvider.builder()
                                                                              .name(UUID.randomUUID().toString())
                                                                              .discoveryEndpoint("http://localhost:8080")
                                                                              .build());

    openIdClientDao.save(OpenIdClient.builder()
                                     .openIdProvider(otherOpenIdProvider)
                                     .clientId(otherClientId)
                                     .clientSecret(clientSecret)
                                     .audience(audience)
                                     .signatureKeyRef(alias)
                                     .build());

    ScimOpenIdClient scimOpenIdClient = ScimOpenIdClient.builder()
                                                        .openIdProviderId(openIdProvider.getId())
                                                        .clientId(otherClientId)
                                                        .clientSecret(clientSecret)
                                                        .audience(audience)
                                                        .certificateInfo(CertificateInfo.builder().alias(alias).build())
                                                        .build();
    ServerResponse<ScimOpenIdClient> response = scimRequestBuilder.update(ScimOpenIdClient.class,
                                                                          OPENID_CLIENT_ENDPOINT,
                                                                          String.valueOf(openIdClient.getId()))
                                                                  .setResource(scimOpenIdClient)
                                                                  .sendRequest();
    Assertions.assertEquals(HttpStatus.OK, response.getHttpStatus());
    ScimOpenIdClient updatedResource = response.getResource();
    Assertions.assertEquals(otherClientId, updatedResource.getClientId());
    Assertions.assertEquals(clientSecret, updatedResource.getClientSecret().get());
    Assertions.assertEquals(audience, updatedResource.getAudience().get());
    Assertions.assertEquals(alias, updatedResource.getCertificateInfo().get().getAlias());
  }

  @Test
  public void testUpdateOpenIdProviderToUnknown()
  {
    final String clientId = "goldfish";
    final String clientSecret = "blubb";
    final String audience = "pond";
    final String alias = "localhost";

    OpenIdClient openIdClient = openIdClientDao.save(OpenIdClient.builder()
                                                                 .openIdProvider(openIdProvider)
                                                                 .clientId(clientId)
                                                                 .clientSecret(clientSecret)
                                                                 .audience(audience)
                                                                 .signatureKeyRef(alias)
                                                                 .build());

    final Long unknownProviderId = 99999999999L;
    ScimOpenIdClient scimOpenIdClient = ScimOpenIdClient.builder()
                                                        .openIdProviderId(unknownProviderId)
                                                        .clientId(clientId)
                                                        .clientSecret(clientSecret)
                                                        .audience(audience)
                                                        .certificateInfo(CertificateInfo.builder().alias(alias).build())
                                                        .build();
    ServerResponse<ScimOpenIdClient> response = scimRequestBuilder.update(ScimOpenIdClient.class,
                                                                          OPENID_CLIENT_ENDPOINT,
                                                                          String.valueOf(openIdClient.getId()))
                                                                  .setResource(scimOpenIdClient)
                                                                  .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();
    Assertions.assertEquals(0, errorResponse.getErrorMessages().size());

    Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
    Assertions.assertEquals(1, fieldErrors.size());
    List<String> openIdProviderErrors = fieldErrors.get(ScimOpenIdClient.FieldNames.OPEN_ID_PROVIDER_ID);
    Assertions.assertEquals(1, openIdProviderErrors.size());
    String errorMessage = String.format("No OpenID Provider with ID '%s' does exist", unknownProviderId);
    MatcherAssert.assertThat(openIdProviderErrors, Matchers.containsInAnyOrder(errorMessage));
  }

  @Test
  public void testRemoveProviderIdReference()
  {
    final String clientId = "goldfish";
    final String clientSecret = "blubb";
    final String audience = "pond";
    final String alias = "localhost";

    OpenIdClient openIdClient = openIdClientDao.save(OpenIdClient.builder()
                                                                 .openIdProvider(openIdProvider)
                                                                 .clientId(clientId)
                                                                 .clientSecret(clientSecret)
                                                                 .audience(audience)
                                                                 .signatureKeyRef(alias)
                                                                 .build());

    ScimOpenIdClient scimOpenIdClient = ScimOpenIdClient.builder()
                                                        .clientId(clientId)
                                                        .clientSecret(clientSecret)
                                                        .audience(audience)
                                                        .certificateInfo(CertificateInfo.builder().alias(alias).build())
                                                        .build();
    ServerResponse<ScimOpenIdClient> response = scimRequestBuilder.update(ScimOpenIdClient.class,
                                                                          OPENID_CLIENT_ENDPOINT,
                                                                          String.valueOf(openIdClient.getId()))
                                                                  .setResource(scimOpenIdClient)
                                                                  .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();
    Assertions.assertEquals(0, errorResponse.getErrorMessages().size());

    Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
    Assertions.assertEquals(1, fieldErrors.size());
    List<String> openIdProviderErrors = fieldErrors.get(ScimOpenIdClient.FieldNames.OPEN_ID_PROVIDER_ID);
    Assertions.assertEquals(1, openIdProviderErrors.size());
    String errorMessage = "Required 'READ_WRITE' attribute "
                          + "'urn:ietf:params:scim:schemas:captaingoldfish:2.0:OpenIdClient:openIdProviderId' is missing";
    MatcherAssert.assertThat(openIdProviderErrors, Matchers.containsInAnyOrder(errorMessage));
  }

  private OpenIdProvider createDefaultProvider()
  {
    return openIdProviderDao.save(OpenIdProvider.builder()
                                                .name(UUID.randomUUID().toString())
                                                .discoveryEndpoint("http://localhost:8080")
                                                .build());
  }

  /**
   * adds all entries from the unit-keystore of type jks into the application keystore
   */
  @SneakyThrows
  private void addDefaultEntriesToApplicationKeystore(String alias)
  {
    this.keystoreEntry = getUnitTestKeystoreEntryAccess(alias);

    KeystoreHandler keystoreHandler = (KeystoreHandler)keystoreResourceType.getResourceHandlerImpl();

    String b64Keystore = Base64.getEncoder().encodeToString(readAsBytes(UNIT_TEST_KEYSTORE_JKS));
    ScimKeystore.FileUpload fileUpload = ScimKeystore.FileUpload.builder()
                                                                .keystoreFile(b64Keystore)
                                                                .keystoreFileName("test.jks")
                                                                .keystorePassword(UNIT_TEST_KEYSTORE_PASSWORD)
                                                                .build();
    ScimKeystore uploadResponse = keystoreHandler.handleKeystoreUpload(fileUpload);

    AliasSelection aliasSelection = AliasSelection.builder()
                                                  .stateId(uploadResponse.getAliasSelection().getStateId())
                                                  .aliases(Collections.singletonList(keystoreEntry.getAlias()))
                                                  .privateKeyPassword(keystoreEntry.getPrivateKeyPassword())
                                                  .build();
    keystoreHandler.handleAliasSelection(aliasSelection);
  }
}
