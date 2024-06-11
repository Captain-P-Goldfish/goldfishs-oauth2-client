package de.captaingoldfish.restclient.database.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;

import de.captaingoldfish.restclient.database.entities.CurrentWorkflowSettings;
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
    removeChildCurrentWorkflowSettings(id);
    removeChildHttpClientSettings(id);
    deleteOpenIdClient(id);
  }

  private void removeChildCurrentWorkflowSettings(Long id)
  {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaDelete<CurrentWorkflowSettings> criteriaDelete = criteriaBuilder.createCriteriaDelete(CurrentWorkflowSettings.class);
    Root<CurrentWorkflowSettings> root = criteriaDelete.from(CurrentWorkflowSettings.class);
    criteriaDelete.where(criteriaBuilder.equal(root.get("openIdClient").get("id"), id));
    entityManager.createQuery(criteriaDelete).executeUpdate();
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
