package de.captaingoldfish.restclient.application.endpoints.tokenstore.filtering;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import de.captaingoldfish.restclient.application.projectconfig.WebAppConfig;
import de.captaingoldfish.restclient.database.entities.TokenStore;
import de.captaingoldfish.restclient.scim.resources.ScimTokenStore;
import de.captaingoldfish.scim.sdk.common.constants.enums.Type;
import de.captaingoldfish.scim.sdk.server.filter.AndExpressionNode;
import de.captaingoldfish.scim.sdk.server.filter.AttributeExpressionLeaf;
import de.captaingoldfish.scim.sdk.server.filter.FilterNode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;


/**
 * This class is used to resolve the token-store filter on database level. The implementation is kept to a
 * minimum level to only support the expected part that is being used within the frontend
 *
 * @author Pascal Knueppel
 * @since 17.09.2021
 */
public class TokenStoreFilterResolver
{

  /**
   * to build the criteria query for accessing the wanted token stores
   */
  private final EntityManager entityManager;

  private final CriteriaBuilder criteriaBuilder;

  private final CriteriaQuery<TokenStore> criteriaQuery;

  private final Root<TokenStore> root;

  public TokenStoreFilterResolver()
  {
    this.entityManager = WebAppConfig.getApplicationContext().getBean(EntityManager.class);
    this.criteriaBuilder = entityManager.getCriteriaBuilder();
    this.criteriaQuery = this.criteriaBuilder.createQuery(TokenStore.class);
    this.root = this.criteriaQuery.from(TokenStore.class);
  }

  /**
   * will resolve the filter expression and returns either all TokenStores or the ones matching the given filter
   */
  public List<TokenStore> resolveFilter(FilterNode filterNode)
  {
    if (filterNode != null)
    {
      Predicate predicate = getFilterPredicate(filterNode);
      criteriaQuery.where(predicate);
    }
    return entityManager.createQuery(criteriaQuery).getResultList();
  }

  /**
   * resolves the filter node to a jpa predicate
   */
  private Predicate getFilterPredicate(FilterNode filterNode)
  {
    if (filterNode instanceof AttributeExpressionLeaf)
    {
      return resolveSimpleExpression((AttributeExpressionLeaf)filterNode);
    }
    else if (filterNode instanceof AndExpressionNode)
    {
      AndExpressionNode andExpressionNode = (AndExpressionNode)filterNode;
      Predicate leftNode = getFilterPredicate(andExpressionNode.getLeftNode());
      Predicate rightNode = getFilterPredicate(andExpressionNode.getRightNode());
      return criteriaBuilder.and(leftNode, rightNode);
    }
    else
    {
      throw new UnsupportedOperationException(String.format("found unsupported operation during filtering: %s",
                                                            filterNode));
    }
  }

  /**
   * resolves a simple filter comparison expression
   */
  private Predicate resolveSimpleExpression(AttributeExpressionLeaf attributeExpressionLeaf)
  {
    Object attributeValue = getValue(attributeExpressionLeaf);
    return getComparisonPredicate(attributeExpressionLeaf, attributeValue);
  }

  /**
   * checks for the comparison operator and returns the comparison as jpa predicate
   */
  private Predicate getComparisonPredicate(AttributeExpressionLeaf attributeExpressionLeaf, Object attributeValue)
  {
    Expression objectPath = resolveAttributeName(root, attributeExpressionLeaf);
    switch (attributeExpressionLeaf.getComparator())
    {
      case EQ:
        return criteriaBuilder.equal(objectPath, attributeValue);
      case CO:
        return criteriaBuilder.like(objectPath, "%" + StringUtils.toRootLowerCase((String)attributeValue) + "%");
      default:
        throw new UnsupportedOperationException(String.format("Found unsupported operation '%s'",
                                                              attributeExpressionLeaf.getComparator(),
                                                              attributeExpressionLeaf));
    }
  }

  /**
   * resolves the attribute path on the {@link TokenStore} jpa entity
   */
  private Expression<? extends Object> resolveAttributeName(Root<TokenStore> root,
                                                            AttributeExpressionLeaf attributeExpressionLeaf)
  {
    switch (attributeExpressionLeaf.getAttributeName())
    {
      case ScimTokenStore.FieldNames.CATEGORY_ID:
        return root.get("tokenCategory").get("id");
      case ScimTokenStore.FieldNames.TOKEN:
        return criteriaBuilder.lower(root.get("token"));
      default:
        throw new UnsupportedOperationException(String.format("Found unsupported filter attribute in filter expression: %s",
                                                              attributeExpressionLeaf));
    }
  }

  /**
   * translates the value of the filter comparison node
   */
  private Object getValue(AttributeExpressionLeaf attributeExpressionLeaf)
  {
    if (Type.INTEGER.equals(attributeExpressionLeaf.getSchemaAttribute().getType()))
    {
      return Long.parseLong(attributeExpressionLeaf.getValue());
    }
    return attributeExpressionLeaf.getValue();
  }
}
