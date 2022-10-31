package de.captaingoldfish.restclient.database.repositories;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import de.captaingoldfish.restclient.database.ScimCrudRepository;
import de.captaingoldfish.restclient.database.entities.HttpResponse;


/**
 * @author Pascal Knueppel
 * @since 31.05.2021
 */
@Repository
public interface HttpResponseDao extends ScimCrudRepository<HttpResponse, Long>
{

  /**
   * Deletes the entity with the given id.
   *
   * @param id must not be {@literal null}.
   * @throws IllegalArgumentException in case the given {@literal id} is {@literal null}
   */
  void deleteById(String id);

  Optional<HttpResponse> findById(String id);

}
