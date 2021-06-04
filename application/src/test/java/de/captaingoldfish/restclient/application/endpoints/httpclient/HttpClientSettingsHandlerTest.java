package de.captaingoldfish.restclient.application.endpoints.httpclient;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import de.captaingoldfish.restclient.application.endpoints.keystore.KeystoreHandler;
import de.captaingoldfish.restclient.application.setup.AbstractScimClientConfig;
import de.captaingoldfish.restclient.application.setup.OAuthRestClientTest;
import de.captaingoldfish.restclient.database.entities.HttpClientSettings;
import de.captaingoldfish.restclient.database.entities.KeystoreEntry;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;
import de.captaingoldfish.restclient.database.entities.OpenIdProvider;
import de.captaingoldfish.restclient.database.entities.Proxy;
import de.captaingoldfish.restclient.scim.resources.ScimHttpClientSettings;
import de.captaingoldfish.restclient.scim.resources.ScimKeystore;
import de.captaingoldfish.scim.sdk.client.response.ServerResponse;
import de.captaingoldfish.scim.sdk.common.constants.HttpStatus;
import de.captaingoldfish.scim.sdk.common.response.ErrorResponse;
import de.captaingoldfish.scim.sdk.common.response.ListResponse;
import de.captaingoldfish.scim.sdk.server.schemas.ResourceType;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


/**
 * @author Pascal Knueppel
 * @since 01.06.2021
 */
@Slf4j
@OAuthRestClientTest
public class HttpClientSettingsHandlerTest extends AbstractScimClientConfig
{

  /**
   * the scim endpoint for managing http client settings
   */
  private static final String HTTP_CLIENT_SETTINGS_ENDPOINT = "/HttpClientSettings";

  @Qualifier("keystoreResourceType")
  @Autowired
  private ResourceType keystoreResourceType;

  @Test
  public void testCreateHttpClientSettings()
  {
    final Long requestTimeout = 120L;
    final Long connectionTimeout = 180L;
    final Long socketTimeout = 240L;
    final boolean useHostnameVerifier = true;
    final KeystoreEntry keystoreEntry = addDefaultEntriesToApplicationKeystore("goldfish");

    OpenIdClient openIdClient = openIdClientDao.save(createOpenIdClient());
    Proxy proxy = createProxy();

    ScimHttpClientSettings scimHttpClientSettings = ScimHttpClientSettings.builder()
                                                                          .openIdClientReference(openIdClient.getId())
                                                                          .proxyReference(proxy.getId())
                                                                          .requestTimeout(requestTimeout)
                                                                          .connectionTimeout(connectionTimeout)
                                                                          .socketTimeout(socketTimeout)
                                                                          .useHostnameVerifier(useHostnameVerifier)
                                                                          .tlsClientAuthAliasReference(keystoreEntry.getAlias())
                                                                          .build();
    ServerResponse<ScimHttpClientSettings> response = scimRequestBuilder.create(ScimHttpClientSettings.class,
                                                                                HTTP_CLIENT_SETTINGS_ENDPOINT)
                                                                        .setResource(scimHttpClientSettings)
                                                                        .sendRequest();
    Assertions.assertEquals(HttpStatus.CREATED, response.getHttpStatus());
    ScimHttpClientSettings returnedResource = response.getResource();
    Assertions.assertEquals(requestTimeout, returnedResource.getRequestTimeout().get());
    Assertions.assertEquals(connectionTimeout, returnedResource.getConnectionTimeout().get());
    Assertions.assertEquals(socketTimeout, returnedResource.getSocketTimeout().get());
    Assertions.assertEquals(useHostnameVerifier, returnedResource.getUseHostnameVerifier().get());
    Assertions.assertEquals(openIdClient.getId(), returnedResource.getOpenIdClientReference().get());
    Assertions.assertEquals(proxy.getId(), returnedResource.getProxyReference().get());
    Assertions.assertEquals(keystoreEntry.getAlias(), returnedResource.getTlsClientAuthAliasReference().get());

    Assertions.assertEquals(1, httpClientSettingsDao.count());
    HttpClientSettings httpClientSettings = httpClientSettingsDao.findById(Long.valueOf(returnedResource.getId().get()))
                                                                 .orElseThrow();
    Assertions.assertEquals(requestTimeout, httpClientSettings.getRequestTimeout());
    Assertions.assertEquals(connectionTimeout, httpClientSettings.getConnectionTimeout());
    Assertions.assertEquals(socketTimeout, httpClientSettings.getSocketTimeout());
    Assertions.assertEquals(useHostnameVerifier, httpClientSettings.isUseHostnameVerifier());
    Assertions.assertEquals(openIdClient.getId(), httpClientSettings.getOpenIdClient().getId());
    Assertions.assertEquals(proxy.getId(), httpClientSettings.getProxy().getId());
    Assertions.assertEquals(keystoreEntry.getAlias(), httpClientSettings.getTlsClientAuthKeyRef());
  }

  @Test
  public void testCreateSecondChildOfHttpClientSettingsForOpenIdClient()
  {
    final Long requestTimeout = 120L;
    final Long connectionTimeout = 180L;
    final Long socketTimeout = 240L;
    final boolean useHostnameVerifier = true;

    OpenIdClient openIdClient = openIdClientDao.save(createOpenIdClient());
    Proxy proxy = createProxy();

    ScimHttpClientSettings scimHttpClientSettings = ScimHttpClientSettings.builder()
                                                                          .openIdClientReference(openIdClient.getId())
                                                                          .proxyReference(proxy.getId())
                                                                          .requestTimeout(requestTimeout)
                                                                          .connectionTimeout(connectionTimeout)
                                                                          .socketTimeout(socketTimeout)
                                                                          .useHostnameVerifier(useHostnameVerifier)
                                                                          .build();
    ServerResponse<ScimHttpClientSettings> response = scimRequestBuilder.create(ScimHttpClientSettings.class,
                                                                                HTTP_CLIENT_SETTINGS_ENDPOINT)
                                                                        .setResource(scimHttpClientSettings)
                                                                        .sendRequest();
    Assertions.assertEquals(HttpStatus.CREATED, response.getHttpStatus());
    response = scimRequestBuilder.create(ScimHttpClientSettings.class, HTTP_CLIENT_SETTINGS_ENDPOINT)
                                 .setResource(scimHttpClientSettings)
                                 .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();
    String errorMessage = String.format("Cannot create a second child for OpenID Client with ID '%s'",
                                        openIdClient.getId());
    Assertions.assertEquals(errorMessage, errorResponse.getDetail().get());
    Assertions.assertEquals(0, errorResponse.getErrorMessages().size());

    Map<String, List<String>> fieldErrorsMap = errorResponse.getFieldErrors();
    Assertions.assertEquals(1, fieldErrorsMap.size());

    List<String> fieldErrors = fieldErrorsMap.get(ScimHttpClientSettings.FieldNames.OPEN_ID_CLIENT_REFERENCE);
    Assertions.assertEquals(1, fieldErrors.size());
    MatcherAssert.assertThat(fieldErrors, Matchers.containsInAnyOrder(errorMessage));
  }

