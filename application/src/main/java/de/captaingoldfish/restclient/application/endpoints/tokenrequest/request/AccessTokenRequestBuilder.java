package de.captaingoldfish.restclient.application.endpoints.tokenrequest.request;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpHeaders;

import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;

import de.captaingoldfish.restclient.application.utils.Utils;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;
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
   * the request headers to send to the access token endpoint
   */
  @Getter
  private Map<String, String> requestHeaders;

  /**
   * the request parameters to send to the access token endpoint
   */
  @Getter
  private Map<String, String> requestParameters;

  public AccessTokenRequestBuilder(OpenIdClient openIdClient)
  {
    this.openIdClient = openIdClient;
    Authenticator authenticator = AuthenticatorFactory.getAuthenticator(openIdClient);
    this.requestHeaders = authenticator.getRequestHeader();
    this.requestParameters = authenticator.getRequestParameter();
  }

  /**
   * extends the request headers that are required for the specific access token request
   */
  protected void addRequestHeaders(Map<String, String> requestHeaders)
  {
    requestHeaders.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
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
    addRequestHeaders(this.requestHeaders);
    addRequestParameters(this.requestParameters);
    Map<String, String> requestHeaders = getRequestHeaders();
    Map<String, String> requestParameters = getRequestParameters();

    OIDCProviderMetadata metadata = Utils.loadDiscoveryEndpointInfos(openIdClient);
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

  /**
   * retrieves the current metadata from the OpenID Connect provider as json string
   */
  public String getMetaDataString()
  {
    OIDCProviderMetadata metadata = Utils.loadDiscoveryEndpointInfos(openIdClient);
    return metadata.toJSONObject().toJSONString();
  }
}
