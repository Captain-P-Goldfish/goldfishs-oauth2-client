package de.captaingoldfish.restclient.database;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;

import javax.persistence.EntityManager;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import de.captaingoldfish.restclient.commons.keyhelper.KeyStoreSupporter;
import de.captaingoldfish.restclient.database.entities.Keystore;
import de.captaingoldfish.restclient.database.entities.KeystoreEntry;
import de.captaingoldfish.restclient.database.repositories.CurrentWorkflowSettingsDao;
import de.captaingoldfish.restclient.database.repositories.HttpClientSettingsDao;
import de.captaingoldfish.restclient.database.repositories.HttpRequestCategoriesDao;
import de.captaingoldfish.restclient.database.repositories.HttpRequestsDao;
import de.captaingoldfish.restclient.database.repositories.KeystoreDao;
import de.captaingoldfish.restclient.database.repositories.OpenIdClientDao;
import de.captaingoldfish.restclient.database.repositories.OpenIdProviderDao;
import de.captaingoldfish.restclient.database.repositories.ProxyDao;
import de.captaingoldfish.restclient.database.repositories.TokenCategoryDao;
import de.captaingoldfish.restclient.database.repositories.TokenStoreDao;
import de.captaingoldfish.restclient.database.repositories.TruststoreDao;
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
  protected OpenIdClientDao openIdClientDao;

  @Autowired
  protected OpenIdProviderDao openIdProviderDao;

  @Autowired
  protected HttpClientSettingsDao httpClientSettingsDao;

  @Autowired
  protected CurrentWorkflowSettingsDao currentWorkflowSettingsDao;

  @Autowired
  protected TokenStoreDao tokenStoreDao;

  @Autowired
  protected TokenCategoryDao tokenCategoryDao;

  @Autowired
  protected HttpRequestCategoriesDao httpRequestCategoriesDao;

  @Autowired
  protected HttpRequestsDao httpRequestsDao;

  @Autowired
  protected EntityManager entityManager;

  @AfterEach
  public void clearTables()
  {
    httpRequestsDao.deleteAll();
    httpRequestCategoriesDao.deleteAll();
    tokenStoreDao.deleteAll();
    tokenCategoryDao.deleteAll();
    httpClientSettingsDao.deleteAll();
    openIdClientDao.deleteAll();
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

  protected int countEntriesOfTableNative(String tableName)
  {
    return ((BigInteger)entityManager.createNativeQuery("select count(*) from " + tableName)
                                     .getSingleResult()).intValue();
  }

  protected int countEntriesOfTable(String tableName)
  {
    return ((Long)entityManager.createQuery("select count(*) from " + tableName).getSingleResult()).intValue();
  }
}
