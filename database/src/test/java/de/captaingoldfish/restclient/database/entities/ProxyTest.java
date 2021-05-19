package de.captaingoldfish.restclient.database.entities;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.captaingoldfish.restclient.database.DatabaseTest;
import de.captaingoldfish.restclient.database.DbBaseTest;
import lombok.extern.slf4j.Slf4j;


/**
 * @author Pascal Knueppel
 * @since 17.03.2021
 */
@Slf4j
@DatabaseTest
public class ProxyTest extends DbBaseTest
{

  @Test
  public void testProxySaveAndReadProxy()
  {
    final String host = "localhost";
    final int port = 8888;
    final String username = "username";
    final String password = "password";
    Proxy proxy = Proxy.builder().host(host).port(port).username(username).password(password).build();
    proxy = proxyDao.save(proxy);
    Assertions.assertNotEquals(0, proxy.getId());
    Assertions.assertEquals(host, proxy.getHost());
    Assertions.assertEquals(port, proxy.getPort());
    Assertions.assertEquals(username, proxy.getUsername());
    Assertions.assertEquals(password, proxy.getPassword());

    Assertions.assertEquals(1, proxyDao.count());
    proxyDao.deleteAll();
    Assertions.assertEquals(0, proxyDao.count());
  }
}
