package de.captaingoldfish.restclient.database.repositories;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import de.captaingoldfish.restclient.database.entities.HttpClientSettings;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;
import lombok.RequiredArgsConstructor;


/**
 * @author Pascal Knueppel
 * @since 31.05.2021
 */
@RequiredArgsConstructor
public class OpenIdClientDaoImpl implements OpenIdClientDaoExtension
{

  private final EntityManager entityManager;

  @Transactional
  @Override
  public void deleteById(Long id)
  {
    removeChildHttpClientSettings(id);
    deleteOpenIdClient(id);
  }

  private void removeChildHttpClientSettings(Long id)
  {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaDelete<HttpClientSettings> criteriaDelete = criteriaBuilder.createCriteriaDelete(HttpClientSettings.class);
    Root<HttpClientSettings> root = criteriaDelete.from(HttpClientSettings.class);
    criteriaDelete.where(criteriaBuilder.equal(root.get("openIdClient").get("id"), id));
    entityManager.createQuery(criteriaDelete).executeUpdate();
  }

  private void deleteOpenIdClient(Long id)
  {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaDelete<OpenIdClient> criteriaDelete = criteriaBuilder.createCriteriaDelete(OpenIdClient.class);
    Root<OpenIdClient> root = criteriaDelete.from(OpenIdClient.class);
    criteriaDelete.where(criteriaBuilder.equal(root.get("id"), id));
    entityManager.createQuery(criteriaDelete).executeUpdate();
  }
}
