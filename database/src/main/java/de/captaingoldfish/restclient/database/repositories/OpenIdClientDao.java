package de.captaingoldfish.restclient.database.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import de.captaingoldfish.restclient.database.entities.OpenIdClient;
import de.captaingoldfish.restclient.database.entities.OpenIdProvider;


/**
 * project: oauth2-test-client<br>
 * author: Pascal Knueppel <br>
 * created at: 29.04.2018 <br>
 * <br>
 * this class represents the database-access from spring to the {@link OpenIdClient}-objects
 */
public interface OpenIdClientDao extends JpaRepository<OpenIdClient, Long>, JpaSpecificationExecutor<OpenIdClient>
{

  /**
   * @return a client by its clientId attribute
   */
  public Optional<OpenIdClient> findByClientIdAndOpenIdProvider(String clientId, OpenIdProvider openIdProvider);
}
