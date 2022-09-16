package de.captaingoldfish.restclient.application.endpoints.httprequests;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import de.captaingoldfish.restclient.application.utils.Utils;
import de.captaingoldfish.restclient.database.entities.HttpRequestGroup;
import de.captaingoldfish.restclient.database.repositories.HttpRequestCategoriesDao;
import de.captaingoldfish.restclient.scim.resources.ScimHttpRequestGroup;
import de.captaingoldfish.scim.sdk.common.constants.enums.SortOrder;
import de.captaingoldfish.scim.sdk.common.exceptions.ResourceNotFoundException;
import de.captaingoldfish.scim.sdk.common.schemas.SchemaAttribute;
import de.captaingoldfish.scim.sdk.server.endpoints.Context;
import de.captaingoldfish.scim.sdk.server.endpoints.ResourceHandler;
import de.captaingoldfish.scim.sdk.server.endpoints.validation.RequestValidator;
import de.captaingoldfish.scim.sdk.server.endpoints.validation.ValidationContext;
import de.captaingoldfish.scim.sdk.server.filter.FilterNode;
import de.captaingoldfish.scim.sdk.server.response.PartialListResponse;
import lombok.RequiredArgsConstructor;


/**
 * a handler to manage http request categories
 */
@RequiredArgsConstructor
public class HttpRequestsGroupHandler extends ResourceHandler<ScimHttpRequestGroup>
{

  private final HttpRequestCategoriesDao httpRequestCategoriesDao;

  /**
   * {@inheritDoc}
   */
  @Override
  public ScimHttpRequestGroup createResource(ScimHttpRequestGroup resource, Context context)
  {
    HttpRequestGroup httpRequestGroup = HttpRequestGroupConverter.toHttpRequestCategory(resource);
    httpRequestGroup = httpRequestCategoriesDao.save(httpRequestGroup);
    return HttpRequestGroupConverter.toScimHttpRequestCategory(httpRequestGroup);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ScimHttpRequestGroup getResource(String id,
                                          List<SchemaAttribute> attributes,
                                          List<SchemaAttribute> excludedAttributes,
                                          Context context)
  {
    long dbId = Utils.parseId(id);
    HttpRequestGroup httpRequestGroup = httpRequestCategoriesDao.findById(dbId).orElse(null);
    if (httpRequestGroup == null)
    {
      throw new ResourceNotFoundException(String.format("HTTP request category with id '%s' does not exist", id));
    }
    return HttpRequestGroupConverter.toScimHttpRequestCategory(httpRequestGroup);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PartialListResponse<ScimHttpRequestGroup> listResources(long startIndex,
                                                                 int count,
                                                                 FilterNode filter,
                                                                 SchemaAttribute sortBy,
                                                                 SortOrder sortOrder,
                                                                 List<SchemaAttribute> attributes,
                                                                 List<SchemaAttribute> excludedAttributes,
                                                                 Context context)
  {
    List<HttpRequestGroup> httpRequestCategories = httpRequestCategoriesDao.findAll();
    List<ScimHttpRequestGroup> scimHttpRequestCategories = httpRequestCategories.stream().map(category -> {
      return HttpRequestGroupConverter.toScimHttpRequestCategory(category);
    }).collect(Collectors.toList());
    return PartialListResponse.<ScimHttpRequestGroup> builder()
                              .resources(new ArrayList<>())
                              .totalResults(httpRequestCategories.size())
                              .resources(scimHttpRequestCategories)
                              .build();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ScimHttpRequestGroup updateResource(ScimHttpRequestGroup resource, Context context)
  {
    long dbId = Utils.parseId(resource.getId().get());
    HttpRequestGroup httpRequestGroup = httpRequestCategoriesDao.findById(dbId).orElse(null);
    if (httpRequestGroup == null)
    {
      throw new ResourceNotFoundException(String.format("HTTP request category with id '%s' does not exist", dbId));
    }

    HttpRequestGroup updatedHttpRequestGroup = HttpRequestGroupConverter.toHttpRequestCategory(resource);
    updatedHttpRequestGroup.setCreated(httpRequestGroup.getCreated());
    updatedHttpRequestGroup.setLastModified(Instant.now());
    httpRequestGroup = httpRequestCategoriesDao.save(updatedHttpRequestGroup);

    return HttpRequestGroupConverter.toScimHttpRequestCategory(httpRequestGroup);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteResource(String id, Context context)
  {
    long dbId = Utils.parseId(id);
    HttpRequestGroup httpRequestGroup = httpRequestCategoriesDao.findById(dbId).orElse(null);
    if (httpRequestGroup == null)
    {
      throw new ResourceNotFoundException(String.format("HTTP request category with id '%s' does not exist", id));
    }
    httpRequestCategoriesDao.deleteById(dbId);
  }

  @Override
  public RequestValidator<ScimHttpRequestGroup> getRequestValidator()
  {
    return new RequestValidator<>()
    {

      @Override
      public void validateCreate(ScimHttpRequestGroup resource,
                                 ValidationContext validationContext,
                                 Context requestContext)
      {
        if (httpRequestCategoriesDao.findByName(resource.getName()).isPresent())
        {
          validationContext.addError(ScimHttpRequestGroup.FieldNames.NAME,
                                     String.format("Duplicate category name '%s'", resource.getName()));
        }
      }

      @Override
      public void validateUpdate(Supplier<ScimHttpRequestGroup> oldResourceSupplier,
                                 ScimHttpRequestGroup newResource,
                                 ValidationContext validationContext,
                                 Context requestContext)
      {
        HttpRequestGroup requestCategory = httpRequestCategoriesDao.findByName(newResource.getName()).orElse(null);
        if (requestCategory != null && requestCategory.getId() != newResource.getId().map(Utils::parseId).orElse(0L))
        {
          validationContext.addError(ScimHttpRequestGroup.FieldNames.NAME,
                                     String.format("Duplicate category name '%s'", newResource.getName()));
        }
      }
    };
  }
}