  @Test
  public void testCreateHttpClientSettingsWithInvalidOpenIdClientReference()
  {
    final Long requestTimeout = 120L;
    final Long connectionTimeout = 180L;
    final Long socketTimeout = 240L;
    final boolean useHostnameVerifier = true;
    final Long invalidClientReference = 9999L;

    Proxy proxy = createProxy();

    ScimHttpClientSettings scimHttpClientSettings = ScimHttpClientSettings.builder()
                                                                          .openIdClientReference(invalidClientReference)
                                                                          .proxyReference(proxy.getId())
                                                                          .requestTimeout(requestTimeout)
                                                                          .connectionTimeout(connectionTimeout)
                                                                          .socketTimeout(socketTimeout)
                                                                          .useHostnameVerifier(useHostnameVerifier)
                                                                          .build();
    ServerResponse<ScimHttpClientSettings> response = scimRequestBuilder.create(ScimHttpClientSettings.class,
                                                                                HTTP_CLIENT_SETTINGS_ENDPOINT)
                                                                        .setResource(scimHttpClientSettings)
                                                                        .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();
    String errorMessage = String.format("No OpenID Client with ID '%s' does exist", invalidClientReference);
    Assertions.assertEquals(errorMessage, errorResponse.getDetail().get());
    Assertions.assertEquals(0, errorResponse.getErrorMessages().size());

    Map<String, List<String>> fieldErrorsMap = errorResponse.getFieldErrors();
    Assertions.assertEquals(1, fieldErrorsMap.size());

    List<String> fieldErrors = fieldErrorsMap.get(ScimHttpClientSettings.FieldNames.OPEN_ID_CLIENT_REFERENCE);
    Assertions.assertEquals(1, fieldErrors.size());
    MatcherAssert.assertThat(fieldErrors, Matchers.containsInAnyOrder(errorMessage));
  }

  @Test
  public void testCreateHttpClientSettingsWithInvalidRequestTimeout()
  {
    final Long requestTimeout = 0L;
    final Long connectionTimeout = 180L;
    final Long socketTimeout = 240L;
    final boolean useHostnameVerifier = true;
    OpenIdClient openIdClient = openIdClientDao.save(createOpenIdClient());
    Proxy proxy = createProxy();

    ScimHttpClientSettings scimHttpClientSettings = ScimHttpClientSettings.builder()
                                                                          .openIdClientReference(openIdClient.getId())
                                                                          .proxyReference(proxy.getId())
                                                                          .requestTimeout(requestTimeout)
                                                                          .connectionTimeout(connectionTimeout)
                                                                          .socketTimeout(socketTimeout)
                                                                          .useHostnameVerifier(useHostnameVerifier)
                                                                          .build();
    ServerResponse<ScimHttpClientSettings> response = scimRequestBuilder.create(ScimHttpClientSettings.class,
                                                                                HTTP_CLIENT_SETTINGS_ENDPOINT)
                                                                        .setResource(scimHttpClientSettings)
                                                                        .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();
    String errorMessage = "The 'INTEGER'-attribute 'requestTimeout' with value '0' must have at least a value of '1'";
    Assertions.assertEquals(errorMessage, errorResponse.getDetail().get());
    Assertions.assertEquals(0, errorResponse.getErrorMessages().size());

    Map<String, List<String>> fieldErrorsMap = errorResponse.getFieldErrors();
    Assertions.assertEquals(1, fieldErrorsMap.size());

    List<String> fieldErrors = fieldErrorsMap.get(ScimHttpClientSettings.FieldNames.REQUEST_TIMEOUT);
    Assertions.assertEquals(1, fieldErrors.size());
    MatcherAssert.assertThat(fieldErrors, Matchers.containsInAnyOrder(errorMessage));
  }

  @Test
  public void testCreateHttpClientSettingsWithInvalidConnectionTimeout()
  {
    final Long requestTimeout = 120L;
    final Long connectionTimeout = 0L;
    final Long socketTimeout = 240L;
    final boolean useHostnameVerifier = true;
    OpenIdClient openIdClient = openIdClientDao.save(createOpenIdClient());
    Proxy proxy = createProxy();

    ScimHttpClientSettings scimHttpClientSettings = ScimHttpClientSettings.builder()
                                                                          .openIdClientReference(openIdClient.getId())
                                                                          .proxyReference(proxy.getId())
                                                                          .requestTimeout(requestTimeout)
                                                                          .connectionTimeout(connectionTimeout)
                                                                          .socketTimeout(socketTimeout)
                                                                          .useHostnameVerifier(useHostnameVerifier)
                                                                          .build();
    ServerResponse<ScimHttpClientSettings> response = scimRequestBuilder.create(ScimHttpClientSettings.class,
                                                                                HTTP_CLIENT_SETTINGS_ENDPOINT)
                                                                        .setResource(scimHttpClientSettings)
                                                                        .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();
    String errorMessage = "The 'INTEGER'-attribute 'connectionTimeout' with value '0' must have at least a value of '1'";
    Assertions.assertEquals(errorMessage, errorResponse.getDetail().get());
    Assertions.assertEquals(0, errorResponse.getErrorMessages().size());

    Map<String, List<String>> fieldErrorsMap = errorResponse.getFieldErrors();
    Assertions.assertEquals(1, fieldErrorsMap.size());

    List<String> fieldErrors = fieldErrorsMap.get(ScimHttpClientSettings.FieldNames.CONNECTION_TIMEOUT);
    Assertions.assertEquals(1, fieldErrors.size());
    MatcherAssert.assertThat(fieldErrors, Matchers.containsInAnyOrder(errorMessage));
  }

