package de.captaingoldfish.restclient.scim.endpoints;

import com.fasterxml.jackson.databind.JsonNode;

import de.captaingoldfish.restclient.scim.constants.ClasspathReferences;
import de.captaingoldfish.scim.sdk.common.utils.JsonHelper;
import de.captaingoldfish.scim.sdk.server.endpoints.EndpointDefinition;
import de.captaingoldfish.scim.sdk.server.endpoints.ResourceHandler;


/**
 * @author Pascal Knueppel
 * @since 21.08.2021
 */
public class AccessTokenRequestEndpoint extends EndpointDefinition
{

  public AccessTokenRequestEndpoint(ResourceHandler resourceHandler)
  {
    super(getResourceTypeSchema(), getResourceSchemaNode(), null, resourceHandler);
  }

  /**
   * @return the resource type schema for the access-token-request endpoint
   */
  private static JsonNode getResourceTypeSchema()
  {
    return JsonHelper.loadJsonDocument(ClasspathReferences.ACCESS_TOKEN_REQUEST_RESOURCE_TYPE);
  }

  /**
   * @return the access-token-request schema defined in the resource type
   */
  private static JsonNode getResourceSchemaNode()
  {
    return JsonHelper.loadJsonDocument(ClasspathReferences.ACCESS_TOKEN_REQUEST_SCHEMA);
  }
}
