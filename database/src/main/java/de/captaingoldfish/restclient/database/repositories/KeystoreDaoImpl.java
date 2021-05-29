package de.captaingoldfish.restclient.database.repositories;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;

import de.captaingoldfish.restclient.commons.keyhelper.KeyStoreSupporter;
import de.captaingoldfish.restclient.database.entities.Keystore;
import de.captaingoldfish.restclient.database.entities.KeystoreEntry;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;


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

  @SneakyThrows
  @Transactional
  @Override
  public void deleteKeystoreAlias(String alias)
  {
    Keystore applicationKeystore = entityManager.find(Keystore.class, 1L);
    Optional<KeystoreEntry> keystoreEntryOptional = applicationKeystore.getKeystoreEntries()
                                                                       .stream()
                                                                       .filter(entry -> entry.getAlias().equals(alias))
                                                                       .findAny();

    KeystoreEntry keystoreEntry = keystoreEntryOptional.orElseThrow();
    applicationKeystore.getKeystoreEntries().remove(keystoreEntry);
    applicationKeystore.getKeyStore().deleteEntry(alias);
    byte[] newKeystoreBytes = KeyStoreSupporter.getBytes(applicationKeystore.getKeyStore(),
                                                         applicationKeystore.getKeystorePassword());
    applicationKeystore.setKeystoreBytes(newKeystoreBytes);
    entityManager.merge(applicationKeystore);

    setOpenIdClientReferencesToNull(alias);
  }

  private void setOpenIdClientReferencesToNull(String alias)
  {
    Query updateQuery = entityManager.createQuery("update  " + OpenIdClient.class.getSimpleName() + " client "
                                                  + "set client.signatureKeyRef = null "
                                                  + "where client.signatureKeyRef = :alias");
    updateQuery.setParameter("alias", alias);
    updateQuery.executeUpdate();
  }
}
