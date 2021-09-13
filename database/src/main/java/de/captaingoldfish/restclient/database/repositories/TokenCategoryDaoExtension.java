package de.captaingoldfish.restclient.database.repositories;

/**
 * @author Pascal Knueppel
 * @since 02.09.2021
 */
public interface TokenCategoryDaoExtension
{

  /**
   * Deletes the entity with the given id.
   *
   * @param id must not be {@literal null}.
   * @throws IllegalArgumentException in case the given {@literal id} is {@literal null}
   */
  void deleteById(Long id);
}
