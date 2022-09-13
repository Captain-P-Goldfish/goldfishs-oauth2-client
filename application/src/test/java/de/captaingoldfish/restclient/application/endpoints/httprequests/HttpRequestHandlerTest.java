package de.captaingoldfish.restclient.application.endpoints.httprequests;

import java.time.Instant;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.captaingoldfish.restclient.application.setup.AbstractScimClientConfig;
import de.captaingoldfish.restclient.application.setup.OAuthRestClientTest;
import de.captaingoldfish.restclient.database.entities.HttpRequestCategory;
import de.captaingoldfish.restclient.scim.resources.ScimHttpClientSettings;
import de.captaingoldfish.restclient.scim.resources.ScimHttpRequest;
import de.captaingoldfish.scim.sdk.client.response.ServerResponse;
import de.captaingoldfish.scim.sdk.common.constants.HttpStatus;
import de.captaingoldfish.scim.sdk.common.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;


/**
 * author Pascal Knueppel <br>
 * created at: 13.09.2022 - 16:16 <br>
 * <br>
 */
@Slf4j
@OAuthRestClientTest
public class HttpRequestHandlerTest extends AbstractScimClientConfig
{

  /**
   * the scim endpoint for managing http requests
   */
  private static final String HTTP_REQUESTS_ENDPOINT = "/HttpRequests";

  private HttpRequestCategory category;

  @BeforeEach
  public void testCreateCategory()
  {
    category = HttpRequestCategory.builder()
                                  .name("keycloak")
                                  .created(Instant.now())
                                  .lastModified(Instant.now())
                                  .build();
    httpRequestCategoriesDao.save(category);
  }


  /**
   * verifies that the request will fail if no category name is present
   */
  @Test
  public void testCreateRequestWithoutCategory()
  {
    ScimHttpRequest scimHttpRequest = ScimHttpRequest.builder()
                                                     .name("create-client")
                                                     .url("https://localhost:8443")
                                                     .requestBody("{}")
                                                     .httpMethod("POST")
                                                     .scimHttpClientSettings(ScimHttpClientSettings.builder()
                                                                                                   .requestTimeout(5L)
                                                                                                   .socketTimeout(5L)
                                                                                                   .connectionTimeout(5L)
                                                                                                   .useHostnameVerifier(true)
                                                                                                   .build())
                                                     .build();
    ServerResponse<ScimHttpRequest> response = scimRequestBuilder.create(ScimHttpRequest.class, HTTP_REQUESTS_ENDPOINT)
                                                                 .setResource(scimHttpRequest)
                                                                 .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();
    Assertions.assertEquals(1, errorResponse.getFieldErrors().size());
    Assertions.assertEquals("Required 'READ_WRITE' attribute 'urn:ietf:params:scim:schemas:captaingoldfish:"
                            + "2.0:HttpRequest:categoryName' is missing",
                            errorResponse.getFieldErrors().get(ScimHttpRequest.FieldNames.CATEGORY_NAME).get(0));
  }

  /**
   * verifies that the request will fail if an unknown category name is present
   */
  @Test
  public void testCreateRequestWithUnknownCategoryName()
  {
    ScimHttpRequest scimHttpRequest = ScimHttpRequest.builder()
                                                     .categoryName("unknown-category")
                                                     .name("create-client")
                                                     .url("https://localhost:8443")
                                                     .requestBody("{}")
                                                     .httpMethod("POST")
                                                     .scimHttpClientSettings(ScimHttpClientSettings.builder()
                                                                                                   .requestTimeout(5L)
                                                                                                   .socketTimeout(5L)
                                                                                                   .connectionTimeout(5L)
                                                                                                   .useHostnameVerifier(true)
                                                                                                   .build())
                                                     .build();
    ServerResponse<ScimHttpRequest> response = scimRequestBuilder.create(ScimHttpRequest.class, HTTP_REQUESTS_ENDPOINT)
                                                                 .setResource(scimHttpRequest)
                                                                 .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();
    Assertions.assertEquals(1, errorResponse.getFieldErrors().size());
    Assertions.assertEquals("Unknown http request category 'unknown-category'",
                            errorResponse.getFieldErrors().get(ScimHttpRequest.FieldNames.CATEGORY_NAME).get(0));
  }

  /**
   * verifies that the request will fail if no category name is present
   */
  @Test
  public void testCreateRequest()
  {
    ScimHttpRequest scimHttpRequest = ScimHttpRequest.builder()
                                                     .categoryName(category.getName())
                                                     .name("create-client")
                                                     .url("https://localhost:8443")
                                                     .requestBody("{}")
                                                     .httpMethod("POST")
                                                     .scimHttpClientSettings(ScimHttpClientSettings.builder()
                                                                                                   .requestTimeout(5L)
                                                                                                   .socketTimeout(5L)
                                                                                                   .connectionTimeout(5L)
                                                                                                   .useHostnameVerifier(true)
                                                                                                   .build())
                                                     .build();
    ServerResponse<ScimHttpRequest> response = scimRequestBuilder.create(ScimHttpRequest.class, HTTP_REQUESTS_ENDPOINT)
                                                                 .setResource(scimHttpRequest)
                                                                 .sendRequest();
    Assertions.assertEquals(HttpStatus.CREATED, response.getHttpStatus());
    ScimHttpRequest createdRequest = response.getResource();
    log.warn(createdRequest.toPrettyString());
  }
}
