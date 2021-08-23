package de.captaingoldfish.restclient.application.endpoints.tokenrequest.request;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import de.captaingoldfish.restclient.application.utils.OAuthConstants;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;


/**
 * @author Pascal Knueppel
 * @since 22.08.2021
 */
public class ResourcePasswordTokenRequestBuilder extends AccessTokenRequestBuilder
{

  /**
   * the name of the user that is being authenticated
   */
  private final String username;

  /**
   * the password of the user that is being authenticated
   */
  private final String password;

  /**
   * an optional scope parameter that may be added to the access token requests
   */
  private final String scope;

  public ResourcePasswordTokenRequestBuilder(OpenIdClient openIdClient, String username, String password, String scope)
  {
    super(openIdClient);
    this.username = username;
    this.password = password;
    this.scope = StringUtils.stripToNull(scope);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void addRequestParameters(Map<String, String> requestParameters)
  {
    requestParameters.put(OAuthConstants.GRANT_TYPE, OAuthConstants.RESOURCE_PASSWORD_GRANT_TYPE);
    requestParameters.put(OAuthConstants.USERNAME, username);
    requestParameters.put(OAuthConstants.PASSWORD, password);
    Optional.ofNullable(scope).ifPresent(s -> {
      requestParameters.put(OAuthConstants.SCOPE, s);
    });
  }
}
