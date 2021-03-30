package de.captaingoldfish.oauthrestclient.database.repositories;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.apache.commons.lang3.RandomStringUtils;

import de.captaingoldfish.oauthrestclient.commons.keyhelper.KeyStoreSupporter;
import de.captaingoldfish.oauthrestclient.database.entities.Keystore;
import lombok.RequiredArgsConstructor;


/**
 * @author Pascal Knueppel
 * @since 30.03.2021
 */
@RequiredArgsConstructor
public class KeystoreDaoImpl implements KeystoreDaoExtension
{

  private final EntityManager entityManager;

  @Transactional
  @Override
  public Keystore getKeystore()
  {
    Keystore applicationKeystore = entityManager.find(Keystore.class, 1L);
    if (applicationKeystore != null)
    {
      return applicationKeystore;
    }
    final String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~`"
                              + "!@#$%^&*()-_=+[{]}\\|;:\'\",<.>/?";
    final String randomPassword = RandomStringUtils.random(15, characters);
    KeyStore javakeyStore = KeyStoreSupporter.createEmptyKeyStore(KeyStoreSupporter.KeyStoreType.PKCS12,
                                                                  randomPassword);
    byte[] keystoreBytes = KeyStoreSupporter.getBytes(javakeyStore, randomPassword);
    Keystore keystore = new Keystore(new ByteArrayInputStream(keystoreBytes), KeyStoreSupporter.KeyStoreType.PKCS12,
                                     randomPassword);
    entityManager.persist(keystore);
    return keystore;
  }
}
