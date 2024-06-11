package de.captaingoldfish.restclient.application.endpoints.httprequests;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.stereotype.Component;

import de.captaingoldfish.restclient.application.utils.HttpClientBuilder;
import de.captaingoldfish.restclient.database.entities.HttpHeader;
import de.captaingoldfish.restclient.database.entities.HttpRequest;
import de.captaingoldfish.restclient.database.entities.HttpResponse;
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
    try (CloseableHttpClient httpClient = HttpClientBuilder.getHttpClient(httpRequest.getHttpClientSettings());
      CloseableHttpResponse response = httpClient.execute(uriRequest))
    {

      String requestDetailsString = toStringRepresentation(httpRequest);
      String responseHeaderString = toStringRepresentation(response.getHeaders());
      String responseBodyString = toResponseBody(response);
      return HttpResponse.builder()
                         .requestDetails(requestDetailsString)
                         .responseStatus(response.getCode())
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
      case "PUT":
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

}
