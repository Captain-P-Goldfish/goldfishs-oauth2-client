package de.captaingoldfish.restclient.application.endpoints.tokenrequest.request;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpHeaders;

import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;

import de.captaingoldfish.restclient.application.utils.Utils;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;
import kong.unirest.HttpResponse;
import kong.unirest.UnirestInstance;


/**
 * @author Pascal Knueppel
 * @since 21.08.2021
 */
public class AuthCodeTokenRequestBuilder implements AccessTokenRequestBuilder
{

  /**
   * the grant type to be used for the authorization code grant
   */
  private static final String GRANT_TYPE = "authorization_code";

  /**
   * used to get the client parameters
   */
  private final OpenIdClient openIdClient;

  /**
   * the authorization code that was received from the idp
   */
  private final String authorizationCode;

  /**
   * the redirect uri that must be appended to the request if it was also present within the authorization
   * request
   */
  private final String redirectUri;

  /**
   * the authentication implementation to use (basic or jwt)
   */
  private final Authenticator authenticator;

  /**
   * the request headers to send to the access token endpoint
   */
  private Map<String, String> requestHeaders;

  /**
   * the request parameters to send to the access token endpoint
   */
  private Map<String, String> requestParameters;

  public AuthCodeTokenRequestBuilder(OpenIdClient openIdClient, String authorizationCode, String redirectUri)
  {
    this.openIdClient = openIdClient;
    this.authorizationCode = authorizationCode;
    this.redirectUri = redirectUri;
    this.authenticator = AuthenticatorFactory.getAuthenticator(openIdClient);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, String> getRequestHeaders()
  {
    if (requestHeaders != null)
    {
      return requestHeaders;
    }
    requestHeaders = authenticator.getRequestHeader();
    requestHeaders.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
    return requestHeaders;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, String> getRequestParameters()
  {
    if (requestParameters != null)
    {
      return requestParameters;
    }
    requestParameters = authenticator.getRequestParameter();
    requestParameters.put("grant_type", GRANT_TYPE);
    requestParameters.put("client_id", openIdClient.getClientId());
    requestParameters.put("code", authorizationCode);
    Optional.ofNullable(redirectUri).ifPresent(uri -> requestParameters.put("redirect_uri", uri));
    return requestParameters;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public HttpResponse<String> sendAccessTokenRequest()
  {
    Map<String, String> requestHeaders = getRequestHeaders();
    Map<String, String> requestParameters = getRequestParameters();

    OIDCProviderMetadata metadata = Utils.loadDiscoveryEndpointInfos(openIdClient.getOpenIdProvider());
    String tokenEndpoint = metadata.getTokenEndpointURI().toString();

    try (UnirestInstance unirest = Utils.getUnirestInstance(openIdClient))
    {
      Map<String, Object> unirestParamsMap = new HashMap<>(requestParameters);
      return unirest.post(tokenEndpoint)
                    .headers(requestHeaders)
                    .fields(unirestParamsMap)
                    .charset(StandardCharsets.UTF_8)
                    .asString();
    }
  }
}
