package de.captaingoldfish.restclient.application.endpoints.proxy;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import de.captaingoldfish.restclient.application.utils.Utils;
import de.captaingoldfish.restclient.database.entities.Proxy;
import de.captaingoldfish.restclient.database.repositories.ProxyDao;
import de.captaingoldfish.restclient.scim.resources.ScimProxy;
import de.captaingoldfish.scim.sdk.common.constants.enums.SortOrder;
import de.captaingoldfish.scim.sdk.common.exceptions.ResourceNotFoundException;
import de.captaingoldfish.scim.sdk.common.schemas.SchemaAttribute;
import de.captaingoldfish.scim.sdk.server.endpoints.Context;
import de.captaingoldfish.scim.sdk.server.endpoints.ResourceHandler;
import de.captaingoldfish.scim.sdk.server.filter.FilterNode;
import de.captaingoldfish.scim.sdk.server.response.PartialListResponse;
import lombok.RequiredArgsConstructor;


/**
 * @author Pascal Knueppel
 * @since 19.05.2021
 */
@RequiredArgsConstructor
public class ProxyHandler extends ResourceHandler<ScimProxy>
{

  /**
   * for CRUD operations on the proxy settings within the database
   */
  private final ProxyDao proxyDao;

  /**
   * creates a new proxy
   */
  @Override
  public ScimProxy createResource(ScimProxy scimProxy, Context context)
  {
    Proxy proxy = ProxyConverter.toProxy(scimProxy);
    proxy = proxyDao.save(proxy);
    return ProxyConverter.toScimProxy(proxy);
  }

  /**
   * gets an existing proxy resource
   */
  @Override
  public ScimProxy getResource(String id,
                               List<SchemaAttribute> attributes,
                               List<SchemaAttribute> excludedAttributes,
                               Context context)
  {
    long proxyId = Utils.parseId(id);
    Proxy proxy = proxyDao.findById(proxyId).orElseThrow(() -> {
      return new ResourceNotFoundException(String.format("Resource with ID '%s' does not exist", id));
    });
    return ProxyConverter.toScimProxy(proxy);
  }

  /**
   * lists all proxy resources to the maximum number that can be returned
   */
  @Override
  public PartialListResponse<ScimProxy> listResources(long startIndex,
                                                      int count,
                                                      FilterNode filter,
                                                      SchemaAttribute sortBy,
                                                      SortOrder sortOrder,
                                                      List<SchemaAttribute> attributes,
                                                      List<SchemaAttribute> excludedAttributes,
                                                      Context context)
  {
    List<Proxy> proxyList = proxyDao.findAll();
    List<ScimProxy> scimProxyList = proxyList.stream().map(ProxyConverter::toScimProxy).collect(Collectors.toList());
    return PartialListResponse.<ScimProxy> builder()
                              .totalResults(scimProxyList.size())
                              .resources(scimProxyList)
                              .build();
  }

  /**
   * updates an existing proxy
   */
  @Override
  public ScimProxy updateResource(ScimProxy scimProxy, Context context)
  {
    final long id = Utils.parseId(scimProxy.getId().get());
    Proxy oldProxy = proxyDao.findById(id).orElseThrow(() -> {
      return new ResourceNotFoundException(String.format("Resource with ID '%s' does not exist", id));
    });
    Proxy proxy = ProxyConverter.toProxy(scimProxy);
    proxy.setCreated(oldProxy.getCreated());
    proxy.setId(id);
    proxy.setLastModified(Instant.now());
    proxy = proxyDao.save(proxy);
    return ProxyConverter.toScimProxy(proxy);
  }

  /**
   * deletes an existing proxy
   */
  @Override
  public void deleteResource(String id, Context context)
  {
    long proxyId = Utils.parseId(id);
    proxyDao.findById(proxyId).orElseThrow(() -> {
      return new ResourceNotFoundException(String.format("Resource with ID '%s' does not exist", id));
    });
    proxyDao.deleteById(proxyId);
  }
}
