package de.captaingoldfish.restclient.application.utils;

import java.util.Optional;

import javax.net.ssl.SSLContext;

import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;

import de.captaingoldfish.restclient.application.endpoints.OpenIdProviderMetdatdataCache;
import de.captaingoldfish.restclient.application.projectconfig.WebAppConfig;
import de.captaingoldfish.restclient.database.entities.HttpClientSettings;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;
import de.captaingoldfish.restclient.database.entities.OpenIdProvider;
import de.captaingoldfish.restclient.database.entities.Proxy;
import de.captaingoldfish.restclient.database.repositories.HttpClientSettingsDao;
import de.captaingoldfish.scim.sdk.common.exceptions.BadRequestException;
import kong.unirest.Config;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestInstance;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


/**
 * @author Pascal Knueppel
 * @since 21.05.2021
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Utils
{

  /**
   * tries to parse a given id for a SCIM [get, update, delete] to a long value
   */
  public static Long parseId(String id)
  {
    try
    {
      return Long.parseLong(id);
    }
    catch (NumberFormatException ex)
    {
      throw new BadRequestException("Invalid ID format: " + id);
    }
  }

  /**
   * loads the OpenID Connect metadata from the identity provider
   *
   * @param openIdClient the OpenID Provider definition
   * @return the metadata of the OpenID Provider
   */
  @SneakyThrows
  public synchronized static OIDCProviderMetadata loadDiscoveryEndpointInfos(OpenIdClient openIdClient)
  {
    final OpenIdProvider openIdProvider = openIdClient.getOpenIdProvider();
    OpenIdProviderMetdatdataCache metadataCache = WebAppConfig.getApplicationContext()
                                                              .getBean(OpenIdProviderMetdatdataCache.class);
    OIDCProviderMetadata metadata = metadataCache.getProviderMetadata(openIdProvider.getId(),
                                                                      openIdProvider.getVersion());
    if (metadata != null)
    {
      return metadata;
    }
    String discoveryUrl = openIdProvider.getDiscoveryEndpoint();
    final String responseBody;
    try (UnirestInstance unirest = getUnirestInstance(openIdClient))
    {
      HttpResponse<String> response = unirest.get(discoveryUrl).asString();
      if (!response.isSuccess())
      {
        throw new BadRequestException(String.format("Failed to load meta-data from OpenID Discovery endpoint: %s",
                                                    response.getStatusText()));
      }
      responseBody = response.getBody();
    }
    catch (Exception ex)
    {
      throw new BadRequestException(String.format("Failed to load meta-data from OpenID Discovery endpoint: %s",
                                                  ex.getMessage()),
                                    ex);
    }
    metadata = OIDCProviderMetadata.parse(responseBody);
    metadataCache.setProviderMetadata(openIdProvider.getId(), openIdProvider.getVersion(), metadata);
    return metadata;
  }

  /**
   * builds an unirest instance with the http settings for the given OpenID Connect client
   *
   * @param openIdClient the client that has the associated http client settings
   * @return the configured unirest instance that should be used
   */
  public static UnirestInstance getUnirestInstance(OpenIdClient openIdClient)
  {
    UnirestInstance unirest = Unirest.spawnInstance();
    HttpClientSettingsDao httpSettingsDao = WebAppConfig.getApplicationContext().getBean(HttpClientSettingsDao.class);
    HttpClientSettings httpSettings = httpSettingsDao.findByOpenIdClient(openIdClient).orElseThrow();
    Proxy proxy = httpSettings.getProxy();
    SSLContext sslContext = SSLContextHelper.getSslContext(httpSettings);
    Config unirestConfig = unirest.config();
    unirestConfig.sslContext(sslContext);
    if (!httpSettings.isUseHostnameVerifier())
    {
      unirestConfig.hostnameVerifier((s, sslSession) -> true);
    }
    unirestConfig.connectTimeout(httpSettings.getConnectionTimeout() * 1000);
    unirestConfig.socketTimeout(httpSettings.getSocketTimeout() * 1000);
    Optional.ofNullable(proxy)
            .ifPresent(p -> unirestConfig.proxy(p.getHost(), p.getPort(), p.getUsername(), p.getPassword()));
    return unirest;
  }
}
