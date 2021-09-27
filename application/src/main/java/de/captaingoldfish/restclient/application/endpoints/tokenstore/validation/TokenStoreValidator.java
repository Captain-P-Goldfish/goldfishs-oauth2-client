package de.captaingoldfish.restclient.application.endpoints.tokenstore.validation;

import java.util.function.Supplier;

import de.captaingoldfish.restclient.application.projectconfig.WebAppConfig;
import de.captaingoldfish.restclient.database.entities.TokenCategory;
import de.captaingoldfish.restclient.database.repositories.TokenCategoryDao;
import de.captaingoldfish.restclient.scim.resources.ScimTokenStore;
import de.captaingoldfish.scim.sdk.server.endpoints.Context;
import de.captaingoldfish.scim.sdk.server.endpoints.validation.RequestValidator;
import de.captaingoldfish.scim.sdk.server.endpoints.validation.ValidationContext;


/**
 * @author Pascal Knueppel
 * @since 02.09.2021
 */
public class TokenStoreValidator implements RequestValidator<ScimTokenStore>
{

  /**
   * {@inheritDoc}
   */
  @Override
  public void validateCreate(ScimTokenStore resource, ValidationContext validationContext, Context requestContext)
  {
    if (validationContext.hasErrors())
    {
      return;
    }

    validateOwningCategory(resource, validationContext);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void validateUpdate(Supplier<ScimTokenStore> oldResourceSupplier,
                             ScimTokenStore newResource,
                             ValidationContext validationContext,
                             Context requestContext)
  {
    if (validationContext.hasErrors())
    {
      return;
    }

    validateOwningCategory(newResource, validationContext);
  }

  /**
   * verifies that the referenced owning category does exist
   */
  private void validateOwningCategory(ScimTokenStore newResource, ValidationContext validationContext)
  {
    TokenCategoryDao tokenCategoryDao = WebAppConfig.getApplicationContext().getBean(TokenCategoryDao.class);
    TokenCategory owner = tokenCategoryDao.findById(newResource.getCategoryId()).orElse(null);
    if (owner == null)
    {
      validationContext.addError("categoryId",
                                 String.format("Could not find owning category with ID '%s'",
                                               newResource.getCategoryId()));
    }
  }
}
