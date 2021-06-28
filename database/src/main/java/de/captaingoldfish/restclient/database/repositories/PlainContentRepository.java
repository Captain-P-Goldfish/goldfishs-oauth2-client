package de.captaingoldfish.restclient.database.repositories;

import org.springframework.stereotype.Repository;

import de.captaingoldfish.restclient.database.ScimCrudRepository;
import de.captaingoldfish.restclient.database.entities.PlainContent;


/**
 * <p>
 * Copyright &copy; 2009-2020 Governikus GmbH &amp; Co. KG
 * </p>
 *
 * @author Pascal Knüppel
 * @since 25.06.2021
 */
@Repository
public interface PlainContentRepository extends ScimCrudRepository<PlainContent, Long>
{

  /**
   * Deletes the entity with the given id.
   *
   * @param id must not be {@literal null}.
   * @throws IllegalArgumentException in case the given {@literal id} is {@literal null}
   */
  void deleteById(Long id);
}
