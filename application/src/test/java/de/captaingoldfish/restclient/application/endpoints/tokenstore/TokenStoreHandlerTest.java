package de.captaingoldfish.restclient.application.endpoints.tokenstore;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.captaingoldfish.restclient.application.setup.AbstractScimClientConfig;
import de.captaingoldfish.restclient.application.setup.OAuthRestClientTest;
import de.captaingoldfish.restclient.database.entities.TokenCategory;
import de.captaingoldfish.restclient.database.entities.TokenStore;
import de.captaingoldfish.restclient.scim.resources.ScimTokenStore;
import de.captaingoldfish.scim.sdk.client.response.ServerResponse;
import de.captaingoldfish.scim.sdk.common.constants.HttpStatus;
import de.captaingoldfish.scim.sdk.common.response.ErrorResponse;
import de.captaingoldfish.scim.sdk.common.response.ListResponse;
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

  /**
   * the base token category that will be used to store tokens
   */
  private TokenCategory tokenCategory;

  @BeforeEach
  public void createBaseCategory()
  {
    tokenCategory = tokenCategoryDao.save(TokenCategory.builder().name("miscellaneous").build());
  }

  @Test
  public void testCreateTokenStore()
  {
    final String name = "name";
    final String token = "token";
    ScimTokenStore scimTokenStore = ScimTokenStore.builder()
                                                  .categoryId(tokenCategory.getId())
                                                  .name(name)
                                                  .token(token)
                                                  .build();
    ServerResponse<ScimTokenStore> response = scimRequestBuilder.create(ScimTokenStore.class, TOKEN_STORE_ENDPOINT)
                                                                .setResource(scimTokenStore)
                                                                .sendRequest();
    Assertions.assertEquals(HttpStatus.CREATED, response.getHttpStatus());
    Assertions.assertTrue(tokenStoreDao.findById(Long.valueOf(response.getResource().getId().orElseThrow()))
                                       .isPresent());
    Assertions.assertEquals(1, tokenStoreDao.count());
  }

  /**
   * tokens are not unique and duplicate names in the same category may be used
   */
  @Test
  public void testCreateDuplicateTokenStore()
  {
    final String name = "name";
    final String token = "token";
    tokenStoreDao.save(TokenStore.builder().tokenCategory(tokenCategory).name(name).token(token).build());

    ScimTokenStore duplicateScimTokenStore = ScimTokenStore.builder()
                                                           .categoryId(tokenCategory.getId())
                                                           .name(name)
                                                           .token(token)
                                                           .build();
    ServerResponse<ScimTokenStore> response = scimRequestBuilder.create(ScimTokenStore.class, TOKEN_STORE_ENDPOINT)
                                                                .setResource(duplicateScimTokenStore)
                                                                .sendRequest();
    Assertions.assertEquals(HttpStatus.CREATED, response.getHttpStatus());
    Assertions.assertTrue(tokenStoreDao.findById(Long.valueOf(response.getResource().getId().orElseThrow()))
                                       .isPresent());
    Assertions.assertEquals(2, tokenStoreDao.count());
  }

  /**
   * tokens cannot be created without an existing category
   */
  @Test
  public void testLinkNewTokenStoreWithNoneExistingCategory()
  {
    final String name = "name";
    final String token = "token";

    final long nonExistingCategoryId = Long.MAX_VALUE;
    ScimTokenStore duplicateScimTokenStore = ScimTokenStore.builder()
                                                           .categoryId(nonExistingCategoryId)
                                                           .name(name)
                                                           .token(token)
                                                           .build();
    ServerResponse<ScimTokenStore> response = scimRequestBuilder.create(ScimTokenStore.class, TOKEN_STORE_ENDPOINT)
                                                                .setResource(duplicateScimTokenStore)
                                                                .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();

    List<String> errorMessages = errorResponse.getErrorMessages();
    Assertions.assertEquals(0, errorMessages.size());

    Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
    Assertions.assertEquals(1, fieldErrors.size());

    List<String> categoryErrors = fieldErrors.get(ScimTokenStore.FieldNames.CATEGORY_ID);
    Assertions.assertEquals(1, categoryErrors.size());
    String expectedMessage = String.format("Could not find owning category with ID '%s'", nonExistingCategoryId);
    Assertions.assertEquals(expectedMessage, categoryErrors.get(0));
  }

  @Test
  public void testGetTokenStore()
  {
    final String name = "name";
    final String token = "token";
    TokenStore tokenStore = TokenStore.builder().tokenCategory(tokenCategory).name(name).token(token).build();
    tokenStore = tokenStoreDao.save(tokenStore);

    ServerResponse<ScimTokenStore> response = scimRequestBuilder.get(ScimTokenStore.class,
                                                                     TOKEN_STORE_ENDPOINT,
                                                                     String.valueOf(tokenStore.getId()))
                                                                .sendRequest();
    Assertions.assertEquals(HttpStatus.OK, response.getHttpStatus());
    Assertions.assertEquals(1, tokenStoreDao.count());
  }

  @Test
  public void testListTokenStores()
  {
    final String name = "name";
    final String token = "token";
    tokenStoreDao.save(TokenStore.builder().tokenCategory(tokenCategory).name(name).token(token).build());
    tokenStoreDao.save(TokenStore.builder().tokenCategory(tokenCategory).name(name).token(token).build());
    tokenStoreDao.save(TokenStore.builder().tokenCategory(tokenCategory).name(name).token(token).build());

    ServerResponse<ListResponse<ScimTokenStore>> response = scimRequestBuilder.list(ScimTokenStore.class,
                                                                                    TOKEN_STORE_ENDPOINT)
                                                                              .get()
                                                                              .sendRequest();
    Assertions.assertEquals(HttpStatus.OK, response.getHttpStatus());
    ListResponse<ScimTokenStore> listResponse = response.getResource();
    Assertions.assertEquals(tokenStoreDao.count(), listResponse.getTotalResults());
    Assertions.assertEquals(tokenStoreDao.count(), listResponse.getListedResources().size());
  }

  @Test
  public void testUpdateTokenStore()
  {
    final String name = "name";
    final String token = "token";
    TokenStore tokenStore = tokenStoreDao.save(TokenStore.builder()
                                                         .tokenCategory(tokenCategory)
                                                         .name(name)
                                                         .token(token)
                                                         .build());

    final String newName = "newName";
    final String newToken = "newToken";
    final TokenCategory newCategory = tokenCategoryDao.save(TokenCategory.builder().name("newCategory").build());
    ScimTokenStore scimTokenStore = ScimTokenStore.builder()
                                                  .categoryId(newCategory.getId())
                                                  .name(newName)
                                                  .token(newToken)
                                                  .build();

    ServerResponse<ScimTokenStore> response = scimRequestBuilder.update(ScimTokenStore.class,
                                                                        TOKEN_STORE_ENDPOINT,
                                                                        String.valueOf(tokenStore.getId()))
                                                                .setResource(scimTokenStore)
                                                                .sendRequest();
    Assertions.assertEquals(HttpStatus.OK, response.getHttpStatus());
    Assertions.assertEquals(1, tokenStoreDao.count());
    ScimTokenStore updatedToken = response.getResource();
    Assertions.assertEquals(newCategory.getId(), updatedToken.getCategoryId());
    Assertions.assertEquals(newName, updatedToken.getName());
    Assertions.assertEquals(newToken, updatedToken.getToken());
  }

  @Test
  public void testUpdateToNoneExistingTokenCategory()
  {
    final String name = "name";
    final String token = "token";
    TokenStore tokenStore = tokenStoreDao.save(TokenStore.builder()
                                                         .tokenCategory(tokenCategory)
                                                         .name(name)
                                                         .token(token)
                                                         .build());

    final String newName = "newName";
    final String newToken = "newToken";
    final Long nonExistingCategoryId = Long.MAX_VALUE;
    ScimTokenStore scimTokenStore = ScimTokenStore.builder()
                                                  .categoryId(nonExistingCategoryId)
                                                  .name(newName)
                                                  .token(newToken)
                                                  .build();

    ServerResponse<ScimTokenStore> response = scimRequestBuilder.update(ScimTokenStore.class,
                                                                        TOKEN_STORE_ENDPOINT,
                                                                        String.valueOf(tokenStore.getId()))
                                                                .setResource(scimTokenStore)
                                                                .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();

    List<String> errorMessages = errorResponse.getErrorMessages();
    Assertions.assertEquals(0, errorMessages.size());

    Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
    Assertions.assertEquals(1, fieldErrors.size());

    List<String> categoryErrors = fieldErrors.get(ScimTokenStore.FieldNames.CATEGORY_ID);
    Assertions.assertEquals(1, categoryErrors.size());
    String expectedMessage = String.format("Could not find owning category with ID '%s'", nonExistingCategoryId);
    Assertions.assertEquals(expectedMessage, categoryErrors.get(0));
  }

  @Test
  public void testDeleteTokenStore()
  {
    final String name = "name";
    final String token = "token";
    TokenStore tokenStore = TokenStore.builder().tokenCategory(tokenCategory).name(name).token(token).build();
    tokenStore = tokenStoreDao.save(tokenStore);

    ServerResponse<ScimTokenStore> response = scimRequestBuilder.delete(ScimTokenStore.class,
                                                                        TOKEN_STORE_ENDPOINT,
                                                                        String.valueOf(tokenStore.getId()))
                                                                .sendRequest();
    Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getHttpStatus());
    Assertions.assertEquals(0, tokenStoreDao.count());
  }

  @Test
  public void testDeleteNonExistingTokenStore()
  {
    ServerResponse<ScimTokenStore> response = scimRequestBuilder.delete(ScimTokenStore.class,
                                                                        TOKEN_STORE_ENDPOINT,
                                                                        String.valueOf(1L))
                                                                .sendRequest();
    Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getHttpStatus());
    Assertions.assertEquals(0, tokenStoreDao.count());
  }
}
