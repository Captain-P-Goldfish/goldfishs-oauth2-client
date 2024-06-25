package de.captaingoldfish.restclient.application.endpoints.tokenrequest.request;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import de.captaingoldfish.restclient.application.crypto.DpopBuilder;
import de.captaingoldfish.restclient.application.utils.OAuthConstants;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;
import de.captaingoldfish.restclient.scim.resources.ScimCurrentWorkflowSettings;
import de.captaingoldfish.scim.sdk.common.constants.HttpHeader;


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

  public ResourcePasswordTokenRequestBuilder(OpenIdClient openIdClient,
                                             String username,
                                             String password,
                                             String scope,
                                             ScimCurrentWorkflowSettings currentWorkflowSettings,
                                             DpopBuilder dpopBuilder)
  {
    super(openIdClient, currentWorkflowSettings, dpopBuilder);
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

  @Override
  public Map<String, String> getRequestHeaders()
  {
    Map<String, String> headers = super.getRequestHeaders();
    if (!headers.containsKey(HttpHeader.AUTHORIZATION))
    {
      headers.put(HttpHeader.AUTHORIZATION,
                  "Basic " + Base64.getEncoder()
                                   .encodeToString(String.format("%s:%s",
                                                                 openIdClient.getClientId(),
                                                                 StringUtils.stripToEmpty(openIdClient.getClientSecret()))
                                                         .getBytes(StandardCharsets.UTF_8)));
    }
    return headers;
  }
}
