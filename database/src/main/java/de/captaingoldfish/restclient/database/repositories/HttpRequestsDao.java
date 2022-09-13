package de.captaingoldfish.restclient.database.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import de.captaingoldfish.restclient.database.ScimCrudRepository;
import de.captaingoldfish.restclient.database.entities.HttpRequest;


/**
 * @author Pascal Knueppel
 * @since 31.05.2021
 */
@Repository
public interface HttpRequestsDao extends ScimCrudRepository<HttpRequest, Long>
{

  /**
   * Deletes the entity with the given id.
   *
   * @param id must not be {@literal null}.
   * @throws IllegalArgumentException in case the given {@literal id} is {@literal null}
   */
  void deleteById(Long id);

  /**
   * gets an http request by its name
   */
  Optional<HttpRequest> findByName(String name);

}
