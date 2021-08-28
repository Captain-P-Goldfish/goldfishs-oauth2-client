package de.captaingoldfish.restclient.scim.endpoints;

import com.fasterxml.jackson.databind.JsonNode;

import de.captaingoldfish.restclient.scim.constants.ClasspathReferences;
import de.captaingoldfish.scim.sdk.common.utils.JsonHelper;
import de.captaingoldfish.scim.sdk.server.endpoints.EndpointDefinition;
import de.captaingoldfish.scim.sdk.server.endpoints.ResourceHandler;


/**
 * @author Pascal Knueppel
 * @since 28.08.2021
 */
public class TokenStoreEndpoint extends EndpointDefinition
{

  public TokenStoreEndpoint(ResourceHandler resourceHandler)
  {
    super(getResourceTypeSchema(), getResourceSchemaNode(), null, resourceHandler);
  }

  /**
   * @return the resource type schema for the token-store endpoint
   */
  private static JsonNode getResourceTypeSchema()
  {
    return JsonHelper.loadJsonDocument(ClasspathReferences.TOKEN_STORE_RESOURCE_TYPE);
  }

  /**
   * @return the token-store schema defined in the resource type
   */
  private static JsonNode getResourceSchemaNode()
  {
    return JsonHelper.loadJsonDocument(ClasspathReferences.TOKEN_STORE_SCHEMA);
  }
}
