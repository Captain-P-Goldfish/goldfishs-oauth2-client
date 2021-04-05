package de.captaingoldfish.oauthrestclient.database.repositories;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

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

  private static final String APPLICATION_KEYSTORE_PASSWORD = "123456";

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
    KeyStore javakeyStore = KeyStoreSupporter.createEmptyKeyStore(KeyStoreSupporter.KeyStoreType.PKCS12,
                                                                  APPLICATION_KEYSTORE_PASSWORD);
    byte[] keystoreBytes = KeyStoreSupporter.getBytes(javakeyStore, APPLICATION_KEYSTORE_PASSWORD);
    Keystore keystore = new Keystore(new ByteArrayInputStream(keystoreBytes), KeyStoreSupporter.KeyStoreType.PKCS12,
                                     APPLICATION_KEYSTORE_PASSWORD);
    entityManager.persist(keystore);
    return keystore;
  }
}
