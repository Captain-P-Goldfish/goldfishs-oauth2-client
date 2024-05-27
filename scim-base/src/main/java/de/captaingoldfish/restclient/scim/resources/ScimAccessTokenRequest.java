package de.captaingoldfish.restclient.scim.resources;


import java.util.Collections;
import java.util.List;
import java.util.Optional;

import de.captaingoldfish.scim.sdk.common.resources.ResourceNode;
import de.captaingoldfish.scim.sdk.common.resources.base.ScimObjectNode;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import lombok.Builder;


/**
 * This resource is used to show details about an access token request
 *
 * @author Pascal Knueppel
 * @since 21.08.2021
 */
public class ScimAccessTokenRequest extends ResourceNode
{

  public ScimAccessTokenRequest()
  {}

  @Builder
  public ScimAccessTokenRequest(String id,
                                Long openIdClientId,
                                String grantType,
                                String authorizationCode,
                                String redirectUri,
                                String username,
                                String password,
                                String scope,
                                List<RequestHeaders> requestHeadersList,
                                List<RequestParams> requestParamsList,
                                Integer statusCode,
                                List<HttpHeaders> responseHeadersList,
                                List<HttpHeaders> resourceEndpointHeaders,
                                String plainResponse,
                                String metaDataJson,
                                ScimCurrentWorkflowSettings currentWorkflowSettings,
                                Meta meta)
  {
    setSchemas(Collections.singletonList(FieldNames.SCHEMA_ID));
    setId(id);
    setOpenIdClientId(openIdClientId);
    setGrantType(grantType);
    setAuthorizationCode(authorizationCode);
    setRedirectUri(redirectUri);
    setUsername(username);
    setPassword(password);
    setScope(scope);
    setRequestHeaders(requestHeadersList);
    setRequestParams(requestParamsList);
    setStatusCode(statusCode);
    setResponseHeaders(responseHeadersList);
    setResourceEndpoint(resourceEndpointHeaders);
    setPlainResponse(plainResponse);
    setMetaDataJson(metaDataJson);
    setCurrentWorkflowSettings(currentWorkflowSettings);
    setMeta(meta);
  }

  /** The foreign key reference to an open-id-client resource. */
  public Long getOpenIdClientId()
  {
    return getLongAttribute(FieldNames.OPEN_ID_CLIENT_ID).orElse(null);
  }

  /** The foreign key reference to an open-id-client resource. */
  public void setOpenIdClientId(Long openIdClientId)
  {
    setAttribute(FieldNames.OPEN_ID_CLIENT_ID, openIdClientId);
  }

  /** The authentication grant type to use */
  public String getGrantType()
  {
    return getStringAttribute(FieldNames.GRANT_TYPE).orElse(null);
  }

  /** The authentication grant type to use */
  public void setGrantType(String grantType)
  {
    setAttribute(FieldNames.GRANT_TYPE, grantType);
  }

  /**
   * The authorization code that is only required in case that the grantType field is set to
   * 'authorization_code'
   */
  public Optional<String> getAuthorizationCode()
  {
    return getStringAttribute(FieldNames.AUTHORIZATION_CODE);
  }

  /**
   * The authorization code that is only required in case that the grantType field is set to
   * 'authorization_code'
   */
  public void setAuthorizationCode(String authorizationCode)
  {
    setAttribute(FieldNames.AUTHORIZATION_CODE, authorizationCode);
  }

  /**
   * If the redirect uri was present in the authorization code request it must also be added to the access token
   * request
   */
  public Optional<String> getRedirectUri()
  {
    return getStringAttribute(FieldNames.REDIRECT_URI);
  }

  /**
   * If the redirect uri was present in the authorization code request it must also be added to the access token
   * request
   */
  public void setRedirectUri(String redirectUri)
  {
    setAttribute(FieldNames.REDIRECT_URI, redirectUri);
  }

  /** If the grant_type is 'password' this field is required for user authentication */
  public Optional<String> getUsername()
  {
    return getStringAttribute(FieldNames.USERNAME);
  }

  /** If the grant_type is 'password' this field is required for user authentication */
  public void setUsername(String username)
  {
    setAttribute(FieldNames.USERNAME, username);
  }

