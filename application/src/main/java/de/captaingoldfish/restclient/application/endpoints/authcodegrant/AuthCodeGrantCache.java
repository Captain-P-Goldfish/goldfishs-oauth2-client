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
public class AuthCodeGrantCache
{

  /**
   * retrieves the authorization code response from an identity provider
   * 
   * @param state The state-parameter from the OAuth2 request
   * @return the full authorization code response url
   */
  @Cacheable(value = CacheConfiguration.AUTH_CODE_GRANT_REQUEST_CACHE, key = "#state")
  public String getAuthorizationResponseUrl(String state)
  {
    return null;
  }

  /**
   * this method will store the authorization response of an identity provider so it can be retrieved by the
   * javascript frontend
   * 
   * @param state The state-parameter from the OAuth2 request
   * @return the full authorization code response url
   */
  @CachePut(value = CacheConfiguration.AUTH_CODE_GRANT_REQUEST_CACHE, key = "#state")
  public String setAuthorizationResponsUrl(String state, String authorizationResponseUrl)
  {
    return authorizationResponseUrl;
  }
}
