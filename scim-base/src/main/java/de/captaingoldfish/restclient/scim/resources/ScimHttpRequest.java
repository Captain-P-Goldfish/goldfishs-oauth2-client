package de.captaingoldfish.restclient.scim.resources;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import de.captaingoldfish.scim.sdk.common.resources.ResourceNode;
import de.captaingoldfish.scim.sdk.common.resources.base.ScimObjectNode;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import lombok.Builder;


/**
 * A representation for a http request
 */
public class ScimHttpRequest extends ResourceNode
{

  public ScimHttpRequest()
  {
    setSchemas(Arrays.asList(FieldNames.SCHEMA));
  }

  @Builder
  public ScimHttpRequest(String id,
                         Meta meta,
                         String groupName,
                         String name,
                         String httpMethod,
                         String url,
                         List<HttpHeaders> requestHeaders,
                         List<HttpHeaders> responseHeaders,
                         String requestBody,
                         String responseBody,
                         ScimHttpClientSettings scimHttpClientSettings)
  {
    setSchemas(Arrays.asList(FieldNames.SCHEMA));
    setId(id);
    setMeta(meta);
    setGroupName(groupName);
    setHttpMethod(httpMethod);
    setName(name);
    setUrl(url);
    setRequestHeaders(requestHeaders);
    setResponseHeaders(responseHeaders);
    setRequestBody(requestBody);
    setResponseBody(responseBody);
    setHttpClientSettings(scimHttpClientSettings);
  }

  /**
   * the http client settings for this request
   */
  public ScimHttpClientSettings getHttpClientSettings()
  {
    return getObjectAttribute(ScimHttpClientSettings.FieldNames.SCHEMA_ID, ScimHttpClientSettings.class).orElseThrow();
  }

  /**
   * the http client settings for this request
   */
  public void setHttpClientSettings(ScimHttpClientSettings scimHttpClientSettings)
  {
    setAttribute(ScimHttpClientSettings.FieldNames.SCHEMA_ID, scimHttpClientSettings);
  }

  /**
   * the name of the parent category into which this request will be grouped.
   */
  public String getGroupName()
  {
    return getStringAttribute(FieldNames.GROUP_NAME).orElse(null);
  }

  /**
   * the name of the parent category into which this request will be grouped.
   */
  public void setGroupName(String categoryName)
  {
    setAttribute(FieldNames.GROUP_NAME, categoryName);
  }

  /**
   * a unique name across the http request category.
   */
  public String getName()
  {
    return getStringAttribute(FieldNames.NAME).get();
  }

  /**
   * a unique name across the http request category.
   */
  public void setName(String name)
  {
    setAttribute(FieldNames.NAME, name);
  }

  /**
   * the http method that is to be used within this request.
   */
  public String getHttpMethod()
  {
    return getStringAttribute(FieldNames.HTTP_METHOD).get();
  }

  /**
   * the http method that is to be used within this request.
   */
  public void setHttpMethod(String httpMethod)
  {
    setAttribute(FieldNames.HTTP_METHOD, httpMethod);
  }

  /**
   * The url that should be accessed.
   */
  public String getUrl()
  {
    return getStringAttribute(FieldNames.URL).get();
  }

  /**
   * The url that should be accessed.
   */
  public void setUrl(String url)
  {
    setAttribute(FieldNames.URL, url);
  }

  /**
   * the http header values that will be added to the request
   */
  public List<HttpHeaders> getRequestHeaders()
  {
    return getArrayAttribute(FieldNames.REQUESTHEADERS, HttpHeaders.class);
  }

  /**
   * the http header values that will be added to the request
   */
  public void setRequestHeaders(List<HttpHeaders> requestHeaders)
  {
    setAttribute(FieldNames.REQUESTHEADERS, requestHeaders);
  }

  /**
   * the http header values that will be added to the request
   */
  public List<HttpHeaders> getResponseHeaders()
  {
    return getArrayAttribute(FieldNames.RESPONSEHEADERS, HttpHeaders.class);
  }

  /**
   * the http header values that will be added to the request
   */
  public void setResponseHeaders(List<HttpHeaders> responseHeaders)
  {
    setAttribute(FieldNames.RESPONSEHEADERS, responseHeaders);
  }

  /**
   * the request body that will be sent to the server.
   */
  public Optional<String> getRequestBody()
  {
    return getStringAttribute(FieldNames.REQUESTBODY);
  }

  /**
   * the request body that will be sent to the server.
   */
  public void setRequestBody(String requestBody)
  {
    setAttribute(FieldNames.REQUESTBODY, requestBody);
  }

  /**
   * The response body from the service that was accessed.
   */
  public Optional<String> getResponseBody()
  {
    return getStringAttribute(FieldNames.RESPONSEBODY);
  }

  /**
   * The response body from the service that was accessed.
   */
  public void setResponseBody(String responseBody)
  {
    setAttribute(FieldNames.RESPONSEBODY, responseBody);
  }

  /**
   * The response history of this request
   */
  public List<String> getResponseHistory()
  {
    return getSimpleArrayAttribute(FieldNames.RESPONSEHISTORY);
  }

  /**
   * The response history of this request
   */
  public void setResponseHistory(List<String> responseHistory)
  {
    setAttributeList(FieldNames.RESPONSEHISTORY, responseHistory);
  }


  /**
   * the http header values that will be added to the request
   */
  public static class HttpHeaders extends ScimObjectNode
  {

    public HttpHeaders()
    {}

    @Builder
    public HttpHeaders(String name, String value)
    {
      setName(name);
      setValue(value);
    }

    /**
     * the http-header name.
     */
    public String getName()
    {
      return getStringAttribute(FieldNames.NAME).get();
    }

    /**
     * the http-header name.
     */
    public void setName(String name)
    {
      setAttribute(FieldNames.NAME, name);
    }

    /**
     * the http-header value.
     */
    public String getValue()
    {
      return getStringAttribute(FieldNames.VALUE).orElse(null);
    }

    /**
     * the http-header value.
     */
    public void setValue(String value)
    {
      setAttribute(FieldNames.VALUE, value);
    }
  }

  /**
   * contains the attribute names of the resource representation
   */
  public static class FieldNames
  {

    public static final String SCHEMA = "urn:ietf:params:scim:schemas:captaingoldfish:2.0:HttpRequest";


    public static final String URL = "url";

    public static final String GROUP_NAME = "groupName";

    public static final String REQUESTHEADERS = "requestHeaders";


    public static final String NAME = "name";

    public static final String HTTP_METHOD = "httpMethod";

    public static final String VALUE = "value";

    public static final String RESPONSEHEADERS = "responseHeaders";

    public static final String REQUESTBODY = "requestBody";

    public static final String RESPONSEBODY = "responseBody";

    public static final String RESPONSEHISTORY = "responseHistory";

  }
}
