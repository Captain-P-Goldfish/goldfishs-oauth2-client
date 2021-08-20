package de.captaingoldfish.restclient.scim.endpoints;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import de.captaingoldfish.restclient.scim.constants.ClasspathReferences;
import de.captaingoldfish.scim.sdk.common.utils.JsonHelper;
import de.captaingoldfish.scim.sdk.server.endpoints.EndpointDefinition;
import de.captaingoldfish.scim.sdk.server.endpoints.ResourceHandler;


/**
 * @author Pascal Knueppel
 * @since 20.08.2021
 */
public class AuthCodeGrantRequestEndpoint extends EndpointDefinition
{

  public AuthCodeGrantRequestEndpoint(ResourceHandler resourceHandler)
  {
    super(getResourceTypeSchema(), getResourceSchemaNode(), getExtensions(), resourceHandler);
  }

  /**
   * @return the resource type schema for the auth code grant request endpoint
   */
  private static JsonNode getResourceTypeSchema()
  {
    return JsonHelper.loadJsonDocument(ClasspathReferences.AUTH_CODE_GRANT_REQUEST_RESOURCE_TYPE);
  }

  /**
   * @return the auth code grant request schema defined in the resource type
   */
  private static JsonNode getResourceSchemaNode()
  {
    return JsonHelper.loadJsonDocument(ClasspathReferences.AUTH_CODE_GRANT_REQUEST_SCHEMA);
  }

  /**
   * @return the extension of the auth code grant request resource type endpoint definition
   */
  private static List<JsonNode> getExtensions()
  {
    return List.of(JsonHelper.loadJsonDocument(ClasspathReferences.CURRENT_WORKFLOW_SETTINGS_SCHEMA));
  }
}
