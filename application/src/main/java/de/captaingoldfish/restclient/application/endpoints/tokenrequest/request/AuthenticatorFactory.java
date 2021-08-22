package de.captaingoldfish.restclient.application.endpoints.tokenrequest.request;

import de.captaingoldfish.restclient.database.entities.OpenIdClient;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;


/**
 * @author Pascal Knueppel
 * @since 21.08.2021
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthenticatorFactory
{

  public static Authenticator getAuthenticator(OpenIdClient openIdClient)
  {
    switch (openIdClient.getAuthenticationType())
    {
      case "basic":
        return new BasicAuthenticator(openIdClient.getClientId(), openIdClient.getClientSecret());
      case "jwt":
        return new JwtAuthenticator(openIdClient);
      default:
        throw new IllegalStateException(String.format("Unknown authentication type in OpenID Client '%s'",
                                                      openIdClient.getAuthenticationType()));
    }
  }

}
