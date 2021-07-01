package de.captaingoldfish.restclient.application.endpoints.proxy;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.captaingoldfish.restclient.application.setup.AbstractScimClientConfig;
import de.captaingoldfish.restclient.application.setup.OAuthRestClientTest;
import de.captaingoldfish.restclient.database.entities.Proxy;
import de.captaingoldfish.restclient.scim.resources.ScimProxy;
import de.captaingoldfish.scim.sdk.client.response.ServerResponse;
import de.captaingoldfish.scim.sdk.common.constants.HttpStatus;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import de.captaingoldfish.scim.sdk.common.response.ErrorResponse;
import de.captaingoldfish.scim.sdk.common.response.ListResponse;
import lombok.extern.slf4j.Slf4j;


/**
 * @author Pascal Knueppel
 * @since 19.05.2021
 */
@Slf4j
@OAuthRestClientTest
public class ProxyHandlerTest extends AbstractScimClientConfig
{

  /**
   * the scim endpoint for managing proxies
   */
  private static final String PROXY_ENDPOINT = "/Proxy";

  @Test
  public void testCreateProxy()
  {
    final String host = "localhost";
    final int port = 8448;
    final String username = "goldfish";
    final String password = "123456";

    ScimProxy scimProxy = ScimProxy.builder().hostname(host).port(port).username(username).password(password).build();
    Assertions.assertEquals(0, proxyDao.count());
    ServerResponse<ScimProxy> response = scimRequestBuilder.create(ScimProxy.class, PROXY_ENDPOINT)
                                                           .setResource(scimProxy)
                                                           .sendRequest();
    Assertions.assertEquals(HttpStatus.CREATED, response.getHttpStatus());
    ScimProxy returnedProxy = response.getResource();
    Assertions.assertEquals(host, returnedProxy.getHostname());
    Assertions.assertEquals(port, returnedProxy.getPort().get());
    Assertions.assertEquals(username, returnedProxy.getUsername().get());
    Assertions.assertEquals(password, returnedProxy.getPassword().get());

    long id = Long.parseLong(returnedProxy.getId().get());
    Proxy proxy = Assertions.assertDoesNotThrow(() -> proxyDao.findById(id).orElseThrow());
    Assertions.assertEquals(proxy.getCreated(), returnedProxy.getMeta().flatMap(Meta::getCreated).get());
    Assertions.assertEquals(proxy.getLastModified(), returnedProxy.getMeta().flatMap(Meta::getLastModified).get());
    Assertions.assertEquals(1, proxyDao.count());
  }

  /**
   * verifies that the default port is set to 8888 if no port was set
   */
  @Test
  public void testCreateProxyWithoutPort()
  {
    final String host = "localhost";
    final int defaultPort = 8888;
    final String username = "goldfish";
    final String password = "123456";

    ScimProxy scimProxy = ScimProxy.builder().hostname(host).username(username).password(password).build();
    Assertions.assertEquals(0, proxyDao.count());
    ServerResponse<ScimProxy> response = scimRequestBuilder.create(ScimProxy.class, PROXY_ENDPOINT)
                                                           .setResource(scimProxy)
                                                           .sendRequest();
    Assertions.assertEquals(HttpStatus.CREATED, response.getHttpStatus());
    ScimProxy returnedProxy = response.getResource();
    Assertions.assertEquals(host, returnedProxy.getHostname());
    Assertions.assertEquals(defaultPort, returnedProxy.getPort().get());
    Assertions.assertEquals(username, returnedProxy.getUsername().get());
    Assertions.assertEquals(password, returnedProxy.getPassword().get());

    long id = Long.parseLong(returnedProxy.getId().get());
    Proxy proxy = Assertions.assertDoesNotThrow(() -> proxyDao.findById(id).orElseThrow());
    Assertions.assertEquals(proxy.getCreated(), returnedProxy.getMeta().flatMap(Meta::getCreated).get());
    Assertions.assertEquals(proxy.getLastModified(), returnedProxy.getMeta().flatMap(Meta::getLastModified).get());
    Assertions.assertEquals(1, proxyDao.count());
  }

