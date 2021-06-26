package de.captaingoldfish.restclient.application.oidc;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import de.captaingoldfish.restclient.application.projectconfig.CacheConfiguration;


/**
 * @author Pascal Knueppel
 * @since 10.06.2021
 */
@Component
public class OidcStateCache
{

  /**
   * shows if the state parameter from a authorization code response belongs to a request or not
   *
   * @param state the state of the authorization code response
   */
  @Cacheable(value = CacheConfiguration.OIDC_STATE_CACHE, key = "#state")
  public String getState(String state)
  {
    return null;
  }

  /**
   * puts the the state from an authorization request into the cache to check eventually if the returned state
   * is valid
   *
   * @param state the state value from the authorization request
   */
  @CachePut(value = CacheConfiguration.OIDC_STATE_CACHE, key = "#state")
  public String setKeystoreFile(String state)
  {
    return state;
  }
}
