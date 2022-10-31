package de.captaingoldfish.restclient.application.endpoints.responsehistory;

import java.util.ArrayList;
import java.util.List;

import de.captaingoldfish.restclient.database.entities.HttpRequest;
import de.captaingoldfish.restclient.database.entities.HttpResponse;
import de.captaingoldfish.restclient.scim.resources.ScimHttpResponseHistory;
import de.captaingoldfish.restclient.scim.resources.ScimHttpResponseHistory.ResponseHistory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;


/**
 * @author Pascal Knueppel
 * @since 21.10.2022
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpResponseConverter
{

  public static ScimHttpResponseHistory toScimResponseHistory(HttpRequest httpRequest)
  {
    List<ResponseHistory> responseHistory = new ArrayList<>();

    for ( HttpResponse httpResponse : httpRequest.getHttpResponses() )
    {
      responseHistory.add(ResponseHistory.builder()
                                         .id(String.valueOf(httpResponse.getId()))
                                         .status(httpResponse.getResponseStatus())
                                         .originalRequest(httpResponse.getRequestDetails())
                                         .responseHeaders(httpResponse.getResponseHeaders())
                                         .responseBody(httpResponse.getResponseBody())
                                         .created(httpResponse.getCreated())
                                         .build());
    }

    return ScimHttpResponseHistory.builder()
                                  .id(String.valueOf(httpRequest.getId()))
                                  .groupName(httpRequest.getHttpRequestGroup().getName())
                                  .requestName(httpRequest.getName())
                                  .responseHistory(responseHistory)
                                  .build();
  }

}