  @Test
  public void testCreateHttpClientSettingsWithInvalidSocketTimeout()
  {
    final Long requestTimeout = 120L;
    final Long connectionTimeout = 180L;
    final Long socketTimeout = 0L;
    final boolean useHostnameVerifier = true;
    OpenIdClient openIdClient = openIdClientDao.save(createOpenIdClient());
    Proxy proxy = createProxy();

    ScimHttpClientSettings scimHttpClientSettings = ScimHttpClientSettings.builder()
                                                                          .openIdClientReference(openIdClient.getId())
                                                                          .proxyReference(proxy.getId())
                                                                          .requestTimeout(requestTimeout)
                                                                          .connectionTimeout(connectionTimeout)
                                                                          .socketTimeout(socketTimeout)
                                                                          .useHostnameVerifier(useHostnameVerifier)
                                                                          .build();
    ServerResponse<ScimHttpClientSettings> response = scimRequestBuilder.create(ScimHttpClientSettings.class,
                                                                                HTTP_CLIENT_SETTINGS_ENDPOINT)
                                                                        .setResource(scimHttpClientSettings)
                                                                        .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();
    String errorMessage = "The 'INTEGER'-attribute 'socketTimeout' with value '0' must have at least a value of '1'";
    Assertions.assertEquals(errorMessage, errorResponse.getDetail().get());
    Assertions.assertEquals(0, errorResponse.getErrorMessages().size());

    Map<String, List<String>> fieldErrorsMap = errorResponse.getFieldErrors();
    Assertions.assertEquals(1, fieldErrorsMap.size());

    List<String> fieldErrors = fieldErrorsMap.get(ScimHttpClientSettings.FieldNames.SOCKET_TIMEOUT);
    Assertions.assertEquals(1, fieldErrors.size());
    MatcherAssert.assertThat(fieldErrors, Matchers.containsInAnyOrder(errorMessage));
  }

  @Test
  public void testCreateHttpClientSettingsWithInvalidProxyReference()
  {
    final Long requestTimeout = 120L;
    final Long connectionTimeout = 180L;
    final Long socketTimeout = 240L;
    final boolean useHostnameVerifier = true;
    OpenIdClient openIdClient = openIdClientDao.save(createOpenIdClient());
    final Long invalidProxyReference = 9999L;

    ScimHttpClientSettings scimHttpClientSettings = ScimHttpClientSettings.builder()
                                                                          .openIdClientReference(openIdClient.getId())
                                                                          .proxyReference(invalidProxyReference)
                                                                          .requestTimeout(requestTimeout)
                                                                          .connectionTimeout(connectionTimeout)
                                                                          .socketTimeout(socketTimeout)
                                                                          .useHostnameVerifier(useHostnameVerifier)
                                                                          .build();
    ServerResponse<ScimHttpClientSettings> response = scimRequestBuilder.create(ScimHttpClientSettings.class,
                                                                                HTTP_CLIENT_SETTINGS_ENDPOINT)
                                                                        .setResource(scimHttpClientSettings)
                                                                        .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();
    String errorMessage = String.format("No Proxy with ID '%s' does exist", invalidProxyReference);
    Assertions.assertEquals(errorMessage, errorResponse.getDetail().get());
    Assertions.assertEquals(0, errorResponse.getErrorMessages().size());

    Map<String, List<String>> fieldErrorsMap = errorResponse.getFieldErrors();
    Assertions.assertEquals(1, fieldErrorsMap.size());

    List<String> fieldErrors = fieldErrorsMap.get(ScimHttpClientSettings.FieldNames.PROXY_REFERENCE);
    Assertions.assertEquals(1, fieldErrors.size());
    MatcherAssert.assertThat(fieldErrors, Matchers.containsInAnyOrder(errorMessage));
  }

  @Test
  public void testCreateHttpClientSettingsWithInvalidKeyAliasReference()
  {
    final Long requestTimeout = 120L;
    final Long connectionTimeout = 180L;
    final Long socketTimeout = 240L;
    final boolean useHostnameVerifier = true;
    OpenIdClient openIdClient = openIdClientDao.save(createOpenIdClient());
    final String invalidKeyAlias = "invalid-alias";

    ScimHttpClientSettings scimHttpClientSettings = ScimHttpClientSettings.builder()
                                                                          .openIdClientReference(openIdClient.getId())
                                                                          .requestTimeout(requestTimeout)
                                                                          .connectionTimeout(connectionTimeout)
                                                                          .socketTimeout(socketTimeout)
                                                                          .useHostnameVerifier(useHostnameVerifier)
                                                                          .tlsClientAuthAliasReference(invalidKeyAlias)
                                                                          .build();
    ServerResponse<ScimHttpClientSettings> response = scimRequestBuilder.create(ScimHttpClientSettings.class,
                                                                                HTTP_CLIENT_SETTINGS_ENDPOINT)
                                                                        .setResource(scimHttpClientSettings)
                                                                        .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();
    String errorMessage = String.format("Alias '%s' does not exist within application keystore", invalidKeyAlias);
    Assertions.assertEquals(errorMessage, errorResponse.getDetail().get());
    Assertions.assertEquals(0, errorResponse.getErrorMessages().size());

    Map<String, List<String>> fieldErrorsMap = errorResponse.getFieldErrors();
    Assertions.assertEquals(1, fieldErrorsMap.size());

    List<String> fieldErrors = fieldErrorsMap.get(ScimHttpClientSettings.FieldNames.TLS_CLIENT_AUTH_ALIAS_REFERENCE);
    Assertions.assertEquals(1, fieldErrors.size());
    MatcherAssert.assertThat(fieldErrors, Matchers.containsInAnyOrder(errorMessage));
  }

