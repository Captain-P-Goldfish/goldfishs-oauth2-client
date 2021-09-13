package de.captaingoldfish.restclient.application.endpoints.tokencategory;

import de.captaingoldfish.restclient.database.entities.TokenCategory;
import de.captaingoldfish.restclient.scim.resources.ScimTokenCategory;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;


/**
 * @author Pascal Knueppel
 * @since 02.09.2021
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TokenCategoryConverter
{

  public static TokenCategory toTokenCategory(ScimTokenCategory scimTokenCategory)
  {
    return TokenCategory.builder()
                        .id(scimTokenCategory.getId().map(Long::parseLong).orElse(0L))
                        .name(scimTokenCategory.getName())
                        .build();
  }

  public static ScimTokenCategory toScimTokenCategory(TokenCategory tokenCategory)
  {
    return ScimTokenCategory.builder()
                            .id(String.valueOf(tokenCategory.getId()))
                            .name(tokenCategory.getName())
                            .meta(Meta.builder()
                                      .created(tokenCategory.getCreated())
                                      .lastModified(tokenCategory.getLastModified())
                                      .build())
                            .build();
  }
}
