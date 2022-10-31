package de.captaingoldfish.restclient.scim.resources;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import de.captaingoldfish.scim.sdk.common.resources.ResourceNode;
import de.captaingoldfish.scim.sdk.common.resources.base.ScimObjectNode;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import lombok.Builder;


/**
 * the response history for a saved http request
 */
public class ScimHttpResponseHistory extends ResourceNode
{

  public ScimHttpResponseHistory()
  {
    setSchemas(Arrays.asList(FieldNames.SCHEMA));
  }

  @Builder
  public ScimHttpResponseHistory(String id,
                                 Meta meta,
                                 String groupName,
                                 String requestName,
                                 List<ResponseHistory> responseHistory)
  {
    setSchemas(Arrays.asList(FieldNames.SCHEMA));
    setId(id);
    setMeta(meta);
    setGroupName(groupName);
    setRequestName(requestName);
    setResponseHistory(responseHistory);
  }

  /**
   * the name of the group to which the request belongs.
   */
  public Optional<String> getGroupName()
  {
    return getStringAttribute(FieldNames.GROUPNAME);

  }

  /**
   * the name of the group to which the request belongs.
   */
  public void setGroupName(String groupName)
  {
    setAttribute(FieldNames.GROUPNAME, groupName);
  }

  /**
   * the name of the saved http request.
   */
  public Optional<String> getRequestName()
  {
    return getStringAttribute(FieldNames.REQUESTNAME);

  }

  /**
   * the name of the saved http request.
   */
  public void setRequestName(String requestName)
  {
    setAttribute(FieldNames.REQUESTNAME, requestName);
  }

  /**
   * the response history of the http request.
   */
  public List<ResponseHistory> getResponseHistory()
  {
    return getArrayAttribute(FieldNames.RESPONSEHISTORY, ResponseHistory.class);
  }

  /**
   * the response history of the http request.
   */
  public void setResponseHistory(List<ResponseHistory> responseHistory)
  {
    setAttribute(FieldNames.RESPONSEHISTORY, responseHistory);
  }



  /**
   * the response history of the http request.
   */
  public static class ResponseHistory extends ScimObjectNode
  {

    public ResponseHistory()
    {}

    @Builder
    public ResponseHistory(String id,
                           Integer status,
                           String originalRequest,
                           String responseHeaders,
                           String responseBody,
                           Instant created)
    {
      setId(id);
      setStatus(status);
      setOriginalRequest(originalRequest);
      setResponseHeaders(responseHeaders);
      setResponseBody(responseBody);
      setCreated(created);
    }

    /**
     * a unique identifier for this response
     */
    public String getId()
    {
      return getStringAttribute(FieldNames.ID).orElse(null);
    }

    /**
     * a unique identifier for this response
     */
    public void setId(String id)
    {
      setAttribute(FieldNames.ID, id);
    }

    /**
     * the status of the http response.
     */
    public Integer getStatus()
    {
      return getIntegerAttribute(FieldNames.STATUS).orElse(0);
    }

    /**
     * the status of the http response.
     */
    public void setStatus(Integer status)
    {
      setAttribute(FieldNames.STATUS, status);
    }

    /**
     * the pre-formatted http request that caused this response.
     */
    public Optional<String> getOriginalRequest()
    {
      return getStringAttribute(FieldNames.ORIGINALREQUEST);

    }

    /**
     * the pre-formatted http request that caused this response.
     */
    public void setOriginalRequest(String originalRequest)
    {
      setAttribute(FieldNames.ORIGINALREQUEST, originalRequest);
    }

    /**
     * the pre-formatted http headers from the response.
     */
    public Optional<String> getResponseHeaders()
    {
      return getStringAttribute(FieldNames.RESPONSEHEADERS);

    }

    /**
     * the pre-formatted http headers from the response.
     */
    public void setResponseHeaders(String responseHeaders)
    {
      setAttribute(FieldNames.RESPONSEHEADERS, responseHeaders);
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
     * The timestamp when this response was returned to this application.
     */
    public Optional<Instant> getCreated()
    {
      return getDateTimeAttribute(FieldNames.CREATED);

    }

    /**
     * The timestamp when this response was returned to this application.
     */
    public void setCreated(Instant created)
    {
      setDateTimeAttribute(FieldNames.CREATED, created);
    }
  }

  /**
   * contains the attribute names of the resource representation
   */
  public static class FieldNames
  {

    public static final String SCHEMA = "urn:ietf:params:scim:schemas:captaingoldfish:2.0:HttpResponseHistory";


    public static final String GROUPNAME = "groupName";

    public static final String REQUESTNAME = "requestName";

    public static final String RESPONSEHISTORY = "responseHistory";

    public static final String ID = "id";

    public static final String STATUS = "status";

    public static final String ORIGINALREQUEST = "originalRequest";

    public static final String RESPONSEHEADERS = "responseHeaders";

    public static final String RESPONSEBODY = "responseBody";

    public static final String CREATED = "created";

  }
}
