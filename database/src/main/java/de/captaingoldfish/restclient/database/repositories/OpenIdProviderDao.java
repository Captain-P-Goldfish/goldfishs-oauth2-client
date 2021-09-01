package de.captaingoldfish.restclient.database.repositories;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import de.captaingoldfish.restclient.database.ScimCrudRepository;
import de.captaingoldfish.restclient.database.entities.OpenIdProvider;


/**
 * project: oauth2-test-client<br>
 * author: Pascal Knueppel <br>
 * created at: 29.04.2018 <br>
 * <br>
 * this class represents the database-access from spring to the {@link OpenIdProvider}-objects
 */
@Repository
public interface OpenIdProviderDao extends ScimCrudRepository<OpenIdProvider, Long>, OpenIdProviderDaoExtension
{

  /**
   * finds a specific open id provider by its name
   */
  public Optional<OpenIdProvider> findByName(String name);

}