  @Test
  public void testCreateProxyWithPortOutOfRange()
  {
    final String host = "localhost";
    final int port = 65536;
    final String username = "goldfish";
    final String password = "123456";

    ScimProxy scimProxy = ScimProxy.builder().hostname(host).port(port).username(username).password(password).build();
    Assertions.assertEquals(0, proxyDao.count());
    ServerResponse<ScimProxy> response = scimRequestBuilder.create(ScimProxy.class, PROXY_ENDPOINT)
                                                           .setResource(scimProxy)
                                                           .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    Assertions.assertEquals(0, proxyDao.count());
    ErrorResponse errorResponse = response.getErrorResponse();
    List<String> errorMessages = errorResponse.getErrorMessages();
    MatcherAssert.assertThat(errorMessages, Matchers.emptyIterable());

    Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
    Assertions.assertEquals(1, fieldErrors.size());
    final String fieldName = ScimProxy.FieldNames.PORT;
    String errorMessage = "The 'INTEGER'-attribute 'port' with value '65536' must not be greater than '65535'";
    MatcherAssert.assertThat(fieldErrors.get(fieldName), Matchers.containsInAnyOrder(errorMessage));
  }

  @Test
  public void testCreateProxyWithPortTooLow()
  {
    final String host = "localhost";
    final int port = 0;
    final String username = "goldfish";
    final String password = "123456";

    ScimProxy scimProxy = ScimProxy.builder().hostname(host).port(port).username(username).password(password).build();
    Assertions.assertEquals(0, proxyDao.count());
    ServerResponse<ScimProxy> response = scimRequestBuilder.create(ScimProxy.class, PROXY_ENDPOINT)
                                                           .setResource(scimProxy)
                                                           .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    Assertions.assertEquals(0, proxyDao.count());
    ErrorResponse errorResponse = response.getErrorResponse();
    List<String> errorMessages = errorResponse.getErrorMessages();
    MatcherAssert.assertThat(errorMessages, Matchers.emptyIterable());

    Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
    Assertions.assertEquals(1, fieldErrors.size());
    final String fieldName = ScimProxy.FieldNames.PORT;
    String errorMessage = "The 'INTEGER'-attribute 'port' with value '0' must have at least a value of '1'";
    MatcherAssert.assertThat(fieldErrors.get(fieldName), Matchers.containsInAnyOrder(errorMessage));
  }

  @Test
  public void testCreateProxyWithEmptyHostname()
  {
    final String host = "";
    final int port = 8080;
    final String username = "goldfish";
    final String password = "123456";

    ScimProxy scimProxy = ScimProxy.builder().hostname(host).port(port).username(username).password(password).build();
    Assertions.assertEquals(0, proxyDao.count());
    ServerResponse<ScimProxy> response = scimRequestBuilder.create(ScimProxy.class, PROXY_ENDPOINT)
                                                           .setResource(scimProxy)
                                                           .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    Assertions.assertEquals(0, proxyDao.count());
    ErrorResponse errorResponse = response.getErrorResponse();
    List<String> errorMessages = errorResponse.getErrorMessages();
    MatcherAssert.assertThat(errorMessages, Matchers.emptyIterable());

    Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
    Assertions.assertEquals(1, fieldErrors.size());
    final String fieldName = ScimProxy.FieldNames.HOSTNAME;
    String errorMessage = "The 'STRING'-attribute 'hostname' with value '' must have a minimum length of '1' "
                          + "characters but is '0' characters long";
    MatcherAssert.assertThat(fieldErrors.get(fieldName), Matchers.containsInAnyOrder(errorMessage));
  }

