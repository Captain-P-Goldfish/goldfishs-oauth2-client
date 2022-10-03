package de.captaingoldfish.restclient.application.endpoints.httprequests;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.stereotype.Component;

import de.captaingoldfish.restclient.application.utils.SSLContextHelper;
import de.captaingoldfish.restclient.database.entities.HttpClientSettings;
import de.captaingoldfish.restclient.database.entities.HttpHeader;
import de.captaingoldfish.restclient.database.entities.HttpRequest;
import de.captaingoldfish.restclient.database.entities.HttpResponse;
import de.captaingoldfish.restclient.database.entities.Proxy;
import de.captaingoldfish.scim.sdk.common.exceptions.BadRequestException;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


/**
 * author Pascal Knueppel <br>
 * created at: 14.09.2022 - 16:21 <br>
 * <br>
 */
@Slf4j
@Component
public class HttpRequestExecutor
{

  /**
   * Number by which timeout variables are multiplied.
   */
  @Getter
  private static final int TIMEOUT_MILLIS = 1000;

  /**
   * @param httpRequest
   * @return
   */
  @SneakyThrows
  public HttpResponse sendHttpRequest(HttpRequest httpRequest)
  {
    HttpUriRequest uriRequest = toApacheHttpUriRequest(httpRequest);
    log.info("sending '{}' request to url '{}'", httpRequest.getHttpMethod(), httpRequest.getUrl());
    try (CloseableHttpClient httpClient = getHttpClient(httpRequest);
      CloseableHttpResponse response = httpClient.execute(uriRequest))
    {

      String requestDetailsString = toStringRepresentation(httpRequest);
      String responseHeaderString = toStringRepresentation(response.getAllHeaders());
      String responseBodyString = toResponseBody(response);
      return HttpResponse.builder()
                         .requestDetails(requestDetailsString)
                         .responseStatus(response.getStatusLine().getStatusCode())
                         .responseHeaders(responseHeaderString)
                         .responseBody(responseBodyString)
                         .created(Instant.now())
                         .build();
    }
  }

  @SneakyThrows
  private String toResponseBody(CloseableHttpResponse response)
  {
    HttpEntity httpEntity = response.getEntity();
    if (httpEntity == null)
    {
      return null;
    }
    if (httpEntity.getContent() == null)
    {
      return null;
    }
    try (InputStream inputStream = httpEntity.getContent())
    {
      return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    }
  }

  private String toStringRepresentation(Header[] allHeaders)
  {

    return Arrays.stream(allHeaders)
                 .map(header -> String.format("%s: %s", header.getName(), header.getValue()))
                 .collect(Collectors.joining("\n"));
  }

  private String toStringRepresentation(HttpRequest httpRequest)
  {
    String httpRequestHeaders = httpRequest.getHttpHeaders()
                                           .stream()
                                           .map(header -> String.format("%s: %s", header.getName(), header.getValue()))
                                           .collect(Collectors.joining("\n"));
    return String.format("%s %s\n%s\n\n%s",
                         httpRequest.getHttpMethod(),
                         httpRequest.getUrl(),
                         httpRequestHeaders,
                         httpRequest.getRequestBody());
  }


  /**
   * translates the database request into a resolvable request object understood by apache http client
   */
  @SneakyThrows
  private HttpUriRequest toApacheHttpUriRequest(HttpRequest httpRequest)
  {
    HttpUriRequest httpUriRequest;
    switch (httpRequest.getHttpMethod())
    {
      case "GET":
        httpUriRequest = new HttpGet(httpRequest.getUrl());
        break;
      case "POST":
        HttpPost httpPost = new HttpPost(httpRequest.getUrl());
        if (StringUtils.isNotBlank(httpRequest.getRequestBody()))
        {
          httpPost.setEntity(new StringEntity(httpRequest.getRequestBody()));
        }
        httpUriRequest = httpPost;
        break;
      case "UPDATE":
        HttpPut httpPut = new HttpPut(httpRequest.getUrl());
        if (StringUtils.isNotBlank(httpRequest.getRequestBody()))
        {
          httpPut.setEntity(new StringEntity(httpRequest.getRequestBody()));
        }
        httpUriRequest = httpPut;
        break;
      case "PATCH":
        HttpPatch httpPatch = new HttpPatch(httpRequest.getUrl());
        if (StringUtils.isNotBlank(httpRequest.getRequestBody()))
        {
          httpPatch.setEntity(new StringEntity(httpRequest.getRequestBody()));
        }
        httpUriRequest = httpPatch;
        break;
      case "DELETE":
        httpUriRequest = new HttpDelete(httpRequest.getUrl());
        break;
      default:
        throw new BadRequestException("Only the methods ['GET', 'POST', 'PUT', 'PATCH', 'DELETE'] are supported ");
    }
    for ( HttpHeader httpHeader : httpRequest.getHttpHeaders() )
    {
      httpUriRequest.setHeader(httpHeader.getName(), httpHeader.getValue());
    }
    return httpUriRequest;
  }

