package de.captaingoldfish.restclient.database.repositories;

import org.springframework.stereotype.Repository;

import de.captaingoldfish.restclient.database.ScimCrudRepository;
import de.captaingoldfish.restclient.database.entities.Keystore;


/**
 * author: Pascal Knueppel <br>
 * created at: 29.04.2018 <br>
 * <br>
 * this class represents the database-access from spring to the {@link Keystore}-objects
 */
@Repository
public interface KeystoreDao extends ScimCrudRepository<Keystore, Long>, KeystoreDaoExtension
{}
