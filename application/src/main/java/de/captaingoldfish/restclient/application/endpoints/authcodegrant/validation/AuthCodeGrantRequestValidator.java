package de.captaingoldfish.restclient.application.endpoints.authcodegrant.validation;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import de.captaingoldfish.restclient.application.projectconfig.WebAppConfig;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;
import de.captaingoldfish.restclient.database.repositories.OpenIdClientDao;
import de.captaingoldfish.restclient.scim.resources.ScimAuthCodeGrantRequest;
import de.captaingoldfish.restclient.scim.resources.ScimCurrentWorkflowSettings;
import de.captaingoldfish.restclient.scim.resources.ScimCurrentWorkflowSettings.AuthCodeParameters;
import de.captaingoldfish.scim.sdk.server.endpoints.validation.RequestValidator;
import de.captaingoldfish.scim.sdk.server.endpoints.validation.ValidationContext;


/**
 * @author Pascal Knueppel
 * @since 20.08.2021
 */
public class AuthCodeGrantRequestValidator implements RequestValidator<ScimAuthCodeGrantRequest>
{

  /**
   * verifies that the workflow settings are present in the create request and that the OpenID Client does exist
   */
  @Override
  public void validateCreate(ScimAuthCodeGrantRequest resource, ValidationContext validationContext)
  {
    if (validationContext.hasErrors())
    {
      return;
    }
    if (resource.getCurrentWorkflowSettings().isEmpty())
    {
      validationContext.addError("The resource type extension for the workflow settings must be "
                                 + "present in request. This is an implementation error in the javascript frontend.");
      return;
    }

    String queryParameters = resource.getCurrentWorkflowSettings()
                                     .flatMap(ScimCurrentWorkflowSettings::getAuthCodeParameters)
                                     .flatMap(AuthCodeParameters::getQueryParameters)
                                     .orElse(null);
    if (StringUtils.isNotBlank(queryParameters))
    {
      UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl("http://localhost").query(queryParameters).build();
      List<String> stateParameters = uriComponents.getQueryParams().get("state");
      if (Optional.ofNullable(stateParameters).map(List::size).orElse(0) > 1)
      {
        String errorMessage = "Only a single state parameter may be added. The state parameter is used to identify the "
                              + "authorization response. If you use several values unpredictable results may occur.";
        validationContext.addError("authCodeParameters.queryParameters", errorMessage);
      }
    }

    Optional<Long> openIdClientId = resource.getCurrentWorkflowSettings()
                                            .map(ScimCurrentWorkflowSettings::getOpenIdClientId);
    OpenIdClientDao openIdClientDao = WebAppConfig.getApplicationContext().getBean(OpenIdClientDao.class);
    Optional<OpenIdClient> openIdClient = openIdClientDao.findById(openIdClientId.get());
    if (openIdClient.isEmpty())
    {
      validationContext.addError("openIdClientId",
                                 String.format("Unknown OpenID Client ID '%s'", openIdClientId.get()));
    }
  }

  /**
   * endpoint is disabled
   */
  @Override
  public void validateUpdate(Supplier<ScimAuthCodeGrantRequest> oldResourceSupplier,
                             ScimAuthCodeGrantRequest newResource,
                             ValidationContext validationContext)
  {
    // not supported
  }
}
