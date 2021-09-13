package de.captaingoldfish.restclient.application.endpoints.tokenstore;

import de.captaingoldfish.restclient.application.projectconfig.WebAppConfig;
import de.captaingoldfish.restclient.database.entities.TokenCategory;
import de.captaingoldfish.restclient.database.entities.TokenStore;
import de.captaingoldfish.restclient.database.repositories.TokenCategoryDao;
import de.captaingoldfish.restclient.scim.resources.ScimTokenStore;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;


/**
 * @author Pascal Knueppel
 * @since 28.08.2021
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TokenStoreConverter
{

  public static TokenStore toTokenStore(ScimTokenStore scimTokenStore)
  {
    TokenCategoryDao tokenCategoryDao = WebAppConfig.getApplicationContext().getBean(TokenCategoryDao.class);
    TokenCategory tokenCategory = tokenCategoryDao.findById(scimTokenStore.getCategoryId()).orElseThrow();

    return TokenStore.builder()
                     .id(scimTokenStore.getId().map(Long::parseLong).orElse(0L))
                     .tokenCategory(tokenCategory)
                     .name(scimTokenStore.getName())
                     .token(scimTokenStore.getToken())
                     .build();
  }

  public static ScimTokenStore toScimTokenStore(TokenStore tokenStore)
  {
    return ScimTokenStore.builder()
                         .id(String.valueOf(tokenStore.getId()))
                         .categoryId(tokenStore.getTokenCategory().getId())
                         .name(tokenStore.getName())
                         .token(tokenStore.getToken())
                         .meta(Meta.builder()
                                   .created(tokenStore.getCreated())
                                   .lastModified(tokenStore.getLastModified())
                                   .build())
                         .build();
  }
}
