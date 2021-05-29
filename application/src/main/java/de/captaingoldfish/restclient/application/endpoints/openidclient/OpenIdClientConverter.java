package de.captaingoldfish.restclient.application.endpoints.openidclient;

import java.security.cert.X509Certificate;
import java.util.List;

import de.captaingoldfish.restclient.application.projectconfig.WebAppConfig;
import de.captaingoldfish.restclient.database.entities.Keystore;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;
import de.captaingoldfish.restclient.database.entities.OpenIdProvider;
import de.captaingoldfish.restclient.database.repositories.KeystoreDao;
import de.captaingoldfish.restclient.database.repositories.OpenIdProviderDao;
import de.captaingoldfish.restclient.scim.resources.CertificateInfo;
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
    String aliasKeyReference = scimOpenIdClient.getCertificateInfo().map(CertificateInfo::getAlias).orElse(null);

    return OpenIdClient.builder()
                       .id(scimOpenIdClient.getId().map(Long::parseLong).orElse(0L))
                       .openIdProvider(openIdProvider)
                       .clientId(scimOpenIdClient.getClientId())
                       .clientSecret(scimOpenIdClient.getClientSecret().orElse(null))
                       .audience(scimOpenIdClient.getAudience().orElse(null))
                       .signatureKeyRef(aliasKeyReference)
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
    CertificateInfo certificateInfo = null;
    if (appKeystoreAliases.contains(openIdClient.getSignatureKeyRef()))
    {
      X509Certificate certificate = applicationKeystore.getCertificate(openIdClient.getSignatureKeyRef());
      certificateInfo = new CertificateInfo(openIdClient.getSignatureKeyRef(), certificate);
    }

    return ScimOpenIdClient.builder()
                           .id(String.valueOf(openIdClient.getId()))
                           .openIdProviderId(openIdClient.getOpenIdProvider().getId())
                           .clientId(openIdClient.getClientId())
                           .clientSecret(openIdClient.getClientSecret())
                           .audience(openIdClient.getAudience())
                           .certificateInfo(certificateInfo)
                           .meta(Meta.builder()
                                     .created(openIdClient.getCreated())
                                     .lastModified(openIdClient.getLastModified())
                                     .build())
                           .build();
  }

}
