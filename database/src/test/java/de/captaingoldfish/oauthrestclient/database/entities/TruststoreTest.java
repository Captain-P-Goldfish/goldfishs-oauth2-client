package de.captaingoldfish.oauthrestclient.database.entities;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import de.captaingoldfish.oauthrestclient.commons.keyhelper.KeyStoreSupporter;
import de.captaingoldfish.oauthrestclient.database.DatabaseTest;
import de.captaingoldfish.oauthrestclient.database.DbBaseTest;
import de.captaingoldfish.oauthrestclient.database.FileReferences;
import lombok.SneakyThrows;


/**
 * @author Pascal Knueppel
 * @since 18.03.2021
 */
@DatabaseTest
public class TruststoreTest extends DbBaseTest implements FileReferences
{

  @SneakyThrows
  @ParameterizedTest
  @CsvSource({"JKS," + UNIT_TEST_JKS_TRUSTSTORE, "PKCS12," + UNIT_TEST_PKCS12_TRUSTSTORE})
  public void testSaveAndReadTruststore(KeyStoreSupporter.KeyStoreType keyStoreType, String truststorePath)
  {
    try (InputStream inputStream = getResourceStream(truststorePath))
    {
      final String truststoreName = "unit-test-" + keyStoreType.name();
      final byte[] truststoreBytes = IOUtils.toByteArray(inputStream);
      Truststore truststore = new Truststore(truststoreName, new ByteArrayInputStream(truststoreBytes), PASSWORD,
                                             keyStoreType);
      truststore = truststoreDao.save(truststore);
      Assertions.assertNotEquals(0, truststore.getId());
      Assertions.assertEquals(truststoreName, truststore.getName());
      Assertions.assertArrayEquals(truststoreBytes, truststore.getTruststoreBytes());
      Assertions.assertEquals(keyStoreType, truststore.getTruststoreType());

      Assertions.assertEquals(1, truststore.getCertificates().size());

      Assertions.assertEquals(1, truststoreDao.count());
      truststoreDao.deleteAll();
      Assertions.assertEquals(0, truststoreDao.count());
    }
  }
}
