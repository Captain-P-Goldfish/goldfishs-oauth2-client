package de.captaingoldfish.oauthrestclient.database.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import de.captaingoldfish.oauthrestclient.database.entities.Keystore;



/**
 * project: oauth2-test-client<br>
 * author: Pascal Knueppel <br>
 * created at: 29.04.2018 <br>
 * <br>
 * this class represents the database-access from spring to the {@link Keystore}-objects
 */
public interface KeystoreDao extends JpaRepository<Keystore, Long>, JpaSpecificationExecutor<Keystore>
{}
