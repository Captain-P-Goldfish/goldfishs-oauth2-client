package de.captaingoldfish.restclient.database.entities;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import de.captaingoldfish.restclient.database.DatabaseTest;
import de.captaingoldfish.restclient.database.DbBaseTest;


/**
 * author Pascal Knueppel <br>
 * created at: 13.09.2022 - 15:27 <br>
 * <br>
 */
@DatabaseTest
public class HttpRequestTest extends DbBaseTest
{

  @Test
  public void testCreateAndDeleteHttpRequest()
  {
    HttpRequestCategory requestCategory = HttpRequestCategory.builder()
                                                             .name("keycloak")
                                                             .created(Instant.now())
                                                             .lastModified(Instant.now())
                                                             .build();
    httpRequestCategoriesDao.save(requestCategory);

    final int countOfHttpRequests = 5;
    for ( int i = 0 ; i < countOfHttpRequests ; i++ )
    {
      HttpRequest httpRequest = HttpRequest.builder()
                                           .httpRequestCategory(requestCategory)
                                           .httpMethod(HttpMethod.POST.name())
                                           .url("https://localhost:8443")
                                           .httpHeaders(List.of(HttpHeader.builder()
                                                                          .name("Content-Type")
                                                                          .value("application/json")
                                                                          .build(),
                                                                HttpHeader.builder()
                                                                          .name("Authorization")
                                                                          .value("Basic Mtox")
                                                                          .build()))
                                           .requestBody("{\"name\": \"" + i + "\"}")
                                           .httpResponses(List.of(HttpResponse.builder()
                                                                              .requestDetails("some details")
                                                                              .responseHeaders("some headers")
                                                                              .responseBody("response body")
                                                                              .created(Instant.now())
                                                                              .build(),
                                                                  HttpResponse.builder()
                                                                              .requestDetails("some details")
                                                                              .responseHeaders("some headers")
                                                                              .responseBody("response body")
                                                                              .created(Instant.now())
                                                                              .build()))
                                           .httpClientSettings(HttpClientSettings.builder()
                                                                                 .openIdClient(null)
                                                                                 .requestTimeout(5)
                                                                                 .connectionTimeout(5)
                                                                                 .socketTimeout(5)
                                                                                 .useHostnameVerifier(false)
                                                                                 .build())
                                           .created(Instant.now())
                                           .lastModified(Instant.now())
                                           .build();
      httpRequestsDao.save(httpRequest);
    }

    Assertions.assertEquals(1, httpRequestCategoriesDao.count());
    Assertions.assertEquals(countOfHttpRequests, httpRequestsDao.count());
    Assertions.assertEquals(2 * countOfHttpRequests, countEntriesOfTable(HttpResponse.class.getSimpleName()));
    Assertions.assertEquals(countOfHttpRequests, countEntriesOfTable(HttpClientSettings.class.getSimpleName()));
    Assertions.assertEquals(2 * countOfHttpRequests, countEntriesOfTable(HttpHeader.class.getSimpleName()));

    httpRequestCategoriesDao.deleteById(requestCategory.getId());

    Assertions.assertEquals(0, httpRequestCategoriesDao.count());
    Assertions.assertEquals(0, httpRequestsDao.count());
    Assertions.assertEquals(0, httpClientSettingsDao.count());
    Assertions.assertEquals(0, countEntriesOfTable(HttpResponse.class.getSimpleName()));
    Assertions.assertEquals(0, countEntriesOfTable(HttpHeader.class.getSimpleName()));
    Assertions.assertEquals(0, countEntriesOfTableNative("HTTP_HEADERS_REQUEST_MAPPING"));
    Assertions.assertEquals(0, countEntriesOfTableNative("HTTP_REQUEST_RESPONSE_MAPPING"));
  }
}
