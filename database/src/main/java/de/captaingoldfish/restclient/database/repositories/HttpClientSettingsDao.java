package de.captaingoldfish.restclient.database.repositories;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import de.captaingoldfish.restclient.database.ScimCrudRepository;
import de.captaingoldfish.restclient.database.entities.HttpClientSettings;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;


/**
 * @author Pascal Knueppel
 * @since 31.05.2021
 */
@Repository
public interface HttpClientSettingsDao extends ScimCrudRepository<HttpClientSettings, Long>
{

  /**
   * Deletes the entity with the given id.
   *
   * @param id must not be {@literal null}.
   * @throws IllegalArgumentException in case the given {@literal id} is {@literal null}
   */
  void deleteById(Long id);

  /**
   * finds http client setting by its parent
   * 
   * @param openIdClient the parent of the instance to find
   */
  Optional<HttpClientSettings> findByOpenIdClient(OpenIdClient openIdClient);
}
