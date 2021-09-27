package de.captaingoldfish.restclient.application.endpoints.tokencategory.validation;

import java.util.function.Supplier;

import de.captaingoldfish.restclient.application.projectconfig.WebAppConfig;
import de.captaingoldfish.restclient.database.repositories.TokenCategoryDao;
import de.captaingoldfish.restclient.scim.resources.ScimTokenCategory;
import de.captaingoldfish.scim.sdk.server.endpoints.Context;
import de.captaingoldfish.scim.sdk.server.endpoints.validation.RequestValidator;
import de.captaingoldfish.scim.sdk.server.endpoints.validation.ValidationContext;


/**
 * @author Pascal Knueppel
 * @since 02.09.2021
 */
public class TokenCategoryValidator implements RequestValidator<ScimTokenCategory>
{


  @Override
  public void validateCreate(ScimTokenCategory resource, ValidationContext validationContext, Context requestContext)
  {
    if (validationContext.hasErrors())
    {
      return;
    }

    TokenCategoryDao tokenCategoryDao = WebAppConfig.getApplicationContext().getBean(TokenCategoryDao.class);
    tokenCategoryDao.findByName(resource.getName()).ifPresent(tokenCategory -> {
      validationContext.addError("name",
                                 String.format("The given token category name '%s' does already exist",
                                               resource.getName()));
    });
  }

  @Override
  public void validateUpdate(Supplier<ScimTokenCategory> oldResourceSupplier,
                             ScimTokenCategory newResource,
                             ValidationContext validationContext,
                             Context requestContext)
  {
    if (validationContext.hasErrors())
    {
      return;
    }

    ScimTokenCategory oldResource = oldResourceSupplier.get();
    if (oldResource.getName().equals(newResource.getName()))
    {
      // everything is fine. There will be no effective update
      return;
    }

    TokenCategoryDao tokenCategoryDao = WebAppConfig.getApplicationContext().getBean(TokenCategoryDao.class);
    tokenCategoryDao.findByName(newResource.getName()).ifPresent(tokenCategory -> {
      validationContext.addError("name",
                                 String.format("The given token category name '%s' does already exist",
                                               newResource.getName()));
    });
  }
}
