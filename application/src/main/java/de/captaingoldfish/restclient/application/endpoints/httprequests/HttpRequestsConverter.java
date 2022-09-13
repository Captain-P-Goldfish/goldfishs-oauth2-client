package de.captaingoldfish.restclient.application.endpoints.httprequests;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import de.captaingoldfish.restclient.application.endpoints.httpclient.HttpClientSettingsConverter;
import de.captaingoldfish.restclient.application.utils.Utils;
import de.captaingoldfish.restclient.database.entities.HttpClientSettings;
import de.captaingoldfish.restclient.database.entities.HttpHeader;
import de.captaingoldfish.restclient.database.entities.HttpRequest;
import de.captaingoldfish.restclient.database.entities.HttpRequestCategory;
import de.captaingoldfish.restclient.database.repositories.HttpRequestCategoriesDao;
import de.captaingoldfish.restclient.database.repositories.HttpRequestsDao;
import de.captaingoldfish.restclient.scim.resources.ScimHttpClientSettings;
import de.captaingoldfish.restclient.scim.resources.ScimHttpRequest;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;


/**
 * author Pascal Knueppel <br>
 * created at: 12.09.2022 - 22:33 <br>
 * <br>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpRequestsConverter
{

  private static final String HTTP_HEADER_DELIMITER = "###;###";

  public static ScimHttpRequest toScimHttpRequest(HttpRequest httpRequest)
  {

    HttpClientSettings dbClientSettings = httpRequest.getHttpClientSettings();
    ScimHttpClientSettings clientSettings = HttpClientSettingsConverter.toScimHttpClientSettings(dbClientSettings);

    List<ScimHttpRequest.HttpHeaders> headers = httpRequest.getHttpHeaders().stream().map(header -> {
      return ScimHttpRequest.HttpHeaders.builder()
                                        .name(header.getName())
                                        .values(Arrays.asList(header.getValue().split(HTTP_HEADER_DELIMITER)))
                                        .build();
    }).collect(Collectors.toList());
    return ScimHttpRequest.builder()
                          .id(String.valueOf(httpRequest.getId()))
                          .name(httpRequest.getName())
                          .categoryName(httpRequest.getHttpRequestCategory().getName())
                          .httpMethod(httpRequest.getHttpMethod())
                          .url(httpRequest.getUrl())
                          .requestHeaders(headers)
                          .requestBody(httpRequest.getRequestBody())
                          .responseBody(null)
                          .scimHttpClientSettings(clientSettings)
                          .meta(Meta.builder()
                                    .created(httpRequest.getCreated())
                                    .lastModified(httpRequest.getLastModified())
                                    .build())
                          .build();
  }

  public static HttpRequest toHttpRequest(ScimHttpRequest scimHttpRequest,
                                          HttpRequestsDao httpRequestsDao,
                                          HttpRequestCategoriesDao httpRequestCategoriesDao)
  {
    HttpRequestCategory category = httpRequestCategoriesDao.findByName(scimHttpRequest.getCategoryName()).orElse(null);
    HttpClientSettings clientSettings = HttpClientSettingsConverter.toHttpClientSettings(scimHttpRequest.getHttpClientSettings());

    List<HttpHeader> headers = scimHttpRequest.getRequestHeaders().stream().map(header -> {
      return HttpHeader.builder()
                       .name(header.getName())
                       .value(String.join(HTTP_HEADER_DELIMITER, header.getValues()))
                       .build();
    }).collect(Collectors.toList());
    Optional<HttpRequest> optionalHttpRequest = httpRequestsDao.findById(scimHttpRequest.getId()
                                                                                        .map(Utils::parseId)
                                                                                        .orElse(0L));
    return HttpRequest.builder()
                      .id(scimHttpRequest.getId().map(Utils::parseId).orElse(0L))
                      .name(scimHttpRequest.getName())
                      .httpClientSettings(clientSettings)
                      .httpRequestCategory(category)
                      .httpMethod(scimHttpRequest.getHttpMethod())
                      .url(scimHttpRequest.getUrl())
                      .httpHeaders(headers)
                      .requestBody(scimHttpRequest.getRequestBody().orElse(null))
                      .httpResponses(optionalHttpRequest.map(HttpRequest::getHttpResponses)
                                                        .orElseGet(Collections::emptyList))
                      .created(scimHttpRequest.getMeta().flatMap(Meta::getCreated).orElse(Instant.now()))
                      .lastModified(scimHttpRequest.getMeta().flatMap(Meta::getLastModified).orElse(Instant.now()))
                      .build();
  }

}
