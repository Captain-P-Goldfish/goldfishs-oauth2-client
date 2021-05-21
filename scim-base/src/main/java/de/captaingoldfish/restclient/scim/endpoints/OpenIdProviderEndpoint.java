package de.captaingoldfish.restclient.scim.endpoints;

import com.fasterxml.jackson.databind.JsonNode;

import de.captaingoldfish.restclient.scim.constants.ClasspathReferences;
import de.captaingoldfish.restclient.scim.resources.ScimOpenIdProvider;
import de.captaingoldfish.scim.sdk.common.utils.JsonHelper;
import de.captaingoldfish.scim.sdk.server.endpoints.EndpointDefinition;
import de.captaingoldfish.scim.sdk.server.endpoints.ResourceHandler;


/**
 * @author Pascal Knueppel
 * @since 21.05.2021
 */
public class OpenIdProviderEndpoint extends EndpointDefinition
{

  public OpenIdProviderEndpoint(ResourceHandler<ScimOpenIdProvider> resourceHandler)
  {
    super(getResourceTypeSchema(), getResourceSchemaNode(), null, resourceHandler);
  }

  /**
   * @return the resource type schema for the OpenID Provider endpoint
   */
  private static JsonNode getResourceTypeSchema()
  {
    return JsonHelper.loadJsonDocument(ClasspathReferences.OPEN_ID_PROVIDER_RESOURCE_TYPE);
  }

  /**
   * @return the OpenID Provider schema defined in the resource type
   */
  private static JsonNode getResourceSchemaNode()
  {
    return JsonHelper.loadJsonDocument(ClasspathReferences.OPEN_ID_PROVIDER_SCHEMA);
  }
}
