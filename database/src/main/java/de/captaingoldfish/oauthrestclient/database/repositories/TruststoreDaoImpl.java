package de.captaingoldfish.oauthrestclient.database.repositories;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.apache.commons.lang3.RandomStringUtils;

import de.captaingoldfish.oauthrestclient.commons.keyhelper.KeyStoreSupporter;
import de.captaingoldfish.oauthrestclient.database.entities.Truststore;
import lombok.RequiredArgsConstructor;


/**
 * @author Pascal Knueppel
 * @since 04.04.2021
 */
@RequiredArgsConstructor
public class TruststoreDaoImpl implements TruststoreDaoExtension
{

  private final EntityManager entityManager;

  @Transactional
  @Override
  public Truststore getTruststore()
  {
    Truststore applicationTruststore = entityManager.find(Truststore.class, 1L);
    if (applicationTruststore != null)
    {
      return applicationTruststore;
    }
    final String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~`"
                              + "!@#$%^&*()-_=+[{]}\\|;:\'\",<.>/?";
    final String randomPassword = RandomStringUtils.random(15, characters);
    KeyStore javakeyStore = KeyStoreSupporter.createEmptyKeyStore(KeyStoreSupporter.KeyStoreType.JKS, randomPassword);
    byte[] keystoreBytes = KeyStoreSupporter.getBytes(javakeyStore, randomPassword);
    Truststore truststore = new Truststore(new ByteArrayInputStream(keystoreBytes), KeyStoreSupporter.KeyStoreType.JKS,
                                           randomPassword);
    entityManager.persist(truststore);
    return truststore;
  }
}
