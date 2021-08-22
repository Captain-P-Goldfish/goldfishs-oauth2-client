package de.captaingoldfish.restclient.application.endpoints.jwt;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;

import de.captaingoldfish.restclient.application.crypto.JwtHandler;
import de.captaingoldfish.restclient.application.endpoints.jwt.validation.ScimJwtBuilderValidator;
import de.captaingoldfish.restclient.scim.resources.ScimJwtBuilder;
import de.captaingoldfish.scim.sdk.common.constants.enums.SortOrder;
import de.captaingoldfish.scim.sdk.common.exceptions.BadRequestException;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import de.captaingoldfish.scim.sdk.common.schemas.SchemaAttribute;
import de.captaingoldfish.scim.sdk.server.endpoints.Context;
import de.captaingoldfish.scim.sdk.server.endpoints.ResourceHandler;
import de.captaingoldfish.scim.sdk.server.endpoints.validation.RequestValidator;
import de.captaingoldfish.scim.sdk.server.filter.FilterNode;
import de.captaingoldfish.scim.sdk.server.response.PartialListResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;


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
  @SneakyThrows
  @Override
  public ScimJwtBuilder createResource(ScimJwtBuilder resource, Context context)
  {
    final String id = UUID.randomUUID().toString();
    JwtHandler.JwtAttribute[] jwtAttributes = getJwtAttributes(resource);
    String jwt = jwtHandler.createJwt(resource.getKeyId(), resource.getHeader(), resource.getBody(), jwtAttributes);
    JWT jsonWebToken = JWTParser.parse(jwt);
    return ScimJwtBuilder.builder()
                         .id(id)
                         .jwt(jwt)
                         .header(jsonWebToken.getHeader().toString())
                         .meta(Meta.builder().created(Instant.now()).build())
                         .build();
  }

  private JwtHandler.JwtAttribute[] getJwtAttributes(ScimJwtBuilder resource)
  {
    List<JwtHandler.JwtAttribute> jwtAttributeList = new ArrayList<>();
    if (resource.isAddX5tSha256tHeader())
    {
      jwtAttributeList.add(JwtHandler.JwtAttribute.X5T_SHA256);
    }
    return jwtAttributeList.toArray(JwtHandler.JwtAttribute[]::new);
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
   * validates or decrypts a given JWT
   */
  @Override
  public ScimJwtBuilder updateResource(ScimJwtBuilder resource, Context context)
  {
    final String id = "1";
    try
    {
      JwtHandler.PlainJwtData plainJwtData = jwtHandler.handleJwt(resource.getKeyId(), resource.getJwt());
      return ScimJwtBuilder.builder()
                           .id(id)
                           .header(plainJwtData.getHeader())
                           .body(plainJwtData.getBody())
                           .meta(Meta.builder().created(Instant.now()).build())
                           .build();
    }
    catch (Exception ex)
    {
      throw new BadRequestException(ex.getMessage(), ex);
    }
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
