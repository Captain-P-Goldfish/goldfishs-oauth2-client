package de.captaingoldfish.restclient.application.endpoints.truststore.validation;

import java.util.Optional;
import java.util.function.Supplier;

import de.captaingoldfish.restclient.scim.resources.ScimTruststore;
import de.captaingoldfish.scim.sdk.server.endpoints.validation.RequestValidator;
import de.captaingoldfish.scim.sdk.server.endpoints.validation.ValidationContext;


/**
 * @author Pascal Knueppel
 * @since 19.05.2021
 */
public class ScimTruststoreRequestValidator implements RequestValidator<ScimTruststore>
{

  /**
   * validates an upload request that might be a truststore upload or a certificate upload
   */
  @Override
  public void validateCreate(ScimTruststore scimTruststore, ValidationContext validationContext)
  {
    if (validationContext.hasErrors())
    {
      return;
    }

    Optional<ScimTruststore.TruststoreUpload> truststoreUpload = scimTruststore.getTruststoreUpload();
    Optional<ScimTruststore.CertificateUpload> certificateUpload = scimTruststore.getCertificateUpload();
    if (truststoreUpload.isEmpty() && certificateUpload.isEmpty())
    {
      validationContext.addError("Missing object in create request. Either one of 'truststoreUpload' "
                                 + "or 'certificateUpload' is required");
      return;
    }
    if (truststoreUpload.isPresent() && certificateUpload.isPresent())
    {
      validationContext.addError("Cannot handle create request. Only one of these objects may be present "
                                 + "['truststoreUpload', 'certificateUpload']");
      return;
    }

    if (truststoreUpload.isPresent())
    {
      TruststoreUploadValidator.validateTruststoreUpload(truststoreUpload.get(), validationContext);
    }
    else
    {
      CertificateUploadValidator.validateCertificateUpload(certificateUpload.get(), validationContext);
    }
  }

  /**
   * update is not supported for the application truststore
   */
  @Override
  public void validateUpdate(Supplier<ScimTruststore> oldResourceSupplier,
                             ScimTruststore newResource,
                             ValidationContext validationContext)
  {
    // not supported
  }
}
