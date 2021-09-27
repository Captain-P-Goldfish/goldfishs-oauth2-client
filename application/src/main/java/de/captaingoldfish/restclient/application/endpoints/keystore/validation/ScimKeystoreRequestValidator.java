package de.captaingoldfish.restclient.application.endpoints.keystore.validation;

import java.util.function.Supplier;

import de.captaingoldfish.restclient.scim.resources.ScimKeystore;
import de.captaingoldfish.scim.sdk.server.endpoints.Context;
import de.captaingoldfish.scim.sdk.server.endpoints.validation.RequestValidator;
import de.captaingoldfish.scim.sdk.server.endpoints.validation.ValidationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * @author Pascal Knueppel
 * @since 09.05.2021
 */
@Slf4j
@RequiredArgsConstructor
public class ScimKeystoreRequestValidator implements RequestValidator<ScimKeystore>
{

  /**
   * validates a keystore upload
   */
  @Override
  public void validateCreate(ScimKeystore scimKeystore, ValidationContext validationContext, Context requestContext)
  {
    if (validationContext.hasErrors())
    {
      return;
    }
    if (scimKeystore.getFileUpload() == null && scimKeystore.getAliasSelection() == null)
    {
      validationContext.addError("Missing object in create request. Either one of 'fileUpload' "
                                 + "or 'aliasSelection' is required");
      return;
    }
    if (scimKeystore.getFileUpload() != null && scimKeystore.getAliasSelection() != null)
    {
      validationContext.addError("Cannot handle create request. Only one of these objects may be present "
                                 + "['fileUpload', 'aliasSelection']");
      return;
    }
    if (scimKeystore.getFileUpload() != null)
    {
      UploadFormValidator.validateUploadForm(scimKeystore.getFileUpload(), validationContext);
    }
    else if (scimKeystore.getAliasSelection() != null)
    {
      AliasSelectionFormValidator.validateAliasSelectionForm(scimKeystore.getAliasSelection(), validationContext);
    }
  }

  /**
   * update is disabled
   */
  @Override
  public void validateUpdate(Supplier<ScimKeystore> originalResourceSupplier,
                             ScimKeystore scimKeystore,
                             ValidationContext validationContext,
                             Context requestContext)
  {
    // not implemented
  }
}
