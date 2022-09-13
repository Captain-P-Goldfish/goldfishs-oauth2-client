package de.captaingoldfish.restclient.application.endpoints.httprequests;

import java.util.Optional;
import java.util.function.Supplier;

import de.captaingoldfish.restclient.application.utils.Utils;
import de.captaingoldfish.restclient.database.entities.HttpRequest;
import de.captaingoldfish.restclient.database.entities.HttpRequestCategory;
import de.captaingoldfish.restclient.database.repositories.HttpRequestCategoriesDao;
import de.captaingoldfish.restclient.database.repositories.HttpRequestsDao;
import de.captaingoldfish.restclient.scim.resources.ScimHttpRequest;
import de.captaingoldfish.scim.sdk.common.constants.HttpStatus;
import de.captaingoldfish.scim.sdk.server.endpoints.Context;
import de.captaingoldfish.scim.sdk.server.endpoints.validation.RequestValidator;
import de.captaingoldfish.scim.sdk.server.endpoints.validation.ValidationContext;
import lombok.RequiredArgsConstructor;


/**
 * author Pascal Knueppel <br>
 * created at: 12.09.2022 - 20:05 <br>
 * <br>
 */
@RequiredArgsConstructor
public class HttpRequestValidator implements RequestValidator<ScimHttpRequest>
{

  private final HttpRequestCategoriesDao httpRequestCategoriesDao;

  private final HttpRequestsDao httpRequestsDao;

  @Override
  public void validateCreate(ScimHttpRequest scimHttpRequest, ValidationContext validationContext, Context context)
  {
    validateScimHttpRequest(scimHttpRequest, validationContext);
  }

  @Override
  public void validateUpdate(Supplier<ScimHttpRequest> supplier,
                             ScimHttpRequest scimHttpRequest,
                             ValidationContext validationContext,
                             Context context)
  {
    validateScimHttpRequest(scimHttpRequest, validationContext);
  }

  private void validateScimHttpRequest(ScimHttpRequest scimHttpRequest, ValidationContext validationContext)
  {
    if (httpRequestCategoriesDao.findByName(scimHttpRequest.getCategoryName()).isEmpty())
    {
      validationContext.addError(ScimHttpRequest.FieldNames.CATEGORY_NAME,
                                 String.format("Unknown http request category '%s'",
                                               scimHttpRequest.getCategoryName()));
    }

    String name = scimHttpRequest.getName();
    isNameDuplicate(scimHttpRequest, validationContext, name);
  }

  private void isNameDuplicate(ScimHttpRequest scimHttpRequest, ValidationContext validationContext, String name)
  {
    Optional<HttpRequest> optionalHttpRequest = httpRequestsDao.findByName(name);
    if (optionalHttpRequest.isPresent()
        && optionalHttpRequest.get().getId() != scimHttpRequest.getId().map(Utils::parseId).orElse(0L))
    {
      HttpRequest httpRequest = optionalHttpRequest.get();
      HttpRequestCategory httpRequestCategory = httpRequest.getHttpRequestCategory();
      if (!httpRequestCategory.getName().equals(scimHttpRequest.getCategoryName()))
      {
        validationContext.setHttpResponseStatus(HttpStatus.CONFLICT);
        validationContext.addError(String.format("HTTP Request with name '%s' does already exist", name));
      }
    }
  }
}
