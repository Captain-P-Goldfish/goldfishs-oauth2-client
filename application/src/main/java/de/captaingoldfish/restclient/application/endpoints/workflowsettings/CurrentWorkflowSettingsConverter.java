package de.captaingoldfish.restclient.application.endpoints.workflowsettings;

import java.util.Optional;

import de.captaingoldfish.restclient.application.projectconfig.WebAppConfig;
import de.captaingoldfish.restclient.database.entities.CurrentWorkflowSettings;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;
import de.captaingoldfish.restclient.database.repositories.CurrentWorkflowSettingsDao;
import de.captaingoldfish.restclient.database.repositories.OpenIdClientDao;
import de.captaingoldfish.restclient.scim.resources.ScimAuthCodeGrantRequest.Pkce;
import de.captaingoldfish.restclient.scim.resources.ScimCurrentWorkflowSettings;
import de.captaingoldfish.restclient.scim.resources.ScimCurrentWorkflowSettings.AuthCodeParameters;
import de.captaingoldfish.restclient.scim.resources.ScimCurrentWorkflowSettings.ClientCredentialsParameters;
import de.captaingoldfish.restclient.scim.resources.ScimCurrentWorkflowSettings.Dpop;
import de.captaingoldfish.restclient.scim.resources.ScimCurrentWorkflowSettings.ResourceOwnerPasswordParameters;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;


/**
 * @author Pascal Knueppel
 * @since 19.08.2021
 */
public class CurrentWorkflowSettingsConverter
{

  public static CurrentWorkflowSettings toCurrentWorkflowSettings(ScimCurrentWorkflowSettings scimSettings)
  {
    OpenIdClientDao openIdClientDao = WebAppConfig.getApplicationContext().getBean(OpenIdClientDao.class);
    CurrentWorkflowSettingsDao workflowSettingsDao = WebAppConfig.getApplicationContext()
                                                                 .getBean(CurrentWorkflowSettingsDao.class);
    OpenIdClient openIdClient = openIdClientDao.findById(scimSettings.getOpenIdClientId()).orElseThrow();

    CurrentWorkflowSettings previousConfig = workflowSettingsDao.findByOpenIdClient(openIdClient)
                                                                .orElseGet(CurrentWorkflowSettings::new);
    boolean usePkce = scimSettings.getPkce().map(Pkce::isUse).orElseGet(previousConfig::isPkceUse);
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
                                  .dpopKeyId(scimSettings.getDpop()
                                                         .flatMap(Dpop::getKeyId)
                                                         .orElseGet(previousConfig::getDpopKeyId))
                                  .dpopJwsAlgorithm(scimSettings.getDpop()
                                                                .flatMap(Dpop::getSignatureAlgorithm)
                                                                .orElseGet(previousConfig::getDpopJwsAlgorithm))
                                  .dpopNonce(scimSettings.getDpop()
                                                         .flatMap(Dpop::getNonce)
                                                         .orElseGet(previousConfig::getDpopNonce))
                                  .dpopJti(scimSettings.getDpop()
                                                       .flatMap(Dpop::getJti)
                                                       .orElseGet(previousConfig::getDpopJti))
                                  .dpopHtm(scimSettings.getDpop()
                                                       .flatMap(Dpop::getHtm)
                                                       .orElseGet(previousConfig::getDpopHtm))
                                  .dpopHtu(scimSettings.getDpop()
                                                       .flatMap(Dpop::getHtu)
                                                       .orElseGet(previousConfig::getDpopHtu))
                                  .pkceUse(usePkce)
                                  .pkceCodeVerifier(scimSettings.getPkce()
                                                                .flatMap(Pkce::getCodeVerifier)
                                                                .orElseGet(() -> {
                                                                  // otherwise it is not possible to delete the value
                                                                  // again from the settings object
                                                                  if (usePkce)
                                                                  {
                                                                    return null;
                                                                  }
                                                                  return previousConfig.getPkceCodeVerifier();
                                                                }))
                                  .build();
  }

  public static ScimCurrentWorkflowSettings toScimWorkflowSettings(CurrentWorkflowSettings settings)
  {
    AuthCodeParameters authCodeParameters = AuthCodeParameters.builder()
                                                              .redirectUri(settings.getRedirectUri())
                                                              .queryParameters(settings.getQueryParameters())
                                                              .build();
    ClientCredentialsParameters clientCredentialsParameters = Optional.ofNullable(settings.getClientCredentialsGrantScope())
                                                                      .map(ClientCredentialsParameters::new)
                                                                      .orElse(null);
    var passwordParams = ResourceOwnerPasswordParameters.builder()
                                                        .username(settings.getUsername())
                                                        .password(settings.getUserPassword())
                                                        .scope(settings.getResourcePasswordGrantScope())
                                                        .build();
    Dpop dpop = Dpop.builder()
                    .keyId(settings.getDpopKeyId())
                    .signatureAlgorithm(settings.getDpopJwsAlgorithm())
                    .nonce(settings.getDpopNonce())
                    .jti(settings.getDpopJti())
                    .htm(settings.getDpopHtm())
                    .htu(settings.getDpopHtu())
                    .build();
    if (dpop.isEmpty())
    {
      dpop = null;
    }

    Pkce pkce = Pkce.builder().use(settings.isPkceUse()).codeVerifier(settings.getPkceCodeVerifier()).build();
    if (pkce.isEmpty())
    {
      pkce = null;
    }

    return ScimCurrentWorkflowSettings.builder()
                                      .openIdClientId(settings.getOpenIdClient().getId())
                                      .dpop(dpop)
                                      .pkce(pkce)
                                      .authCodeParameters(authCodeParameters)
                                      .clientCredentialsParameters(clientCredentialsParameters)
                                      .resourceOwnerPasswordParameters(passwordParams)
                                      .meta(Meta.builder()
                                                .resourceType("CurrentWorkflowSettings")
                                                .created(settings.getCreated())
                                                .lastModified(settings.getLastModified())
                                                .build())
                                      .build();
  }
}
