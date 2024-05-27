package de.captaingoldfish.restclient.application.endpoints.tokenrequest.request;

import de.captaingoldfish.restclient.application.crypto.DpopBuilder;
import de.captaingoldfish.restclient.application.crypto.JwtHandler;
import de.captaingoldfish.restclient.application.projectconfig.WebAppConfig;
import de.captaingoldfish.restclient.application.utils.OAuthConstants;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;
import de.captaingoldfish.restclient.database.repositories.KeystoreDao;
import de.captaingoldfish.restclient.database.repositories.OpenIdClientDao;
import de.captaingoldfish.restclient.scim.resources.ScimAccessTokenRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;


/**
 * @author Pascal Knueppel
 * @since 22.08.2021
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AccessTokenRequestBuilderFactory
{

  /**
   * used to get the correct {@link AccessTokenRequestBuilder} implementation for the current request
   *
   * @param accessTokenRequest the current request object
   * @return the correct {@link AccessTokenRequestBuilder} implementation
   */
  public static AccessTokenRequestBuilder getBuilder(ScimAccessTokenRequest accessTokenRequest)
  {
    OpenIdClientDao openIdClientDao = WebAppConfig.getApplicationContext().getBean(OpenIdClientDao.class);
    KeystoreDao keystoreDao = WebAppConfig.getApplicationContext().getBean(KeystoreDao.class);
    DpopBuilder dpopBuilder = new DpopBuilder(new JwtHandler(keystoreDao));
    OpenIdClient openIdClient = openIdClientDao.findById(accessTokenRequest.getOpenIdClientId()).orElseThrow();
    if (OAuthConstants.AUTH_CODE_GRANT_TYPE.equals(accessTokenRequest.getGrantType()))
    {
      return new AuthCodeTokenRequestBuilder(openIdClient, accessTokenRequest.getAuthorizationCode().orElseThrow(),
                                             accessTokenRequest.getRedirectUri().orElse(null),
                                             accessTokenRequest.getCurrentWorkflowSettings().orElse(null), dpopBuilder);
    }
    if (OAuthConstants.CLIENT_CREDENTIALS_GRANT_TYPE.equals(accessTokenRequest.getGrantType()))
    {
      return new ClientCredentialsTokenRequestBuilder(openIdClient, accessTokenRequest.getScope().orElse(null),
                                                      accessTokenRequest.getCurrentWorkflowSettings().orElse(null),
                                                      dpopBuilder);
    }
    if (OAuthConstants.RESOURCE_PASSWORD_GRANT_TYPE.equals(accessTokenRequest.getGrantType()))
    {
      return new ResourcePasswordTokenRequestBuilder(openIdClient, accessTokenRequest.getUsername().orElseThrow(),
                                                     accessTokenRequest.getPassword().orElseThrow(),
                                                     accessTokenRequest.getScope().orElse(null),
                                                     accessTokenRequest.getCurrentWorkflowSettings().orElse(null),
                                                     dpopBuilder);
    }
    throw new IllegalStateException(String.format("Unsupported grant_type '%s'", accessTokenRequest.getGrantType()));
  }

}
