package de.captaingoldfish.restclient.database.repositories;

import org.springframework.stereotype.Repository;

import de.captaingoldfish.restclient.database.ScimCrudRepository;
import de.captaingoldfish.restclient.database.entities.TokenStore;


/**
 * @author Pascal Knueppel
 * @since 28.08.2021
 */
@Repository
public interface TokenStoreDao extends ScimCrudRepository<TokenStore, Long>
{

}
