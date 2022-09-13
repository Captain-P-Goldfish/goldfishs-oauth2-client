package de.captaingoldfish.restclient.application.endpoints.httprequests;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.captaingoldfish.restclient.application.utils.Utils;
import de.captaingoldfish.restclient.database.entities.HttpRequest;
import de.captaingoldfish.restclient.database.repositories.HttpRequestCategoriesDao;
import de.captaingoldfish.restclient.database.repositories.HttpRequestsDao;
import de.captaingoldfish.restclient.scim.resources.ScimHttpRequest;
import de.captaingoldfish.scim.sdk.common.constants.enums.SortOrder;
import de.captaingoldfish.scim.sdk.common.exceptions.ResourceNotFoundException;
import de.captaingoldfish.scim.sdk.common.schemas.SchemaAttribute;
import de.captaingoldfish.scim.sdk.server.endpoints.Context;
import de.captaingoldfish.scim.sdk.server.endpoints.ResourceHandler;
import de.captaingoldfish.scim.sdk.server.endpoints.validation.RequestValidator;
import de.captaingoldfish.scim.sdk.server.filter.FilterNode;
import de.captaingoldfish.scim.sdk.server.response.PartialListResponse;
import lombok.RequiredArgsConstructor;


/**
 *
 */
@RequiredArgsConstructor
public class HttpRequestHandler extends ResourceHandler<ScimHttpRequest>
{

  private final HttpRequestCategoriesDao httpRequestCategoriesDao;

  private final HttpRequestsDao httpRequestsDao;

  /**
   * {@inheritDoc}
   */
  @Override
  public ScimHttpRequest createResource(ScimHttpRequest resource, Context context)
  {
    HttpRequest httpRequest = HttpRequestsConverter.toHttpRequest(resource, httpRequestsDao, httpRequestCategoriesDao);
    httpRequest = httpRequestsDao.save(httpRequest);
    return HttpRequestsConverter.toScimHttpRequest(httpRequest);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ScimHttpRequest getResource(String id,
                                     List<SchemaAttribute> attributes,
                                     List<SchemaAttribute> excludedAttributes,
                                     Context context)
  {
    long dbId = Utils.parseId(id);
    HttpRequest httpRequest = httpRequestsDao.findById(dbId).orElse(null);
    if (httpRequest == null)
    {
      throw new ResourceNotFoundException(String.format("HTTP request with id '%s' does not exist", id));
    }
    return HttpRequestsConverter.toScimHttpRequest(httpRequest);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PartialListResponse<ScimHttpRequest> listResources(long startIndex,
                                                            int count,
                                                            FilterNode filter,
                                                            SchemaAttribute sortBy,
                                                            SortOrder sortOrder,
                                                            List<SchemaAttribute> attributes,
                                                            List<SchemaAttribute> excludedAttributes,
                                                            Context context)
  {
    List<HttpRequest> httpRequest = httpRequestsDao.findAll();
    List<ScimHttpRequest> scimHttpRequest = httpRequest.stream().map(category -> {
      return HttpRequestsConverter.toScimHttpRequest(category);
    }).collect(Collectors.toList());
    return PartialListResponse.<ScimHttpRequest> builder()
                              .resources(new ArrayList<>())
                              .totalResults(httpRequest.size())
                              .resources(scimHttpRequest)
                              .build();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ScimHttpRequest updateResource(ScimHttpRequest resource, Context context)
  {
    long dbId = Utils.parseId(resource.getId().get());
    HttpRequest httpRequest = httpRequestsDao.findById(dbId).orElse(null);
    if (httpRequest == null)
    {
      throw new ResourceNotFoundException(String.format("HTTP request with id '%s' does not exist", dbId));
    }

    HttpRequest updatedHttpRequest = HttpRequestsConverter.toHttpRequest(resource,
                                                                         httpRequestsDao,
                                                                         httpRequestCategoriesDao);
    updatedHttpRequest.setCreated(httpRequest.getCreated());
    updatedHttpRequest.setLastModified(Instant.now());
    httpRequest = httpRequestsDao.save(httpRequest);

    return HttpRequestsConverter.toScimHttpRequest(httpRequest);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteResource(String id, Context context)
  {
    long dbId = Utils.parseId(id);
    if (httpRequestsDao.findById(dbId).isEmpty())
    {
      throw new ResourceNotFoundException(String.format("HTTP request with id '%s' does not exist", id));
    }
    httpRequestsDao.deleteById(dbId);
  }

  @Override
  public RequestValidator<ScimHttpRequest> getRequestValidator()
  {
    return new HttpRequestValidator(httpRequestCategoriesDao, httpRequestsDao);
  }
}
