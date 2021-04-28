package de.captaingoldfish.restclient.application.endpoints.proxy;

import java.util.Arrays;
import java.util.List;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.core.type.TypeReference;

import de.captaingoldfish.restclient.application.endpoints.proxy.forms.ProxyCreateForm;
import de.captaingoldfish.restclient.application.endpoints.proxy.forms.ProxyDeleteForm;
import de.captaingoldfish.restclient.application.endpoints.proxy.forms.ProxyResponseForm;
import de.captaingoldfish.restclient.application.endpoints.proxy.forms.ProxyUpdateForm;
import de.captaingoldfish.restclient.application.setup.AbstractOAuthRestClientTest;
import de.captaingoldfish.restclient.application.setup.ErrorResponseForm;
import de.captaingoldfish.restclient.application.setup.OAuthRestClientTest;
import de.captaingoldfish.restclient.database.entities.Proxy;
import kong.unirest.HttpResponse;
import kong.unirest.HttpStatus;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import lombok.extern.slf4j.Slf4j;


/**
 * @author Pascal Knueppel
 * @since 07.04.2021
 */
@Slf4j
@OAuthRestClientTest
public class ProxyControllerTest extends AbstractOAuthRestClientTest
{

  @Test
  public void testCreateProxyWithHostAndPort()
  {
    final String host = "localhost";
    final String port = "8888";

    Assertions.assertEquals(0, proxyDao.count());
    ProxyCreateForm proxyForm = ProxyCreateForm.builder().host(host).port(port).build();
    HttpResponse<JsonNode> response = Unirest.post(getApplicationUrl("/proxy/create"))
                                             .contentType(MediaType.APPLICATION_JSON_VALUE)
                                             .body(toJson(proxyForm))
                                             .asJson();
    Assertions.assertEquals(HttpStatus.CREATED, response.getStatus());
    ProxyResponseForm responseForm = getForm(response.getBody().toString(), ProxyResponseForm.class);
    Assertions.assertNotNull(responseForm.getId());
    Assertions.assertEquals(host, responseForm.getHost());
    Assertions.assertEquals(port, responseForm.getPort());
    Assertions.assertNull(responseForm.getUsername());
    Assertions.assertNull(responseForm.getPassword());

    Assertions.assertEquals(1, proxyDao.count());
  }

  @Test
  public void testCreateProxyWithFullData()
  {
    final String host = "localhost";
    final String port = "8888";
    final String username = "goldfish";
    final String password = "123456";

    ProxyCreateForm proxyForm = ProxyCreateForm.builder()
                                               .host(host)
                                               .port(port)
                                               .username(username)
                                               .password(password)
                                               .build();
    HttpResponse<JsonNode> response = Unirest.post(getApplicationUrl("/proxy/create"))
                                             .contentType(MediaType.APPLICATION_JSON_VALUE)
                                             .body(toJson(proxyForm))
                                             .asJson();
    Assertions.assertEquals(HttpStatus.CREATED, response.getStatus());
    ProxyResponseForm responseForm = getForm(response.getBody().toString(), ProxyResponseForm.class);
    Assertions.assertNotNull(responseForm.getId());
    Assertions.assertEquals(host, responseForm.getHost());
    Assertions.assertEquals(port, responseForm.getPort());
    Assertions.assertEquals(username, responseForm.getUsername());
    Assertions.assertEquals(password, responseForm.getPassword());
  }

  /**
   * proxy might use an empty password so this must not be blocked by the validation
   */
  @Test
  public void testCreateProxyWithoutPassword()
  {
    final String host = "localhost";
    final String port = "8888";
    final String username = "goldfish";

    ProxyCreateForm proxyForm = ProxyCreateForm.builder().host(host).port(port).username(username).build();
    HttpResponse<JsonNode> response = Unirest.post(getApplicationUrl("/proxy/create"))
                                             .contentType(MediaType.APPLICATION_JSON_VALUE)
                                             .body(toJson(proxyForm))
                                             .asJson();
    Assertions.assertEquals(HttpStatus.CREATED, response.getStatus());
    ProxyResponseForm responseForm = getForm(response.getBody().toString(), ProxyResponseForm.class);
    Assertions.assertNotNull(responseForm.getId());
    Assertions.assertEquals(host, responseForm.getHost());
    Assertions.assertEquals(port, responseForm.getPort());
    Assertions.assertEquals(username, responseForm.getUsername());
    Assertions.assertNull(responseForm.getPassword());
  }

