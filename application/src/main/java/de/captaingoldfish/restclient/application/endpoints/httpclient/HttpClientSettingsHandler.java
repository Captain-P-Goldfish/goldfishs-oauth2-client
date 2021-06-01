package de.captaingoldfish.restclient.application.endpoints.httpclient;

import java.util.List;

import de.captaingoldfish.restclient.application.endpoints.httpclient.validation.HttpClientSettingsValidator;
import de.captaingoldfish.restclient.application.utils.Utils;
import de.captaingoldfish.restclient.database.entities.HttpClientSettings;
import de.captaingoldfish.restclient.database.repositories.HttpClientSettingsDao;
import de.captaingoldfish.restclient.scim.resources.ScimHttpClientSettings;
import de.captaingoldfish.scim.sdk.common.constants.enums.SortOrder;
import de.captaingoldfish.scim.sdk.common.exceptions.ResourceNotFoundException;
import de.captaingoldfish.scim.sdk.common.schemas.SchemaAttribute;
import de.captaingoldfish.scim.sdk.server.endpoints.ResourceHandler;
import de.captaingoldfish.scim.sdk.server.endpoints.authorize.Authorization;
import de.captaingoldfish.scim.sdk.server.endpoints.validation.RequestValidator;
import de.captaingoldfish.scim.sdk.server.filter.FilterNode;
import de.captaingoldfish.scim.sdk.server.response.PartialListResponse;
import lombok.RequiredArgsConstructor;


/**
 * @author Pascal Knueppel
 * @since 31.05.2021
 */
@RequiredArgsConstructor
public class HttpClientSettingsHandler extends ResourceHandler<ScimHttpClientSettings>
{

  /**
   * to perform CRUD operations on the database
   */
  private final HttpClientSettingsDao httpClientSettingsDao;

  /**
   * creates a new resource instance
   */
  @Override
  public ScimHttpClientSettings createResource(ScimHttpClientSettings resource, Authorization authorization)
  {
    HttpClientSettings httpClientSettings = HttpClientSettingsConverter.toHttpClientSettings(resource);
    httpClientSettings = httpClientSettingsDao.save(httpClientSettings);
    return HttpClientSettingsConverter.toScimHttpClientSettings(httpClientSettings);
  }

  /**
   * @return an existing resource
   */
  @Override
  public ScimHttpClientSettings getResource(String id,
                                            Authorization authorization,
                                            List<SchemaAttribute> attributes,
                                            List<SchemaAttribute> excludedAttributes)
  {
    Long dbId = Utils.parseId(id);
    HttpClientSettings httpClientSettings = httpClientSettingsDao.findById(dbId).orElseThrow(() -> {
      return new ResourceNotFoundException(String.format("Resource with ID '%s' does not exist", id));
    });
    return HttpClientSettingsConverter.toScimHttpClientSettings(httpClientSettings);
  }

  /**
   * endpoint is disabled
   */
  @Override
  public PartialListResponse<ScimHttpClientSettings> listResources(long startIndex,
                                                                   int count,
                                                                   FilterNode filter,
                                                                   SchemaAttribute sortBy,
                                                                   SortOrder sortOrder,
                                                                   List<SchemaAttribute> attributes,
                                                                   List<SchemaAttribute> excludedAttributes,
                                                                   Authorization authorization)
  {
    // not supported. Disabled endpoint
    return null;
  }

  /**
   * updates an existing resource
   */
  @Override
  public ScimHttpClientSettings updateResource(ScimHttpClientSettings resourceToUpdate, Authorization authorization)
  {
    Long dbId = Utils.parseId(resourceToUpdate.getId().orElseThrow());
    HttpClientSettings oldHttpClientSettings = httpClientSettingsDao.findById(dbId).orElseThrow(() -> {
      return new ResourceNotFoundException(String.format("Resource with ID '%s' does not exist", dbId));
    });
    HttpClientSettings newHttpClientSettings = HttpClientSettingsConverter.toHttpClientSettings(resourceToUpdate);
    newHttpClientSettings.setCreated(oldHttpClientSettings.getCreated());
    newHttpClientSettings = httpClientSettingsDao.save(newHttpClientSettings);
    return HttpClientSettingsConverter.toScimHttpClientSettings(newHttpClientSettings);
  }

  /**
   * deletes an existing resource
   */
  @Override
  public void deleteResource(String id, Authorization authorization)
  {
    Long dbId = Utils.parseId(id);
    HttpClientSettings httpClientSettings = httpClientSettingsDao.findById(dbId).orElseThrow(() -> {
      return new ResourceNotFoundException(String.format("Resource with ID '%s' does not exist", dbId));
    });
    httpClientSettingsDao.deleteById(httpClientSettings.getId());
  }

  /**
   * @return a request validator for http client settings
   */
  @Override
  public RequestValidator<ScimHttpClientSettings> getRequestValidator()
  {
    return new HttpClientSettingsValidator();
  }
}
