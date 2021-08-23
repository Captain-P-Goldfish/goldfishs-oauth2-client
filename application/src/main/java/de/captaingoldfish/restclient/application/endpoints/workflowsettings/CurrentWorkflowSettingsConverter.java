package de.captaingoldfish.restclient.application.endpoints.workflowsettings;

import de.captaingoldfish.restclient.application.projectconfig.WebAppConfig;
import de.captaingoldfish.restclient.database.entities.CurrentWorkflowSettings;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;
import de.captaingoldfish.restclient.database.repositories.OpenIdClientDao;
import de.captaingoldfish.restclient.scim.resources.ScimCurrentWorkflowSettings;
import de.captaingoldfish.restclient.scim.resources.ScimCurrentWorkflowSettings.AuthCodeParameters;
import de.captaingoldfish.restclient.scim.resources.ScimCurrentWorkflowSettings.ClientCredentialsParameters;
import de.captaingoldfish.restclient.scim.resources.ScimCurrentWorkflowSettings.ResourceOwnerPasswordParameters;


/**
 * @author Pascal Knueppel
 * @since 19.08.2021
 */
public class CurrentWorkflowSettingsConverter
{

  public static CurrentWorkflowSettings toCurrentWorkflowSettings(ScimCurrentWorkflowSettings scimSettings)
  {
    OpenIdClientDao openIdClientDao = WebAppConfig.getApplicationContext().getBean(OpenIdClientDao.class);
    OpenIdClient openIdClient = openIdClientDao.findById(scimSettings.getOpenIdClientId()).orElseThrow();
    return CurrentWorkflowSettings.builder()
                                  .openIdClient(openIdClient)
                                  .redirectUri(scimSettings.getAuthCodeParameters()
                                                           .flatMap(AuthCodeParameters::getRedirectUri)
                                                           .orElse(null))
                                  .queryParameters(scimSettings.getAuthCodeParameters()
                                                               .flatMap(AuthCodeParameters::getQueryParameters)
                                                               .orElse(null))
                                  .clientCredentialsGrantScope(scimSettings.getClientCredentialsParameters()
                                                                           .map(ClientCredentialsParameters::getScope)
                                                                           .orElse(null))
                                  .username(scimSettings.getResourceOwnerPasswordParameters()
                                                        .map(ResourceOwnerPasswordParameters::getUsername)
                                                        .orElse(null))
                                  .userPassword(scimSettings.getResourceOwnerPasswordParameters()
                                                            .map(ResourceOwnerPasswordParameters::getPassword)
                                                            .orElse(null))
                                  .resourcePasswordGrantScope(scimSettings.getResourceOwnerPasswordParameters()
                                                                          .map(ResourceOwnerPasswordParameters::getScope)
                                                                          .orElse(null))
                                  .build();
  }

  public static ScimCurrentWorkflowSettings toScimWorkflowSettings(CurrentWorkflowSettings settings)
  {
    AuthCodeParameters authCodeParameters = AuthCodeParameters.builder()
                                                              .redirectUri(settings.getRedirectUri())
                                                              .queryParameters(settings.getQueryParameters())
                                                              .build();
    var clientCredentialsParameters = new ClientCredentialsParameters(settings.getClientCredentialsGrantScope());
    var passwordParams = ResourceOwnerPasswordParameters.builder()
                                                        .username(settings.getUsername())
                                                        .password(settings.getUserPassword())
                                                        .scope(settings.getResourcePasswordGrantScope())
                                                        .build();
    return ScimCurrentWorkflowSettings.builder()
                                      .openIdClientId(settings.getOpenIdClient().getId())
                                      .authCodeParameters(authCodeParameters)
                                      .clientCredentialsParameters(clientCredentialsParameters)
                                      .resourceOwnerPasswordParameters(passwordParams)
                                      .build();
  }
}
