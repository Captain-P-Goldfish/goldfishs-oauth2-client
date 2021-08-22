package de.captaingoldfish.restclient.application.utils;

import java.util.Optional;

import javax.net.ssl.SSLContext;

import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderConfigurationRequest;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;

import de.captaingoldfish.restclient.application.endpoints.authcodegrant.OpenIdProviderMetdatdataCache;
import de.captaingoldfish.restclient.application.projectconfig.WebAppConfig;
import de.captaingoldfish.restclient.database.entities.HttpClientSettings;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;
import de.captaingoldfish.restclient.database.entities.OpenIdProvider;
import de.captaingoldfish.restclient.database.entities.Proxy;
import de.captaingoldfish.restclient.database.repositories.HttpClientSettingsDao;
import de.captaingoldfish.scim.sdk.common.exceptions.BadRequestException;
import kong.unirest.Config;
import kong.unirest.Unirest;
import kong.unirest.UnirestInstance;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;


/**
 * @author Pascal Knueppel
 * @since 21.05.2021
 */
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
   * @param openIdProvider the OpenID Provider definition
   * @return the metadata of the OpenID Provider
   */
  @SneakyThrows
  public static OIDCProviderMetadata loadDiscoveryEndpointInfos(OpenIdProvider openIdProvider)
  {
    OpenIdProviderMetdatdataCache metadataCache = WebAppConfig.getApplicationContext()
                                                              .getBean(OpenIdProviderMetdatdataCache.class);
    OIDCProviderMetadata metadata = metadataCache.getProviderMetadata(openIdProvider.getId());
    if (metadata != null)
    {
      return metadata;
    }
    String discoveryUrl = openIdProvider.getDiscoveryEndpoint()
                                        // workaround to bypass the problem with the automatically appended well-known
                                        // endpoint path from nimbus
                                        .replace(OIDCProviderConfigurationRequest.OPENID_PROVIDER_WELL_KNOWN_PATH, "");
    Issuer issuer = new Issuer(discoveryUrl);
    OIDCProviderConfigurationRequest request = new OIDCProviderConfigurationRequest(issuer);
    // Make HTTP request
    HTTPRequest httpRequest = request.toHTTPRequest();
    HTTPResponse httpResponse = httpRequest.send();
    // Parse OpenID provider metadata
    metadata = OIDCProviderMetadata.parse(httpResponse.getContentAsJSONObject());
    metadataCache.setProviderMetadata(openIdProvider.getId(), metadata);
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
    unirestConfig.connectTimeout(httpSettings.getConnectionTimeout() * 1000);
    unirestConfig.socketTimeout(httpSettings.getSocketTimeout() * 1000);
    Optional.ofNullable(proxy)
            .ifPresent(p -> unirestConfig.proxy(p.getHost(), p.getPort(), p.getUsername(), p.getPassword()));
    return unirest;
  }
}