  @Test
  public void testCreateProxyEmptyHostAndPort()
  {

    ProxyCreateForm proxyForm = ProxyCreateForm.builder().build();
    HttpResponse<JsonNode> response = Unirest.post(getApplicationUrl("/proxy/create"))
                                             .contentType(MediaType.APPLICATION_JSON_VALUE)
                                             .body(toJson(proxyForm))
                                             .asJson();
    log.debug("status: {}", response.getStatus());
    log.debug("response: {}", response.getBody().toPrettyString());
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
    ErrorResponseForm errorResponseForm = getForm(response.getBody().toString(), ErrorResponseForm.class);
    Assertions.assertNull(errorResponseForm.getErrorMessages());
    Assertions.assertNotNull(errorResponseForm.getInputFieldErrors());
    Assertions.assertEquals(2, errorResponseForm.getInputFieldErrors().size());

    List<String> hostErrors = errorResponseForm.getInputFieldErrors().get("host");
    Assertions.assertEquals(1, hostErrors.size());
    MatcherAssert.assertThat(hostErrors, Matchers.containsInAnyOrder("Hostname must not be blank"));

    List<String> portErrors = errorResponseForm.getInputFieldErrors().get("port");
    Assertions.assertEquals(1, portErrors.size());
    MatcherAssert.assertThat(portErrors,
                             Matchers.containsInAnyOrder("Port must not be blank and within "
                                                         + "range of '1' and '65535'"));
  }

  @Test
  public void testCreateProxyWithinvalidPort()
  {

    ProxyCreateForm proxyForm = ProxyCreateForm.builder().host("localhost").port("a").build();
    HttpResponse<JsonNode> response = Unirest.post(getApplicationUrl("/proxy/create"))
                                             .contentType(MediaType.APPLICATION_JSON_VALUE)
                                             .body(toJson(proxyForm))
                                             .asJson();
    log.debug("status: {}", response.getStatus());
    log.debug("response: {}", response.getBody().toPrettyString());
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
    ErrorResponseForm errorResponseForm = getForm(response.getBody().toString(), ErrorResponseForm.class);
    Assertions.assertNull(errorResponseForm.getErrorMessages());
    Assertions.assertNotNull(errorResponseForm.getInputFieldErrors());
    Assertions.assertEquals(1, errorResponseForm.getInputFieldErrors().size());

    List<String> portErrors = errorResponseForm.getInputFieldErrors().get("port");
    Assertions.assertEquals(2, portErrors.size());
    MatcherAssert.assertThat(portErrors,
                             Matchers.containsInAnyOrder("Port is not a number",
                                                         "Port must be within range of '1' and '65535'"));
  }

  @Test
  public void testCreateProxyWithPortTooLow()
  {

    ProxyCreateForm proxyForm = ProxyCreateForm.builder().host("localhost").port("0").build();
    HttpResponse<JsonNode> response = Unirest.post(getApplicationUrl("/proxy/create"))
                                             .contentType(MediaType.APPLICATION_JSON_VALUE)
                                             .body(toJson(proxyForm))
                                             .asJson();
    log.debug("status: {}", response.getStatus());
    log.debug("response: {}", response.getBody().toPrettyString());
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
    ErrorResponseForm errorResponseForm = getForm(response.getBody().toString(), ErrorResponseForm.class);
    Assertions.assertNull(errorResponseForm.getErrorMessages());
    Assertions.assertNotNull(errorResponseForm.getInputFieldErrors());
    Assertions.assertEquals(1, errorResponseForm.getInputFieldErrors().size());

    List<String> portErrors = errorResponseForm.getInputFieldErrors().get("port");
    Assertions.assertEquals(1, portErrors.size());
    MatcherAssert.assertThat(portErrors, Matchers.containsInAnyOrder("Port must be within range of '1' and '65535'"));
  }

  @Test
  public void testCreateProxyWithPortTooHigh()
  {

    ProxyCreateForm proxyForm = ProxyCreateForm.builder().host("localhost").port("65536").build();
    HttpResponse<JsonNode> response = Unirest.post(getApplicationUrl("/proxy/create"))
                                             .contentType(MediaType.APPLICATION_JSON_VALUE)
                                             .body(toJson(proxyForm))
                                             .asJson();
    log.debug("status: {}", response.getStatus());
    log.debug("response: {}", response.getBody().toPrettyString());
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
    ErrorResponseForm errorResponseForm = getForm(response.getBody().toString(), ErrorResponseForm.class);
    Assertions.assertNull(errorResponseForm.getErrorMessages());
    Assertions.assertNotNull(errorResponseForm.getInputFieldErrors());
    Assertions.assertEquals(1, errorResponseForm.getInputFieldErrors().size());

    List<String> portErrors = errorResponseForm.getInputFieldErrors().get("port");
    Assertions.assertEquals(1, portErrors.size());
    MatcherAssert.assertThat(portErrors, Matchers.containsInAnyOrder("Port must be within range of '1' and '65535'"));
  }

