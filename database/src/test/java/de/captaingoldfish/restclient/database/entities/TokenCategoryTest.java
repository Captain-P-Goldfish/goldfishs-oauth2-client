package de.captaingoldfish.restclient.database.entities;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.captaingoldfish.restclient.database.DatabaseTest;
import de.captaingoldfish.restclient.database.DbBaseTest;


/**
 * @author Pascal Knueppel
 * @since 02.09.2021
 */
@DatabaseTest
public class TokenCategoryTest extends DbBaseTest
{

  @Test
  public void testDeleteCategoryWithExistingChildren()
  {
    TokenCategory tokenCategory = TokenCategory.builder().name("keycloak").build();
    tokenCategory = tokenCategoryDao.save(tokenCategory);

    TokenStore tokenStore = TokenStore.builder()
                                      .tokenCategory(tokenCategory)
                                      .name("access_token")
                                      .token("abcd")
                                      .build();
    tokenStoreDao.save(tokenStore);

    Assertions.assertEquals(1, tokenStoreDao.count());
    Assertions.assertEquals(1, tokenCategoryDao.count());

    tokenCategoryDao.deleteById(tokenCategory.getId());

    Assertions.assertEquals(0, tokenStoreDao.count());
    Assertions.assertEquals(0, tokenCategoryDao.count());
  }
}
