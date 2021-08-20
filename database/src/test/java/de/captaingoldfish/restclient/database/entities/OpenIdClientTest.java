package de.captaingoldfish.restclient.database.entities;

import java.util.UUID;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
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
  public void testCascadingDeleteOfOpenIdClient()
  {
    OpenIdClient openIdClient = openIdClientDao.save(OpenIdClient.builder()
                                                                 .clientId(UUID.randomUUID().toString())
                                                                 .clientSecret(UUID.randomUUID().toString())
                                                                 .build());
    HttpClientSettings httpClientSettings = HttpClientSettings.builder().openIdClient(openIdClient).build();
    CurrentWorkflowSettings currentWorkflowSettings = CurrentWorkflowSettings.builder()
                                                                             .openIdClient(openIdClient)
                                                                             .build();

    httpClientSettingsDao.save(httpClientSettings);
    currentWorkflowSettingsDao.save(currentWorkflowSettings);

    Assertions.assertEquals(1, openIdClientDao.count());
    Assertions.assertEquals(1, httpClientSettingsDao.count());
    Assertions.assertEquals(1, currentWorkflowSettingsDao.count());
    openIdClientDao.deleteById(openIdClient.getId());
    Assertions.assertEquals(0, openIdClientDao.count());
    Assertions.assertEquals(0, httpClientSettingsDao.count());
    Assertions.assertEquals(0, currentWorkflowSettingsDao.count());
  }

  @Test
  public void testSaveAndDeleteClient()
  {
    final OpenIdProvider openIdProvider = createOpenIdProvider();
    final String clientId = "goldfish";
    final String clientSecret = "123456";

    OpenIdClient openIdClient = OpenIdClient.builder()
                                            .openIdProvider(openIdProvider)
                                            .clientId(clientId)
                                            .clientSecret(clientSecret)
                                            .build();
    openIdClient = openIdClientDao.save(openIdClient);

    Assertions.assertEquals(openIdProvider, openIdClient.getOpenIdProvider());
    MatcherAssert.assertThat(openIdClient.getId(), Matchers.greaterThan(0L));
    Assertions.assertEquals(clientId, openIdClient.getClientId());
    Assertions.assertEquals(clientSecret, openIdClient.getClientSecret());

    Assertions.assertEquals(1, openIdProviderDao.count());
    Assertions.assertEquals(1, openIdClientDao.count());
    openIdClientDao.deleteAll();
    Assertions.assertEquals(0, keystoreDao.count());
    Assertions.assertEquals(1, openIdProviderDao.count());
  }

  @Test
  public void testSaveAndDeleteClient2()
  {
    final OpenIdProvider openIdProvider = createOpenIdProvider();
    final String clientId = "goldfish";
    final String audience = "http://localhost/trustful/idp";
    final String signatureKeyRef = "any-alias";

    OpenIdClient openIdClient = OpenIdClient.builder()
                                            .openIdProvider(openIdProvider)
                                            .clientId(clientId)
                                            .signingKeyRef(signatureKeyRef)
                                            .audience(audience)
                                            .build();
    openIdClient = openIdClientDao.save(openIdClient);

    MatcherAssert.assertThat(openIdClient.getId(), Matchers.greaterThan(0L));
    Assertions.assertEquals(openIdProvider, openIdClient.getOpenIdProvider());
    Assertions.assertEquals(clientId, openIdClient.getClientId());
    Assertions.assertEquals(audience, openIdClient.getAudience());
    Assertions.assertEquals(signatureKeyRef, openIdClient.getSigningKeyRef());

    Assertions.assertEquals(1, openIdProviderDao.count());
    Assertions.assertEquals(1, openIdClientDao.count());
    openIdClientDao.deleteAll();
    Assertions.assertEquals(0, openIdClientDao.count());
    Assertions.assertEquals(1, openIdProviderDao.count());
  }

  private OpenIdProvider createOpenIdProvider()
  {
    return openIdProviderDao.save(OpenIdProvider.builder()
                                                .name("keycloak")
                                                .discoveryEndpoint("http://localhost:8080/")
                                                .build());
  }
}
