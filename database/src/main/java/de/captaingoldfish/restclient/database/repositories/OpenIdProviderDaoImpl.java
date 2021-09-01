package de.captaingoldfish.restclient.database.repositories;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.transaction.Transactional;

import de.captaingoldfish.restclient.database.entities.CurrentWorkflowSettings;
import de.captaingoldfish.restclient.database.entities.HttpClientSettings;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;
import de.captaingoldfish.restclient.database.entities.OpenIdProvider;
import lombok.RequiredArgsConstructor;


/**
 * @author Pascal Knueppel
 * @since 31.05.2021
 */
@RequiredArgsConstructor
public class OpenIdProviderDaoImpl implements OpenIdProviderDaoExtension
{

  private final EntityManager entityManager;

  @Transactional
  @Override
  public void deleteById(Long id)
  {
    deleteCurrentWorkflowSettingsReferences(id);
    deleteHttpClientSettingsReferences(id);
    deleteOpenIdClients(id);
    deleteOpenIdProvider(id);
  }

  private void deleteCurrentWorkflowSettingsReferences(Long id)
  {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaDelete<CurrentWorkflowSettings> criteriaDelete = criteriaBuilder.createCriteriaDelete(CurrentWorkflowSettings.class);
    Root<CurrentWorkflowSettings> root = criteriaDelete.from(CurrentWorkflowSettings.class);

    Subquery<OpenIdClient> subquery = criteriaDelete.subquery(OpenIdClient.class);
    Root<OpenIdClient> subRoot = subquery.from(OpenIdClient.class);
    subquery.select(subRoot.get("id"));
    subquery.where(criteriaBuilder.equal(subRoot.get("openIdProvider").get("id"), id));

    criteriaDelete.where(root.get("openIdClient").get("id").in(subquery));
    entityManager.createQuery(criteriaDelete).executeUpdate();
  }

  private void deleteHttpClientSettingsReferences(Long id)
  {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaDelete<HttpClientSettings> criteriaDelete = criteriaBuilder.createCriteriaDelete(HttpClientSettings.class);
    Root<HttpClientSettings> root = criteriaDelete.from(HttpClientSettings.class);

    Subquery<OpenIdClient> subquery = criteriaDelete.subquery(OpenIdClient.class);
    Root<OpenIdClient> subRoot = subquery.from(OpenIdClient.class);
    subquery.select(subRoot.get("id"));
    subquery.where(criteriaBuilder.equal(subRoot.get("openIdProvider").get("id"), id));

    criteriaDelete.where(root.get("openIdClient").get("id").in(subquery));
    entityManager.createQuery(criteriaDelete).executeUpdate();
  }

  private void deleteOpenIdClients(Long id)
  {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaDelete<OpenIdClient> criteriaDelete = criteriaBuilder.createCriteriaDelete(OpenIdClient.class);
    Root<OpenIdClient> root = criteriaDelete.from(OpenIdClient.class);
    criteriaDelete.where(criteriaBuilder.equal(root.get("openIdProvider").get("id"), id));
    entityManager.createQuery(criteriaDelete).executeUpdate();
  }

  private void deleteOpenIdProvider(Long id)
  {
    OpenIdProvider openIdProvider = entityManager.find(OpenIdProvider.class, id);
    entityManager.remove(openIdProvider);
  }
}
