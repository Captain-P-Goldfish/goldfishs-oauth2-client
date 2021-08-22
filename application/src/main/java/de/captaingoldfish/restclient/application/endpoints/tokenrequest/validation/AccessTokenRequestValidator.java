package de.captaingoldfish.restclient.application.endpoints.tokenrequest.validation;

import java.util.Optional;
import java.util.function.Supplier;

import de.captaingoldfish.restclient.application.projectconfig.WebAppConfig;
import de.captaingoldfish.restclient.application.utils.OAuthConstants;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;
import de.captaingoldfish.restclient.database.repositories.OpenIdClientDao;
import de.captaingoldfish.restclient.scim.resources.ScimAccessTokenRequest;
import de.captaingoldfish.scim.sdk.server.endpoints.validation.RequestValidator;
import de.captaingoldfish.scim.sdk.server.endpoints.validation.ValidationContext;


/**
 * @author Pascal Knueppel
 * @since 21.08.2021
 */
public class AccessTokenRequestValidator implements RequestValidator<ScimAccessTokenRequest>
{

  /**
   * verifies that the data within the access token request is sanitized
   */
  @Override
  public void validateCreate(ScimAccessTokenRequest resource, ValidationContext validationContext)
  {
    if (validationContext.hasErrors())
    {
      return;
    }

    if (OAuthConstants.AUTH_CODE_GRANT_TYPE.equals(resource.getGrantType()))
    {
      if (resource.getAuthorizationCode().isEmpty())
      {
        validationContext.addError(ScimAccessTokenRequest.FieldNames.AUTHORIZATION_CODE,
                                   String.format("The authorization code is required in case of grant_type '%s'",
                                                 OAuthConstants.AUTH_CODE_GRANT_TYPE));
      }
    }
    if (OAuthConstants.RESOURCE_PASSWORD_GRANT_TYPE.equals(resource.getGrantType()))
    {
      if (resource.getUsername().isEmpty())
      {
        validationContext.addError(ScimAccessTokenRequest.FieldNames.USERNAME,
                                   String.format("The username is required in case of grant_type '%s'",
                                                 OAuthConstants.RESOURCE_PASSWORD_GRANT_TYPE));
      }
      if (resource.getPassword().isEmpty())
      {
        validationContext.addError(ScimAccessTokenRequest.FieldNames.PASSWORD,
                                   String.format("The password is required in case of grant_type '%s'",
                                                 OAuthConstants.RESOURCE_PASSWORD_GRANT_TYPE));
      }
    }

    OpenIdClientDao openIdClientDao = WebAppConfig.getApplicationContext().getBean(OpenIdClientDao.class);
    Optional<OpenIdClient> openIdClient = openIdClientDao.findById(resource.getOpenIdClientId());
    if (openIdClient.isEmpty())
    {
      validationContext.addError("openIdClientId",
                                 String.format("Unknown OpenID Client ID '%s'", resource.getOpenIdClientId()));
    }
  }

  /**
   * endpoint disabled
   */
  @Override
  public void validateUpdate(Supplier<ScimAccessTokenRequest> oldResourceSupplier,
                             ScimAccessTokenRequest newResource,
                             ValidationContext validationContext)
  {
    // do nothing
  }
}
