package de.captaingoldfish.oauthrestclient.database.entities;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.captaingoldfish.oauthrestclient.commons.keyhelper.KeyStoreSupporter;
import de.captaingoldfish.oauthrestclient.database.DatabaseTest;
import de.captaingoldfish.oauthrestclient.database.DbBaseTest;
import de.captaingoldfish.oauthrestclient.database.FileReferences;
import lombok.SneakyThrows;


/**
 * @author Pascal Knueppel
 * @since 03.02.2021
 */
@DatabaseTest
public class KeystoreTest extends DbBaseTest
{


  @SneakyThrows
  @Test
  public void testSaveAndDeleteKeystore()
  {
    final String alias = "unit-tests";
    final String keystorePassword = "123456";
    final String privateKeyPassword = keystorePassword;
    final byte[] keystoreBytes;
    try (InputStream inputStream = getResourceStream(FileReferences.UNIT_TEST_JKS_KEYSTORE))
    {
      keystoreBytes = IOUtils.toByteArray(inputStream);
    }
    InputStream inputStream = new ByteArrayInputStream(keystoreBytes);
    Keystore keystore = new Keystore(inputStream, KeyStoreSupporter.KeyStoreType.JKS, keystorePassword);
    keystore.addAliasEntry(alias, privateKeyPassword);
    Assertions.assertDoesNotThrow(() -> keystoreDao.save(keystore));
    Assertions.assertEquals(KeyStoreSupporter.KeyStoreType.JKS, keystore.getKeystoreType());
    Assertions.assertEquals(keystorePassword, keystore.getKeystorePassword());

    Assertions.assertEquals(1, keystore.getKeystoreEntries().size());
    KeystoreEntry aliasKey = keystore.getKeystoreEntries().get(0);
    Assertions.assertEquals(privateKeyPassword, aliasKey.getPrivateKeyPassword());
    Assertions.assertEquals(alias, aliasKey.getAlias());
    Assertions.assertArrayEquals(keystoreBytes, keystore.getKeystoreBytes());
    Assertions.assertNotNull(keystore.getPrivateKey(aliasKey));
    Assertions.assertNotNull(keystore.getCertificate(aliasKey));

    Keystore loadedKeystore = keystoreDao.findById(1L).get();
    Assertions.assertEquals(1, loadedKeystore.getKeystoreEntries().size());

    Assertions.assertEquals(1, countEntriesOfTable("KEYSTORE_ENTRIES"));
    keystoreDao.deleteAll();
    Assertions.assertEquals(0, keystoreDao.count());
    Assertions.assertEquals(0, countEntriesOfTable("KEYSTORE_ENTRIES"));
  }
}
