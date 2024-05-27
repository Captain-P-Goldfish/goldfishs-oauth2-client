package de.captaingoldfish.restclient.application.endpoints.tokenrequest.request;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpHeaders;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;

import de.captaingoldfish.restclient.application.crypto.DpopBuilder;
import de.captaingoldfish.restclient.application.utils.OAuthConstants;
import de.captaingoldfish.restclient.application.utils.Utils;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;
import de.captaingoldfish.restclient.scim.resources.ScimAccessTokenRequest;
import de.captaingoldfish.restclient.scim.resources.ScimCurrentWorkflowSettings;
import de.captaingoldfish.scim.sdk.common.utils.JsonHelper;
import kong.unirest.HttpResponse;
import kong.unirest.UnirestInstance;
import lombok.Getter;


/**
 * @author Pascal Knueppel
 * @since 21.08.2021
 */
public abstract class AccessTokenRequestBuilder
{

  /**
   * used to get the client parameters
   */
  protected final OpenIdClient openIdClient;

  /**
   * the current workflow settings to use with the token-request
   */
  private final ScimCurrentWorkflowSettings currentWorkflowSettings;

  /**
   * used to add DPoP headers to the access token request if wanted
   */
  private final DpopBuilder dpopBuilder;

  /**
   * the request headers to send to the access token endpoint
   */
  @Getter
  private Map<String, String> requestHeaders;

  /**
   * the request parameters to send to the access token endpoint
   */
  @Getter
  private Map<String, String> requestParameters;

  public AccessTokenRequestBuilder(OpenIdClient openIdClient,
                                   ScimCurrentWorkflowSettings currentWorkflowSettings,
                                   DpopBuilder dpopBuilder)
  {
    this.openIdClient = openIdClient;
    Authenticator authenticator = AuthenticatorFactory.getAuthenticator(openIdClient);
    this.requestHeaders = authenticator.getRequestHeader();
    this.requestParameters = authenticator.getRequestParameter();
    this.currentWorkflowSettings = currentWorkflowSettings;
    this.dpopBuilder = dpopBuilder;
  }

  /**
   * extends the request headers that are required for the specific access token request
   */
  protected void addRequestHeaders(Map<String, String> requestHeaders, OIDCProviderMetadata metadata)
  {
    requestHeaders.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
    dpopBuilder.generateDpopAccessTokenHeader(currentWorkflowSettings, metadata).ifPresent(dpopHeaderaValue -> {
      requestHeaders.put(OAuthConstants.DPOP_HEADER, dpopHeaderaValue);
    });
  }

  /**
   * extends the request parameters that are required for the specific access token request
   */
  protected abstract void addRequestParameters(Map<String, String> requestParameters);

  /**
   * sends the request to the access token endpoint and returns the response from the identity provider
   */
  public HttpResponse<String> sendAccessTokenRequest()
  {
    OIDCProviderMetadata metadata = Utils.loadDiscoveryEndpointInfos(openIdClient);

    addRequestHeaders(this.requestHeaders, metadata);
    addRequestParameters(this.requestParameters);
    Map<String, String> requestHeaders = getRequestHeaders();
    Map<String, String> requestParameters = getRequestParameters();

    String tokenEndpoint = metadata.getTokenEndpointURI().toString();

    Optional.ofNullable(currentWorkflowSettings)
            .flatMap(ScimCurrentWorkflowSettings::getDpop)
            .flatMap(dpop -> dpopBuilder.generateDpopAccessTokenHeader(currentWorkflowSettings, metadata, null))
            .ifPresent(dpopString -> {
              requestHeaders.put(OAuthConstants.DPOP_HEADER, dpopString);
            });

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

  /**
   * retrieves the current metadata from the OpenID Connect provider as json string
   */
  public String getMetaDataString()
  {
    OIDCProviderMetadata metadata = Utils.loadDiscoveryEndpointInfos(openIdClient);
    return metadata.toJSONObject().toJSONString();
  }

  /**
   * generates additional headers based on the accessToken response of the authorization-server
   *
   * @param accessTokenResponse the accessToken response of the token-endpoint of the authorization-server
   */
  public List<ScimAccessTokenRequest.HttpHeaders> getResourceEndpointHeaders(HttpResponse<String> accessTokenResponse)
  {
    List<ScimAccessTokenRequest.HttpHeaders> resourceEndpointHeaders = new ArrayList<>();
    if (accessTokenResponse.isSuccess())
    {
      ObjectNode accessTokenResponseNode = JsonHelper.readJsonDocument(accessTokenResponse.getBody(), ObjectNode.class);
      String tokenType = accessTokenResponseNode.get(OAuthConstants.TOKEN_TYPE).textValue();
      if (OAuthConstants.DPOP_TOKEN_TYPE.equalsIgnoreCase(tokenType))
      {
        OIDCProviderMetadata metadata = Utils.loadDiscoveryEndpointInfos(openIdClient);
        final String accessToken = accessTokenResponseNode.get(OAuthConstants.ACCESS_TOKEN).textValue();
        Optional.ofNullable(currentWorkflowSettings)
                .flatMap(ScimCurrentWorkflowSettings::getDpop)
                .flatMap(dpop -> dpopBuilder.generateDpopAccessTokenHeader(currentWorkflowSettings,
                                                                           metadata,
                                                                           accessToken))
                .ifPresent(dpopString -> {
                  resourceEndpointHeaders.add(new ScimAccessTokenRequest.HttpHeaders(OAuthConstants.DPOP_HEADER,
                                                                                     dpopString));
                });
      }
    }
    return resourceEndpointHeaders;
  }
}
