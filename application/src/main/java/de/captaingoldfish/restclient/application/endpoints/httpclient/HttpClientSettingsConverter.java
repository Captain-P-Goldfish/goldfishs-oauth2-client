package de.captaingoldfish.restclient.application.endpoints.httpclient;

import java.util.Optional;

import de.captaingoldfish.restclient.application.projectconfig.WebAppConfig;
import de.captaingoldfish.restclient.application.utils.Utils;
import de.captaingoldfish.restclient.database.entities.HttpClientSettings;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;
import de.captaingoldfish.restclient.database.entities.Proxy;
import de.captaingoldfish.restclient.database.repositories.OpenIdClientDao;
import de.captaingoldfish.restclient.database.repositories.ProxyDao;
import de.captaingoldfish.restclient.scim.resources.ScimHttpClientSettings;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;


/**
 * @author Pascal Knueppel
 * @since 01.06.2021
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpClientSettingsConverter
{

  /**
   * converts the SCIM representation of a HTTP client config into its database representation
   */
  public static HttpClientSettings toHttpClientSettings(ScimHttpClientSettings scimHttpClientSettings)
  {
    ProxyDao proxyDao = WebAppConfig.getApplicationContext().getBean(ProxyDao.class);
    Proxy proxy = scimHttpClientSettings.getProxyReference().flatMap(proxyDao::findById).orElse(null);

    OpenIdClientDao openIdClientDao = WebAppConfig.getApplicationContext().getBean(OpenIdClientDao.class);
    OpenIdClient openIdClient = scimHttpClientSettings.getOpenIdClientReference()
                                                      .flatMap(openIdClientDao::findById)
                                                      .orElse(null);

    return HttpClientSettings.builder()
                             .id(scimHttpClientSettings.getId().map(Utils::parseId).orElse(0L))
                             .connectionTimeout(scimHttpClientSettings.getConnectionTimeout()
                                                                      .map(Long::intValue)
                                                                      .orElse(60))
                             .socketTimeout(scimHttpClientSettings.getSocketTimeout().map(Long::intValue).orElse(60))
                             .requestTimeout(scimHttpClientSettings.getRequestTimeout().map(Long::intValue).orElse(60))
                             .useHostnameVerifier(scimHttpClientSettings.getUseHostnameVerifier().orElse(true))
                             .proxy(proxy)
                             .openIdClient(openIdClient)
                             .tlsClientAuthKeyRef(scimHttpClientSettings.getTlsClientAuthAliasReference().orElse(null))
                             .build();
  }

  /**
   * converts the database representation of a HTTP client config into its SCIM representation
   */
  public static ScimHttpClientSettings toScimHttpClientSettings(HttpClientSettings httpClientSettings)
  {
    return ScimHttpClientSettings.builder()
                                 .id(String.valueOf(httpClientSettings.getId()))
                                 .connectionTimeout((long)httpClientSettings.getConnectionTimeout())
                                 .socketTimeout((long)httpClientSettings.getSocketTimeout())
                                 .requestTimeout((long)httpClientSettings.getRequestTimeout())
                                 .useHostnameVerifier(httpClientSettings.isUseHostnameVerifier())
                                 .proxyReference(Optional.ofNullable(httpClientSettings.getProxy())
                                                         .map(Proxy::getId)
                                                         .map(String::valueOf)
                                                         .orElse(null))
                                 .openIdClientReference(Optional.ofNullable(httpClientSettings.getOpenIdClient())
                                                                .map(OpenIdClient::getId)
                                                                .orElse(null))
                                 .tlsClientAuthAliasReference(httpClientSettings.getTlsClientAuthKeyRef())
                                 .meta(Meta.builder()
                                           .created(httpClientSettings.getCreated())
                                           .lastModified(httpClientSettings.getLastModified())
                                           .build())
                                 .build();
  }

}
