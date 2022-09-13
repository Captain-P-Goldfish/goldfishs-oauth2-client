package de.captaingoldfish.restclient.scim.endpoints;

import de.captaingoldfish.restclient.scim.constants.ClasspathReferences;
import de.captaingoldfish.restclient.scim.resources.ScimHttpRequestCategory;
import de.captaingoldfish.scim.sdk.common.utils.JsonHelper;
import de.captaingoldfish.scim.sdk.server.endpoints.EndpointDefinition;
import de.captaingoldfish.scim.sdk.server.endpoints.ResourceHandler;


public class HttpRequestCategoryEndpointDefinition extends EndpointDefinition
{

  public HttpRequestCategoryEndpointDefinition(ResourceHandler<ScimHttpRequestCategory> resourceHandler)
  {
    super(JsonHelper.loadJsonDocument(ClasspathReferences.HTTP_REQUESTS_CATEGORY_RESOURCE_TYPE),
          JsonHelper.loadJsonDocument(ClasspathReferences.HTTP_REQUESTS_CATEGORY_SCHEMA), null, resourceHandler);
  }
}
