package de.captaingoldfish.restclient.application.endpoints.tokenrequest.request;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpHeaders;

import lombok.Data;
import lombok.RequiredArgsConstructor;


/**
 * used to authenticate at the token endpoint with basic client credentials
 * 
 * @author Pascal Knueppel
 * @since 21.08.2021
 */
@Data
@RequiredArgsConstructor
public class BasicAuthenticator implements Authenticator
{

  /**
   * the clientId for the authentication
   */
  private final String clientId;

  /**
   * the client secret for the authentication
   */
  private final String clientSecret;

  /**
   * creates the basic authorization request header
   */
  @Override
  public Map<String, String> getRequestHeader()
  {
    final String urlEncodedAuthHeaderValue = String.format("%s:%s",
                                                           URLEncoder.encode(clientId, StandardCharsets.UTF_8),
                                                           URLEncoder.encode(clientSecret, StandardCharsets.UTF_8));
    final String base64AuthValue = Base64.getEncoder()
                                         .encodeToString(urlEncodedAuthHeaderValue.getBytes(StandardCharsets.UTF_8));
    final String authorizationHeaderValue = String.format("Basic %s", base64AuthValue);
    Map<String, String> requestHeader = new HashMap<>();
    requestHeader.put(HttpHeaders.AUTHORIZATION, authorizationHeaderValue);
    return requestHeader;
  }

  /**
   * no additional parameters are needed in case of basic authentication
   */
  @Override
  public Map<String, String> getRequestParameter()
  {
    return new HashMap<>();
  }
}
