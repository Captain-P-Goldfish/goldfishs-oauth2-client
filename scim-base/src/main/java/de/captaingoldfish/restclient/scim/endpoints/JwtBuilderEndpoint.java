package de.captaingoldfish.restclient.scim.endpoints;

import com.fasterxml.jackson.databind.JsonNode;

import de.captaingoldfish.restclient.scim.constants.ClasspathReferences;
import de.captaingoldfish.scim.sdk.common.utils.JsonHelper;
import de.captaingoldfish.scim.sdk.server.endpoints.EndpointDefinition;
import de.captaingoldfish.scim.sdk.server.endpoints.ResourceHandler;


/**
 * @author Pascal Knueppel
 * @since 27.06.2021
 */
public class JwtBuilderEndpoint extends EndpointDefinition
{

  public JwtBuilderEndpoint(ResourceHandler resourceHandler)
  {
    super(getResourceTypeSchema(), getResourceSchemaNode(), null, resourceHandler);
  }

  /**
   * @return the resource type schema for the keystore endpoint
   */
  private static JsonNode getResourceTypeSchema()
  {
    return JsonHelper.loadJsonDocument(ClasspathReferences.JWT_BUILDER_RESOURCE_TYPE);
  }

  /**
   * @return the keystore schema defined in the resource type
   */
  private static JsonNode getResourceSchemaNode()
  {
    return JsonHelper.loadJsonDocument(ClasspathReferences.JWT_BUILDER_SCHEMA);
  }
}
