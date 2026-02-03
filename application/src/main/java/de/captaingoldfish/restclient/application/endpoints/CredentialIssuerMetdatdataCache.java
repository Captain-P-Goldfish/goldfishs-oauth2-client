package de.captaingoldfish.restclient.application.endpoints;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.node.ObjectNode;

import de.captaingoldfish.restclient.application.projectconfig.CacheConfiguration;


/**
 * @author Pascal Knueppel
 * @since 20.08.2021
 */
@Component
public class CredentialIssuerMetdatdataCache
{

  /**
   * the cached metadata of a Credential Issuers discovery endpoint
   *
   * @param openIdProviderId the database id of a
   *          {@link de.captaingoldfish.restclient.database.entities.OpenIdProvider}
   * @return the OpenID Provider metadata of an existing provider if already retrieved once
   */
  @Cacheable(value = CacheConfiguration.OID4VCI_PROVIDER_METADATA_CACHE, key = "#p0")
  public ObjectNode getProviderMetadata(Long openIdProviderId)
  {
    return null;
  }

  /**
   * cache the metadata of a Credential Issuers discovery endpoint for the given provider
   *
   * @param openIdProviderId the database id of a
   *          {@link de.captaingoldfish.restclient.database.entities.OpenIdProvider}
   * @return the Credential Issuers metadata of an existing provider
   */
  @CachePut(value = CacheConfiguration.OID4VCI_PROVIDER_METADATA_CACHE, key = "#p0")
  public ObjectNode setProviderMetadata(Long openIdProviderId, ObjectNode oid4vciMetadata)
  {
    return oid4vciMetadata;
  }
}
