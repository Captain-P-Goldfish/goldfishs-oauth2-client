package de.captaingoldfish.restclient.application.endpoints.tokenrequest.request;

import java.util.Map;
import java.util.Optional;

import de.captaingoldfish.restclient.application.crypto.DpopBuilder;
import de.captaingoldfish.restclient.application.endpoints.authcodegrant.PkceCodeVerifierCache;
import de.captaingoldfish.restclient.application.utils.OAuthConstants;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;
import de.captaingoldfish.restclient.scim.resources.ScimCurrentWorkflowSettings;


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
   * The state from the authorizationRequest. We use it here to access the optional PKCE code_verifier if one
   * was created in the AuthorizationRequest
   */
  private final String state;

  /**
   * the redirect uri that must be appended to the request if it was also present within the authorization
   * request
   */
  private final String redirectUri;

  /**
   * used to access to PKCE code-verifier if one was used in the authorizationRequest
   */
  private final PkceCodeVerifierCache pkceCodeVerifierCache;


  public AuthCodeTokenRequestBuilder(OpenIdClient openIdClient,
                                     String authorizationCode,
                                     String state,
                                     String redirectUri,
                                     ScimCurrentWorkflowSettings currentWorkflowSettings,
                                     DpopBuilder dpopBuilder,
                                     PkceCodeVerifierCache pkceCodeVerifierCache)
  {
    super(openIdClient, currentWorkflowSettings, dpopBuilder);
    this.authorizationCode = authorizationCode;
    this.state = state;
    this.redirectUri = redirectUri;
    this.pkceCodeVerifierCache = pkceCodeVerifierCache;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addRequestParameters(Map<String, String> requestParameters)
  {
    requestParameters.put(OAuthConstants.GRANT_TYPE, OAuthConstants.AUTH_CODE_GRANT_TYPE);
    requestParameters.put(OAuthConstants.REDIRECT_URI, redirectUri);
    requestParameters.put(OAuthConstants.CLIENT_ID, openIdClient.getClientId());
    requestParameters.put(OAuthConstants.CODE, authorizationCode);
    Optional.ofNullable(state).map(pkceCodeVerifierCache::getCodeVerifier).ifPresent(codeVerifier -> {
      requestParameters.put(OAuthConstants.CODE_VERIFIER, codeVerifier);
    });
    Optional.ofNullable(redirectUri).ifPresent(uri -> requestParameters.put(OAuthConstants.REDIRECT_URI, uri));
  }
}
