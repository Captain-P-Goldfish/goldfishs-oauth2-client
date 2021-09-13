package de.captaingoldfish.restclient.application.endpoints.tokencategory;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.captaingoldfish.restclient.application.setup.AbstractScimClientConfig;
import de.captaingoldfish.restclient.application.setup.OAuthRestClientTest;
import de.captaingoldfish.restclient.database.entities.TokenCategory;
import de.captaingoldfish.restclient.scim.resources.ScimTokenCategory;
import de.captaingoldfish.scim.sdk.client.response.ServerResponse;
import de.captaingoldfish.scim.sdk.common.constants.HttpStatus;
import de.captaingoldfish.scim.sdk.common.response.ErrorResponse;
import de.captaingoldfish.scim.sdk.common.response.ListResponse;
import lombok.extern.slf4j.Slf4j;


/**
 * @author Pascal Knueppel
 * @since 11.09.2021
 */
@Slf4j
@OAuthRestClientTest
public class TokenCategoryHandlerTest extends AbstractScimClientConfig
{

  private static final String TOKEN_CATEGORY_ENDPOINT = "/TokenCategory";

  /**
   * verifies that a category for tokens can successfully be created
   */
  @Test
  public void testCreateTokenCategory()
  {
    final String categoryName = "keycloak";
    ScimTokenCategory scimTokenCategory = ScimTokenCategory.builder().name(categoryName).build();
    ServerResponse<ScimTokenCategory> response = scimRequestBuilder.create(ScimTokenCategory.class,
                                                                           TOKEN_CATEGORY_ENDPOINT)
                                                                   .setResource(scimTokenCategory)
                                                                   .sendRequest();
    Assertions.assertEquals(HttpStatus.CREATED, response.getHttpStatus());
    Assertions.assertEquals(categoryName, response.getResource().getName());
    Assertions.assertEquals(1, tokenCategoryDao.count());
    Assertions.assertTrue(tokenCategoryDao.findByName(categoryName).isPresent());
  }

  /**
   * verifies that a duplicate category for tokens cannot be created and a 400 is returned
   */
  @Test
  public void testCreateDuplicateTokenCategory()
  {
    final String categoryName = "keycloak";
    tokenCategoryDao.save(TokenCategory.builder().name(categoryName).build());

    ScimTokenCategory scimTokenCategory = ScimTokenCategory.builder().name(categoryName).build();
    ServerResponse<ScimTokenCategory> response = scimRequestBuilder.create(ScimTokenCategory.class,
                                                                           TOKEN_CATEGORY_ENDPOINT)
                                                                   .setResource(scimTokenCategory)
                                                                   .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();

    List<String> errorMessages = errorResponse.getErrorMessages();
    Assertions.assertEquals(0, errorMessages.size());

    Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
    Assertions.assertEquals(1, fieldErrors.size());

    List<String> nameErrors = fieldErrors.get(ScimTokenCategory.FieldNames.NAME);
    Assertions.assertEquals(1, nameErrors.size());
    String expectedMessage = String.format("The given token category name '%s' does already exist", categoryName);
    Assertions.assertEquals(expectedMessage, nameErrors.get(0));

    Assertions.assertEquals(1, tokenCategoryDao.count());
    Assertions.assertTrue(tokenCategoryDao.findByName(categoryName).isPresent());
  }

  /**
   * shows that a token category can be successfully retrieved
   */
  @Test
  public void testGetTokenCategory()
  {
    final String categoryName = "keycloak";
    TokenCategory tokenCategory = tokenCategoryDao.save(TokenCategory.builder().name(categoryName).build());

    ServerResponse<ScimTokenCategory> response = scimRequestBuilder.get(ScimTokenCategory.class,
                                                                        TOKEN_CATEGORY_ENDPOINT,
                                                                        String.valueOf(tokenCategory.getId()))
                                                                   .sendRequest();
    Assertions.assertEquals(HttpStatus.OK, response.getHttpStatus());
    Assertions.assertEquals(String.valueOf(tokenCategory.getId()), response.getResource().getId().get());
    Assertions.assertEquals(categoryName, response.getResource().getName());
  }

