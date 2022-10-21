package de.captaingoldfish.restclient.application.endpoints.httprequests;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import de.captaingoldfish.restclient.application.endpoints.httpclient.HttpClientSettingsConverter;
import de.captaingoldfish.restclient.application.utils.Utils;
import de.captaingoldfish.restclient.database.entities.HttpClientSettings;
import de.captaingoldfish.restclient.database.entities.HttpHeader;
import de.captaingoldfish.restclient.database.entities.HttpRequest;
import de.captaingoldfish.restclient.database.entities.HttpRequestGroup;
import de.captaingoldfish.restclient.database.entities.HttpResponse;
import de.captaingoldfish.restclient.database.repositories.HttpRequestCategoriesDao;
import de.captaingoldfish.restclient.database.repositories.HttpRequestsDao;
import de.captaingoldfish.restclient.scim.resources.ScimHttpClientSettings;
import de.captaingoldfish.restclient.scim.resources.ScimHttpRequest;
import de.captaingoldfish.restclient.scim.resources.ScimHttpRequest.HttpHeaders;
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

  public static ScimHttpRequest toScimHttpRequest(HttpRequest httpRequest, HttpResponse httpResponse)
  {

    HttpClientSettings dbClientSettings = httpRequest.getHttpClientSettings();
    ScimHttpClientSettings clientSettings = HttpClientSettingsConverter.toScimHttpClientSettings(dbClientSettings);

    List<HttpHeaders> headers = httpRequest.getHttpHeaders().stream().map(header -> {
      return HttpHeaders.builder().name(header.getName()).value(header.getValue()).build();
    }).collect(Collectors.toList());

    List<HttpHeaders> httpHeaders = Collections.emptyList();
    boolean responseHeadersPresent = Optional.ofNullable(httpResponse)
                                             .map(HttpResponse::getResponseHeaders)
                                             .map(list -> !list.isEmpty())
                                             .orElse(false);
    if (responseHeadersPresent)
    {
      httpHeaders = Arrays.stream(httpResponse.getResponseHeaders().split("\n"))
                          .map(line -> line.split(":"))
                          .map(keyValue -> new HttpHeaders(keyValue[0],
                                                           keyValue.length > 1 ? StringUtils.trim(keyValue[1]) : null))
                          .collect(Collectors.toList());
    }

    return ScimHttpRequest.builder()
                          .id(String.valueOf(httpRequest.getId()))
                          .name(httpRequest.getName())
                          .groupName(Optional.ofNullable(httpRequest.getHttpRequestGroup())
                                             .map(HttpRequestGroup::getName)
                                             .orElse(null))
                          .httpMethod(httpRequest.getHttpMethod())
                          .url(httpRequest.getUrl())
                          .requestHeaders(headers)
                          .requestBody(httpRequest.getRequestBody())
                          .responseStatus(Optional.ofNullable(httpResponse)
                                                  .map(HttpResponse::getResponseStatus)
                                                  .map(String::valueOf)
                                                  .orElse(null))
                          .responseHeaders(httpHeaders)
                          .responseBody(Optional.ofNullable(httpResponse)
                                                .map(HttpResponse::getResponseBody)
                                                .orElse(null))
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
    HttpRequestGroup group = httpRequestCategoriesDao.findByName(scimHttpRequest.getGroupName()).orElse(null);
    HttpClientSettings clientSettings = HttpClientSettingsConverter.toHttpClientSettings(scimHttpRequest.getHttpClientSettings());

    List<HttpHeader> headers = scimHttpRequest.getRequestHeaders().stream().map(header -> {
      return HttpHeader.builder().name(header.getName()).value(header.getValue()).build();
    }).collect(Collectors.toList());
    Optional<HttpRequest> optionalHttpRequest = httpRequestsDao.findById(scimHttpRequest.getId()
                                                                                        .map(Utils::parseId)
                                                                                        .orElse(0L));
    return HttpRequest.builder()
                      .id(scimHttpRequest.getId().map(Utils::parseId).orElse(0L))
                      .name(scimHttpRequest.getName())
                      .httpClientSettings(clientSettings)
                      .httpRequestGroup(group)
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
