package de.captaingoldfish.restclient.scim.endpoints;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import de.captaingoldfish.restclient.scim.constants.ClasspathReferences;
import de.captaingoldfish.scim.sdk.common.utils.JsonHelper;
import de.captaingoldfish.scim.sdk.server.endpoints.EndpointDefinition;
import de.captaingoldfish.scim.sdk.server.endpoints.ResourceHandler;


/**
 * @author Pascal Knueppel
 * @since 28.05.2021
 */
public class OpenIdClientEndpoint extends EndpointDefinition
{

  public OpenIdClientEndpoint(ResourceHandler resourceHandler)
  {
    super(getResourceTypeSchema(), getResourceSchemaNode(), getExtensions(), resourceHandler);
  }

  /**
   * @return the resource type schema for the keystore endpoint
   */
  private static JsonNode getResourceTypeSchema()
  {
    return JsonHelper.loadJsonDocument(ClasspathReferences.OPEN_ID_CLIENT_RESOURCE_TYPE);
  }

  /**
   * @return the openid client schema defined in the resource type
   */
  private static JsonNode getResourceSchemaNode()
  {
    return JsonHelper.loadJsonDocument(ClasspathReferences.OPEN_ID_CLIENT_SCHEMA);
  }

  /**
   * @return the extension of the openid client resource type endpoint definition
   */
  private static List<JsonNode> getExtensions()
  {
    return List.of(JsonHelper.loadJsonDocument(ClasspathReferences.HTTP_CLIENT_SETTINGS_SCHEMA),
                   JsonHelper.loadJsonDocument(ClasspathReferences.CURRENT_WORKFLOW_SETTINGS_SCHEMA));
  }
}