  @Test
  public void testListProxies()
  {
    List<Integer> portList = Arrays.asList(8888, 8889, 8890);
    for ( Integer port : portList )
    {
      Proxy proxy = Proxy.builder()
                         .proxyHost("localhost")
                         .proxyPort(port)
                         .proxyUsername("goldfish")
                         .proxyPassword("123456")
                         .build();
      proxyDao.save(proxy);
    }

    HttpResponse<JsonNode> response = Unirest.get(getApplicationUrl("/proxy/list")).asJson();
    log.debug("status: {}", response.getStatus());
    log.debug("response: {}", response.getBody().toPrettyString());
    Assertions.assertEquals(HttpStatus.OK, response.getStatus());

    List<ProxyResponseForm> proxyResponses = getForm(response.getBody().toString(), new TypeReference<>()
    {});
    for ( ProxyResponseForm proxyResponse : proxyResponses )
    {
      Assertions.assertNotNull(proxyResponse.getId());
      Assertions.assertEquals("localhost", proxyResponse.getHost());
      MatcherAssert.assertThat(portList, Matchers.hasItem(Integer.parseInt(proxyResponse.getPort())));
      Assertions.assertEquals("goldfish", proxyResponse.getUsername());
      Assertions.assertEquals("123456", proxyResponse.getPassword());
    }
  }

  @Test
  public void testUpdateProxyWithHostAndPort()
  {
    Proxy proxy = Proxy.builder()
                       .proxyHost("localhost")
                       .proxyPort(8888)
                       .proxyUsername("goldfish")
                       .proxyPassword("123456")
                       .build();
    proxy = proxyDao.save(proxy);

    final String newHost = "127.0.0.1";
    final String newPort = "9000";
    ProxyUpdateForm updateForm = ProxyUpdateForm.builder()
                                                .id(String.valueOf(proxy.getId()))
                                                .host(newHost)
                                                .port(newPort)
                                                .build();

    HttpResponse<JsonNode> response = Unirest.put(getApplicationUrl("/proxy/update"))
                                             .contentType(MediaType.APPLICATION_JSON_VALUE)
                                             .body(updateForm)
                                             .asJson();
    log.debug("status: {}", response.getStatus());
    log.debug("response: {}", response.getBody().toPrettyString());
    Assertions.assertEquals(HttpStatus.OK, response.getStatus());

    ProxyResponseForm proxyResponse = getForm(response.getBody().toString(), ProxyResponseForm.class);
    Assertions.assertEquals(String.valueOf(proxy.getId()), proxyResponse.getId());
    Assertions.assertEquals(newHost, proxyResponse.getHost());
    Assertions.assertEquals(newPort, proxyResponse.getPort());
    Assertions.assertNull(proxyResponse.getUsername());
    Assertions.assertNull(proxyResponse.getPassword());
  }

  @Test
  public void testUpdateProxyWithFullData()
  {
    Proxy proxy = Proxy.builder()
                       .proxyHost("localhost")
                       .proxyPort(8888)
                       .proxyUsername("goldfish")
                       .proxyPassword("123456")
                       .build();
    proxy = proxyDao.save(proxy);

    final String newHost = "127.0.0.1";
    final String newPort = "9000";
    final String newUsername = "super-mario";
    final String newPassword = "peach";
    ProxyUpdateForm updateForm = ProxyUpdateForm.builder()
                                                .id(String.valueOf(proxy.getId()))
                                                .host(newHost)
                                                .port(newPort)
                                                .username(newUsername)
                                                .password(newPassword)
                                                .build();

    Assertions.assertEquals(1, proxyDao.count());
    HttpResponse<JsonNode> response = Unirest.put(getApplicationUrl("/proxy/update"))
                                             .contentType(MediaType.APPLICATION_JSON_VALUE)
                                             .body(updateForm)
                                             .asJson();
    log.debug("status: {}", response.getStatus());
    log.debug("response: {}", response.getBody().toPrettyString());
    Assertions.assertEquals(HttpStatus.OK, response.getStatus());

    ProxyResponseForm proxyResponse = getForm(response.getBody().toString(), ProxyResponseForm.class);
    Assertions.assertEquals(String.valueOf(proxy.getId()), proxyResponse.getId());
    Assertions.assertEquals(newHost, proxyResponse.getHost());
    Assertions.assertEquals(newPort, proxyResponse.getPort());
    Assertions.assertEquals(newUsername, proxyResponse.getUsername());
    Assertions.assertEquals(newPassword, proxyResponse.getPassword());

    Assertions.assertEquals(1, proxyDao.count());
    Proxy dbProxy = proxyDao.findAll().get(0);
    Assertions.assertEquals(proxyResponse.getId(), String.valueOf(dbProxy.getId()));
    Assertions.assertEquals(newHost, dbProxy.getProxyHost());
    Assertions.assertEquals(newPort, String.valueOf(dbProxy.getProxyPort()));
    Assertions.assertEquals(newUsername, dbProxy.getProxyUsername());
    Assertions.assertEquals(newPassword, dbProxy.getProxyPassword());
  }

