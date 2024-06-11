package de.captaingoldfish.restclient.application.endpoints.keystore;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import de.captaingoldfish.restclient.application.projectconfig.CacheConfiguration;
import de.captaingoldfish.restclient.database.entities.Keystore;


/**
 * @author Pascal Knueppel
 * @since 26.03.2021
 */
@Component
public class KeystoreFileCache
{

  /**
   * gets a keystore from the cache if it is present
   *
   * @param stateId this id is an identifier that must be set and then returned from the view
   * @return either the keystore file or null
   */
  @Cacheable(value = CacheConfiguration.KEYSTORE_CACHE, key = "#p0")
  public Keystore getKeystoreFile(String stateId)
  {
    return null;
  }

  /**
   * puts the given keystore file into the cache
   *
   * @param stateId this id is an identifier that must be set and then returned from the view
   * @param keystoreFile the uploaded keystore file
   * @return the keystore file that was cached
   */
  @CachePut(value = CacheConfiguration.KEYSTORE_CACHE, key = "#p0")
  public Keystore setKeystoreFile(String stateId, Keystore keystoreFile)
  {
    return keystoreFile;
  }
}
