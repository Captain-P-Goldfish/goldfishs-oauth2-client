package de.captaingoldfish.restclient.scim.endpoints;

import java.util.Arrays;

import de.captaingoldfish.restclient.scim.constants.ClasspathReferences;
import de.captaingoldfish.restclient.scim.resources.ScimHttpRequest;
import de.captaingoldfish.scim.sdk.common.utils.JsonHelper;
import de.captaingoldfish.scim.sdk.server.endpoints.EndpointDefinition;
import de.captaingoldfish.scim.sdk.server.endpoints.ResourceHandler;


public class HttpRequestEndpointDefinition extends EndpointDefinition
{

  public HttpRequestEndpointDefinition(ResourceHandler<ScimHttpRequest> resourceHandler)
  {
    super(JsonHelper.loadJsonDocument(ClasspathReferences.HTTP_REQUESTS_RESOURCE_TYPE),
          JsonHelper.loadJsonDocument(ClasspathReferences.HTTP_REQUESTS_SCHEMA),
          Arrays.asList(JsonHelper.loadJsonDocument(ClasspathReferences.HTTP_CLIENT_SETTINGS_SCHEMA)), resourceHandler);
  }
}
