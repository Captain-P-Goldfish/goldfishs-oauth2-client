package de.captaingoldfish.restclient.database.entities;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.captaingoldfish.restclient.database.DatabaseTest;
import de.captaingoldfish.restclient.database.DbBaseTest;


/**
 * @author Pascal Knueppel
 * @since 31.05.2021
 */
@DatabaseTest
public class OpenIdClientTest extends DbBaseTest
{

  @Test
  public void testOpenIdClientTest()
  {
    OpenIdClient openIdClient = openIdClientDao.save(OpenIdClient.builder()
                                                                 .clientId(UUID.randomUUID().toString())
                                                                 .clientSecret(UUID.randomUUID().toString())
                                                                 .build());
    HttpClientSettings httpClientSettings = HttpClientSettings.builder().openIdClient(openIdClient).build();
    httpClientSettingsDao.save(httpClientSettings);
    Assertions.assertEquals(1, openIdClientDao.count());
    Assertions.assertEquals(1, httpClientSettingsDao.count());
    openIdClientDao.deleteById(openIdClient.getId());
    Assertions.assertEquals(0, openIdClientDao.count());
    Assertions.assertEquals(0, httpClientSettingsDao.count());
  }
}
