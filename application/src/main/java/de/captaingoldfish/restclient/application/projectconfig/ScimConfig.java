package de.captaingoldfish.restclient.application.projectconfig;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.captaingoldfish.restclient.application.crypto.JwtHandler;
import de.captaingoldfish.restclient.application.endpoints.appinfo.AppInfoHandler;
import de.captaingoldfish.restclient.application.endpoints.httpclient.HttpClientSettingsHandler;
import de.captaingoldfish.restclient.application.endpoints.jwt.JwtBuilderHandler;
import de.captaingoldfish.restclient.application.endpoints.jwt.validation.ScimJwtBuilderValidator;
import de.captaingoldfish.restclient.application.endpoints.keystore.KeystoreFileCache;
import de.captaingoldfish.restclient.application.endpoints.keystore.KeystoreHandler;
import de.captaingoldfish.restclient.application.endpoints.openidclient.OpenIdClientHandler;
import de.captaingoldfish.restclient.application.endpoints.openidprovider.OpenIdProviderHandler;
import de.captaingoldfish.restclient.application.endpoints.proxy.ProxyHandler;
import de.captaingoldfish.restclient.application.endpoints.truststore.TruststoreHandler;
import de.captaingoldfish.restclient.database.repositories.HttpClientSettingsDao;
import de.captaingoldfish.restclient.database.repositories.KeystoreDao;
import de.captaingoldfish.restclient.database.repositories.OpenIdClientDao;
import de.captaingoldfish.restclient.database.repositories.OpenIdProviderDao;
import de.captaingoldfish.restclient.database.repositories.ProxyDao;
import de.captaingoldfish.restclient.database.repositories.TruststoreDao;
import de.captaingoldfish.restclient.scim.endpoints.AppInfoEndpoint;
import de.captaingoldfish.restclient.scim.endpoints.HttpClientSettingsEndpoint;
import de.captaingoldfish.restclient.scim.endpoints.JwtBuilderEndpoint;
import de.captaingoldfish.restclient.scim.endpoints.KeystoreEndpoint;
import de.captaingoldfish.restclient.scim.endpoints.OpenIdClientEndpoint;
import de.captaingoldfish.restclient.scim.endpoints.OpenIdProviderEndpoint;
import de.captaingoldfish.restclient.scim.endpoints.ProxyEndpoint;
import de.captaingoldfish.restclient.scim.endpoints.TruststoreEndpoint;
import de.captaingoldfish.scim.sdk.common.resources.ServiceProvider;
import de.captaingoldfish.scim.sdk.common.resources.complex.BulkConfig;
import de.captaingoldfish.scim.sdk.common.resources.complex.ChangePasswordConfig;
import de.captaingoldfish.scim.sdk.common.resources.complex.ETagConfig;
import de.captaingoldfish.scim.sdk.common.resources.complex.FilterConfig;
import de.captaingoldfish.scim.sdk.common.resources.complex.PatchConfig;
import de.captaingoldfish.scim.sdk.common.resources.complex.SortConfig;
import de.captaingoldfish.scim.sdk.common.resources.multicomplex.AuthenticationScheme;
import de.captaingoldfish.scim.sdk.server.endpoints.ResourceEndpoint;
import de.captaingoldfish.scim.sdk.server.schemas.ResourceType;


/**
 * @author Pascal Knueppel
 * @since 09.05.2021
 */
@Configuration
public class ScimConfig
{

  /**
   * create the service provider configuration
   */
  @Bean
  public ServiceProvider getServiceProviderConfig()
  {
    AuthenticationScheme authScheme = AuthenticationScheme.builder()
                                                          .name("None")
                                                          .description("Access without any authentication")
                                                          .type("none")
                                                          .build();
    return ServiceProvider.builder()
                          .filterConfig(FilterConfig.builder().supported(true).maxResults(50).build())
                          .sortConfig(SortConfig.builder().supported(true).build())
                          .changePasswordConfig(ChangePasswordConfig.builder().supported(true).build())
                          .bulkConfig(BulkConfig.builder().supported(true).maxOperations(10).build())
                          .patchConfig(PatchConfig.builder().supported(true).build())
                          .authenticationSchemes(Collections.singletonList(authScheme))
                          .eTagConfig(ETagConfig.builder().supported(true).build())
                          .build();
  }

  /**
   * creates a resource endpoint for scim
   *
   * @param serviceProvider the service provider configuration
   * @return the resource endpoint
   */
  @Bean
  public ResourceEndpoint getResourceEndpoint(ServiceProvider serviceProvider)
  {
    return new ResourceEndpoint(serviceProvider);
  }

  /**
   * registers the keystore resourceType under the endpoint /Keystore.
   *
   * @param resourceEndpoint the resource endpoint that was previously defined
   * @return the keystore resource type
   */
  @Bean
  public ResourceType keystoreResourceType(ResourceEndpoint resourceEndpoint,
                                           KeystoreFileCache keystoreFileCache,
                                           KeystoreDao keystoreDao)
  {
    KeystoreEndpoint keystoreEndpoint = new KeystoreEndpoint(new KeystoreHandler(keystoreFileCache, keystoreDao));
    ResourceType keystoreResourceType = resourceEndpoint.registerEndpoint(keystoreEndpoint);
    return keystoreResourceType;
  }

