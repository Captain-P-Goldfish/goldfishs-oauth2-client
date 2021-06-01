package de.captaingoldfish.restclient.database;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;


/**
 * @author Pascal Knueppel
 * @since 31.05.2021
 */
public interface ScimCrudRepository<T, ID> extends Repository<T, ID>
{


  /**
   * Saves a given entity. Use the returned instance for further operations as the save operation might have
   * changed the entity instance completely.
   *
   * @param entity must not be {@literal null}.
   * @return the saved entity; will never be {@literal null}.
   * @throws IllegalArgumentException in case the given {@literal entity} is {@literal null}.
   */
  <S extends T> S save(S entity);

  /**
   * Retrieves an entity by its id.
   *
   * @param id must not be {@literal null}.
   * @return the entity with the given id or {@literal Optional#empty()} if none found.
   * @throws IllegalArgumentException if {@literal id} is {@literal null}.
   */
  Optional<T> findById(ID id);

  /**
   * Returns all instances of the type.
   *
   * @return all entities
   */
  List<T> findAll();

  /**
   * Returns the number of entities available.
   *
   * @return the number of entities.
   */
  long count();

  /**
   * Deletes a given entity.
   *
   * @param entity must not be {@literal null}.
   * @throws IllegalArgumentException in case the given entity is {@literal null}.
   */
  void delete(T entity);

  /**
   * Deletes all entities managed by the repository.
   */
  void deleteAll();
}
