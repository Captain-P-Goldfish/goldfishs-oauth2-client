package de.captaingoldfish.restclient.application.endpoints.responsehistory;

import java.util.List;

import de.captaingoldfish.restclient.application.utils.Utils;
import de.captaingoldfish.restclient.database.entities.HttpRequest;
import de.captaingoldfish.restclient.database.entities.HttpResponse;
import de.captaingoldfish.restclient.database.repositories.HttpRequestsDao;
import de.captaingoldfish.restclient.database.repositories.HttpResponseDao;
import de.captaingoldfish.restclient.scim.resources.ScimHttpResponseHistory;
import de.captaingoldfish.scim.sdk.common.constants.enums.SortOrder;
import de.captaingoldfish.scim.sdk.common.exceptions.NotImplementedException;
import de.captaingoldfish.scim.sdk.common.exceptions.ResourceNotFoundException;
import de.captaingoldfish.scim.sdk.common.schemas.SchemaAttribute;
import de.captaingoldfish.scim.sdk.server.endpoints.Context;
import de.captaingoldfish.scim.sdk.server.endpoints.ResourceHandler;
import de.captaingoldfish.scim.sdk.server.filter.FilterNode;
import de.captaingoldfish.scim.sdk.server.response.PartialListResponse;
import lombok.RequiredArgsConstructor;


/**
 *
 */
@RequiredArgsConstructor
public class HttpResponseHistoryResourceHandler extends ResourceHandler<ScimHttpResponseHistory>
{

  private final HttpRequestsDao httpRequestsDao;

  private final HttpResponseDao httpResponseDao;

  /**
   * {@inheritDoc}
   */
  @Override
  public ScimHttpResponseHistory createResource(ScimHttpResponseHistory resource, Context context)
  {
    throw new NotImplementedException("Disabled endpoint");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ScimHttpResponseHistory getResource(String id,
                                             List<SchemaAttribute> attributes,
                                             List<SchemaAttribute> excludedAttributes,
                                             Context context)
  {
    long databaseId = Utils.parseId(id);
    HttpRequest httpRequest = httpRequestsDao.findById(databaseId).orElse(null);
    if (httpRequest == null)
    {
      throw new ResourceNotFoundException(String.format("History could not be accessed because HTTP Request with id "
                                                        + "'%s' does not exist",
                                                        id));
    }
    return HttpResponseConverter.toScimResponseHistory(httpRequest);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PartialListResponse<ScimHttpResponseHistory> listResources(long startIndex,
                                                                    int count,
                                                                    FilterNode filter,
                                                                    SchemaAttribute sortBy,
                                                                    SortOrder sortOrder,
                                                                    List<SchemaAttribute> attributes,
                                                                    List<SchemaAttribute> excludedAttributes,
                                                                    Context context)
  {
    throw new NotImplementedException("Disabled endpoint");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ScimHttpResponseHistory updateResource(ScimHttpResponseHistory resource, Context context)
  {
    throw new NotImplementedException("Disabled endpoint");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteResource(String id, Context context)
  {
    HttpResponse response = httpResponseDao.findById(id).orElse(null);
    if (response == null)
    {
      throw new ResourceNotFoundException(String.format("HTTP Response with id '%s' does not exist", id));
    }

    httpResponseDao.deleteById(id);
  }
}
