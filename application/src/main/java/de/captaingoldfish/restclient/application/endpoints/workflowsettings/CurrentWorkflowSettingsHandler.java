package de.captaingoldfish.restclient.application.endpoints.workflowsettings;

import java.util.List;

import de.captaingoldfish.restclient.application.utils.Utils;
import de.captaingoldfish.restclient.database.entities.CurrentWorkflowSettings;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;
import de.captaingoldfish.restclient.scim.resources.ScimCurrentWorkflowSettings;
import de.captaingoldfish.scim.sdk.common.constants.enums.SortOrder;
import de.captaingoldfish.scim.sdk.common.schemas.SchemaAttribute;
import de.captaingoldfish.scim.sdk.server.endpoints.Context;
import de.captaingoldfish.scim.sdk.server.endpoints.ResourceHandler;
import de.captaingoldfish.scim.sdk.server.filter.FilterNode;
import de.captaingoldfish.scim.sdk.server.response.PartialListResponse;
import lombok.RequiredArgsConstructor;


/**
 * @author Pascal Knueppel
 * @since 23.08.2021
 */
@RequiredArgsConstructor
public class CurrentWorkflowSettingsHandler extends ResourceHandler<ScimCurrentWorkflowSettings>
{

  private final CurrentWorkflowSettingsService currentWorkflowSettingsService;

  /**
   * endpoint disabled
   */
  @Override
  public ScimCurrentWorkflowSettings createResource(ScimCurrentWorkflowSettings resource, Context context)
  {
    return null;
  }

  /**
   * accessed during patch operations
   * 
   * @param id the value used here is the id of the owner {@link OpenIdClient#getId()}
   */
  @Override
  public ScimCurrentWorkflowSettings getResource(String id,
                                                 List<SchemaAttribute> attributes,
                                                 List<SchemaAttribute> excludedAttributes,
                                                 Context context)
  {
    Long openIdClientId = Utils.parseId(id);
    CurrentWorkflowSettings currentSettings = currentWorkflowSettingsService.getCurrentSettings(openIdClientId);
    return CurrentWorkflowSettingsConverter.toScimWorkflowSettings(currentSettings);
  }

  /**
   * endpoint disabled
   */
  @Override
  public PartialListResponse<ScimCurrentWorkflowSettings> listResources(long startIndex,
                                                                        int count,
                                                                        FilterNode filter,
                                                                        SchemaAttribute sortBy,
                                                                        SortOrder sortOrder,
                                                                        List<SchemaAttribute> attributes,
                                                                        List<SchemaAttribute> excludedAttributes,
                                                                        Context context)
  {
    return null;
  }

  /**
   * used to patch the current workflow settings from different sub-views
   */
  @Override
  public ScimCurrentWorkflowSettings updateResource(ScimCurrentWorkflowSettings resourceToUpdate, Context context)
  {
    currentWorkflowSettingsService.overrideCurrentWorkflowSettings(resourceToUpdate);
    return resourceToUpdate;
  }

  /**
   * endpoint disabled
   */
  @Override
  public void deleteResource(String id, Context context)
  {

  }
}
