package de.captaingoldfish.restclient.application.endpoints;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;

import de.captaingoldfish.restclient.application.projectconfig.CacheConfiguration;


/**
 * @author Pascal Knueppel
 * @since 20.08.2021
 */
@Component
public class OpenIdProviderMetdatdataCache
{

  /**
   * the cached metadata of an OpenID Connect discovery endpoint
   *
   * @param openIdProviderId the database id of a
   *          {@link de.captaingoldfish.restclient.database.entities.OpenIdProvider}
   * @return the OpenID Provider metadata of an existing provider if already retrieved once
   */
  @Cacheable(value = CacheConfiguration.PROVIDER_METADATA_CACHE, key = "#p0")
  public OIDCProviderMetadata getProviderMetadata(Long openIdProviderId)
  {
    return null;
  }

  /**
   * cache the metadata of an OpenID Connect discovery endpoint for the given provider
   *
   * @param openIdProviderId the database id of a
   *          {@link de.captaingoldfish.restclient.database.entities.OpenIdProvider}
   * @return the OpenID Provider metadata of an existing provider
   */
  @CachePut(value = CacheConfiguration.PROVIDER_METADATA_CACHE, key = "#p0")
  public OIDCProviderMetadata setProviderMetadata(Long openIdProviderId, OIDCProviderMetadata authorizationResponseUrl)
  {
    return authorizationResponseUrl;
  }
}
