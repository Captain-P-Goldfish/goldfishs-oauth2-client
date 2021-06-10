package de.captaingoldfish.restclient.application.endpoints.openidclient;

import java.util.List;
import java.util.Optional;

import de.captaingoldfish.restclient.application.projectconfig.WebAppConfig;
import de.captaingoldfish.restclient.database.entities.Keystore;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;
import de.captaingoldfish.restclient.database.entities.OpenIdProvider;
import de.captaingoldfish.restclient.database.repositories.KeystoreDao;
import de.captaingoldfish.restclient.database.repositories.OpenIdProviderDao;
import de.captaingoldfish.restclient.scim.resources.ScimOpenIdClient;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;


/**
 * @author Pascal Knueppel
 * @since 28.05.2021
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OpenIdClientConverter
{

  /**
   * converts a SCIM representation of an OpenID Client to the database representation
   */
  public static OpenIdClient toOpenIdClient(ScimOpenIdClient scimOpenIdClient)
  {
    OpenIdProviderDao providerDao = WebAppConfig.getApplicationContext().getBean(OpenIdProviderDao.class);
    OpenIdProvider openIdProvider = providerDao.findById(scimOpenIdClient.getOpenIdProviderId()).orElseThrow();
    String signingKeyReference = scimOpenIdClient.getSigningKeyRef().orElse(null);
    String decryptionKeyReference = scimOpenIdClient.getDecryptionKeyRef().orElse(null);

    return OpenIdClient.builder()
                       .id(scimOpenIdClient.getId().map(Long::parseLong).orElse(0L))
                       .openIdProvider(openIdProvider)
                       .clientId(scimOpenIdClient.getClientId())
                       .clientSecret(scimOpenIdClient.getClientSecret().orElse(null))
                       .authenticationType(scimOpenIdClient.getAuthenticationType())
                       .signingKeyRef(signingKeyReference)
                       .audience(scimOpenIdClient.getAudience().orElse(null))
                       .decryptionKeyRef(decryptionKeyReference)
                       .build();
  }

  /**
   * converts a database representation of an OpenID Client to the SCIM representation
   */
  public static ScimOpenIdClient toScimOpenIdClient(OpenIdClient openIdClient)
  {
    KeystoreDao keystoreDao = WebAppConfig.getApplicationContext().getBean(KeystoreDao.class);
    Keystore applicationKeystore = keystoreDao.getKeystore();
    List<String> appKeystoreAliases = applicationKeystore.getKeyStoreAliases();
    String signingKeyReference = Optional.ofNullable(openIdClient.getSigningKeyRef())
                                         .filter(appKeystoreAliases::contains)
                                         .orElse(null);
    String decryptionKeyReference = Optional.ofNullable(openIdClient.getDecryptionKeyRef())
                                            .filter(appKeystoreAliases::contains)
                                            .orElse(null);

    return ScimOpenIdClient.builder()
                           .id(String.valueOf(openIdClient.getId()))
                           .openIdProviderId(openIdClient.getOpenIdProvider().getId())
                           .clientId(openIdClient.getClientId())
                           .clientSecret(openIdClient.getClientSecret())
                           .authenticationType(openIdClient.getAuthenticationType())
                           .signingKeyRef(signingKeyReference)
                           .audience(openIdClient.getAudience())
                           .decryptionKeyRef(decryptionKeyReference)
                           .meta(Meta.builder()
                                     .created(openIdClient.getCreated())
                                     .lastModified(openIdClient.getLastModified())
                                     .build())
                           .build();
  }

}
