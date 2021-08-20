package de.captaingoldfish.restclient.application.endpoints.openidclient;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import de.captaingoldfish.restclient.application.endpoints.httpclient.HttpClientSettingsConverter;
import de.captaingoldfish.restclient.application.endpoints.openidclient.validation.OpenIdClientRequestValidator;
import de.captaingoldfish.restclient.application.endpoints.workflowsettings.CurrentWorkflowSettingsConverter;
import de.captaingoldfish.restclient.application.projectconfig.WebAppConfig;
import de.captaingoldfish.restclient.application.utils.Utils;
import de.captaingoldfish.restclient.database.entities.CurrentWorkflowSettings;
import de.captaingoldfish.restclient.database.entities.HttpClientSettings;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;
import de.captaingoldfish.restclient.database.repositories.CurrentWorkflowSettingsDao;
import de.captaingoldfish.restclient.database.repositories.HttpClientSettingsDao;
import de.captaingoldfish.restclient.database.repositories.OpenIdClientDao;
import de.captaingoldfish.restclient.scim.resources.ScimCurrentWorkflowSettings;
import de.captaingoldfish.restclient.scim.resources.ScimHttpClientSettings;
import de.captaingoldfish.restclient.scim.resources.ScimOpenIdClient;
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
 * @author Pascal Knueppel
 * @since 28.05.2021
 */
@RequiredArgsConstructor
public class OpenIdClientHandler extends ResourceHandler<ScimOpenIdClient>
{

  /**
   * to peform CRUD operations on OpenID Clients
   */
  private final OpenIdClientDao openIdClientDao;

  /**
   * simply creates the new resource
   */
  @Override
  public ScimOpenIdClient createResource(ScimOpenIdClient resource, Context context)
  {
    OpenIdClient openIdClient = OpenIdClientConverter.toOpenIdClient(resource);
    openIdClient = openIdClientDao.save(openIdClient);
    return OpenIdClientConverter.toScimOpenIdClient(openIdClient);
  }

  /**
   * @return tries to return an existing OpenID Client
   */
  @Override
  public ScimOpenIdClient getResource(String id,
                                      List<SchemaAttribute> attributes,
                                      List<SchemaAttribute> excludedAttributes,
                                      Context context)
  {
    Long openIdClientId = Utils.parseId(id);
    OpenIdClient openIdClient = openIdClientDao.findById(openIdClientId).orElseThrow(() -> {
      return new ResourceNotFoundException(String.format("OpenID Client with id '%s' does not exist", id));
    });
    ScimOpenIdClient scimOpenIdClient = OpenIdClientConverter.toScimOpenIdClient(openIdClient);
    ScimHttpClientSettings httpClientSettings = getHttpClientSettings(openIdClient);
    scimOpenIdClient.setHttpClientSettings(httpClientSettings);
    ScimCurrentWorkflowSettings currentWorkflowSettings = getCurrentWorkflowSettings(openIdClient);
    scimOpenIdClient.setCurrentWorkflowSettings(currentWorkflowSettings);
    return scimOpenIdClient;
  }

  /**
   * gets the current http client settings for this client or creates a default configuration
   */
  private ScimHttpClientSettings getHttpClientSettings(OpenIdClient openIdClient)
  {
    HttpClientSettingsDao httpClientSettingsDao = WebAppConfig.getApplicationContext()
                                                              .getBean(HttpClientSettingsDao.class);
    Supplier<HttpClientSettings> defaultSettingsSupplier = () -> {
      HttpClientSettings defaultSettings = HttpClientSettings.builder()
                                                             .openIdClient(openIdClient)
                                                             .useHostnameVerifier(true)
                                                             .build();
      defaultSettings = httpClientSettingsDao.save(defaultSettings);
      return defaultSettings;
    };
    HttpClientSettings httpClientSettings = httpClientSettingsDao.findByOpenIdClient(openIdClient)
                                                                 .orElseGet(defaultSettingsSupplier);
    return HttpClientSettingsConverter.toScimHttpClientSettings(httpClientSettings);
  }

  /**
   * gets the current openid workflow settings for this client or creates a default configuration
   */
  private ScimCurrentWorkflowSettings getCurrentWorkflowSettings(OpenIdClient openIdClient)
  {
    CurrentWorkflowSettingsDao currentWorkflowSettingsDao = WebAppConfig.getApplicationContext()
                                                                        .getBean(CurrentWorkflowSettingsDao.class);
    Supplier<CurrentWorkflowSettings> defaultCurrentSettingsSupplier = () -> {
      CurrentWorkflowSettings defaultSettings = CurrentWorkflowSettings.builder().openIdClient(openIdClient).build();
      defaultSettings = currentWorkflowSettingsDao.save(defaultSettings);
      return defaultSettings;
    };
    CurrentWorkflowSettings settings = currentWorkflowSettingsDao.findByOpenIdClient(openIdClient)
                                                                 .orElseGet(defaultCurrentSettingsSupplier);
    return CurrentWorkflowSettingsConverter.toScimWorkflowSettings(settings);
  }

  /**
   * @return simply returns all OpenID Clients
   */
  @Override
  public PartialListResponse<ScimOpenIdClient> listResources(long startIndex,
                                                             int count,
                                                             FilterNode filter,
                                                             SchemaAttribute sortBy,
                                                             SortOrder sortOrder,
                                                             List<SchemaAttribute> attributes,
                                                             List<SchemaAttribute> excludedAttributes,
                                                             Context context)
  {
    List<OpenIdClient> openIdClientList = openIdClientDao.findAll();
    List<ScimOpenIdClient> scimOpenIdClientList = openIdClientList.stream()
                                                                  .map(OpenIdClientConverter::toScimOpenIdClient)
                                                                  .collect(Collectors.toList());
    return PartialListResponse.<ScimOpenIdClient> builder()
                              .totalResults(scimOpenIdClientList.size())
                              .resources(scimOpenIdClientList)
                              .build();
  }

  /**
   * @return updates an existing OpenID Client with the new data
   */
  @Override
  public ScimOpenIdClient updateResource(ScimOpenIdClient resourceToUpdate, Context context)
  {
    Long openIdClientId = Utils.parseId(resourceToUpdate.getId().orElseThrow());
    OpenIdClient openIdClient = openIdClientDao.findById(openIdClientId).orElseThrow();
    OpenIdClient newOpenIdClient = OpenIdClientConverter.toOpenIdClient(resourceToUpdate);
    newOpenIdClient.setId(openIdClientId);
    newOpenIdClient.setCreated(openIdClient.getCreated());
    newOpenIdClient = openIdClientDao.save(newOpenIdClient);
    return OpenIdClientConverter.toScimOpenIdClient(newOpenIdClient);
  }

  /**
   * deletes an existing OpenID Client
   */
  @Override
  public void deleteResource(String id, Context context)
  {
    Long openIdClientId = Utils.parseId(id);
    OpenIdClient openIdClient = openIdClientDao.findById(openIdClientId).orElseThrow(() -> {
      return new ResourceNotFoundException(String.format("OpenID Client with id '%s' does not exist", openIdClientId));
    });
    openIdClientDao.delete(openIdClient);
  }

  /**
   * @return the request validator to check that the resources received in the handler are valid
   */
  @Override
  public RequestValidator<ScimOpenIdClient> getRequestValidator()
  {
    return new OpenIdClientRequestValidator();
  }
}
