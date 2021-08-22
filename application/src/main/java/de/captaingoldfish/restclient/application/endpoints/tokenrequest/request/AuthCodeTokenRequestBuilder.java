package de.captaingoldfish.restclient.application.endpoints.tokenrequest.request;

import java.util.Map;
import java.util.Optional;

import de.captaingoldfish.restclient.application.utils.OAuthConstants;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;


/**
 * builds the request headers and parameters for an authorization-code-grant access-token request
 * 
 * @author Pascal Knueppel
 * @since 21.08.2021
 */
public class AuthCodeTokenRequestBuilder extends AccessTokenRequestBuilder
{

  /**
   * the authorization code that was received from the idp
   */
  private final String authorizationCode;

  /**
   * the redirect uri that must be appended to the request if it was also present within the authorization
   * request
   */
  private final String redirectUri;


  public AuthCodeTokenRequestBuilder(OpenIdClient openIdClient, String authorizationCode, String redirectUri)
  {
    super(openIdClient);
    this.authorizationCode = authorizationCode;
    this.redirectUri = redirectUri;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addRequestParameters(Map<String, String> requestParameters)
  {
    requestParameters.put(OAuthConstants.GRANT_TYPE, OAuthConstants.AUTH_CODE_GRANT_TYPE);
    requestParameters.put(OAuthConstants.CLIENT_ID, openIdClient.getClientId());
    requestParameters.put(OAuthConstants.CODE, authorizationCode);
    Optional.ofNullable(redirectUri).ifPresent(uri -> requestParameters.put(OAuthConstants.REDIRECT_URI, uri));
  }
}
