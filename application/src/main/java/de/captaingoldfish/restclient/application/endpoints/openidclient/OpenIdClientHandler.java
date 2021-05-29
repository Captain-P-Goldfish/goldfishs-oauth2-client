package de.captaingoldfish.restclient.application.endpoints.openidclient;

import java.util.List;
import java.util.stream.Collectors;

import de.captaingoldfish.restclient.application.endpoints.openidclient.validation.OpenIdProviderRequestValidator;
import de.captaingoldfish.restclient.application.utils.Utils;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;
import de.captaingoldfish.restclient.database.repositories.OpenIdClientDao;
import de.captaingoldfish.restclient.scim.resources.ScimOpenIdClient;
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
  public ScimOpenIdClient createResource(ScimOpenIdClient resource, Authorization authorization)
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
                                      Authorization authorization,
                                      List<SchemaAttribute> attributes,
                                      List<SchemaAttribute> excludedAttributes)
  {
    Long openIdClientId = Utils.parseId(id);
    OpenIdClient openIdClient = openIdClientDao.findById(openIdClientId).orElseThrow(() -> {
      return new ResourceNotFoundException(String.format("OpenID Client with id '%s' does not exist", id));
    });
    return OpenIdClientConverter.toScimOpenIdClient(openIdClient);
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
                                                             Authorization authorization)
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
  public ScimOpenIdClient updateResource(ScimOpenIdClient resourceToUpdate, Authorization authorization)
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
  public void deleteResource(String id, Authorization authorization)
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
    return new OpenIdProviderRequestValidator();
  }
}
