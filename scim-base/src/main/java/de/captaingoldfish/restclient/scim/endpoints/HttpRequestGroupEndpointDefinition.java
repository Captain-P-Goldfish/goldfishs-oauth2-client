package de.captaingoldfish.restclient.scim.endpoints;

import de.captaingoldfish.restclient.scim.constants.ClasspathReferences;
import de.captaingoldfish.restclient.scim.resources.ScimHttpRequestGroup;
import de.captaingoldfish.scim.sdk.common.utils.JsonHelper;
import de.captaingoldfish.scim.sdk.server.endpoints.EndpointDefinition;
import de.captaingoldfish.scim.sdk.server.endpoints.ResourceHandler;


public class HttpRequestGroupEndpointDefinition extends EndpointDefinition
{

  public HttpRequestGroupEndpointDefinition(ResourceHandler<ScimHttpRequestGroup> resourceHandler)
  {
    super(JsonHelper.loadJsonDocument(ClasspathReferences.HTTP_REQUESTS_GROUP_RESOURCE_TYPE),
          JsonHelper.loadJsonDocument(ClasspathReferences.HTTP_REQUESTS_GROUP_SCHEMA), null, resourceHandler);
  }
}
