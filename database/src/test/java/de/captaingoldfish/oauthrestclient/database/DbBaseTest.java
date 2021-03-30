package de.captaingoldfish.oauthrestclient.database;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;

import javax.persistence.EntityManager;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import de.captaingoldfish.oauthrestclient.commons.keyhelper.KeyStoreSupporter;
import de.captaingoldfish.oauthrestclient.database.entities.Keystore;
import de.captaingoldfish.oauthrestclient.database.entities.KeystoreEntry;
import de.captaingoldfish.oauthrestclient.database.repositories.ClientDao;
import de.captaingoldfish.oauthrestclient.database.repositories.KeystoreDao;
import de.captaingoldfish.oauthrestclient.database.repositories.OpenIdProviderDao;
import de.captaingoldfish.oauthrestclient.database.repositories.ProxyDao;
import de.captaingoldfish.oauthrestclient.database.repositories.TruststoreDao;
import lombok.SneakyThrows;


/**
 * @author Pascal Knueppel
 * @since 17.03.2021
 */
public abstract class DbBaseTest implements FileReferences
{

  @Autowired
  protected KeystoreDao keystoreDao;

  @Autowired
  protected TruststoreDao truststoreDao;

  @Autowired
  protected ProxyDao proxyDao;

  @Autowired
  protected ClientDao clientDao;

  @Autowired
  protected OpenIdProviderDao openIdProviderDao;

  @Autowired
  private EntityManager entityManager;

  @AfterEach
  public void clearTables()
  {
    clientDao.deleteAll();
    openIdProviderDao.deleteAll();
    proxyDao.deleteAll();
    truststoreDao.deleteAll();
    keystoreDao.deleteAll();
  }

  @SneakyThrows
  protected Keystore getUnitTestKeystore()
  {
    final String alias = "unit-tests";
    final String keystorePassword = "123456";
    final String privateKeyPassword = keystorePassword;
    final byte[] keystoreBytes;
    try (InputStream inputStream = getResourceStream(UNIT_TEST_JKS_KEYSTORE))
    {
      keystoreBytes = IOUtils.toByteArray(inputStream);
    }
    InputStream inputStream = new ByteArrayInputStream(keystoreBytes);
    Keystore keystore = new Keystore(inputStream, KeyStoreSupporter.KeyStoreType.JKS, keystorePassword);
    KeystoreEntry keystoreEntry = new KeystoreEntry(alias, privateKeyPassword);
    keystore.getKeystoreEntries().add(keystoreEntry);
    return Assertions.assertDoesNotThrow(() -> keystoreDao.save(keystore));
  }

  protected int countEntriesOfTable(String tableName)
  {
    return ((BigInteger)entityManager.createNativeQuery("select count(*) from " + tableName)
                                     .getSingleResult()).intValue();
  }
}
