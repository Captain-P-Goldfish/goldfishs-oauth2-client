package de.captaingoldfish.restclient.database.entities;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.captaingoldfish.restclient.database.DatabaseTest;
import de.captaingoldfish.restclient.database.DbBaseTest;
import lombok.SneakyThrows;


/**
 * @author Pascal Knueppel
 * @since 22.03.2021
 */
@DatabaseTest
public class OpenIdProviderTest extends DbBaseTest
{

  @SneakyThrows
  @Test
  public void testSaveAndDeleteOpenIdProvider()
  {
    final String name = "keycloak";
    final String discorverEndpointUrl = "http://localhost/auth/realms/ia/.well-known/openid-configuration";

    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
    keyPairGenerator.initialize(512, random);
    KeyPair keyPair = keyPairGenerator.generateKeyPair();
    final byte[] signatureVerificationKey = keyPair.getPublic().getEncoded();


    OpenIdProvider openIdProvider = OpenIdProvider.builder()
                                                  .name(name)
                                                  .discoveryEndpointUrl(discorverEndpointUrl)
                                                  .signatureVerificationKey(signatureVerificationKey)
                                                  .build();

    openIdProvider = openIdProviderDao.save(openIdProvider);
    MatcherAssert.assertThat(openIdProvider.getId(), Matchers.greaterThan(0L));
    Assertions.assertEquals(name, openIdProvider.getName());
    Assertions.assertEquals(discorverEndpointUrl, openIdProvider.getDiscoveryEndpointUrl());
    Assertions.assertArrayEquals(signatureVerificationKey, openIdProvider.getSignatureVerificationKey());
    Assertions.assertEquals(1, openIdProviderDao.count());
    openIdProviderDao.deleteAll();
    Assertions.assertEquals(0, openIdProviderDao.count());
  }

  @SneakyThrows
  @Test
  public void testSaveAndDeleteOpenIdProvider2()
  {
    final String name = "keycloak";
    final String authorizationEndpoint = "http://localhost/auth/realms/ia/auth";
    final String tokennEndpoint = "http://localhost/auth/realms/ia/token";
    final String userInfoEndpoint = "http://localhost/auth/realms/ia/user-info";

    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
    keyPairGenerator.initialize(512, random);
    KeyPair keyPair = keyPairGenerator.generateKeyPair();
    final byte[] signatureVerificationKey = keyPair.getPublic().getEncoded();


    OpenIdProvider openIdProvider = OpenIdProvider.builder()
                                                  .name(name)
                                                  .authorizationEndpointUrl(authorizationEndpoint)
                                                  .tokenEndpointUrl(tokennEndpoint)
                                                  .userInfoEndpointUrl(userInfoEndpoint)
                                                  .signatureVerificationKey(signatureVerificationKey)
                                                  .build();

    openIdProvider = openIdProviderDao.save(openIdProvider);
    MatcherAssert.assertThat(openIdProvider.getId(), Matchers.greaterThan(0L));
    Assertions.assertEquals(name, openIdProvider.getName());
    Assertions.assertEquals(authorizationEndpoint, openIdProvider.getAuthorizationEndpointUrl());
    Assertions.assertEquals(tokennEndpoint, openIdProvider.getTokenEndpointUrl());
    Assertions.assertEquals(userInfoEndpoint, openIdProvider.getUserInfoEndpointUrl());
    Assertions.assertArrayEquals(signatureVerificationKey, openIdProvider.getSignatureVerificationKey());
    Assertions.assertEquals(1, openIdProviderDao.count());
    openIdProviderDao.deleteAll();
    Assertions.assertEquals(0, openIdProviderDao.count());
  }
}
