package de.captaingoldfish.restclient.application.endpoints.tokenrequest.request;

import java.util.Map;

import kong.unirest.HttpResponse;


/**
 * @author Pascal Knueppel
 * @since 21.08.2021
 */
public interface AccessTokenRequestBuilder
{

  /**
   * @return the request header that will be sent to the access token endpoint
   */
  public Map<String, String> getRequestHeaders();

  /**
   * @return the request parameters that will be sent to the access token endpoint
   */
  public Map<String, String> getRequestParameters();

  /**
   * sends the request to the access token endpoint and returns the response from the identity provider
   */
  public HttpResponse<String> sendAccessTokenRequest();
}