  /** If the grant_type is 'password' this field is required for user authentication */
  public Optional<String> getPassword()
  {
    return getStringAttribute(FieldNames.PASSWORD);
  }

  /** If the grant_type is 'password' this field is required for user authentication */
  public void setPassword(String password)
  {
    setAttribute(FieldNames.PASSWORD, password);
  }


  /** an optional scope parameter that may be added to the access token requests */
  public Optional<String> getScope()
  {
    return getStringAttribute(FieldNames.SCOPE);
  }

  /** an optional scope parameter that may be added to the access token requests */
  public void setScope(String scope)
  {
    setAttribute(FieldNames.SCOPE, scope);
  }

  /** The response code from the AccessToken response */
  public Integer getStatusCode()
  {
    return getIntegerAttribute(FieldNames.STATUS_CODE).orElse(null);
  }

  /** The response code from the AccessToken response */
  public void setStatusCode(Integer statusCode)
  {
    setAttribute(FieldNames.STATUS_CODE, statusCode);
  }

  /** the plain response body of the AccessToken response */
  public String getPlainResponse()
  {
    return getStringAttribute(FieldNames.PLAIN_RESPONSE).orElse(null);
  }

  /** the plain response body of the AccessToken response */
  public void setPlainResponse(String plainResponse)
  {
    setAttribute(FieldNames.PLAIN_RESPONSE, plainResponse);
  }

  /**
   * the json string that represents the metadata of the openId provider
   */
  public Optional<String> getMetaDataJson()
  {
    return getStringAttribute(FieldNames.META_DATA_JSON);
  }

  /**
   * the json string that represents the metadata of the openId provider
   */
  public void setMetaDataJson(String metaDataJson)
  {
    setAttribute(FieldNames.META_DATA_JSON, metaDataJson);
  }

  /** Contains the HTTP request headers that were send in the AccessToken request */
  public List<RequestHeaders> getRequestHeaders()
  {
    return getArrayAttribute(FieldNames.REQUEST_HEADERS, RequestHeaders.class);
  }

  /** Contains the HTTP request headers that were send in the AccessToken request */
  public void setRequestHeaders(List<RequestHeaders> requestHeadersList)
  {
    setAttribute(FieldNames.REQUEST_HEADERS, requestHeadersList);
  }

  /** Contains the HTTP request parameter that were send in the AccessToken request */
  public List<RequestParams> getRequestParams()
  {
    return getArrayAttribute(FieldNames.REQUEST_PARAMS, RequestParams.class);
  }

  /** Contains the HTTP request parameter that were send in the AccessToken request */
  public void setRequestParams(List<RequestParams> requestParamsList)
  {
    setAttribute(FieldNames.REQUEST_PARAMS, requestParamsList);
  }

  /** Contains the HTTP response headers that were received with the AccessToken response */
  public List<HttpHeaders> getResponseHeaders()
  {
    return getArrayAttribute(FieldNames.RESPONSE_HEADERS, HttpHeaders.class);
  }

  /** Contains the HTTP response headers that were received with the AccessToken response */
  public void setResponseHeaders(List<HttpHeaders> responseHeadersList)
  {
    setAttribute(FieldNames.RESPONSE_HEADERS, responseHeadersList);
  }

  /**
   * This may contain additional headers that must be added to access resource-endpoints together with the
   * AccessToken
   */
  public List<HttpHeaders> getResourceEndpointHeaders()
  {
    return getArrayAttribute(FieldNames.RESOURCE_ENDPOINT_HEADERS, HttpHeaders.class);
  }

  /**
   * This may contain additional headers that must be added to access resource-endpoints together with the
   * AccessToken
   */
  public void setResourceEndpoint(List<HttpHeaders> responseHeadersList)
  {
    setAttribute(FieldNames.RESOURCE_ENDPOINT_HEADERS, responseHeadersList);
  }