  @Test
  public void testGetProxy()
  {
    final String host = "localhost";
    final int port = 8448;
    final String username = "goldfish";
    final String password = "123456";

    Proxy proxy = Proxy.builder().host(host).port(port).username(username).password(password).build();
    proxy = proxyDao.save(proxy);

    Assertions.assertEquals(1, proxyDao.count());
    ServerResponse<ScimProxy> response = scimRequestBuilder.get(ScimProxy.class,
                                                                PROXY_ENDPOINT,
                                                                String.valueOf(proxy.getId()))
                                                           .sendRequest();
    Assertions.assertEquals(HttpStatus.OK, response.getHttpStatus());
    ScimProxy scimProxy = response.getResource();
    Assertions.assertEquals(proxy.getHost(), scimProxy.getHostname());
    Assertions.assertEquals(proxy.getPort(), scimProxy.getPort().get());
    Assertions.assertEquals(proxy.getUsername(), scimProxy.getUsername().get());
    Assertions.assertEquals(proxy.getPassword(), scimProxy.getPassword().get());
    Assertions.assertEquals(proxy.getCreated(), scimProxy.getMeta().flatMap(Meta::getCreated).get());
    Assertions.assertEquals(proxy.getLastModified(), scimProxy.getMeta().flatMap(Meta::getLastModified).get());
    Assertions.assertEquals(1, proxyDao.count());
  }

  @Test
  public void testGetProxyWithMalformedId()
  {
    ServerResponse<ScimProxy> response = scimRequestBuilder.get(ScimProxy.class, PROXY_ENDPOINT, "a").sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();
    Assertions.assertEquals("Invalid ID format: a", errorResponse.getDetail().get());
  }

  @Test
  public void testGetProxyWithUnknownId()
  {
    final String unknownId = "999999";
    ServerResponse<ScimProxy> response = scimRequestBuilder.get(ScimProxy.class, PROXY_ENDPOINT, unknownId)
                                                           .sendRequest();
    Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();
    Assertions.assertEquals(String.format("Resource with ID '%s' does not exist", unknownId),
                            errorResponse.getDetail().get());
  }

  @Test
  public void testListProxies()
  {
    int numberOfProxies = 10;
    Random random = new Random();
    for ( int i = 0 ; i < numberOfProxies ; i++ )
    {
      final String host = "localhost";
      final int port = random.nextInt(65535) + 1;
      final String username = "goldfish";
      final String password = "123456";
      Proxy proxy = Proxy.builder().host(host).port(port).username(username).password(password).build();
      proxyDao.save(proxy);
    }
    Assertions.assertEquals(numberOfProxies, proxyDao.count());
    ServerResponse<ListResponse<ScimProxy>> response = scimRequestBuilder.list(ScimProxy.class, PROXY_ENDPOINT)
                                                                         .get()
                                                                         .sendRequest();
    Assertions.assertEquals(HttpStatus.OK, response.getHttpStatus());
    ListResponse<ScimProxy> listResponse = response.getResource();
    Assertions.assertEquals(numberOfProxies, listResponse.getTotalResults());
    Assertions.assertEquals(numberOfProxies, listResponse.getListedResources().size());
    Assertions.assertEquals(numberOfProxies, proxyDao.count());
  }

