package de.captaingoldfish.restclient.database;

import java.io.InputStream;


/**
 * @author Pascal Knueppel
 * @since 03.02.2021
 */
public interface FileReferences
{


  public static final String PASSWORD = "123456";

  public static final String BASE_PATH = "/de/captaingoldfish/restclient";

  public static final String UNIT_TEST_JKS_KEYSTORE = BASE_PATH + "/database/keystores/unit-test.jks";

  public static final String UNIT_TEST_PKCS12_KEYSTORE = BASE_PATH + "/database/keystores/unit-test.p12";

  public static final String UNIT_TEST_JKS_TRUSTSTORE = BASE_PATH + "/database/keystores/unit-test-truststore.jks";

  public static final String UNIT_TEST_PKCS12_TRUSTSTORE = BASE_PATH + "/database/keystores/unit-test-truststore.p12";

  default InputStream getResourceStream(String file)
  {
    return getClass().getResourceAsStream(file);
  }

}
