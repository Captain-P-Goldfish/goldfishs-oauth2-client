package de.captaingoldfish.restclient.application.endpoints.tokencategory;

import java.util.List;
import java.util.stream.Collectors;

import de.captaingoldfish.restclient.application.endpoints.tokencategory.validation.TokenCategoryValidator;
import de.captaingoldfish.restclient.application.utils.Utils;
import de.captaingoldfish.restclient.database.entities.TokenCategory;
import de.captaingoldfish.restclient.database.repositories.TokenCategoryDao;
import de.captaingoldfish.restclient.scim.resources.ScimTokenCategory;
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
 * the handler for managing token categories under which
 * {@link de.captaingoldfish.restclient.database.entities.TokenStore} objects will be saved
 * 
 * @author Pascal Knueppel
 * @since 02.09.2021
 */
@RequiredArgsConstructor
public class TokenCategoryHandler extends ResourceHandler<ScimTokenCategory>
{

  private final TokenCategoryDao tokenCategoryDao;

  @Override
  public ScimTokenCategory createResource(ScimTokenCategory resource, Context context)
  {
    TokenCategory tokenCategory = TokenCategoryConverter.toTokenCategory(resource);
    tokenCategory = tokenCategoryDao.save(tokenCategory);
    return TokenCategoryConverter.toScimTokenCategory(tokenCategory);
  }

  @Override
  public ScimTokenCategory getResource(String id,
                                       List<SchemaAttribute> attributes,
                                       List<SchemaAttribute> excludedAttributes,
                                       Context context)
  {
    Long dbId = Utils.parseId(id);
    return tokenCategoryDao.findById(dbId).map(TokenCategoryConverter::toScimTokenCategory).orElse(null);
  }

  @Override
  public PartialListResponse<ScimTokenCategory> listResources(long startIndex,
                                                              int count,
                                                              FilterNode filter,
                                                              SchemaAttribute sortBy,
                                                              SortOrder sortOrder,
                                                              List<SchemaAttribute> attributes,
                                                              List<SchemaAttribute> excludedAttributes,
                                                              Context context)
  {
    List<ScimTokenCategory> tokenCategoryList = tokenCategoryDao.findAll()
                                                                .stream()
                                                                .map(TokenCategoryConverter::toScimTokenCategory)
                                                                .collect(Collectors.toList());
    return PartialListResponse.<ScimTokenCategory> builder()
                              .totalResults(tokenCategoryList.size())
                              .resources(tokenCategoryList)
                              .build();
  }

  @Override
  public ScimTokenCategory updateResource(ScimTokenCategory resourceToUpdate, Context context)
  {
    Long dbId = Utils.parseId(resourceToUpdate.getId().orElseThrow());
    TokenCategory oldTokenCategory = tokenCategoryDao.findById(dbId).orElseThrow(() -> {
      return new ResourceNotFoundException(String.format("Resource with ID '%s' does not exist", dbId));
    });
    TokenCategory newTokenCategory = TokenCategoryConverter.toTokenCategory(resourceToUpdate);
    newTokenCategory.setCreated(oldTokenCategory.getCreated());
    tokenCategoryDao.save(newTokenCategory);
    return TokenCategoryConverter.toScimTokenCategory(newTokenCategory);
  }

  @Override
  public void deleteResource(String id, Context context)
  {
    Long dbId = Utils.parseId(id);
    tokenCategoryDao.findById(dbId).orElseThrow(() -> {
      return new ResourceNotFoundException(String.format("Resource with ID '%s' does not exist", dbId));
    });
    tokenCategoryDao.deleteById(dbId);
  }

  @Override
  public RequestValidator<ScimTokenCategory> getRequestValidator()
  {
    return new TokenCategoryValidator();
  }
}
