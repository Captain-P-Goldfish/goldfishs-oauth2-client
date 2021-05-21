package de.captaingoldfish.restclient.application.endpoints.openidprovider;

import java.util.List;
import java.util.stream.Collectors;

import de.captaingoldfish.restclient.application.endpoints.openidprovider.validation.OpenIdProviderRequestValidator;
import de.captaingoldfish.restclient.application.utils.Utils;
import de.captaingoldfish.restclient.database.entities.OpenIdProvider;
import de.captaingoldfish.restclient.database.repositories.OpenIdProviderDao;
import de.captaingoldfish.restclient.scim.resources.ScimOpenIdProvider;
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
 * @since 21.05.2021
 */
@RequiredArgsConstructor
public class OpenIdProviderHandler extends ResourceHandler<ScimOpenIdProvider>
{

  /**
   * to execute CRUD operations on the {@link OpenIdProvider}
   */
  private final OpenIdProviderDao openIdProviderDao;


  /**
   * creates a new {@link OpenIdProvider}
   */
  @Override
  public ScimOpenIdProvider createResource(ScimOpenIdProvider scimOpenIdProvider, Authorization authorization)
  {
    OpenIdProvider openIdProvider = OpenIdProviderConverter.toOpenIdProvider(scimOpenIdProvider);
    openIdProvider = openIdProviderDao.save(openIdProvider);
    return OpenIdProviderConverter.toScimOpenIdProvider(openIdProvider);
  }

  /**
   * gets a {@link OpenIdProvider} by its id value
   */
  @Override
  public ScimOpenIdProvider getResource(String id,
                                        Authorization authorization,
                                        List<SchemaAttribute> attributes,
                                        List<SchemaAttribute> excludedAttributes)
  {
    Long dbId = Utils.parseId(id);
    OpenIdProvider openIdProvider = openIdProviderDao.findById(dbId).orElseThrow(() -> {
      return new ResourceNotFoundException(String.format("Resource with ID '%s' does not exist", id));
    });
    return OpenIdProviderConverter.toScimOpenIdProvider(openIdProvider);
  }

  /**
   * lists all {@link OpenIdProvider}s from the database
   */
  @Override
  public PartialListResponse<ScimOpenIdProvider> listResources(long startIndex,
                                                               int count,
                                                               FilterNode filter,
                                                               SchemaAttribute sortBy,
                                                               SortOrder sortOrder,
                                                               List<SchemaAttribute> attributes,
                                                               List<SchemaAttribute> excludedAttributes,
                                                               Authorization authorization)
  {
    List<OpenIdProvider> openIdProviderList = openIdProviderDao.findAll();
    List<ScimOpenIdProvider> scimOpenIdProviderList = openIdProviderList.stream()
                                                                        .map(OpenIdProviderConverter::toScimOpenIdProvider)
                                                                        .collect(Collectors.toList());
    return PartialListResponse.<ScimOpenIdProvider> builder()
                              .resources(scimOpenIdProviderList)
                              .totalResults(openIdProviderList.size())
                              .build();
  }

  /**
   * updates an existing {@link OpenIdProvider}
   */
  @Override
  public ScimOpenIdProvider updateResource(ScimOpenIdProvider resourceToUpdate, Authorization authorization)
  {
    Long dbId = Utils.parseId(resourceToUpdate.getId().get());
    OpenIdProvider oldProvider = openIdProviderDao.findById(dbId).orElseThrow(() -> {
      return new ResourceNotFoundException(String.format("Resource with ID '%s' does not exist", dbId));
    });
    OpenIdProvider openIdProvider = OpenIdProviderConverter.toOpenIdProvider(resourceToUpdate);
    openIdProvider.setId(dbId);
    openIdProvider.setCreated(oldProvider.getCreated());
    openIdProvider = openIdProviderDao.save(openIdProvider);
    return OpenIdProviderConverter.toScimOpenIdProvider(openIdProvider);
  }

  /**
   * deletes an existing {@link OpenIdProvider}
   */
  @Override
  public void deleteResource(String id, Authorization authorization)
  {
    Long dbId = Utils.parseId(id);
    openIdProviderDao.findById(dbId).orElseThrow(() -> {
      return new ResourceNotFoundException(String.format("Resource with ID '%s' does not exist", dbId));
    });
    openIdProviderDao.deleteById(dbId);
  }

  /**
   * @return a request validator that checks the request content for create and update requests
   */
  @Override
  public RequestValidator<ScimOpenIdProvider> getRequestValidator()
  {
    return new OpenIdProviderRequestValidator(openIdProviderDao);
  }
}
