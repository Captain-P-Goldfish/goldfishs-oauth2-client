package de.captaingoldfish.restclient.application.endpoints.tokenrequest;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import de.captaingoldfish.restclient.application.endpoints.tokenrequest.request.AccessTokenRequestBuilder;
import de.captaingoldfish.restclient.application.endpoints.tokenrequest.request.AccessTokenRequestBuilderFactory;
import de.captaingoldfish.restclient.application.endpoints.tokenrequest.validation.AccessTokenRequestValidator;
import de.captaingoldfish.restclient.scim.resources.ScimAccessTokenRequest;
import de.captaingoldfish.restclient.scim.resources.ScimAccessTokenRequest.RequestHeaders;
import de.captaingoldfish.restclient.scim.resources.ScimAccessTokenRequest.RequestParams;
import de.captaingoldfish.restclient.scim.resources.ScimAccessTokenRequest.ResponseHeaders;
import de.captaingoldfish.scim.sdk.common.constants.enums.SortOrder;
import de.captaingoldfish.scim.sdk.common.exceptions.BadRequestException;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import de.captaingoldfish.scim.sdk.common.schemas.SchemaAttribute;
import de.captaingoldfish.scim.sdk.server.endpoints.Context;
import de.captaingoldfish.scim.sdk.server.endpoints.ResourceHandler;
import de.captaingoldfish.scim.sdk.server.endpoints.validation.RequestValidator;
import de.captaingoldfish.scim.sdk.server.filter.FilterNode;
import de.captaingoldfish.scim.sdk.server.response.PartialListResponse;
import kong.unirest.HttpResponse;


/**
 * @author Pascal Knueppel
 * @since 21.08.2021
 */
public class AccessTokenRequestHandler extends ResourceHandler<ScimAccessTokenRequest>
{


  /**
   * used to initiate an access token request, to gather all information about the request and response and to
   * return this information to the frontend for display purposes
   */
  @Override
  public ScimAccessTokenRequest createResource(ScimAccessTokenRequest resource, Context context)
  {
    try
    {
      AccessTokenRequestBuilder requestBuilder = AccessTokenRequestBuilderFactory.getBuilder(resource);

      Map<String, String> requestHeaders = requestBuilder.getRequestHeaders();
      Map<String, String> requestParameters = requestBuilder.getRequestParameters();

      HttpResponse<String> accessTokenResponse = requestBuilder.sendAccessTokenRequest();

      String metadata = requestBuilder.getMetaDataString();

      return ScimAccessTokenRequest.builder()
                                   .id(UUID.randomUUID().toString())
                                   .requestHeadersList(requestHeaders.entrySet().stream().map(entry -> {
                                     return new RequestHeaders(entry.getKey(), entry.getValue());
                                   }).collect(Collectors.toList()))
                                   .requestParamsList(requestParameters.entrySet().stream().map(entry -> {
                                     return new RequestParams(entry.getKey(), entry.getValue());
                                   }).collect(Collectors.toList()))
                                   .statusCode(accessTokenResponse.getStatus())
                                   .responseHeadersList(accessTokenResponse.getHeaders().all().stream().map(header -> {
                                     return new ResponseHeaders(header.getName(), header.getValue());
                                   }).collect(Collectors.toList()))
                                   .plainResponse(accessTokenResponse.getBody())
                                   .metaDataJson(metadata)
                                   .meta(Meta.builder().created(Instant.now()).build())
                                   .build();
    }
    catch (Exception ex)
    {
      throw new BadRequestException(ex.getMessage(), ex);
    }
  }

  /**
   * endpoint disabled
   */
  @Override
  public ScimAccessTokenRequest getResource(String id,
                                            List<SchemaAttribute> attributes,
                                            List<SchemaAttribute> excludedAttributes,
                                            Context context)
  {
    return null;
  }

  /**
   * endpoint disabled
   */
  @Override
  public PartialListResponse<ScimAccessTokenRequest> listResources(long startIndex,
                                                                   int count,
                                                                   FilterNode filter,
                                                                   SchemaAttribute sortBy,
                                                                   SortOrder sortOrder,
                                                                   List<SchemaAttribute> attributes,
                                                                   List<SchemaAttribute> excludedAttributes,
                                                                   Context context)
  {
    return null;
  }

  /**
   * endpoint disabled
   */
  @Override
  public ScimAccessTokenRequest updateResource(ScimAccessTokenRequest resourceToUpdate, Context context)
  {
    return null;
  }

  /**
   * endpoint disabled
   */
  @Override
  public void deleteResource(String id, Context context)
  {
    // do nothing
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RequestValidator<ScimAccessTokenRequest> getRequestValidator()
  {
    return new AccessTokenRequestValidator();
  }
}