  @Test
  public void testUpdateProxy()
  {
    final String host = "localhost";
    final int port = 8448;
    final String username = "goldfish";
    final String password = "123456";

    Proxy proxy = Proxy.builder().host(host).port(port).username(username).password(password).build();
    proxy = proxyDao.save(proxy);

    final String newHost = "goldfish-proxy";
    final int newPort = 4455;
    final String newUsername = "unit-test";
    final String newPassword = "654321";

    ScimProxy scimProxy = ScimProxy.builder()
                                   .hostname(newHost)
                                   .username(newUsername)
                                   .port(newPort)
                                   .password(newPassword)
                                   .build();
    Assertions.assertEquals(1, proxyDao.count());
    ServerResponse<ScimProxy> response = scimRequestBuilder.update(ScimProxy.class,
                                                                   PROXY_ENDPOINT,
                                                                   String.valueOf(proxy.getId()))
                                                           .setResource(scimProxy)
                                                           .sendRequest();
    Assertions.assertEquals(HttpStatus.OK, response.getHttpStatus());

    proxy = proxyDao.findById(proxy.getId()).orElseThrow();
    Assertions.assertNotEquals(proxy.getCreated(), proxy.getLastModified());

    ScimProxy returnedProxy = response.getResource();
    Assertions.assertEquals(newHost, returnedProxy.getHostname());
    Assertions.assertEquals(newPort, returnedProxy.getPort().get());
    Assertions.assertEquals(newUsername, returnedProxy.getUsername().get());
    Assertions.assertEquals(newPassword, returnedProxy.getPassword().get());

    Assertions.assertEquals(proxy.getHost(), returnedProxy.getHostname());
    Assertions.assertEquals(proxy.getPort(), returnedProxy.getPort().get());
    Assertions.assertEquals(proxy.getUsername(), returnedProxy.getUsername().get());
    Assertions.assertEquals(proxy.getPassword(), returnedProxy.getPassword().get());
    Assertions.assertEquals(proxy.getCreated(), returnedProxy.getMeta().flatMap(Meta::getCreated).get());
    Assertions.assertNotEquals(proxy.getLastModified(), returnedProxy.getMeta().flatMap(Meta::getLastModified).get());

    Assertions.assertEquals(1, proxyDao.count());
  }

  @Test
  public void testUpdateProxyWithMalformedId()
  {
    final String host = "localhost";
    final int port = 8448;
    final String username = "goldfish";
    final String password = "123456";
    ScimProxy scimProxy = ScimProxy.builder().hostname(host).port(port).username(username).password(password).build();

    ServerResponse<ScimProxy> response = scimRequestBuilder.update(ScimProxy.class, PROXY_ENDPOINT, "a")
                                                           .setResource(scimProxy)
                                                           .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();
    Assertions.assertEquals("Invalid ID format: a", errorResponse.getDetail().get());
  }

  @Test
  public void testUpdateProxyWithUnknownId()
  {
    final String host = "localhost";
    final int port = 8448;
    final String username = "goldfish";
    final String password = "123456";
    ScimProxy scimProxy = ScimProxy.builder().hostname(host).port(port).username(username).password(password).build();

    final String unknownId = "999999";
    ServerResponse<ScimProxy> response = scimRequestBuilder.update(ScimProxy.class, PROXY_ENDPOINT, unknownId)
                                                           .setResource(scimProxy)
                                                           .sendRequest();
    Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();
    Assertions.assertEquals(String.format("Resource with ID '%s' does not exist", unknownId),
                            errorResponse.getDetail().get());
  }

  @Test
  public void testDeleteProxy()
  {
    final String host = "localhost";
    final int port = 8448;
    final String username = "goldfish";
    final String password = "123456";

    Proxy proxy = Proxy.builder().host(host).port(port).username(username).password(password).build();
    proxy = proxyDao.save(proxy);

    Assertions.assertEquals(1, proxyDao.count());
    ServerResponse<ScimProxy> response = scimRequestBuilder.delete(ScimProxy.class,
                                                                   PROXY_ENDPOINT,
                                                                   String.valueOf(proxy.getId()))
                                                           .sendRequest();
    Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getHttpStatus());
    Assertions.assertEquals(0, proxyDao.count());
  }

  @Test
  public void testDeleteProxyWithMalformedId()
  {
    ServerResponse<ScimProxy> response = scimRequestBuilder.delete(ScimProxy.class, PROXY_ENDPOINT, "a").sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();
    Assertions.assertEquals("Invalid ID format: a", errorResponse.getDetail().get());
  }

  @Test
  public void testDeleteProxyWithUnknownId()
  {
    final String unknownId = "999999";
    ServerResponse<ScimProxy> response = scimRequestBuilder.delete(ScimProxy.class, PROXY_ENDPOINT, unknownId)
                                                           .sendRequest();
    Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();
    Assertions.assertEquals(String.format("Resource with ID '%s' does not exist", unknownId),
                            errorResponse.getDetail().get());
  }
}
