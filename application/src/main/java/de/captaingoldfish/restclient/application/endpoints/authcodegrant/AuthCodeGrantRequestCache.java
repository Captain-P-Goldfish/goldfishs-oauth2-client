package de.captaingoldfish.restclient.application.endpoints.authcodegrant;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import de.captaingoldfish.restclient.application.projectconfig.CacheConfiguration;


/**
 * @author Pascal Knueppel
 * @since 20.08.2021
 */
@Component
public class AuthCodeGrantRequestCache
{

  /**
   * retrieves the authorization code request that was generated to delegate to an identity provider
   * 
   * @param state The state-parameter from the OAuth2 request
   * @return the full authorization code request url
   */
  @Cacheable(value = CacheConfiguration.AUTH_CODE_GRANT_REQUEST_CACHE, key = "#state")
  public String getAuthorizationRequestUrl(String state)
  {
    return null;
  }

  /**
   * this method will store the authorization code request for an identity provider, to validate the
   * authorization response of an identity provider
   * 
   * @param state The state-parameter from the OAuth2 request
   * @return the full authorization code request url
   */
  @CachePut(value = CacheConfiguration.AUTH_CODE_GRANT_REQUEST_CACHE, key = "#state")
  public String setAuthorizationRequestUrl(String state, String authorizationRequestUrl)
  {
    return authorizationRequestUrl;
  }
}
