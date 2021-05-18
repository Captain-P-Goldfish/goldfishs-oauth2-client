package de.captaingoldfish.restclient.scim.endpoints;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import de.captaingoldfish.restclient.scim.constants.ClasspathReferences;
import de.captaingoldfish.scim.sdk.common.utils.JsonHelper;
import de.captaingoldfish.scim.sdk.server.endpoints.EndpointDefinition;
import de.captaingoldfish.scim.sdk.server.endpoints.ResourceHandler;


/**
 * @author Pascal Knueppel
 * @since 28.04.2021
 */
public class KeystoreEndpoint extends EndpointDefinition
{

  public KeystoreEndpoint(ResourceHandler resourceHandler)
  {
    super(getResourceTypeSchema(), getResourceSchemaNode(), getResourceSchemaExtensionsSchema(), resourceHandler);
  }

  /**
   * @return the resource type schema for the keystore endpoint
   */
  private static JsonNode getResourceTypeSchema()
  {
    return JsonHelper.loadJsonDocument(ClasspathReferences.KEYSTORE_RESOURCE_TYPE);
  }

  /**
   * @return the keystore schema defined in the resource type
   */
  private static JsonNode getResourceSchemaNode()
  {
    return JsonHelper.loadJsonDocument(ClasspathReferences.KEYSTORE_SCHEMA);
  }

  /**
   * @return the extension of the keystore resource type endpoint definition
   */
  private static List<JsonNode> getResourceSchemaExtensionsSchema()
  {
    return Collections.singletonList(JsonHelper.loadJsonDocument(ClasspathReferences.CERTIFICATE_INFO_SCHEMA));
  }
}
