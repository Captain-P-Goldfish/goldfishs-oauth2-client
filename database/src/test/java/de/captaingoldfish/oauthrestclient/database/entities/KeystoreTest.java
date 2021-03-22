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
  public void testKeystoreTest()
  {
    final String name = "my-keystore";
    final String alias = "unit-tests";
    final String keystorePassword = "123456";
    final String privateKeyPassword = keystorePassword;
    final byte[] keystoreBytes;
    try (InputStream inputStream = getResourceStream(FileReferences.UNIT_TEST_JKS_KEYSTORE))
    {
      keystoreBytes = IOUtils.toByteArray(inputStream);
    }
    InputStream inputStream = new ByteArrayInputStream(keystoreBytes);
    Keystore keystore = new Keystore(name, inputStream, KeyStoreSupporter.KeyStoreType.JKS, alias, keystorePassword,
                                     privateKeyPassword);
    Assertions.assertDoesNotThrow(() -> keystoreDao.save(keystore));
    Assertions.assertEquals(KeyStoreSupporter.KeyStoreType.JKS, keystore.getKeystoreType());
    Assertions.assertEquals(name, keystore.getName());
    Assertions.assertEquals(name, keystore.getName());
    Assertions.assertEquals(keystorePassword, keystore.getKeystorePassword());
    Assertions.assertEquals(privateKeyPassword, keystore.getPrivateKeyPassword());
    Assertions.assertEquals(alias, keystore.getAlias());
    Assertions.assertArrayEquals(keystoreBytes, keystore.getKeystoreBytes());
    Assertions.assertNotNull(keystore.getPrivateKey());
    Assertions.assertNotNull(keystore.getCertificate());
  }
}
