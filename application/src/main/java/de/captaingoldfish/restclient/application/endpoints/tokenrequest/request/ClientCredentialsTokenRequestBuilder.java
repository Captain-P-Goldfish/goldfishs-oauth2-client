package de.captaingoldfish.restclient.application.endpoints.tokenrequest.request;

import java.util.Map;
import java.util.Optional;

import de.captaingoldfish.restclient.application.utils.OAuthConstants;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;


/**
 * builds the request headers and parameters for a client-credentials access-token request
 * 
 * @author Pascal Knueppel
 * @since 22.08.2021
 */
public class ClientCredentialsTokenRequestBuilder extends AccessTokenRequestBuilder
{

  /**
   * an optional scope parameter that may be added to the access token requests
   */
  private final String scope;

  public ClientCredentialsTokenRequestBuilder(OpenIdClient openIdClient, String scope)
  {
    super(openIdClient);
    this.scope = scope;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addRequestParameters(Map<String, String> requestParameters)
  {
    requestParameters.put(OAuthConstants.GRANT_TYPE, OAuthConstants.CLIENT_CREDENTIALS_GRANT_TYPE);
    Optional.ofNullable(scope).ifPresent(s -> {
      requestParameters.put(OAuthConstants.SCOPE, s);
    });
  }


}
