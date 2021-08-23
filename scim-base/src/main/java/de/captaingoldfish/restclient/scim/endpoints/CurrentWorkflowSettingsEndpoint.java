package de.captaingoldfish.restclient.scim.endpoints;

import com.fasterxml.jackson.databind.JsonNode;

import de.captaingoldfish.restclient.scim.constants.ClasspathReferences;
import de.captaingoldfish.scim.sdk.common.utils.JsonHelper;
import de.captaingoldfish.scim.sdk.server.endpoints.EndpointDefinition;
import de.captaingoldfish.scim.sdk.server.endpoints.ResourceHandler;


/**
 * @author Pascal Knueppel
 * @since 23.08.2021
 */
public class CurrentWorkflowSettingsEndpoint extends EndpointDefinition
{

  public CurrentWorkflowSettingsEndpoint(ResourceHandler resourceHandler)
  {
    super(getResourceTypeSchema(), getResourceSchemaNode(), null, resourceHandler);
  }

  /**
   * @return the resource type schema for the current-workflow-settings endpoint
   */
  private static JsonNode getResourceTypeSchema()
  {
    return JsonHelper.loadJsonDocument(ClasspathReferences.CURRENT_WORKFLOW_SETTINGS_RESOURCE_TYPE);
  }

  /**
   * @return the current-workflow-settings schema defined in the resource type
   */
  private static JsonNode getResourceSchemaNode()
  {
    return JsonHelper.loadJsonDocument(ClasspathReferences.CURRENT_WORKFLOW_SETTINGS_SCHEMA);
  }
}
