package de.captaingoldfish.restclient.application.endpoints.jwt;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import de.captaingoldfish.restclient.application.crypto.JwtHandler;
import de.captaingoldfish.restclient.application.endpoints.jwt.validation.ScimJwtBuilderValidator;
import de.captaingoldfish.restclient.scim.resources.ScimJwtBuilder;
import de.captaingoldfish.scim.sdk.common.constants.enums.SortOrder;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import de.captaingoldfish.scim.sdk.common.schemas.SchemaAttribute;
import de.captaingoldfish.scim.sdk.server.endpoints.Context;
import de.captaingoldfish.scim.sdk.server.endpoints.ResourceHandler;
import de.captaingoldfish.scim.sdk.server.endpoints.validation.RequestValidator;
import de.captaingoldfish.scim.sdk.server.filter.FilterNode;
import de.captaingoldfish.scim.sdk.server.response.PartialListResponse;
import lombok.RequiredArgsConstructor;


/**
 * @author Pascal Knueppel
 * @since 26.06.2021
 */
@RequiredArgsConstructor
public class JwtBuilderHandler extends ResourceHandler<ScimJwtBuilder>
{

  /**
   * this builder will be used to create JWTs from plain content
   */
  private final JwtHandler jwtHandler;

  /**
   * used to validate the creation requests
   */
  private final ScimJwtBuilderValidator scimJwtBuilderValidator;

  /**
   * creates a new JWT
   */
  @Override
  public ScimJwtBuilder createResource(ScimJwtBuilder resource, Context context)
  {
    final String id = UUID.randomUUID().toString();
    String jwt = jwtHandler.createJwt(resource.getKeyId(), resource.getHeader(), resource.getBody());
    return ScimJwtBuilder.builder().id(id).body(jwt).meta(Meta.builder().created(Instant.now()).build()).build();
  }

  /**
   * disabled endpoint
   */
  @Override
  public ScimJwtBuilder getResource(String id,
                                    List<SchemaAttribute> attributes,
                                    List<SchemaAttribute> excludedAttributes,
                                    Context context)
  {
    return null;
  }

  /**
   * disabled endpoint
   */
  @Override
  public PartialListResponse<ScimJwtBuilder> listResources(long startIndex,
                                                           int count,
                                                           FilterNode filter,
                                                           SchemaAttribute sortBy,
                                                           SortOrder sortOrder,
                                                           List<SchemaAttribute> attributes,
                                                           List<SchemaAttribute> excludedAttributes,
                                                           Context context)
  {
    return null;
  }

  /**
   * disabled endpoint
   */
  @Override
  public ScimJwtBuilder updateResource(ScimJwtBuilder resourceToUpdate, Context context)
  {
    return null;
  }

  /**
   * disabled endpoint
   */
  @Override
  public void deleteResource(String id, Context context)
  {}

  @Override
  public RequestValidator<ScimJwtBuilder> getRequestValidator()
  {
    return scimJwtBuilderValidator;
  }
}
