package de.captaingoldfish.restclient.application.endpoints.httprequests;

import java.util.List;
import java.util.stream.Collectors;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.captaingoldfish.restclient.application.setup.AbstractScimClientConfig;
import de.captaingoldfish.restclient.application.setup.OAuthRestClientTest;
import de.captaingoldfish.restclient.application.utils.Utils;
import de.captaingoldfish.restclient.database.entities.HttpRequestCategory;
import de.captaingoldfish.restclient.scim.resources.ScimHttpRequestCategory;
import de.captaingoldfish.scim.sdk.client.response.ServerResponse;
import de.captaingoldfish.scim.sdk.common.constants.HttpStatus;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import de.captaingoldfish.scim.sdk.common.response.ListResponse;
import lombok.extern.slf4j.Slf4j;


/**
 * author Pascal Knueppel <br>
 * created at: 13.09.2022 - 19:54 <br>
 * <br>
 */
@Slf4j
@OAuthRestClientTest
public class HttpRequestsCategoriesHandlerTest extends AbstractScimClientConfig
{

  /**
   * the scim endpoint for managing http request categories
   */
  private static final String HTTP_REQUEST_CATEGORY_ENDPOINT = "/HttpRequestCategories";

  /**
   * verifies that http request categories can be created successfully
   */
  @ParameterizedTest
  @ValueSource(strings = {"a", "a-a", "a_a", "a.1.0"})
  public void testCreateHttpRequestCategory(String name)
  {
    ScimHttpRequestCategory httpRequestCategory = ScimHttpRequestCategory.builder().name(name).build();

    ServerResponse<ScimHttpRequestCategory> response = scimRequestBuilder.create(ScimHttpRequestCategory.class,
                                                                                 HTTP_REQUEST_CATEGORY_ENDPOINT)
                                                                         .setResource(httpRequestCategory)
                                                                         .sendRequest();

    Assertions.assertEquals(HttpStatus.CREATED, response.getHttpStatus());
    ScimHttpRequestCategory category = response.getResource();

    Assertions.assertEquals(1, httpRequestCategoriesDao.count());
    HttpRequestCategory dbCategory = httpRequestCategoriesDao.findById(Utils.parseId(category.getId().get())).get();
    Assertions.assertEquals(name, dbCategory.getName());
    Assertions.assertEquals(name, category.getName());
    Assertions.assertEquals(dbCategory.getCreated(), category.getMeta().flatMap(Meta::getCreated).orElse(null));
    Assertions.assertEquals(dbCategory.getLastModified(),
                            category.getMeta().flatMap(Meta::getLastModified).orElse(null));
  }

  /**
   * shows that the http categories can be listed
   */
  @Test
  public void testListHttpRequestCategory()
  {
    httpRequestCategoriesDao.save(HttpRequestCategory.builder().name("hello").build());
    httpRequestCategoriesDao.save(HttpRequestCategory.builder().name("world").build());

    ServerResponse<ListResponse<ScimHttpRequestCategory>> response = scimRequestBuilder.list(ScimHttpRequestCategory.class,
                                                                                             HTTP_REQUEST_CATEGORY_ENDPOINT)
                                                                                       .get()
                                                                                       .sendRequest();

    Assertions.assertEquals(HttpStatus.OK, response.getHttpStatus());
    ListResponse<ScimHttpRequestCategory> requestCategories = response.getResource();
    Assertions.assertEquals(2, requestCategories.getTotalResults());
    List<ScimHttpRequestCategory> requestCategoryList = requestCategories.getListedResources();
    MatcherAssert.assertThat(requestCategoryList.stream()
                                                .map(ScimHttpRequestCategory::getName)
                                                .collect(Collectors.toList()),
                             Matchers.containsInAnyOrder("hello", "world"));
  }

  /**
   * verifies that a request category can be successfully updated
   */
  @Test
  public void testUpdateRequestCategory()
  {
    HttpRequestCategory dbCategory = httpRequestCategoriesDao.save(HttpRequestCategory.builder()
                                                                                      .name("hello-world")
                                                                                      .build());

    ScimHttpRequestCategory updateResource = ScimHttpRequestCategory.builder().name("new-value").build();

    ServerResponse<ScimHttpRequestCategory> response = scimRequestBuilder.update(ScimHttpRequestCategory.class,
                                                                                 HTTP_REQUEST_CATEGORY_ENDPOINT,
                                                                                 String.valueOf(dbCategory.getId()))
                                                                         .setResource(updateResource)
                                                                         .sendRequest();

    Assertions.assertEquals(HttpStatus.OK, response.getHttpStatus());
    ScimHttpRequestCategory updatedCategory = response.getResource();

    dbCategory = httpRequestCategoriesDao.findById(updatedCategory.getId().map(Utils::parseId).get()).get();

    Assertions.assertEquals(1, httpRequestCategoriesDao.count());
    Assertions.assertEquals(updateResource.getName(), dbCategory.getName());
  }

  /**
   * verifies that a http request category can successfully be deleted
   */
  @Test
  public void testDeleteRequestCategory()
  {
    HttpRequestCategory dbCategory = httpRequestCategoriesDao.save(HttpRequestCategory.builder()
                                                                                      .name("hello-world")
                                                                                      .build());

    ServerResponse<ScimHttpRequestCategory> response = scimRequestBuilder.delete(ScimHttpRequestCategory.class,
                                                                                 HTTP_REQUEST_CATEGORY_ENDPOINT,
                                                                                 String.valueOf(dbCategory.getId()))
                                                                         .sendRequest();

    Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getHttpStatus());
    Assertions.assertEquals(0, httpRequestCategoriesDao.count());
  }
}
