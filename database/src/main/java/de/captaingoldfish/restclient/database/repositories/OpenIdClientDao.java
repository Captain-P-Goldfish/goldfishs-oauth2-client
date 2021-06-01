package de.captaingoldfish.restclient.database.repositories;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import de.captaingoldfish.restclient.database.ScimCrudRepository;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;
import de.captaingoldfish.restclient.database.entities.OpenIdProvider;


/**
 * project: oauth2-test-client<br>
 * author: Pascal Knueppel <br>
 * created at: 29.04.2018 <br>
 * <br>
 * this class represents the database-access from spring to the {@link OpenIdClient}-objects
 */
@Repository
public interface OpenIdClientDao extends ScimCrudRepository<OpenIdClient, Long>, OpenIdClientDaoExtension
{

  /**
   * @return a client by its clientId attribute
   */
  public Optional<OpenIdClient> findByClientIdAndOpenIdProvider(String clientId, OpenIdProvider openIdProvider);

}
