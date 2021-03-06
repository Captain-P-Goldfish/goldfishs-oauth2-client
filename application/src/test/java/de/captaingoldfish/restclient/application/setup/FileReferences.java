package de.captaingoldfish.restclient.application.setup;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import de.captaingoldfish.restclient.database.entities.KeystoreEntry;
import lombok.SneakyThrows;


/**
 * @author Pascal Knueppel
 * @since 31.03.2021
 */
public interface FileReferences
{

  public static final String BASE_PATH = "/de/captaingoldfish/restclient/application";

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

  public static final String UNIT_TEST_KEYSTORE_JKS_EXTENDED = BASE_PATH + "/files/unit-test-extended.jks";

  /**
   * an OpenID Connect discovery json body as returned by keycloak
   */
  public static final String TEST_IDP_OIDC_DISCOVERY_JSON = BASE_PATH + "/files/keycloak-oidc-discovery.json";


  default InputStream readAsInputStream(String classpathFile)
  {
    return getClass().getResourceAsStream(classpathFile);
  }

  @SneakyThrows
  default byte[] readAsBytes(String classpathFile)
  {
    try (InputStream inputStream = getClass().getResourceAsStream(classpathFile))
    {
      return inputStream.readAllBytes();
    }
  }

  default String getFilename(String classPath)
  {
    String[] parts = classPath.split("/");
    return parts[parts.length - 1];
  }

  /**
   * @return the aliases and passwords of the entries within the keystores.
   */
  default List<KeystoreEntry> getUnitTestKeystoreEntryAccess()
  {
    return Arrays.asList(new KeystoreEntry("unit-test", "unit-test", "RSA", 512),
                         new KeystoreEntry("goldfish", "123456", "RSA", 512),
                         new KeystoreEntry("localhost", "123456", "RSA", 512));
  }

  /**
   * @return the aliases and passwords of the entries within the keystores.
   */
  default List<KeystoreEntry> getExtendedUnitTestKeystoreEntryAccess()
  {
    return Arrays.asList(new KeystoreEntry("unit-test-rsa", "unit-test", "RSA", 2048),
                         new KeystoreEntry("goldfish-rsa", "123456", "RSA", 2048),
                         new KeystoreEntry("localhost-rsa", "123456", "RSA", 2048),
                         new KeystoreEntry("unit-test-ec", "unit-test", "EC", 521),
                         new KeystoreEntry("goldfish-ec", "123456", "EC", 256),
                         new KeystoreEntry("ec-256k", "123456", "EC", 256),
                         new KeystoreEntry("localhost-ec", "123456", "EC", 384),
                         new KeystoreEntry("hello-dsa", "123456", "DSA", 2048),
                         new KeystoreEntry("cert-dsa", null, "DSA", 2048),
                         new KeystoreEntry("cert-ec", null, "EC", 256),
                         new KeystoreEntry("cert-rsa", null, "RSA", 2048));
  }

  /**
   * @return the entry for the given alias.
   */
  default KeystoreEntry getUnitTestKeystoreEntryAccess(String alias)
  {
    return getUnitTestKeystoreEntryAccess().stream()
                                           .filter(keystoreEntry -> keystoreEntry.getAlias().equals(alias))
                                           .findAny()
                                           .orElseThrow();
  }

  /**
   * @return the entry for the given alias.
   */
  default KeystoreEntry getExtendedUnitTestKeystoreEntryAccess(String alias)
  {
    return getExtendedUnitTestKeystoreEntryAccess().stream()
                                                   .filter(keystoreEntry -> keystoreEntry.getAlias().equals(alias))
                                                   .findAny()
                                                   .orElseThrow();
  }
}