  /**
   * this method generates a http-client instance and will set the ssl-context
   *
   * @return a http-client instance
   */
  public CloseableHttpClient getHttpClient(HttpRequest httpRequest)
  {
    HttpClientSettings clientSettings = httpRequest.getHttpClientSettings();

    HttpClientBuilder clientBuilder = HttpClientBuilder.create();
    CredentialsProvider credentialsProvider = null;
    if (clientSettings.getProxy() != null)
    {
      credentialsProvider = getProxyCredentials(clientSettings.getProxy());
    }
    clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
    clientBuilder.setSSLContext(SSLContextHelper.getSslContext(httpRequest.getHttpClientSettings()));

    clientBuilder.setConnectionReuseStrategy((response, context) -> false);
    if (!clientSettings.isUseHostnameVerifier())
    {
      clientBuilder.setSSLHostnameVerifier((s, sslSession) -> true);
    }
    clientBuilder.setDefaultRequestConfig(getRequestConfig(httpRequest));
    // TODO
    // if (!scimClientConfig.isEnableCookieManagement())
    // {
    // clientBuilder.disableCookieManagement();
    // }
    return clientBuilder.build();
  }

  /**
   * will configure the apache http-client
   *
   * @return the original request with an extended configuration
   */
  public RequestConfig getRequestConfig(HttpRequest httpRequest)
  {
    HttpClientSettings clientSettings = httpRequest.getHttpClientSettings();
    RequestConfig.Builder configBuilder;
    if (clientSettings.getProxy() == null)
    {
      configBuilder = RequestConfig.copy(RequestConfig.DEFAULT);
    }
    else
    {
      RequestConfig proxyConfig = getProxyConfig(clientSettings.getProxy());
      configBuilder = RequestConfig.copy(proxyConfig);
    }
    if (clientSettings.getConnectionTimeout() > 0)
    {
      configBuilder.setConnectTimeout(clientSettings.getConnectionTimeout() * TIMEOUT_MILLIS);
      log.trace("Connection timeout '{}' seconds", clientSettings.getConnectionTimeout());
    }
    if (clientSettings.getSocketTimeout() > 0)
    {
      configBuilder.setSocketTimeout(clientSettings.getSocketTimeout() * TIMEOUT_MILLIS);
      log.trace("Socket timeout '{}' seconds", clientSettings.getSocketTimeout());
    }
    if (clientSettings.getRequestTimeout() > 0)
    {
      configBuilder.setConnectionRequestTimeout(clientSettings.getRequestTimeout() * TIMEOUT_MILLIS);
      log.trace("Request timeout '{}' seconds", clientSettings.getRequestTimeout());
    }

    return configBuilder.build();
  }

  /**
   * @return a basic credentials provider that will be used for proxy authentication.
   */
  public CredentialsProvider getProxyCredentials(Proxy proxy)
  {
    if (StringUtils.isBlank(proxy.getUsername()))
    {
      log.trace("Proxy username is empty cannot create client credentials");
      return null;
    }
    if (proxy.getPassword() == null)
    {
      log.debug("Proxy password is null cannot create client credentials");
      return null;
    }
    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(new AuthScope(proxy.getHost(), proxy.getPort()),
                                       new UsernamePasswordCredentials(proxy.getUsername(), proxy.getPassword()));
    return credentialsProvider;
  }

  /**
   * will give back a request-config with the proxy settings based on the configuration-poperties
   *
   * @return a new config with the configured proxy or the default-config
   */
  public RequestConfig getProxyConfig(Proxy proxy)
  {
    if (StringUtils.isNotBlank(proxy.getHost()))
    {
      HttpHost systemProxy = new HttpHost(proxy.getHost(), proxy.getPort());
      log.debug("Using proxy configuration: {}", systemProxy);
      return RequestConfig.custom().setProxy(systemProxy).build();
    }
    return RequestConfig.DEFAULT;
  }

}
