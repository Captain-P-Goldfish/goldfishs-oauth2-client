package de.captaingoldfish.restclient.database.repositories;

import java.util.Optional;

import de.captaingoldfish.restclient.database.ScimCrudRepository;
import de.captaingoldfish.restclient.database.entities.CurrentWorkflowSettings;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;


/**
 * @author Pascal Knueppel
 * @since 19.08.2021
 */
public interface CurrentWorkflowSettingsDao extends ScimCrudRepository<CurrentWorkflowSettings, Long>
{

  /**
   * Deletes the entity with the given id.
   *
   * @param id must not be {@literal null}.
   * @throws IllegalArgumentException in case the given {@literal id} is {@literal null}
   */
  void deleteById(Long id);

  /**
   * finds the instance by its parent
   *
   * @param openIdClient the parent of the instance to find
   */
  Optional<CurrentWorkflowSettings> findByOpenIdClient(OpenIdClient openIdClient);
}
