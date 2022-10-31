package de.captaingoldfish.restclient.scim.endpoints;

import java.util.Arrays;

import de.captaingoldfish.restclient.scim.constants.ClasspathReferences;
import de.captaingoldfish.restclient.scim.resources.ScimHttpResponseHistory;
import de.captaingoldfish.scim.sdk.common.utils.JsonHelper;
import de.captaingoldfish.scim.sdk.server.endpoints.EndpointDefinition;
import de.captaingoldfish.scim.sdk.server.endpoints.ResourceHandler;


public class HttpResponseHistoryEndpointDefinition extends EndpointDefinition
{

  public HttpResponseHistoryEndpointDefinition(ResourceHandler<ScimHttpResponseHistory> resourceHandler)
  {
    super(JsonHelper.loadJsonDocument(ClasspathReferences.HTTP_RESPONSE_HISTORY_RESOURCE_TYPE),
          JsonHelper.loadJsonDocument(ClasspathReferences.HTTP_RESPONSE_HISTORY_SCHEMA), Arrays.asList(),
          resourceHandler);
  }
}
