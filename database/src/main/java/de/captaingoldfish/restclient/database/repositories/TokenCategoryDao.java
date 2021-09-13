package de.captaingoldfish.restclient.database.repositories;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import de.captaingoldfish.restclient.database.ScimCrudRepository;
import de.captaingoldfish.restclient.database.entities.TokenCategory;


/**
 * @author Pascal Knueppel
 * @since 28.08.2021
 */
@Repository
public interface TokenCategoryDao extends ScimCrudRepository<TokenCategory, Long>, TokenCategoryDaoExtension
{

  /**
   * searches for a category by its name attribute
   */
  public Optional<TokenCategory> findByName(String name);
}
