package de.captaingoldfish.oauthrestclient.database.entities;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.captaingoldfish.oauthrestclient.database.DatabaseTest;
import de.captaingoldfish.oauthrestclient.database.DbBaseTest;


/**
 * @author Pascal Knueppel
 * @since 22.03.2021
 */
@DatabaseTest
public class ClientTest extends DbBaseTest
{

  @Test
  public void testSaveAndDeleteClient()
  {
    final String clientId = "goldfish";
    final String clientSecret = "123456";
    final String redirectUri = "http://localhost:9543/authcode";

    Client client = Client.builder().clientId(clientId).clientSecret(clientSecret).redirectUri(redirectUri).build();
    client = clientDao.save(client);

    MatcherAssert.assertThat(client.getId(), Matchers.greaterThan(0L));
    Assertions.assertEquals(clientId, client.getClientId());
    Assertions.assertEquals(clientSecret, client.getClientSecret());
    Assertions.assertEquals(redirectUri, client.getRedirectUri());

    Assertions.assertEquals(0, keystoreDao.count());
    Assertions.assertEquals(1, clientDao.count());
    clientDao.deleteAll();
    Assertions.assertEquals(0, keystoreDao.count());
    Assertions.assertEquals(0, clientDao.count());
  }

  @Test
  public void testSaveAndDeleteClient2()
  {
    final String clientId = "goldfish";
    final String redirectUri = "http://localhost:9543/authcode";
    final Keystore keystore = getUnitTestKeystore();
    final String audience = "http://localhost/trustful/idp";

    Client client = Client.builder()
                          .clientId(clientId)
                          .redirectUri(redirectUri)
                          .signatureKeystore(keystore)
                          .audience(audience)
                          .build();
    client = clientDao.save(client);

    MatcherAssert.assertThat(client.getId(), Matchers.greaterThan(0L));
    Assertions.assertEquals(clientId, client.getClientId());
    Assertions.assertEquals(redirectUri, client.getRedirectUri());
    Assertions.assertEquals(audience, client.getAudience());
    Assertions.assertArrayEquals(keystore.getKeystoreBytes(), client.getSignatureKeystore().getKeystoreBytes());

    Assertions.assertEquals(1, keystoreDao.count());
    Assertions.assertEquals(1, clientDao.count());
    clientDao.deleteAll();
    Assertions.assertEquals(1, keystoreDao.count());
    Assertions.assertEquals(0, clientDao.count());
  }
}
