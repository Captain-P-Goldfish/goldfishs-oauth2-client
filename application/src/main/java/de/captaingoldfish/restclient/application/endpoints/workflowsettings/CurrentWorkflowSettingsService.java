package de.captaingoldfish.restclient.application.endpoints.workflowsettings;

import org.springframework.stereotype.Service;

import de.captaingoldfish.restclient.database.entities.CurrentWorkflowSettings;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;
import de.captaingoldfish.restclient.database.repositories.CurrentWorkflowSettingsDao;
import de.captaingoldfish.restclient.database.repositories.OpenIdClientDao;
import de.captaingoldfish.restclient.scim.resources.ScimCurrentWorkflowSettings;
import lombok.RequiredArgsConstructor;


/**
 * @author Pascal Knueppel
 * @since 20.08.2021
 */
@RequiredArgsConstructor
@Service
public class CurrentWorkflowSettingsService
{

  private final CurrentWorkflowSettingsDao workflowSettingsDao;

  private final OpenIdClientDao openIdClientDao;

  public CurrentWorkflowSettings getCurrentSettings(Long openIdClientId)
  {
    OpenIdClient openIdClient = openIdClientDao.findById(openIdClientId).orElseThrow();
    return workflowSettingsDao.findByOpenIdClient(openIdClient).orElseThrow();
  }

  /**
   * overrides the current settings if the frontend changes the current client workflow parameters
   */
  public void overrideCurrentWorkflowSettings(ScimCurrentWorkflowSettings newSettings)
  {

    CurrentWorkflowSettings parsedSettings = CurrentWorkflowSettingsConverter.toCurrentWorkflowSettings(newSettings);
    Long settingsId = workflowSettingsDao.findByOpenIdClient(parsedSettings.getOpenIdClient())
                                         .map(CurrentWorkflowSettings::getId)
                                         .orElse(0L);
    parsedSettings.setId(settingsId);
    workflowSettingsDao.save(parsedSettings);
  }
}
