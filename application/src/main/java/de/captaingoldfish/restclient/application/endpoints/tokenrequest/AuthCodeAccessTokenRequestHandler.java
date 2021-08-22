package de.captaingoldfish.restclient.application.endpoints.tokenrequest;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import de.captaingoldfish.restclient.application.endpoints.tokenrequest.request.AuthCodeTokenRequestBuilder;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;
import de.captaingoldfish.restclient.database.repositories.OpenIdClientDao;
import de.captaingoldfish.restclient.scim.resources.ScimAccessTokenRequest;
import de.captaingoldfish.restclient.scim.resources.ScimAccessTokenRequest.RequestHeaders;
import de.captaingoldfish.restclient.scim.resources.ScimAccessTokenRequest.RequestParams;
import de.captaingoldfish.restclient.scim.resources.ScimAccessTokenRequest.ResponseHeaders;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import kong.unirest.HttpResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;


/**
 * @author Pascal Knueppel
 * @since 21.08.2021
 */
@RequiredArgsConstructor
@Component
public class AuthCodeAccessTokenRequestHandler
{

  private final OpenIdClientDao openIdClientDao;

  @SneakyThrows
  public ScimAccessTokenRequest handleAccessTokenRequest(Long openIdClientId,
                                                         String authorizationCode,
                                                         String redirectUri)
  {
    OpenIdClient openIdClient = openIdClientDao.findById(openIdClientId).orElseThrow();
    AuthCodeTokenRequestBuilder requestBuilder = new AuthCodeTokenRequestBuilder(openIdClient, authorizationCode,
                                                                                 redirectUri);

    Map<String, String> requestHeaders = requestBuilder.getRequestHeaders();
    Map<String, String> requestParameters = requestBuilder.getRequestParameters();

    HttpResponse<String> accessTokenResponse = requestBuilder.sendAccessTokenRequest();

    return ScimAccessTokenRequest.builder()
                                 .id(UUID.randomUUID().toString())
                                 .requestHeadersList(requestHeaders.entrySet()
                                                                   .stream()
                                                                   .map(entry -> new RequestHeaders(entry.getKey(),
                                                                                                    entry.getValue()))
                                                                   .collect(Collectors.toList()))
                                 .requestParamsList(requestParameters.entrySet()
                                                                     .stream()
                                                                     .map(entry -> new RequestParams(entry.getKey(),
                                                                                                     entry.getValue()))
                                                                     .collect(Collectors.toList()))
                                 .statusCode(accessTokenResponse.getStatus())
                                 .responseHeadersList(accessTokenResponse.getHeaders()
                                                                         .all()
                                                                         .stream()
                                                                         .map(header -> new ResponseHeaders(header.getName(),
                                                                                                            header.getValue()))
                                                                         .collect(Collectors.toList()))
                                 .plainResponse(accessTokenResponse.getBody())
                                 .meta(Meta.builder().created(Instant.now()).build())
                                 .build();
  }
}
