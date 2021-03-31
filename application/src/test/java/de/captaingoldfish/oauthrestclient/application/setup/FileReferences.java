package de.captaingoldfish.oauthrestclient.application.setup;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import de.captaingoldfish.oauthrestclient.database.entities.KeystoreEntry;


/**
 * @author Pascal Knueppel
 * @since 31.03.2021
 */
public interface FileReferences
{

  public static final String BASE_PATH = "/de/captaingoldfish/oauthrestclient/application";

  /**
   * the password to open any keystore
   */
  public static final String UNIT_TEST_KEYSTORE_PASSWORD = "123456";

  /**
   * a keystore with 3 entries "unit-test", "goldfish" and "localhost". The stored keys are different keys from
   * the unit-test.p12 keystore. This keystore contains an entry with a different password than the keystore
   * password
   */
  public static final String UNIT_TEST_KEYSTORE_JKS = BASE_PATH + "/files/unit-test.jks";

  /**
   * a keystore with 3 entries "unit-test", "goldfish" and "localhost". The stored keys are different keys from
   * the unit-test.jks keystore. This keystore contains an entry with a different password than the keystore
   * password
   */
  public static final String UNIT_TEST_KEYSTORE_PKCS12 = BASE_PATH + "/files/unit-test.p12";


  default InputStream readAsInputStream(String classpathFile)
  {
    return getClass().getResourceAsStream(classpathFile);
  }

  /**
   * @return the aliases and passwords of the entries within the keystores.
   */
  default List<KeystoreEntry> getUnitTestKeystoreEntryAccess()
  {
    return Arrays.asList(new KeystoreEntry("unit-test", "unit-test"),
                         new KeystoreEntry("goldfish", "123456"),
                         new KeystoreEntry("localhost", "123456"));
  }

  /**
   * @return the entry for the given alias.
   */
  default KeystoreEntry getUnitTestKeystoreEntryAccess(String alias)
  {
    return getUnitTestKeystoreEntryAccess().stream()
                                           .filter(keystoreEntry -> keystoreEntry.getAlias().equals(alias))
                                           .findAny()
                                           .get();
  }
}
