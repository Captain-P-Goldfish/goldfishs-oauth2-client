package de.captaingoldfish.restclient.application.endpoints.tokenstore;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import de.captaingoldfish.restclient.application.endpoints.tokenstore.filtering.TokenStoreFilterResolver;
import de.captaingoldfish.restclient.application.endpoints.tokenstore.validation.TokenStoreValidator;
import de.captaingoldfish.restclient.application.utils.Utils;
import de.captaingoldfish.restclient.database.entities.TokenStore;
import de.captaingoldfish.restclient.database.repositories.TokenStoreDao;
import de.captaingoldfish.restclient.scim.resources.ScimTokenStore;
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
 * This handler provides functionality to create and access arbitrary tokens that should be stored for later
 * retrieval
 *
 * @author Pascal Knueppel
 * @since 28.08.2021
 */
@RequiredArgsConstructor
public class TokenStoreHandler extends ResourceHandler<ScimTokenStore>
{

  /**
   * to perform CRUD operations on the tokenstore table
   */
  private final TokenStoreDao tokenStoreDao;

  /**
   * {@inheritDoc}
   */
  @Override
  public ScimTokenStore createResource(ScimTokenStore resource, Context context)
  {
    TokenStore tokenStore = TokenStoreConverter.toTokenStore(resource);
    tokenStore = tokenStoreDao.save(tokenStore);
    return TokenStoreConverter.toScimTokenStore(tokenStore);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ScimTokenStore getResource(String id,
                                    List<SchemaAttribute> attributes,
                                    List<SchemaAttribute> excludedAttributes,
                                    Context context)
  {
    Long dbId = Utils.parseId(id);
    return tokenStoreDao.findById(dbId).map(TokenStoreConverter::toScimTokenStore).orElse(null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PartialListResponse<ScimTokenStore> listResources(long startIndex,
                                                           int count,
                                                           FilterNode filter,
                                                           SchemaAttribute sortBy,
                                                           SortOrder sortOrder,
                                                           List<SchemaAttribute> attributes,
                                                           List<SchemaAttribute> excludedAttributes,
                                                           Context context)
  {
    List<ScimTokenStore> tokenStoreList = new TokenStoreFilterResolver().resolveFilter(filter)
                                                                        .stream()
                                                                        .map(TokenStoreConverter::toScimTokenStore)
                                                                        .collect(Collectors.toList());
    return PartialListResponse.<ScimTokenStore> builder()
                              .totalResults(tokenStoreList.size())
                              .resources(tokenStoreList)
                              .build();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ScimTokenStore updateResource(ScimTokenStore resourceToUpdate, Context context)
  {
    Long dbId = Utils.parseId(resourceToUpdate.getId().orElseThrow());
    TokenStore oldTokenStore = tokenStoreDao.findById(dbId).orElseThrow(() -> {
      return new ResourceNotFoundException(String.format("Resource with ID '%s' does not exist", dbId));
    });
    TokenStore newTokenStore = TokenStoreConverter.toTokenStore(resourceToUpdate);
    newTokenStore.setCreated(oldTokenStore.getCreated());
    newTokenStore.setLastModified(Instant.now());
    tokenStoreDao.save(newTokenStore);
    return TokenStoreConverter.toScimTokenStore(newTokenStore);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteResource(String id, Context context)
  {
    Long dbId = Utils.parseId(id);
    Optional<TokenStore> tokenStore = tokenStoreDao.findById(dbId);
    if (tokenStore.isEmpty())
    {
      throw new ResourceNotFoundException(String.format("Resource with id '%s' does not exist", id));
    }
    tokenStoreDao.delete(tokenStore.get());
  }

  @Override
  public RequestValidator<ScimTokenStore> getRequestValidator()
  {
    return new TokenStoreValidator();
  }
}
