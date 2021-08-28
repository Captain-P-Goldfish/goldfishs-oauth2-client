package de.captaingoldfish.restclient.application.endpoints.tokenstore;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.captaingoldfish.restclient.application.setup.AbstractScimClientConfig;
import de.captaingoldfish.restclient.application.setup.OAuthRestClientTest;
import de.captaingoldfish.restclient.database.entities.TokenStore;
import de.captaingoldfish.restclient.scim.resources.ScimTokenStore;
import de.captaingoldfish.scim.sdk.client.response.ServerResponse;
import de.captaingoldfish.scim.sdk.common.constants.HttpStatus;
import lombok.extern.slf4j.Slf4j;


/**
 * @author Pascal Knueppel
 * @since 28.08.2021
 */
@Slf4j
@OAuthRestClientTest
public class TokenStoreHandlerTest extends AbstractScimClientConfig
{

  private static final String TOKEN_STORE_ENDPOINT = "/TokenStore";

  @Test
  public void testCreateTokenStore()
  {
    final String origin = "origin";
    final String name = "name";
    final String token = "token";
    ScimTokenStore scimTokenStore = ScimTokenStore.builder().origin(origin).name(name).token(token).build();
    ServerResponse<ScimTokenStore> response = scimRequestBuilder.create(ScimTokenStore.class, TOKEN_STORE_ENDPOINT)
                                                                .setResource(scimTokenStore)
                                                                .sendRequest();
    Assertions.assertEquals(HttpStatus.CREATED, response.getHttpStatus());
    Assertions.assertTrue(tokenStoreDao.findById(Long.valueOf(response.getResource().getId().orElseThrow()))
                                       .isPresent());
    Assertions.assertEquals(1, tokenStoreDao.count());
  }

  @Test
  public void testGetTokenStore()
  {
    final String origin = "origin";
    final String name = "name";
    final String token = "token";
    TokenStore tokenStore = TokenStore.builder().origin(origin).name(name).token(token).build();
    tokenStore = tokenStoreDao.save(tokenStore);

    ServerResponse<ScimTokenStore> response = scimRequestBuilder.get(ScimTokenStore.class,
                                                                     TOKEN_STORE_ENDPOINT,
                                                                     String.valueOf(tokenStore.getId()))
                                                                .sendRequest();
    Assertions.assertEquals(HttpStatus.OK, response.getHttpStatus());
    Assertions.assertEquals(1, tokenStoreDao.count());
  }

  @Test
  public void testDeleteTokenStore()
  {
    final String origin = "origin";
    final String name = "name";
    final String token = "token";
    TokenStore tokenStore = TokenStore.builder().origin(origin).name(name).token(token).build();
    tokenStore = tokenStoreDao.save(tokenStore);

    ServerResponse<ScimTokenStore> response = scimRequestBuilder.delete(ScimTokenStore.class,
                                                                        TOKEN_STORE_ENDPOINT,
                                                                        String.valueOf(tokenStore.getId()))
                                                                .sendRequest();
    Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getHttpStatus());
    Assertions.assertEquals(0, tokenStoreDao.count());
  }
}
