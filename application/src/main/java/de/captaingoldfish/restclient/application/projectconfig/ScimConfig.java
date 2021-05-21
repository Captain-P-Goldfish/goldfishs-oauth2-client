package de.captaingoldfish.restclient.application.projectconfig;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.captaingoldfish.restclient.application.endpoints.keystore.KeystoreFileCache;
import de.captaingoldfish.restclient.application.endpoints.keystore.KeystoreHandler;
import de.captaingoldfish.restclient.application.endpoints.openidprovider.OpenIdProviderHandler;
import de.captaingoldfish.restclient.application.endpoints.proxy.ProxyHandler;
import de.captaingoldfish.restclient.application.endpoints.truststore.TruststoreHandler;
import de.captaingoldfish.restclient.database.repositories.KeystoreDao;
import de.captaingoldfish.restclient.database.repositories.OpenIdProviderDao;
import de.captaingoldfish.restclient.database.repositories.ProxyDao;
import de.captaingoldfish.restclient.database.repositories.TruststoreDao;
import de.captaingoldfish.restclient.scim.endpoints.KeystoreEndpoint;
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
}