  @Test
  public void testGetHttpClientSettings()
  {
    final Integer requestTimeout = 120;
    final Integer connectionTimeout = 180;
    final Integer socketTimeout = 240;
    final boolean useHostnameVerifier = true;
    OpenIdClient openIdClient = openIdClientDao.save(createOpenIdClient());
    Proxy proxy = createProxy();

    HttpClientSettings httpClientSettings = HttpClientSettings.builder()
                                                              .openIdClient(openIdClient)
                                                              .proxy(proxy)
                                                              .requestTimeout(requestTimeout)
                                                              .connectionTimeout(connectionTimeout)
                                                              .socketTimeout(socketTimeout)
                                                              .useHostnameVerifier(useHostnameVerifier)
                                                              .build();
    httpClientSettings = httpClientSettingsDao.save(httpClientSettings);

    ServerResponse<ScimHttpClientSettings> response = scimRequestBuilder.get(ScimHttpClientSettings.class,
                                                                             HTTP_CLIENT_SETTINGS_ENDPOINT,
                                                                             String.valueOf(httpClientSettings.getId()))
                                                                        .sendRequest();
    Assertions.assertEquals(HttpStatus.OK, response.getHttpStatus());
    ScimHttpClientSettings returnedResource = response.getResource();
    Assertions.assertEquals((long)requestTimeout, returnedResource.getRequestTimeout().get());
    Assertions.assertEquals((long)connectionTimeout, returnedResource.getConnectionTimeout().get());
    Assertions.assertEquals((long)socketTimeout, returnedResource.getSocketTimeout().get());
    Assertions.assertEquals(useHostnameVerifier, returnedResource.getUseHostnameVerifier().get());
    Assertions.assertEquals(openIdClient.getId(), returnedResource.getOpenIdClientReference().get());
    Assertions.assertEquals(proxy.getId(), returnedResource.getProxyReference().get());
  }

  @Test
  public void testGetNoneExistingResource()
  {
    final String invalidId = "99999";
    ServerResponse<ScimHttpClientSettings> response = scimRequestBuilder.get(ScimHttpClientSettings.class,
                                                                             HTTP_CLIENT_SETTINGS_ENDPOINT,
                                                                             invalidId)
                                                                        .sendRequest();
    Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();
    String errorMessage = String.format("Resource with ID '%s' does not exist", invalidId);
    Assertions.assertEquals(errorMessage, errorResponse.getDetail().get());
  }

  @Test
  public void testListResource()
  {
    ServerResponse<ListResponse<ScimHttpClientSettings>> response = scimRequestBuilder.list(ScimHttpClientSettings.class,
                                                                                            HTTP_CLIENT_SETTINGS_ENDPOINT)
                                                                                      .get()
                                                                                      .sendRequest();
    Assertions.assertEquals(HttpStatus.NOT_IMPLEMENTED, response.getHttpStatus());
  }

