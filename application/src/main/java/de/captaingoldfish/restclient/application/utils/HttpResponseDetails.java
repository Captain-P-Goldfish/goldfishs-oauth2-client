package de.captaingoldfish.restclient.application.utils;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.NameValuePair;

import lombok.Getter;


/**
 * @author Pascal Knueppel
 * @since 10.06.2024
 */
@Getter
public class HttpResponseDetails
{

  private final int statusCode;

  private final String body;

  private final Map<String, String> headers;

  public HttpResponseDetails(CloseableHttpResponse response)
  {
    this.statusCode = response.getCode();
    this.body = Utils.getBody(response);
    this.headers = Arrays.stream(response.getHeaders())
                         .collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));
  }
}
