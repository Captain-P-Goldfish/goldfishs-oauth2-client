package de.captaingoldfish.restclient.application.projectconfig;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.captaingoldfish.restclient.application.endpoints.keystore.KeystoreFileCache;
import de.captaingoldfish.restclient.application.endpoints.keystore.KeystoreHandler;
import de.captaingoldfish.restclient.database.repositories.KeystoreDao;
import de.captaingoldfish.restclient.scim.endpoints.KeystoreEndpoint;
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
import de.captaingoldfish.scim.sdk.server.schemas.custom.ResourceTypeFeatures;


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
   * registers the keystore resourceType under the endpoint /Keystore. We will also activate the auto-filtering
   * extension so that filtering will work on the fly
   *
   * @param resourceEndpoint the resource endpoint that was previously defined
   * @return the keystore resource type
   */
  @Bean
  public ResourceType keystoreResourceType(ResourceEndpoint resourceEndpoint,
                                           KeystoreFileCache keystoreFileCache,
                                           KeystoreDao keystoreDa)
  {
    KeystoreEndpoint keystoreEndpoint = new KeystoreEndpoint(new KeystoreHandler(keystoreFileCache, keystoreDa));
    ResourceType keystoreResourceType = resourceEndpoint.registerEndpoint(keystoreEndpoint);
    keystoreResourceType.setFeatures(ResourceTypeFeatures.builder().autoFiltering(true).autoSorting(true).build());
    return keystoreResourceType;
  }
}
