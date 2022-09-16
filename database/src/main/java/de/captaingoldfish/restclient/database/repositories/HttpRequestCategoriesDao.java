package de.captaingoldfish.restclient.database.repositories;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import de.captaingoldfish.restclient.database.ScimCrudRepository;
import de.captaingoldfish.restclient.database.entities.HttpRequestGroup;


/**
 * @author Pascal Knueppel
 * @since 31.05.2021
 */
@Repository
public interface HttpRequestCategoriesDao
  extends ScimCrudRepository<HttpRequestGroup, Long>, HttpRequestCategoriesDaoExtension
{

  /**
   * finds a category by its name value
   */
  Optional<HttpRequestGroup> findByName(String name);
}