  /**
   * only returned if the get-resource method is called. It contains the last used settings of previous OpenID
   * Connect workflows for this client.
   */
  public Optional<ScimCurrentWorkflowSettings> getCurrentWorkflowSettings()
  {
    return getObjectAttribute(ScimCurrentWorkflowSettings.FieldNames.SCHEMA_ID, ScimCurrentWorkflowSettings.class);
  }

  /**
   * only returned if the get-resource method is called. It contains the last used settings of previous OpenID
   * Connect workflows for this client.
   */
  public void setCurrentWorkflowSettings(ScimCurrentWorkflowSettings currentWorkflowSettings)
  {
    setAttribute(ScimCurrentWorkflowSettings.FieldNames.SCHEMA_ID, currentWorkflowSettings);
  }

  /** Contains the HTTP request headers that were send in the AccessToken request */
  public static class RequestHeaders extends ScimObjectNode
  {

    public RequestHeaders()
    {}

    public RequestHeaders(String name, String value)
    {
      setName(name);
      setValue(value);
    }

    /** The name of the HTTP header */
    public String getName()
    {
      return getStringAttribute(FieldNames.NAME).orElse(null);
    }

    /** The name of the HTTP header */
    public void setName(String name)
    {
      setAttribute(FieldNames.NAME, name);
    }

    /** The value of the HTTP header */
    public String getValue()
    {
      return getStringAttribute(FieldNames.VALUE).orElse(null);
    }

    /** The value of the HTTP header */
    public void setValue(String value)
    {
      setAttribute(FieldNames.VALUE, value);
    }


  }

  /** Contains the HTTP request parameter that were send in the AccessToken request */
  public static class RequestParams extends ScimObjectNode
  {

    public RequestParams()
    {}

    public RequestParams(String name, String value)
    {
      setName(name);
      setValue(value);
    }

    /** The name of the request parameter */
    public String getName()
    {
      return getStringAttribute(FieldNames.NAME).orElse(null);
    }

    /** The name of the request parameter */
    public void setName(String name)
    {
      setAttribute(FieldNames.NAME, name);
    }

    /** The value of the request parameter */
    public String getValue()
    {
      return getStringAttribute(FieldNames.VALUE).orElse(null);
    }

    /** The value of the request parameter */
    public void setValue(String value)
    {
      setAttribute(FieldNames.VALUE, value);
    }


  }

  /** Contains the HTTP response headers that were received with the AccessToken response */
  public static class HttpHeaders extends ScimObjectNode
  {

    public HttpHeaders()
    {}

    public HttpHeaders(String name, String value)
    {
      setName(name);
      setValue(value);
    }

    /** The name of the HTTP header */
    public String getName()
    {
      return getStringAttribute(FieldNames.NAME).orElse(null);
    }

    /** The name of the HTTP header */
    public void setName(String name)
    {
      setAttribute(FieldNames.NAME, name);
    }

    /** The value of the HTTP header */
    public String getValue()
    {
      return getStringAttribute(FieldNames.VALUE).orElse(null);
    }

    /** The value of the HTTP header */
    public void setValue(String value)
    {
      setAttribute(FieldNames.VALUE, value);
    }


  }

  public static class FieldNames
  {

    public static final String SCHEMA_ID = "urn:ietf:params:scim:schemas:captaingoldfish:2.0:AccessTokenRequest";

    public static final String REDIRECT_URI = "redirectUri";

    public static final String AUTHORIZATION_CODE = "authorizationCode";

    public static final String REQUEST_PARAMS = "requestParams";

    public static final String OPEN_ID_CLIENT_ID = "openIdClientId";

    public static final String PLAIN_RESPONSE = "plainResponse";

    public static final String META_DATA_JSON = "metaDataJson";

    public static final String PASSWORD = "password";

    public static final String REQUEST_HEADERS = "requestHeaders";

    public static final String RESPONSE_HEADERS = "responseHeaders";

    public static final String RESOURCE_ENDPOINT_HEADERS = "resourceEndpointHeaders";

    public static final String SCOPE = "scope";

    public static final String NAME = "name";

    public static final String ID = "id";

    public static final String GRANT_TYPE = "grantType";

    public static final String VALUE = "value";

    public static final String USERNAME = "username";

    public static final String STATUS_CODE = "statusCode";
  }
}
