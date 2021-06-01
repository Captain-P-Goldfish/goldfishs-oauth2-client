package de.captaingoldfish.restclient.database.repositories;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import de.captaingoldfish.restclient.database.entities.HttpClientSettings;
import de.captaingoldfish.restclient.database.entities.Proxy;
import lombok.RequiredArgsConstructor;


/**
 * @author Pascal Knueppel
 * @since 31.05.2021
 */
@RequiredArgsConstructor
public class ProxyDaoImpl implements ProxyDaoExtension
{

  private final EntityManager entityManager;

  @Transactional
  @Override
  public void deleteById(Long id)
  {
    removeForeignKeyReferences(id);
    deleteProxy(id);
  }

  private void removeForeignKeyReferences(Long id)
  {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaUpdate<HttpClientSettings> criteriaUpdate = criteriaBuilder.createCriteriaUpdate(HttpClientSettings.class);
    Root<HttpClientSettings> root = criteriaUpdate.from(HttpClientSettings.class);
    criteriaUpdate.set(root.get("proxy"), (Object)null);
    criteriaUpdate.where(criteriaBuilder.equal(root.get("proxy").get("id"), id));
    entityManager.createQuery(criteriaUpdate).executeUpdate();
  }

  private void deleteProxy(Long id)
  {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaDelete<Proxy> criteriaDelete = criteriaBuilder.createCriteriaDelete(Proxy.class);
    Root<Proxy> root = criteriaDelete.from(Proxy.class);
    criteriaDelete.where(criteriaBuilder.equal(root.get("id"), id));
    entityManager.createQuery(criteriaDelete).executeUpdate();
  }
}
