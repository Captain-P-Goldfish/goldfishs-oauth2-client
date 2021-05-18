package de.captaingoldfish.restclient.database.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import de.captaingoldfish.restclient.database.entities.OpenIdProvider;


/**
 * project: oauth2-test-client<br>
 * author: Pascal Knueppel <br>
 * created at: 29.04.2018 <br>
 * <br>
 * this class represents the database-access from spring to the {@link OpenIdProvider}-objects
 */
public interface OpenIdProviderDao extends JpaRepository<OpenIdProvider, Long>, JpaSpecificationExecutor<OpenIdProvider>
{}