  @Test
  public void testDeleteResource()
  {
    final Integer requestTimeout = 120;
    final Integer connectionTimeout = 180;
    final Integer socketTimeout = 240;
    final boolean useHostnameVerifier = true;
    OpenIdClient openIdClient = openIdClientDao.save(createOpenIdClient());
    Proxy proxy = createProxy();

    HttpClientSettings httpClientSettings = HttpClientSettings.builder()
                                                              .openIdClient(openIdClient)
                                                              .proxy(proxy)
                                                              .requestTimeout(requestTimeout)
                                                              .connectionTimeout(connectionTimeout)
                                                              .socketTimeout(socketTimeout)
                                                              .useHostnameVerifier(useHostnameVerifier)
                                                              .build();
    httpClientSettings = httpClientSettingsDao.save(httpClientSettings);

    ServerResponse<ScimHttpClientSettings> response = scimRequestBuilder.delete(ScimHttpClientSettings.class,
                                                                                HTTP_CLIENT_SETTINGS_ENDPOINT,
                                                                                String.valueOf(httpClientSettings.getId()))
                                                                        .sendRequest();
    Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getHttpStatus());
    Assertions.assertEquals(0, httpClientSettingsDao.count());
  }

  @Test
  public void testDeleteNoneExistingResource()
  {
    final String invalidId = "99999";
    ServerResponse<ScimHttpClientSettings> response = scimRequestBuilder.delete(ScimHttpClientSettings.class,
                                                                                HTTP_CLIENT_SETTINGS_ENDPOINT,
                                                                                invalidId)
                                                                        .sendRequest();
    Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();
    String errorMessage = String.format("Resource with ID '%s' does not exist", invalidId);
    Assertions.assertEquals(errorMessage, errorResponse.getDetail().get());
  }

  @Test
  public void testUpdateHttpClientSettings()
  {
    final Integer requestTimeout = 120;
    final Integer connectionTimeout = 180;
    final Integer socketTimeout = 240;
    final boolean useHostnameVerifier = true;
    OpenIdClient openIdClient = openIdClientDao.save(createOpenIdClient());
    Proxy proxy = createProxy();

    HttpClientSettings httpClientSettings = HttpClientSettings.builder()
                                                              .openIdClient(openIdClient)
                                                              .proxy(proxy)
                                                              .requestTimeout(requestTimeout)
                                                              .connectionTimeout(connectionTimeout)
                                                              .socketTimeout(socketTimeout)
                                                              .useHostnameVerifier(useHostnameVerifier)
                                                              .build();
    httpClientSettings = httpClientSettingsDao.save(httpClientSettings);


    final Long newRequestTimeout = 120L;
    final Long newConnectionTimeout = 180L;
    final Long newSocketTimeout = 240L;
    final boolean newUseHostnameVerifier = false;
    Proxy newProxy = createProxy();

    ScimHttpClientSettings scimHttpClientSettings = ScimHttpClientSettings.builder()
                                                                          .openIdClientReference(openIdClient.getId())
                                                                          .proxyReference(newProxy.getId())
                                                                          .requestTimeout(newRequestTimeout)
                                                                          .connectionTimeout(newConnectionTimeout)
                                                                          .socketTimeout(newSocketTimeout)
                                                                          .useHostnameVerifier(newUseHostnameVerifier)
                                                                          .build();

    ServerResponse<ScimHttpClientSettings> response = scimRequestBuilder.update(ScimHttpClientSettings.class,
                                                                                HTTP_CLIENT_SETTINGS_ENDPOINT,
                                                                                String.valueOf(httpClientSettings.getId()))
                                                                        .setResource(scimHttpClientSettings)
                                                                        .sendRequest();
    Assertions.assertEquals(HttpStatus.OK, response.getHttpStatus());
    ScimHttpClientSettings returnedResource = response.getResource();
    Assertions.assertEquals(newRequestTimeout, returnedResource.getRequestTimeout().get());
    Assertions.assertEquals(newConnectionTimeout, returnedResource.getConnectionTimeout().get());
    Assertions.assertEquals(newSocketTimeout, returnedResource.getSocketTimeout().get());
    Assertions.assertEquals(newUseHostnameVerifier, returnedResource.getUseHostnameVerifier().get());
    Assertions.assertEquals(openIdClient.getId(), returnedResource.getOpenIdClientReference().get());
    Assertions.assertEquals(newProxy.getId(), returnedResource.getProxyReference().get());

    Assertions.assertEquals(1, httpClientSettingsDao.count());
    httpClientSettings = httpClientSettingsDao.findById(Long.valueOf(returnedResource.getId().get())).orElseThrow();
    Assertions.assertEquals(newRequestTimeout, httpClientSettings.getRequestTimeout());
    Assertions.assertEquals(newConnectionTimeout, httpClientSettings.getConnectionTimeout());
    Assertions.assertEquals(newSocketTimeout, httpClientSettings.getSocketTimeout());
    Assertions.assertEquals(newUseHostnameVerifier, httpClientSettings.isUseHostnameVerifier());
    Assertions.assertEquals(openIdClient.getId(), httpClientSettings.getOpenIdClient().getId());
    Assertions.assertEquals(newProxy.getId(), httpClientSettings.getProxy().getId());
  }

  @Test
  public void testUpdateHttpClientSettingsAndRemoveProxyReference()
  {
    final Integer requestTimeout = 120;
    final Integer connectionTimeout = 180;
    final Integer socketTimeout = 240;
    final boolean useHostnameVerifier = true;
    OpenIdClient openIdClient = openIdClientDao.save(createOpenIdClient());
    Proxy proxy = createProxy();

    HttpClientSettings httpClientSettings = HttpClientSettings.builder()
                                                              .openIdClient(openIdClient)
                                                              .proxy(proxy)
                                                              .requestTimeout(requestTimeout)
                                                              .connectionTimeout(connectionTimeout)
                                                              .socketTimeout(socketTimeout)
                                                              .useHostnameVerifier(useHostnameVerifier)
                                                              .build();
    httpClientSettings = httpClientSettingsDao.save(httpClientSettings);


    final Long newRequestTimeout = 120L;
    final Long newConnectionTimeout = 180L;
    final Long newSocketTimeout = 240L;
    final boolean newUseHostnameVerifier = false;

    ScimHttpClientSettings scimHttpClientSettings = ScimHttpClientSettings.builder()
                                                                          .openIdClientReference(openIdClient.getId())
                                                                          .requestTimeout(newRequestTimeout)
                                                                          .connectionTimeout(newConnectionTimeout)
                                                                          .socketTimeout(newSocketTimeout)
                                                                          .useHostnameVerifier(newUseHostnameVerifier)
                                                                          .build();

    ServerResponse<ScimHttpClientSettings> response = scimRequestBuilder.update(ScimHttpClientSettings.class,
                                                                                HTTP_CLIENT_SETTINGS_ENDPOINT,
                                                                                String.valueOf(httpClientSettings.getId()))
                                                                        .setResource(scimHttpClientSettings)
                                                                        .sendRequest();
    Assertions.assertEquals(HttpStatus.OK, response.getHttpStatus());
    ScimHttpClientSettings returnedResource = response.getResource();
    Assertions.assertFalse(returnedResource.getProxyReference().isPresent());

    Assertions.assertEquals(1, httpClientSettingsDao.count());
    httpClientSettings = httpClientSettingsDao.findById(Long.valueOf(returnedResource.getId().get())).orElseThrow();
    Assertions.assertNull(httpClientSettings.getProxy());
  }

  @Test
  public void testUpdateWithInvalidRequestTimeout()
  {
    final Integer requestTimeout = 120;
    final Integer connectionTimeout = 180;
    final Integer socketTimeout = 240;
    final boolean useHostnameVerifier = true;
    OpenIdClient openIdClient = openIdClientDao.save(createOpenIdClient());
    Proxy proxy = createProxy();

    HttpClientSettings httpClientSettings = HttpClientSettings.builder()
                                                              .openIdClient(openIdClient)
                                                              .proxy(proxy)
                                                              .requestTimeout(requestTimeout)
                                                              .connectionTimeout(connectionTimeout)
                                                              .socketTimeout(socketTimeout)
                                                              .useHostnameVerifier(useHostnameVerifier)
                                                              .build();
    httpClientSettings = httpClientSettingsDao.save(httpClientSettings);

    final Long newRequestTimeout = 0L;
    final Long newConnectionTimeout = 180L;
    final Long newSocketTimeout = 240L;
    final boolean newUseHostnameVerifier = false;
    Proxy newProxy = createProxy();


    ScimHttpClientSettings scimHttpClientSettings = ScimHttpClientSettings.builder()
                                                                          .openIdClientReference(openIdClient.getId())
                                                                          .proxyReference(newProxy.getId())
                                                                          .requestTimeout(newRequestTimeout)
                                                                          .connectionTimeout(newConnectionTimeout)
                                                                          .socketTimeout(newSocketTimeout)
                                                                          .useHostnameVerifier(newUseHostnameVerifier)
                                                                          .build();

    ServerResponse<ScimHttpClientSettings> response = scimRequestBuilder.update(ScimHttpClientSettings.class,
                                                                                HTTP_CLIENT_SETTINGS_ENDPOINT,
                                                                                String.valueOf(httpClientSettings.getId()))
                                                                        .setResource(scimHttpClientSettings)
                                                                        .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();
    String errorMessage = "The 'INTEGER'-attribute 'requestTimeout' with value '0' must have at least a value of '1'";
    Assertions.assertEquals(errorMessage, errorResponse.getDetail().get());
    Assertions.assertEquals(0, errorResponse.getErrorMessages().size());

    Map<String, List<String>> fieldErrorsMap = errorResponse.getFieldErrors();
    Assertions.assertEquals(1, fieldErrorsMap.size());

    List<String> fieldErrors = fieldErrorsMap.get(ScimHttpClientSettings.FieldNames.REQUEST_TIMEOUT);
    Assertions.assertEquals(1, fieldErrors.size());
    MatcherAssert.assertThat(fieldErrors, Matchers.containsInAnyOrder(errorMessage));
  }

  @Test
  public void testUpdateWithInvalidConnectionTimeout()
  {
    final Integer requestTimeout = 120;
    final Integer connectionTimeout = 180;
    final Integer socketTimeout = 240;
    final boolean useHostnameVerifier = true;
    OpenIdClient openIdClient = openIdClientDao.save(createOpenIdClient());
    Proxy proxy = createProxy();

    HttpClientSettings httpClientSettings = HttpClientSettings.builder()
                                                              .openIdClient(openIdClient)
                                                              .proxy(proxy)
                                                              .requestTimeout(requestTimeout)
                                                              .connectionTimeout(connectionTimeout)
                                                              .socketTimeout(socketTimeout)
                                                              .useHostnameVerifier(useHostnameVerifier)
                                                              .build();
    httpClientSettings = httpClientSettingsDao.save(httpClientSettings);


    final Long newRequestTimeout = 120L;
    final Long newConnectionTimeout = 0L;
    final Long newSocketTimeout = 240L;
    final boolean newUseHostnameVerifier = false;
    Proxy newProxy = createProxy();

    ScimHttpClientSettings scimHttpClientSettings = ScimHttpClientSettings.builder()
                                                                          .openIdClientReference(openIdClient.getId())
                                                                          .proxyReference(newProxy.getId())
                                                                          .requestTimeout(newRequestTimeout)
                                                                          .connectionTimeout(newConnectionTimeout)
                                                                          .socketTimeout(newSocketTimeout)
                                                                          .useHostnameVerifier(newUseHostnameVerifier)
                                                                          .build();

    ServerResponse<ScimHttpClientSettings> response = scimRequestBuilder.update(ScimHttpClientSettings.class,
                                                                                HTTP_CLIENT_SETTINGS_ENDPOINT,
                                                                                String.valueOf(httpClientSettings.getId()))
                                                                        .setResource(scimHttpClientSettings)
                                                                        .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();
    String errorMessage = "The 'INTEGER'-attribute 'connectionTimeout' with value '0' must have at least a value of '1'";
    Assertions.assertEquals(errorMessage, errorResponse.getDetail().get());
    Assertions.assertEquals(0, errorResponse.getErrorMessages().size());

    Map<String, List<String>> fieldErrorsMap = errorResponse.getFieldErrors();
    Assertions.assertEquals(1, fieldErrorsMap.size());

    List<String> fieldErrors = fieldErrorsMap.get(ScimHttpClientSettings.FieldNames.CONNECTION_TIMEOUT);
    Assertions.assertEquals(1, fieldErrors.size());
    MatcherAssert.assertThat(fieldErrors, Matchers.containsInAnyOrder(errorMessage));
  }

  @Test
  public void testUpdateWithInvalidSocketTimeout()
  {
    final Integer requestTimeout = 120;
    final Integer connectionTimeout = 180;
    final Integer socketTimeout = 240;
    final boolean useHostnameVerifier = true;
    OpenIdClient openIdClient = openIdClientDao.save(createOpenIdClient());
    Proxy proxy = createProxy();

    HttpClientSettings httpClientSettings = HttpClientSettings.builder()
                                                              .openIdClient(openIdClient)
                                                              .proxy(proxy)
                                                              .requestTimeout(requestTimeout)
                                                              .connectionTimeout(connectionTimeout)
                                                              .socketTimeout(socketTimeout)
                                                              .useHostnameVerifier(useHostnameVerifier)
                                                              .build();
    httpClientSettings = httpClientSettingsDao.save(httpClientSettings);


    final Long newRequestTimeout = 120L;
    final Long newConnectionTimeout = 180L;
    final Long newSocketTimeout = 0L;
    final boolean newUseHostnameVerifier = false;
    Proxy newProxy = createProxy();

    ScimHttpClientSettings scimHttpClientSettings = ScimHttpClientSettings.builder()
                                                                          .openIdClientReference(openIdClient.getId())
                                                                          .proxyReference(newProxy.getId())
                                                                          .requestTimeout(newRequestTimeout)
                                                                          .connectionTimeout(newConnectionTimeout)
                                                                          .socketTimeout(newSocketTimeout)
                                                                          .useHostnameVerifier(newUseHostnameVerifier)
                                                                          .build();

    ServerResponse<ScimHttpClientSettings> response = scimRequestBuilder.update(ScimHttpClientSettings.class,
                                                                                HTTP_CLIENT_SETTINGS_ENDPOINT,
                                                                                String.valueOf(httpClientSettings.getId()))
                                                                        .setResource(scimHttpClientSettings)
                                                                        .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();
    String errorMessage = "The 'INTEGER'-attribute 'socketTimeout' with value '0' must have at least a value of '1'";
    Assertions.assertEquals(errorMessage, errorResponse.getDetail().get());
    Assertions.assertEquals(0, errorResponse.getErrorMessages().size());

    Map<String, List<String>> fieldErrorsMap = errorResponse.getFieldErrors();
    Assertions.assertEquals(1, fieldErrorsMap.size());

    List<String> fieldErrors = fieldErrorsMap.get(ScimHttpClientSettings.FieldNames.SOCKET_TIMEOUT);
    Assertions.assertEquals(1, fieldErrors.size());
    MatcherAssert.assertThat(fieldErrors, Matchers.containsInAnyOrder(errorMessage));
  }

  @Test
  public void testUpdateNoneExistingResource()
  {
    final Integer requestTimeout = 120;
    final Integer connectionTimeout = 180;
    final Integer socketTimeout = 240;
    final boolean useHostnameVerifier = true;
    OpenIdClient openIdClient = openIdClientDao.save(createOpenIdClient());
    Proxy proxy = createProxy();

    HttpClientSettings httpClientSettings = HttpClientSettings.builder()
                                                              .openIdClient(openIdClient)
                                                              .proxy(proxy)
                                                              .requestTimeout(requestTimeout)
                                                              .connectionTimeout(connectionTimeout)
                                                              .socketTimeout(socketTimeout)
                                                              .useHostnameVerifier(useHostnameVerifier)
                                                              .build();
    httpClientSettingsDao.save(httpClientSettings);


    final Long newRequestTimeout = 120L;
    final Long newConnectionTimeout = 180L;
    final Long newSocketTimeout = 240L;
    final boolean newUseHostnameVerifier = false;
    Proxy newProxy = createProxy();

    ScimHttpClientSettings scimHttpClientSettings = ScimHttpClientSettings.builder()
                                                                          .openIdClientReference(openIdClient.getId())
                                                                          .proxyReference(newProxy.getId())
                                                                          .requestTimeout(newRequestTimeout)
                                                                          .connectionTimeout(newConnectionTimeout)
                                                                          .socketTimeout(newSocketTimeout)
                                                                          .useHostnameVerifier(newUseHostnameVerifier)
                                                                          .build();

    ServerResponse<ScimHttpClientSettings> response = scimRequestBuilder.update(ScimHttpClientSettings.class,
                                                                                HTTP_CLIENT_SETTINGS_ENDPOINT,
                                                                                "9999")
                                                                        .setResource(scimHttpClientSettings)
                                                                        .sendRequest();
    Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();
    String errorMessage = "Resource with ID '9999' does not exist";
    Assertions.assertEquals(errorMessage, errorResponse.getDetail().get());
  }

  @Test
  public void testChangeParentReference()
  {
    final Integer requestTimeout = 120;
    final Integer connectionTimeout = 180;
    final Integer socketTimeout = 240;
    final boolean useHostnameVerifier = true;
    OpenIdClient openIdClient = openIdClientDao.save(createOpenIdClient());
    Proxy proxy = createProxy();

    HttpClientSettings httpClientSettings = HttpClientSettings.builder()
                                                              .openIdClient(openIdClient)
                                                              .proxy(proxy)
                                                              .requestTimeout(requestTimeout)
                                                              .connectionTimeout(connectionTimeout)
                                                              .socketTimeout(socketTimeout)
                                                              .useHostnameVerifier(useHostnameVerifier)
                                                              .build();
    httpClientSettings = httpClientSettingsDao.save(httpClientSettings);


    final Long newRequestTimeout = 120L;
    final Long newConnectionTimeout = 180L;
    final Long newSocketTimeout = 240L;
    final boolean newUseHostnameVerifier = false;
    Proxy newProxy = createProxy();
    OpenIdClient newOpenIdClient = openIdClientDao.save(createOpenIdClient());

    ScimHttpClientSettings scimHttpClientSettings = ScimHttpClientSettings.builder()
                                                                          .openIdClientReference(newOpenIdClient.getId())
                                                                          .proxyReference(newProxy.getId())
                                                                          .requestTimeout(newRequestTimeout)
                                                                          .connectionTimeout(newConnectionTimeout)
                                                                          .socketTimeout(newSocketTimeout)
                                                                          .useHostnameVerifier(newUseHostnameVerifier)
                                                                          .build();

    ServerResponse<ScimHttpClientSettings> response = scimRequestBuilder.update(ScimHttpClientSettings.class,
                                                                                HTTP_CLIENT_SETTINGS_ENDPOINT,
                                                                                String.valueOf(httpClientSettings.getId()))
                                                                        .setResource(scimHttpClientSettings)
                                                                        .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();
    String errorMessage = "The parent reference to the OpenID Client must not be changed";
    Assertions.assertEquals(errorMessage, errorResponse.getDetail().get());
    Assertions.assertEquals(0, errorResponse.getErrorMessages().size());

    Map<String, List<String>> fieldErrorsMap = errorResponse.getFieldErrors();
    Assertions.assertEquals(1, fieldErrorsMap.size());

    List<String> fieldErrors = fieldErrorsMap.get(ScimHttpClientSettings.FieldNames.OPEN_ID_CLIENT_REFERENCE);
    Assertions.assertEquals(1, fieldErrors.size());
    MatcherAssert.assertThat(fieldErrors, Matchers.containsInAnyOrder(errorMessage));
  }

  @Test
  public void testUpdateWithInvalidProxy()
  {
    final Integer requestTimeout = 120;
    final Integer connectionTimeout = 180;
    final Integer socketTimeout = 240;
    final boolean useHostnameVerifier = true;
    OpenIdClient openIdClient = openIdClientDao.save(createOpenIdClient());
    Proxy proxy = createProxy();

    HttpClientSettings httpClientSettings = HttpClientSettings.builder()
                                                              .openIdClient(openIdClient)
                                                              .proxy(proxy)
                                                              .requestTimeout(requestTimeout)
                                                              .connectionTimeout(connectionTimeout)
                                                              .socketTimeout(socketTimeout)
                                                              .useHostnameVerifier(useHostnameVerifier)
                                                              .build();
    httpClientSettings = httpClientSettingsDao.save(httpClientSettings);

    final Long newRequestTimeout = 120L;
    final Long newConnectionTimeout = 180L;
    final Long newSocketTimeout = 240L;
    final boolean newUseHostnameVerifier = false;
    final Long invalidProxyReference = 9999L;

    ScimHttpClientSettings scimHttpClientSettings = ScimHttpClientSettings.builder()
                                                                          .openIdClientReference(openIdClient.getId())
                                                                          .proxyReference(invalidProxyReference)
                                                                          .requestTimeout(newRequestTimeout)
                                                                          .connectionTimeout(newConnectionTimeout)
                                                                          .socketTimeout(newSocketTimeout)
                                                                          .useHostnameVerifier(newUseHostnameVerifier)
                                                                          .build();

    ServerResponse<ScimHttpClientSettings> response = scimRequestBuilder.update(ScimHttpClientSettings.class,
                                                                                HTTP_CLIENT_SETTINGS_ENDPOINT,
                                                                                String.valueOf(httpClientSettings.getId()))
                                                                        .setResource(scimHttpClientSettings)
                                                                        .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();
    String errorMessage = String.format("No Proxy with ID '%s' does exist", invalidProxyReference);
    Assertions.assertEquals(errorMessage, errorResponse.getDetail().get());
    Assertions.assertEquals(0, errorResponse.getErrorMessages().size());

    Map<String, List<String>> fieldErrorsMap = errorResponse.getFieldErrors();
    Assertions.assertEquals(1, fieldErrorsMap.size());

    List<String> fieldErrors = fieldErrorsMap.get(ScimHttpClientSettings.FieldNames.PROXY_REFERENCE);
    Assertions.assertEquals(1, fieldErrors.size());
    MatcherAssert.assertThat(fieldErrors, Matchers.containsInAnyOrder(errorMessage));
  }

  @Test
  public void testUpdateWithInvalidTlsAuthReference()
  {
    final Integer requestTimeout = 120;
    final Integer connectionTimeout = 180;
    final Integer socketTimeout = 240;
    final boolean useHostnameVerifier = true;
    OpenIdClient openIdClient = openIdClientDao.save(createOpenIdClient());
    Proxy proxy = createProxy();

    HttpClientSettings httpClientSettings = HttpClientSettings.builder()
                                                              .openIdClient(openIdClient)
                                                              .proxy(proxy)
                                                              .requestTimeout(requestTimeout)
                                                              .connectionTimeout(connectionTimeout)
                                                              .socketTimeout(socketTimeout)
                                                              .useHostnameVerifier(useHostnameVerifier)
                                                              .build();
    httpClientSettings = httpClientSettingsDao.save(httpClientSettings);

    final Long newRequestTimeout = 120L;
    final Long newConnectionTimeout = 180L;
    final Long newSocketTimeout = 240L;
    final boolean newUseHostnameVerifier = false;
    final String invalidKeyAlias = "unknown-alias";

    ScimHttpClientSettings scimHttpClientSettings = ScimHttpClientSettings.builder()
                                                                          .openIdClientReference(openIdClient.getId())
                                                                          .proxyReference(proxy.getId())
                                                                          .requestTimeout(newRequestTimeout)
                                                                          .connectionTimeout(newConnectionTimeout)
                                                                          .socketTimeout(newSocketTimeout)
                                                                          .useHostnameVerifier(newUseHostnameVerifier)
                                                                          .tlsClientAuthAliasReference(invalidKeyAlias)
                                                                          .build();

    ServerResponse<ScimHttpClientSettings> response = scimRequestBuilder.update(ScimHttpClientSettings.class,
                                                                                HTTP_CLIENT_SETTINGS_ENDPOINT,
                                                                                String.valueOf(httpClientSettings.getId()))
                                                                        .setResource(scimHttpClientSettings)
                                                                        .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();
    String errorMessage = String.format("Alias '%s' does not exist within application keystore", invalidKeyAlias);
    Assertions.assertEquals(errorMessage, errorResponse.getDetail().get());
    Assertions.assertEquals(0, errorResponse.getErrorMessages().size());

    Map<String, List<String>> fieldErrorsMap = errorResponse.getFieldErrors();
    Assertions.assertEquals(1, fieldErrorsMap.size());

    List<String> fieldErrors = fieldErrorsMap.get(ScimHttpClientSettings.FieldNames.TLS_CLIENT_AUTH_ALIAS_REFERENCE);
    Assertions.assertEquals(1, fieldErrors.size());
    MatcherAssert.assertThat(fieldErrors, Matchers.containsInAnyOrder(errorMessage));
  }

  private Proxy createProxy()
  {
    return proxyDao.save(Proxy.builder().host("localhost").build());
  }

  private OpenIdClient createOpenIdClient()
  {
    OpenIdProvider openIdProvider = createProvider();
    return OpenIdClient.builder()
                       .clientId(UUID.randomUUID().toString())
                       .clientSecret(UUID.randomUUID().toString())
                       .openIdProvider(openIdProvider)
                       .build();
  }

  private OpenIdProvider createProvider()
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
  private KeystoreEntry addDefaultEntriesToApplicationKeystore(String alias)
  {
    KeystoreEntry keystoreEntry = getUnitTestKeystoreEntryAccess(alias);

    KeystoreHandler keystoreHandler = (KeystoreHandler)keystoreResourceType.getResourceHandlerImpl();

    String b64Keystore = Base64.getEncoder().encodeToString(readAsBytes(UNIT_TEST_KEYSTORE_JKS));
    ScimKeystore.FileUpload fileUpload = ScimKeystore.FileUpload.builder()
                                                                .keystoreFile(b64Keystore)
                                                                .keystoreFileName("test.jks")
                                                                .keystorePassword(UNIT_TEST_KEYSTORE_PASSWORD)
                                                                .build();
    ScimKeystore uploadResponse = keystoreHandler.handleKeystoreUpload(fileUpload);

    ScimKeystore.AliasSelection aliasSelection = ScimKeystore.AliasSelection.builder()
                                                                            .stateId(uploadResponse.getAliasSelection()
                                                                                                   .getStateId())
                                                                            .aliases(Collections.singletonList(keystoreEntry.getAlias()))
                                                                            .privateKeyPassword(keystoreEntry.getPrivateKeyPassword())
                                                                            .build();
    keystoreHandler.handleAliasSelection(aliasSelection);
    return keystoreEntry;
  }
}