  /**
   * registers the truststore resourceType under the endpoint /Truststore.
   *
   * @param resourceEndpoint the resource endpoint that was previously defined
   * @return the truststore resource type
   */
  @Bean
  public ResourceType truststoreResourceType(ResourceEndpoint resourceEndpoint, TruststoreDao truststoreDao)
  {
    TruststoreEndpoint truststoreEndpoint = new TruststoreEndpoint(new TruststoreHandler(truststoreDao));
    ResourceType truststoreResourceType = resourceEndpoint.registerEndpoint(truststoreEndpoint);
    return truststoreResourceType;
  }

  /**
   * registers the proxy resourceType under the endpoint /Proxy.
   *
   * @param resourceEndpoint the resource endpoint that was previously defined
   * @return the proxy resource type
   */
  @Bean
  public ResourceType proxyResourceType(ResourceEndpoint resourceEndpoint, ProxyDao proxyDao)
  {
    ProxyEndpoint proxyEndpoint = new ProxyEndpoint(new ProxyHandler(proxyDao));
    ResourceType proxyResourceType = resourceEndpoint.registerEndpoint(proxyEndpoint);
    return proxyResourceType;
  }

  /**
   * registers the OpenID Provider resourceType under the endpoint /OpenIdProvider.
   *
   * @param resourceEndpoint the resource endpoint that was previously defined
   * @return the OpenId Provider resource type
   */
  @Bean
  public ResourceType openIdProviderResourceType(ResourceEndpoint resourceEndpoint, OpenIdProviderDao openIdProviderDao)
  {
    OpenIdProviderEndpoint openIdEndpoint = new OpenIdProviderEndpoint(new OpenIdProviderHandler(openIdProviderDao));
    ResourceType openIdProviderResourceType = resourceEndpoint.registerEndpoint(openIdEndpoint);
    openIdProviderResourceType.getFeatures().setAutoFiltering(true);
    openIdProviderResourceType.getFeatures().setAutoSorting(true);
    return openIdProviderResourceType;
  }

  /**
   * registers the OpenID Client resourceType under the endpoint /OpenIdClient.
   *
   * @param resourceEndpoint the resource endpoint that was previously defined
   * @return the OpenId Client resource type
   */
  @Bean
  public ResourceType openIdClientResourceType(ResourceEndpoint resourceEndpoint, OpenIdClientDao openIdClientDao)
  {
    OpenIdClientEndpoint openIdClientEndpoint = new OpenIdClientEndpoint(new OpenIdClientHandler(openIdClientDao));
    ResourceType openIdProviderResourceType = resourceEndpoint.registerEndpoint(openIdClientEndpoint);
    openIdProviderResourceType.getFeatures().setAutoFiltering(true);
    openIdProviderResourceType.getFeatures().setAutoSorting(true);
    return openIdProviderResourceType;
  }

  /**
   * registers the HTTP client settings resourceType under the endpoint /HttpClientSettings.
   *
   * @param resourceEndpoint the resource endpoint that was previously defined
   * @return the HTTP client setting resource type
   */
  @Bean
  public ResourceType httpClientSettingsResourceType(ResourceEndpoint resourceEndpoint,
                                                     HttpClientSettingsDao httpClientSettingsDao)
  {
    HttpClientSettingsHandler httpClientHandler = new HttpClientSettingsHandler(httpClientSettingsDao);
    HttpClientSettingsEndpoint httpclientSettingsEndpoint = new HttpClientSettingsEndpoint(httpClientHandler);
    ResourceType httpClientSettingsResourceType = resourceEndpoint.registerEndpoint(httpclientSettingsEndpoint);
    httpClientSettingsResourceType.getFeatures().setAutoFiltering(true);
    httpClientSettingsResourceType.getFeatures().setAutoSorting(true);
    return httpClientSettingsResourceType;
  }

  /**
   * registers the jwt builder resourceType under the endpoint /JwtBuilder.
   *
   * @param resourceEndpoint the resource endpoint that was previously defined
   * @return the JWT builder resource type
   */
  @Bean
  public ResourceType jwtBuilderResourceType(ResourceEndpoint resourceEndpoint, KeystoreDao keystoreDao)
  {
    JwtHandler jwtHandler = new JwtHandler(keystoreDao);
    JwtBuilderHandler jwtBuilderHandler = new JwtBuilderHandler(jwtHandler, new ScimJwtBuilderValidator(keystoreDao));
    JwtBuilderEndpoint jwtBuilderEndpoint = new JwtBuilderEndpoint(jwtBuilderHandler);
    ResourceType jwtBuilderResourceType = resourceEndpoint.registerEndpoint(jwtBuilderEndpoint);
    jwtBuilderResourceType.getFeatures().setAutoFiltering(true);
    jwtBuilderResourceType.getFeatures().setAutoSorting(true);
    return jwtBuilderResourceType;
  }

  /**
   * registers the app info resourceType under the endpoint /AppInfo.
   *
   * @param resourceEndpoint the resource endpoint that was previously defined
   * @return the app info resource type
   */
  @Bean
  public ResourceType appInfoResourceType(ResourceEndpoint resourceEndpoint)
  {
    AppInfoEndpoint appInfoEndpoint = new AppInfoEndpoint(new AppInfoHandler());
    return resourceEndpoint.registerEndpoint(appInfoEndpoint);
  }
}
