package de.captaingoldfish.restclient.application.endpoints.httprequests;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import de.captaingoldfish.restclient.application.endpoints.proxy.ProxyHandler;
import de.captaingoldfish.restclient.application.setup.AbstractScimClientConfig;
import de.captaingoldfish.restclient.application.setup.OAuthRestClientTest;
import de.captaingoldfish.restclient.application.utils.Utils;
import de.captaingoldfish.restclient.database.entities.HttpRequest;
import de.captaingoldfish.restclient.database.entities.HttpRequestGroup;
import de.captaingoldfish.restclient.scim.resources.ScimHttpClientSettings;
import de.captaingoldfish.restclient.scim.resources.ScimHttpRequest;
import de.captaingoldfish.restclient.scim.resources.ScimProxy;
import de.captaingoldfish.scim.sdk.client.response.ServerResponse;
import de.captaingoldfish.scim.sdk.common.constants.HttpHeader;
import de.captaingoldfish.scim.sdk.common.constants.HttpStatus;
import de.captaingoldfish.scim.sdk.server.endpoints.ResourceEndpoint;
import de.captaingoldfish.scim.sdk.server.schemas.ResourceType;
import lombok.extern.slf4j.Slf4j;


/**
 * author Pascal Knueppel <br>
 * created at: 13.09.2022 - 16:16 <br>
 * <br>
 */
@Slf4j
@OAuthRestClientTest
public class HttpRequestHandlerTest extends AbstractScimClientConfig
{

  /**
   * the scim endpoint for managing http requests
   */
  private static final String HTTP_REQUESTS_ENDPOINT = "/HttpRequests";

  private HttpRequestGroup group;

  @Autowired
  private ResourceEndpoint resourceEndpoint;

  private ProxyHandler originalProxyHandler;

  private ProxyHandler spiedProxyHandler;

  @BeforeEach
  public void initialize()
  {
    group = HttpRequestGroup.builder().name("keycloak").created(Instant.now()).lastModified(Instant.now()).build();
    httpRequestCategoriesDao.save(group);

    ResourceType resourceType = resourceEndpoint.getResourceTypeByName("Proxy").get();
    originalProxyHandler = (ProxyHandler)resourceType.getResourceHandlerImpl();
    spiedProxyHandler = Mockito.spy(originalProxyHandler);
    resourceType.setResourceHandlerImpl(spiedProxyHandler);
  }

  @AfterEach
  public void rollback()
  {
    ResourceType resourceType = resourceEndpoint.getResourceTypeByName("Proxy").get();
    resourceType.setResourceHandlerImpl(originalProxyHandler);
  }

  /**
   * verifies that the request will fail if no group name is present
   */
  @Test
  public void testCreateRequestWithoutGroupName()
  {
    String proxyEndpoint = getScimApplicationUrl("/Proxy");
    ScimProxy scimProxy = ScimProxy.builder().hostname("localhost").port(8888).build();

    ScimHttpRequest scimHttpRequest = ScimHttpRequest.builder()
                                                     .name("create-client")
                                                     .url(proxyEndpoint)
                                                     .requestHeaders(getRequestHeaders())
                                                     .requestBody(scimProxy.toString())
                                                     .httpMethod("POST")
                                                     .scimHttpClientSettings(ScimHttpClientSettings.builder()
                                                                                                   .requestTimeout(5L)
                                                                                                   .socketTimeout(5L)
                                                                                                   .connectionTimeout(5L)
                                                                                                   .useHostnameVerifier(true)
                                                                                                   .build())
                                                     .build();
    ServerResponse<ScimHttpRequest> response = scimRequestBuilder.create(ScimHttpRequest.class, HTTP_REQUESTS_ENDPOINT)
                                                                 .setResource(scimHttpRequest)
                                                                 .sendRequest();
    Assertions.assertEquals(HttpStatus.CREATED, response.getHttpStatus());
    Mockito.verify(spiedProxyHandler).createResource(Mockito.any(), Mockito.any());
    Assertions.assertEquals(1, proxyDao.count());
    Assertions.assertEquals(0, httpRequestsDao.count());
  }

