package de.captaingoldfish.restclient.database.repositories;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import de.captaingoldfish.restclient.commons.keyhelper.KeyStoreSupporter;
import de.captaingoldfish.restclient.database.entities.Truststore;
import lombok.RequiredArgsConstructor;


/**
 * @author Pascal Knueppel
 * @since 04.04.2021
 */
@RequiredArgsConstructor
public class TruststoreDaoImpl implements TruststoreDaoExtension
{

  private static final String APPLICATION_TRUSTSTORE_PASSWORD = "123456";

  private final EntityManager entityManager;

  @Transactional
  @Override
  public Truststore
  getTruststore()
  {
    Truststore applicationTruststore = entityManager.find(Truststore.class, 1L);
    if (applicationTruststore != null)
    {
      return applicationTruststore;
    }
    KeyStore javakeyStore = KeyStoreSupporter.createEmptyKeyStore(KeyStoreSupporter.KeyStoreType.JKS,
                                                                  APPLICATION_TRUSTSTORE_PASSWORD);
    byte[] keystoreBytes = KeyStoreSupporter.getBytes(javakeyStore, APPLICATION_TRUSTSTORE_PASSWORD);
    Truststore truststore = new Truststore(new ByteArrayInputStream(keystoreBytes), KeyStoreSupporter.KeyStoreType.JKS,
                                           APPLICATION_TRUSTSTORE_PASSWORD);
    entityManager.persist(truststore);
    return truststore;
  }
}
