package de.captaingoldfish.restclient.application.endpoints.authcodegrant;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import de.captaingoldfish.restclient.application.projectconfig.CacheConfiguration;


/**
 * @author Pascal Knueppel
 * @since 30.05.2024
 */
@Component
public class PkceCodeVerifierCache
{

  /**
   * retrieves the code-verifier of the PKCE code-challenge saved by the OAuth2 state parameter
   *
   * @param state The state-parameter from the OAuth2 request
   * @return the PKCE code-verifier of RFC7636
   */
  @Cacheable(value = CacheConfiguration.PKCE_CODE_VERIFIER_CACHE, key = "#state")
  public String getCodeVerifier(String state)
  {
    return null;
  }

  /**
   * this method will store the PKCE code_verifier parameter for the Proof Key for Code Exchange specification
   * RFC7636
   *
   * @param state The state-parameter from the OAuth2 request
   * @return the PKCE code-verifier of RFC7636
   */
  @CachePut(value = CacheConfiguration.PKCE_CODE_VERIFIER_CACHE, key = "#state")
  public String setCodeVerifier(String state, String codeVerifier)
  {
    return codeVerifier;
  }
}