  @Test
  public void testUpdateProxyWithoutId()
  {
    Proxy proxy = Proxy.builder()
                       .proxyHost("localhost")
                       .proxyPort(8888)
                       .proxyUsername("goldfish")
                       .proxyPassword("123456")
                       .build();
    proxyDao.save(proxy);

    ProxyUpdateForm updateForm = ProxyUpdateForm.builder().build();

    HttpResponse<JsonNode> response = Unirest.put(getApplicationUrl("/proxy/update"))
                                             .contentType(MediaType.APPLICATION_JSON_VALUE)
                                             .body(updateForm)
                                             .asJson();
    log.debug("status: {}", response.getStatus());
    log.debug("response: {}", response.getBody().toPrettyString());
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());

    ErrorResponseForm errorResponse = getForm(response.getBody().toString(), ErrorResponseForm.class);
    Assertions.assertNotNull(errorResponse.getErrorMessages());
    Assertions.assertEquals(1, errorResponse.getErrorMessages().size());
    MatcherAssert.assertThat(errorResponse.getErrorMessages(), Matchers.hasItem("Cannot update proxy for empty ID"));

    Assertions.assertNull(errorResponse.getInputFieldErrors());
  }

  @Test
  public void testUpdateProxyWithEmptyHostAndPort()
  {
    Proxy proxy = Proxy.builder()
                       .proxyHost("localhost")
                       .proxyPort(8888)
                       .proxyUsername("goldfish")
                       .proxyPassword("123456")
                       .build();
    proxy = proxyDao.save(proxy);

    ProxyUpdateForm updateForm = ProxyUpdateForm.builder().id(String.valueOf(proxy.getId())).build();

    HttpResponse<JsonNode> response = Unirest.put(getApplicationUrl("/proxy/update"))
                                             .contentType(MediaType.APPLICATION_JSON_VALUE)
                                             .body(updateForm)
                                             .asJson();
    log.debug("status: {}", response.getStatus());
    log.debug("response: {}", response.getBody().toPrettyString());
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());

    ErrorResponseForm errorResponse = getForm(response.getBody().toString(), ErrorResponseForm.class);
    Assertions.assertNull(errorResponse.getErrorMessages());

    Assertions.assertNotNull(errorResponse.getInputFieldErrors());
    Assertions.assertEquals(2, errorResponse.getInputFieldErrors().size());

    List<String> hostErrors = errorResponse.getInputFieldErrors().get("host");
    Assertions.assertEquals(1, hostErrors.size());
    MatcherAssert.assertThat(hostErrors, Matchers.containsInAnyOrder("Hostname must not be blank"));

    List<String> portErrors = errorResponse.getInputFieldErrors().get("port");
    Assertions.assertEquals(1, portErrors.size());
    MatcherAssert.assertThat(portErrors,
                             Matchers.containsInAnyOrder("Port must not be blank and within "
                                                         + "range of '1' and '65535'"));
  }

  @Test
  public void testUpdateProxyUnknownProxy()
  {
    Proxy proxy = Proxy.builder()
                       .proxyHost("localhost")
                       .proxyPort(8888)
                       .proxyUsername("goldfish")
                       .proxyPassword("123456")
                       .build();
    proxyDao.save(proxy);

    final String unknownProxyId = "0";
    ProxyUpdateForm updateForm = ProxyUpdateForm.builder().id(unknownProxyId).host("127.0.0.1").port("7777").build();

    HttpResponse<JsonNode> response = Unirest.put(getApplicationUrl("/proxy/update"))
                                             .contentType(MediaType.APPLICATION_JSON_VALUE)
                                             .body(updateForm)
                                             .asJson();
    log.debug("status: {}", response.getStatus());
    log.debug("response: {}", response.getBody().toPrettyString());
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());

    ErrorResponseForm errorResponse = getForm(response.getBody().toString(), ErrorResponseForm.class);
    Assertions.assertNotNull(errorResponse.getErrorMessages());
    Assertions.assertEquals(1, errorResponse.getErrorMessages().size());
    MatcherAssert.assertThat(errorResponse.getErrorMessages(),
                             Matchers.hasItem("Cannot update proxy for entry with ID '" + unknownProxyId
                                              + "' does not exist"));
  }

  @Test
  public void testDeleteProxy()
  {
    Proxy proxy = Proxy.builder()
                       .proxyHost("localhost")
                       .proxyPort(8888)
                       .proxyUsername("goldfish")
                       .proxyPassword("123456")
                       .build();
    proxy = proxyDao.save(proxy);

    ProxyDeleteForm deleteForm = new ProxyDeleteForm(String.valueOf(proxy.getId()));
    HttpResponse<String> response = Unirest.delete(getApplicationUrl("/proxy/delete"))
                                           .contentType(MediaType.APPLICATION_JSON_VALUE)
                                           .body(deleteForm)
                                           .asString();
    log.debug("status: {}", response.getStatus());
    log.debug("response: {}", response.getBody());
    Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getStatus());

    MatcherAssert.assertThat(response.getBody(), Matchers.emptyOrNullString());
  }

  @Test
  public void testDeleteProxyWithUnknownId()
  {
    Proxy proxy = Proxy.builder()
                       .proxyHost("localhost")
                       .proxyPort(8888)
                       .proxyUsername("goldfish")
                       .proxyPassword("123456")
                       .build();
    proxyDao.save(proxy);

    final String unknownProxyId = "0";
    ProxyDeleteForm deleteForm = new ProxyDeleteForm(unknownProxyId);

    Assertions.assertEquals(1, proxyDao.count());
    HttpResponse<JsonNode> response = Unirest.delete(getApplicationUrl("/proxy/delete"))
                                             .contentType(MediaType.APPLICATION_JSON_VALUE)
                                             .body(deleteForm)
                                             .asJson();
    log.debug("status: {}", response.getStatus());
    log.debug("response: {}", response.getBody().toPrettyString());
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());

    ErrorResponseForm errorResponse = getForm(response.getBody().toString(), ErrorResponseForm.class);
    Assertions.assertNotNull(errorResponse.getErrorMessages());
    Assertions.assertEquals(1, errorResponse.getErrorMessages().size());
    MatcherAssert.assertThat(errorResponse.getErrorMessages(),
                             Matchers.hasItem("Cannot delete proxy for entry with ID '" + unknownProxyId
                                              + "' does not exist"));
    Assertions.assertEquals(1, proxyDao.count());
  }

  @Test
  public void testDeleteProxyWithoutAnId()
  {
    ProxyDeleteForm deleteForm = new ProxyDeleteForm();

    HttpResponse<JsonNode> response = Unirest.delete(getApplicationUrl("/proxy/delete"))
                                             .contentType(MediaType.APPLICATION_JSON_VALUE)
                                             .body(deleteForm)
                                             .asJson();
    log.debug("status: {}", response.getStatus());
    log.debug("response: {}", response.getBody().toPrettyString());
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());

    ErrorResponseForm errorResponse = getForm(response.getBody().toString(), ErrorResponseForm.class);
    Assertions.assertNotNull(errorResponse.getErrorMessages());
    Assertions.assertEquals(1, errorResponse.getErrorMessages().size());
    MatcherAssert.assertThat(errorResponse.getErrorMessages(), Matchers.hasItem("Cannot delete proxy for empty ID"));
  }

  @Test
  public void testDeleteProxyWithIllegalId()
  {
    ProxyDeleteForm deleteForm = new ProxyDeleteForm("a");

    HttpResponse<JsonNode> response = Unirest.delete(getApplicationUrl("/proxy/delete"))
                                             .contentType(MediaType.APPLICATION_JSON_VALUE)
                                             .body(deleteForm)
                                             .asJson();
    log.debug("status: {}", response.getStatus());
    log.debug("response: {}", response.getBody().toPrettyString());
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());

    ErrorResponseForm errorResponse = getForm(response.getBody().toString(), ErrorResponseForm.class);
    Assertions.assertNotNull(errorResponse.getErrorMessages());
    Assertions.assertEquals(1, errorResponse.getErrorMessages().size());
    MatcherAssert.assertThat(errorResponse.getErrorMessages(),
                             Matchers.hasItem("Cannot delete proxy for illegal ID '" + deleteForm.getId() + "'"));
  }
}
