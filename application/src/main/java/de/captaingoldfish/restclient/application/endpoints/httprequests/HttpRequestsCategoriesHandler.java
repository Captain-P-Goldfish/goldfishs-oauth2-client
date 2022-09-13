package de.captaingoldfish.restclient.application.endpoints.httprequests;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import de.captaingoldfish.restclient.application.utils.Utils;
import de.captaingoldfish.restclient.database.entities.HttpRequestCategory;
import de.captaingoldfish.restclient.database.repositories.HttpRequestCategoriesDao;
import de.captaingoldfish.restclient.scim.resources.ScimHttpRequestCategory;
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
public class HttpRequestsCategoriesHandler extends ResourceHandler<ScimHttpRequestCategory>
{

  private final HttpRequestCategoriesDao httpRequestCategoriesDao;

  /**
   * {@inheritDoc}
   */
  @Override
  public ScimHttpRequestCategory createResource(ScimHttpRequestCategory resource, Context context)
  {
    HttpRequestCategory httpRequestCategory = HttpRequestCategoryConverter.toHttpRequestCategory(resource);
    httpRequestCategory = httpRequestCategoriesDao.save(httpRequestCategory);
    return HttpRequestCategoryConverter.toScimHttpRequestCategory(httpRequestCategory);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ScimHttpRequestCategory getResource(String id,
                                             List<SchemaAttribute> attributes,
                                             List<SchemaAttribute> excludedAttributes,
                                             Context context)
  {
    long dbId = Utils.parseId(id);
    HttpRequestCategory httpRequestCategory = httpRequestCategoriesDao.findById(dbId).orElse(null);
    if (httpRequestCategory == null)
    {
      throw new ResourceNotFoundException(String.format("HTTP request category with id '%s' does not exist", id));
    }
    return HttpRequestCategoryConverter.toScimHttpRequestCategory(httpRequestCategory);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PartialListResponse<ScimHttpRequestCategory> listResources(long startIndex,
                                                                    int count,
                                                                    FilterNode filter,
                                                                    SchemaAttribute sortBy,
                                                                    SortOrder sortOrder,
                                                                    List<SchemaAttribute> attributes,
                                                                    List<SchemaAttribute> excludedAttributes,
                                                                    Context context)
  {
    List<HttpRequestCategory> httpRequestCategories = httpRequestCategoriesDao.findAll();
    List<ScimHttpRequestCategory> scimHttpRequestCategories = httpRequestCategories.stream().map(category -> {
      return HttpRequestCategoryConverter.toScimHttpRequestCategory(category);
    }).collect(Collectors.toList());
    return PartialListResponse.<ScimHttpRequestCategory> builder()
                              .resources(new ArrayList<>())
                              .totalResults(httpRequestCategories.size())
                              .resources(scimHttpRequestCategories)
                              .build();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ScimHttpRequestCategory updateResource(ScimHttpRequestCategory resource, Context context)
  {
    long dbId = Utils.parseId(resource.getId().get());
    HttpRequestCategory httpRequestCategory = httpRequestCategoriesDao.findById(dbId).orElse(null);
    if (httpRequestCategory == null)
    {
      throw new ResourceNotFoundException(String.format("HTTP request category with id '%s' does not exist", dbId));
    }

    HttpRequestCategory updatedHttpRequestCategory = HttpRequestCategoryConverter.toHttpRequestCategory(resource);
    updatedHttpRequestCategory.setCreated(httpRequestCategory.getCreated());
    updatedHttpRequestCategory.setLastModified(Instant.now());
    httpRequestCategory = httpRequestCategoriesDao.save(updatedHttpRequestCategory);

    return HttpRequestCategoryConverter.toScimHttpRequestCategory(httpRequestCategory);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteResource(String id, Context context)
  {
    long dbId = Utils.parseId(id);
    HttpRequestCategory httpRequestCategory = httpRequestCategoriesDao.findById(dbId).orElse(null);
    if (httpRequestCategory == null)
    {
      throw new ResourceNotFoundException(String.format("HTTP request category with id '%s' does not exist", id));
    }
    httpRequestCategoriesDao.deleteById(dbId);
  }

  @Override
  public RequestValidator<ScimHttpRequestCategory> getRequestValidator()
  {
    return new RequestValidator<>()
    {

      @Override
      public void validateCreate(ScimHttpRequestCategory resource,
                                 ValidationContext validationContext,
                                 Context requestContext)
      {
        if (httpRequestCategoriesDao.findByName(resource.getName()).isPresent())
        {
          validationContext.addError(ScimHttpRequestCategory.FieldNames.NAME,
                                     String.format("Duplicate category name '%s'", resource.getName()));
        }
      }

      @Override
      public void validateUpdate(Supplier<ScimHttpRequestCategory> oldResourceSupplier,
                                 ScimHttpRequestCategory newResource,
                                 ValidationContext validationContext,
                                 Context requestContext)
      {
        HttpRequestCategory requestCategory = httpRequestCategoriesDao.findByName(newResource.getName()).orElse(null);
        if (requestCategory != null && requestCategory.getId() != newResource.getId().map(Utils::parseId).orElse(0L))
        {
          validationContext.addError(ScimHttpRequestCategory.FieldNames.NAME,
                                     String.format("Duplicate category name '%s'", newResource.getName()));
        }
      }
    };
  }
}