  /**
   * verifies that the request will still be sent even if no group name is present within the request. In such a
   * case the request will not be saved within the database
   */
  @Test
  public void testCreateRequestWithUnsaveableData()
  {
    String proxyEndpoint = getScimApplicationUrl("/Proxy");
    ScimProxy scimProxy = ScimProxy.builder().hostname("localhost").port(8888).build();

    ScimHttpRequest scimHttpRequest = ScimHttpRequest.builder()
                                                     .groupName("unknown-category")
                                                     .name("create-client")
                                                     .url(proxyEndpoint)
                                                     .requestHeaders(getRequestHeaders())
                                                     .requestBody(scimProxy.toString())
                                                     .httpMethod("POST")
                                                     .scimHttpClientSettings(ScimHttpClientSettings.builder()
                                                                                                   .requestTimeout(5L)
                                                                                                   .socketTimeout(5L)
                                                                                                   .connectionTimeout(5L)
                                                                                                   .useHostnameVerifier(true)
                                                                                                   .build())
                                                     .build();
    ServerResponse<ScimHttpRequest> response = scimRequestBuilder.create(ScimHttpRequest.class, HTTP_REQUESTS_ENDPOINT)
                                                                 .setResource(scimHttpRequest)
                                                                 .sendRequest();
    Assertions.assertEquals(HttpStatus.CREATED, response.getHttpStatus());
    Mockito.verify(spiedProxyHandler).createResource(Mockito.any(), Mockito.any());
    Assertions.assertEquals(1, proxyDao.count());
    Assertions.assertEquals(0, httpRequestsDao.count());
  }

  /**
   * verifies that the nested http request will be sent by the application if successfully received on the
   * server. This test nests a create request to create a proxy within the scim endpoint
   */
  @Test
  public void testCreateRequest()
  {
    String proxyEndpoint = getScimApplicationUrl("/Proxy");
    ScimProxy scimProxy = ScimProxy.builder().hostname("localhost").port(8888).build();

    ScimHttpRequest scimHttpRequest = ScimHttpRequest.builder()
                                                     .groupName(group.getName())
                                                     .name("create-client")
                                                     .url(proxyEndpoint)
                                                     .requestBody(scimProxy.toString())
                                                     .requestHeaders(getRequestHeaders())
                                                     .httpMethod("POST")
                                                     .scimHttpClientSettings(ScimHttpClientSettings.builder()
                                                                                                   .requestTimeout(5L)
                                                                                                   .socketTimeout(5L)
                                                                                                   .connectionTimeout(5L)
                                                                                                   .useHostnameVerifier(true)
                                                                                                   .build())
                                                     .build();
    ServerResponse<ScimHttpRequest> response = scimRequestBuilder.create(ScimHttpRequest.class, HTTP_REQUESTS_ENDPOINT)
                                                                 .setResource(scimHttpRequest)
                                                                 .sendRequest();
    Assertions.assertEquals(HttpStatus.CREATED, response.getHttpStatus());
    ScimHttpRequest createdRequest = response.getResource();
    Mockito.verify(spiedProxyHandler).createResource(Mockito.any(), Mockito.any());
    Assertions.assertEquals(1, proxyDao.count());
    Assertions.assertEquals(1, httpRequestsDao.count());
    HttpRequest httpRequest = httpRequestsDao.findById(Utils.parseId(createdRequest.getId().get())).get();
    Assertions.assertEquals("POST", httpRequest.getHttpMethod());
    Assertions.assertEquals(scimProxy.toString(), httpRequest.getRequestBody());
    Assertions.assertEquals(proxyEndpoint, httpRequest.getUrl());
    Assertions.assertEquals(1, httpRequest.getHttpHeaders().size());
    Assertions.assertEquals(1, httpRequest.getHttpResponses().size());
    Assertions.assertTrue(StringUtils.isNotBlank(httpRequest.getHttpResponses().get(0).getRequestDetails()));
    Assertions.assertTrue(StringUtils.isNotBlank(httpRequest.getHttpResponses().get(0).getResponseHeaders()));
    Assertions.assertTrue(StringUtils.isNotBlank(httpRequest.getHttpResponses().get(0).getResponseBody()));
    Assertions.assertNotNull(httpRequest.getHttpResponses().get(0).getCreated());
  }

  private List<ScimHttpRequest.HttpHeaders> getRequestHeaders()
  {
    return List.of(new ScimHttpRequest.HttpHeaders(HttpHeader.CONTENT_TYPE_HEADER, HttpHeader.SCIM_CONTENT_TYPE));
  }
}
