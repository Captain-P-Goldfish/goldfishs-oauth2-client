package de.captaingoldfish.oauthrestclient.database.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import de.captaingoldfish.oauthrestclient.database.entities.Proxy;


/**
 * project: oauth2-test-client<br>
 * author: Pascal Knueppel <br>
 * created at: 29.04.2018 <br>
 * <br>
 * this class represents the database-access from spring to the {@link Proxy}-objects
 */
public interface ProxyDao extends JpaRepository<Proxy, Long>, JpaSpecificationExecutor<Proxy>
{}
