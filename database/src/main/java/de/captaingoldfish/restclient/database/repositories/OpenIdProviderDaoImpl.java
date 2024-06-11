package de.captaingoldfish.restclient.database.repositories;

import de.captaingoldfish.restclient.database.entities.OpenIdClient;
import de.captaingoldfish.restclient.database.entities.OpenIdProvider;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
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
    final String jpql = """
      delete from CurrentWorkflowSettings cws
       where cws.openIdClient.id in (select id from OpenIdClient oc
                                      where oc.openIdProvider.id = :openIdProviderId)
      """;

    entityManager.createQuery(jpql).setParameter("openIdProviderId", id).executeUpdate();
  }

  private void deleteHttpClientSettingsReferences(Long id)
  {
    final String jpql = """
      delete from HttpClientSettings hcs
       where hcs.openIdClient.id in (select id from OpenIdClient oc
                                      where oc.openIdProvider.id = :openIdProviderId)
      """;

    entityManager.createQuery(jpql).setParameter("openIdProviderId", id).executeUpdate();
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