  /**
   * shows that a token category can be successfully retrieved
   */
  @Test
  public void testListTokenCategories()
  {
    final String categoryName1 = "bearer";
    final String categoryName2 = "saml";
    TokenCategory tokenCategory1 = tokenCategoryDao.save(TokenCategory.builder().name(categoryName1).build());
    TokenCategory tokenCategory2 = tokenCategoryDao.save(TokenCategory.builder().name(categoryName2).build());

    ServerResponse<ListResponse<ScimTokenCategory>> response = scimRequestBuilder.list(ScimTokenCategory.class,
                                                                                       TOKEN_CATEGORY_ENDPOINT)
                                                                                 .get()
                                                                                 .sendRequest();
    Assertions.assertEquals(HttpStatus.OK, response.getHttpStatus());

    ListResponse<ScimTokenCategory> listResponse = response.getResource();
    Assertions.assertEquals(2, listResponse.getTotalResults());
    Assertions.assertTrue(listResponse.getListedResources()
                                      .stream()
                                      .anyMatch(resource -> resource.getName().equals(categoryName1)
                                                            && resource.getId()
                                                                       .get()
                                                                       .equals(String.valueOf(tokenCategory1.getId()))));
    Assertions.assertTrue(listResponse.getListedResources()
                                      .stream()
                                      .anyMatch(resource -> resource.getName().equals(categoryName2)
                                                            && resource.getId()
                                                                       .get()
                                                                       .equals(String.valueOf(tokenCategory2.getId()))));
  }

  /**
   * verifies that a token category can be successfully renamed
   */
  @Test
  public void testUpdateTokenCategory()
  {
    final String categoryName = "keycloak";
    TokenCategory tokenCategory = tokenCategoryDao.save(TokenCategory.builder().name(categoryName).build());

    final String newCategoryName = "bearer";
    ScimTokenCategory scimTokenCategory = ScimTokenCategory.builder().name(newCategoryName).build();
    ServerResponse<ScimTokenCategory> response = scimRequestBuilder.update(ScimTokenCategory.class,
                                                                           TOKEN_CATEGORY_ENDPOINT,
                                                                           String.valueOf(tokenCategory.getId()))
                                                                   .setResource(scimTokenCategory)
                                                                   .sendRequest();
    Assertions.assertEquals(HttpStatus.OK, response.getHttpStatus());
    Assertions.assertEquals(String.valueOf(tokenCategory.getId()), response.getResource().getId().get());
    Assertions.assertEquals(newCategoryName, response.getResource().getName());
    Assertions.assertEquals(1, tokenCategoryDao.count());
  }

  /**
   * verifies that a token category cannot be renamed if the new name is already taken
   */
  @Test
  public void testUpdateTokenCategoryToAlreadyExistingName()
  {
    final String categoryName1 = "keycloak";
    final String categoryName2 = "bearer";
    TokenCategory tokenCategory1 = tokenCategoryDao.save(TokenCategory.builder().name(categoryName1).build());
    tokenCategoryDao.save(TokenCategory.builder().name(categoryName2).build());

    ScimTokenCategory scimTokenCategory = ScimTokenCategory.builder().name(categoryName2).build();
    ServerResponse<ScimTokenCategory> response = scimRequestBuilder.update(ScimTokenCategory.class,
                                                                           TOKEN_CATEGORY_ENDPOINT,
                                                                           String.valueOf(tokenCategory1.getId()))
                                                                   .setResource(scimTokenCategory)
                                                                   .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();

    List<String> errorMessages = errorResponse.getErrorMessages();
    Assertions.assertEquals(0, errorMessages.size());

    Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
    Assertions.assertEquals(1, fieldErrors.size());

    List<String> nameErrors = fieldErrors.get(ScimTokenCategory.FieldNames.NAME);
    Assertions.assertEquals(1, nameErrors.size());
    String expectedMessage = String.format("The given token category name '%s' does already exist", categoryName2);
    Assertions.assertEquals(expectedMessage, nameErrors.get(0));
    Assertions.assertEquals(2, tokenCategoryDao.count());
  }

  /**
   * verifies that a token category can be successfully deleted
   */
  @Test
  public void testDeleteTokenCategory()
  {
    final String categoryName = "keycloak";
    TokenCategory tokenCategory = tokenCategoryDao.save(TokenCategory.builder().name(categoryName).build());

    Assertions.assertEquals(1, tokenCategoryDao.count());
    ServerResponse<ScimTokenCategory> response = scimRequestBuilder.delete(ScimTokenCategory.class,
                                                                           TOKEN_CATEGORY_ENDPOINT,
                                                                           String.valueOf(tokenCategory.getId()))
                                                                   .sendRequest();
    Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getHttpStatus());
    Assertions.assertEquals(0, tokenCategoryDao.count());
  }
}
