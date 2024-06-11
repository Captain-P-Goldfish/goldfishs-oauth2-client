package de.captaingoldfish.restclient.application.utils;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.HttpsSupport;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.HttpHost;

import de.captaingoldfish.restclient.application.projectconfig.WebAppConfig;
import de.captaingoldfish.restclient.database.entities.HttpClientSettings;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;
import de.captaingoldfish.restclient.database.entities.Proxy;
import de.captaingoldfish.restclient.database.repositories.HttpClientSettingsDao;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


/**
 * @author Pascal Knueppel
 * @since 10.06.2024
 */
@Slf4j
public class HttpClientBuilder
{

  /**
   * this method generates a http-client instance and will set the ssl-context
   *
   * @return a http-client instance
   */
  public static CloseableHttpClient getHttpClient(OpenIdClient openIdClient)
  {
    HttpClientSettingsDao httpSettingsDao = WebAppConfig.getApplicationContext().getBean(HttpClientSettingsDao.class);
    HttpClientSettings httpSettings = httpSettingsDao.findByOpenIdClient(openIdClient).orElseThrow();
    return getHttpClient(httpSettings);
  }

  /**
   * this method generates a http-client instance and will set the ssl-context
   *
   * @return a http-client instance
   */
  @SneakyThrows
  public static CloseableHttpClient getHttpClient(HttpClientSettings clientSettings)
  {
    org.apache.hc.client5.http.impl.classic.HttpClientBuilder clientBuilder = org.apache.hc.client5.http.impl.classic.HttpClientBuilder.create();
    CredentialsProvider credentialsProvider = null;
    if (clientSettings.getProxy() != null)
    {
      credentialsProvider = getProxyCredentials(clientSettings.getProxy());
    }
    clientBuilder.setDefaultCredentialsProvider(credentialsProvider);

    SSLConnectionSocketFactory sslCnSockFactory = //
      new SSLConnectionSocketFactory(SSLContextHelper.getSslContext(clientSettings),
                                     clientSettings.isUseHostnameVerifier() ? HttpsSupport.getDefaultHostnameVerifier()
                                       : (s, sslSession) -> true);
    var connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                                                                     .setSSLSocketFactory(sslCnSockFactory)
                                                                     .build();
    clientBuilder.setConnectionManager(connectionManager);
    clientBuilder.setDefaultRequestConfig(getRequestConfig(clientSettings));

    return clientBuilder.build();
  }

  /**
   * @return a basic credentials provider that will be used for proxy authentication.
   */
  private static CredentialsProvider getProxyCredentials(Proxy proxy)
  {
    if (StringUtils.isBlank(proxy.getUsername()))
    {
      log.trace("Proxy username is empty cannot create client credentials");
      return null;
    }
    if (proxy.getPassword() == null)
    {
      log.debug("Proxy password is null cannot create client credentials");
      return null;
    }
    BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(new AuthScope(proxy.getHost(), proxy.getPort()),
                                       new UsernamePasswordCredentials(proxy.getUsername(),
                                                                       StringUtils.stripToEmpty(proxy.getPassword())
                                                                                  .toCharArray()));
    return credentialsProvider;
  }

  /**
   * will configure the apache http-client
   *
   * @return the original request with an extended configuration
   */
  private static RequestConfig getRequestConfig(HttpClientSettings clientSettings)
  {
    RequestConfig.Builder configBuilder;
    if (clientSettings.getProxy() == null)
    {
      configBuilder = RequestConfig.copy(RequestConfig.DEFAULT);
    }
    else
    {
      RequestConfig proxyConfig = getProxyConfig(clientSettings.getProxy());
      configBuilder = RequestConfig.copy(proxyConfig);
    }

    PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
    ConnectionConfig.Builder connectionConfigBuilder = ConnectionConfig.custom();

    if (clientSettings.getConnectionTimeout() > 0)
    {
      connectionConfigBuilder.setConnectTimeout(clientSettings.getConnectionTimeout(), TimeUnit.SECONDS);
      log.trace("Connection timeout '{}' seconds", clientSettings.getConnectionTimeout());
    }
    if (clientSettings.getSocketTimeout() > 0)
    {
      connectionConfigBuilder.setSocketTimeout(clientSettings.getSocketTimeout(), TimeUnit.SECONDS);
      log.trace("Socket timeout '{}' seconds", clientSettings.getSocketTimeout());
    }
    connectionManager.setDefaultConnectionConfig(connectionConfigBuilder.build());

    return configBuilder.build();
  }

  /**
   * will give back a request-config with the proxy settings based on the configuration-poperties
   *
   * @return a new config with the configured proxy or the default-config
   */
  private static RequestConfig getProxyConfig(Proxy proxy)
  {
    if (StringUtils.isNotBlank(proxy.getHost()))
    {
      HttpHost systemProxy = new HttpHost(proxy.getHost(), proxy.getPort());
      log.debug("Using proxy configuration: {}", systemProxy);
      return RequestConfig.custom().setProxy(systemProxy).build();
    }
    return RequestConfig.DEFAULT;
  }
}
