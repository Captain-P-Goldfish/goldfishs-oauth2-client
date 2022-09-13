package de.captaingoldfish.restclient.database.repositories;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import de.captaingoldfish.restclient.database.entities.HttpClientSettings;
import de.captaingoldfish.restclient.database.entities.HttpRequest;
import de.captaingoldfish.restclient.database.entities.HttpRequestCategory;
import lombok.RequiredArgsConstructor;


/**
 * @author Pascal Knueppel
 * @since 31.05.2021
 */
@RequiredArgsConstructor
public class HttpRequestCategoriesDaoImpl implements HttpRequestCategoriesDaoExtension
{

  private final EntityManager entityManager;

  /**
   * Deletes the entity with the given id.
   *
   * @param id must not be {@literal null}.
   * @throws IllegalArgumentException in case the given {@literal id} is {@literal null}
   */
  @Transactional
  @Override
  public void deleteById(Long id)
  {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

    HttpRequestCategory httpRequestCategory = entityManager.find(HttpRequestCategory.class, id);
    List<HttpRequest> httpRequests = getHttpRequests(criteriaBuilder, httpRequestCategory);

    httpRequests.forEach(entityManager::remove);
    entityManager.remove(httpRequestCategory);
  }

  /**
   * retrieves all http requests that belong to a specific category
   */
  private List<HttpRequest> getHttpRequests(CriteriaBuilder criteriaBuilder, HttpRequestCategory httpRequestCategory)
  {
    CriteriaQuery<HttpRequest> criteriaQuery = criteriaBuilder.createQuery(HttpRequest.class);
    Root<HttpRequest> root = criteriaQuery.from(HttpRequest.class);
    criteriaQuery.where(criteriaBuilder.equal(root.get("httpRequestCategory"), httpRequestCategory));
    return entityManager.createQuery(criteriaQuery).getResultList();
  }

}
