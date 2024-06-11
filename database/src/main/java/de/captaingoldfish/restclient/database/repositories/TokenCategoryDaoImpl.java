package de.captaingoldfish.restclient.database.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;

import de.captaingoldfish.restclient.database.entities.TokenCategory;
import de.captaingoldfish.restclient.database.entities.TokenStore;
import lombok.RequiredArgsConstructor;


/**
 * @author Pascal Knueppel
 * @since 02.09.2021
 */
@RequiredArgsConstructor
public class TokenCategoryDaoImpl implements TokenCategoryDaoExtension
{

  private final EntityManager entityManager;

  @Transactional
  @Override
  public void deleteById(Long id)
  {
    deleteRelatedTokens(id);
    deleteCategory(id);
  }

  private void deleteRelatedTokens(Long id)
  {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaDelete<TokenStore> criteriaDelete = criteriaBuilder.createCriteriaDelete(TokenStore.class);
    Root<TokenStore> root = criteriaDelete.from(TokenStore.class);
    criteriaDelete.where(criteriaBuilder.equal(root.get("tokenCategory").get("id"), id));
    entityManager.createQuery(criteriaDelete).executeUpdate();
  }

  private void deleteCategory(Long id)
  {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaDelete<TokenCategory> criteriaUpdate = criteriaBuilder.createCriteriaDelete(TokenCategory.class);
    Root<TokenCategory> root = criteriaUpdate.from(TokenCategory.class);
    criteriaUpdate.where(criteriaBuilder.equal(root.get("id"), id));
    entityManager.createQuery(criteriaUpdate).executeUpdate();
  }
}
