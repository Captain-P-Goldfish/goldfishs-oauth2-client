package de.captaingoldfish.restclient.application.endpoints.truststore.validation;

import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Optional;

import de.captaingoldfish.restclient.application.projectconfig.WebAppConfig;
import de.captaingoldfish.restclient.commons.keyhelper.KeyReader;
import de.captaingoldfish.restclient.database.entities.Truststore;
import de.captaingoldfish.restclient.database.repositories.TruststoreDao;
import de.captaingoldfish.restclient.scim.resources.ScimTruststore.CertificateUpload;
import de.captaingoldfish.scim.sdk.server.endpoints.validation.ValidationContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


/**
 * @author Pascal Knueppel
 * @since 19.05.2021
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CertificateUploadValidator
{

  /**
   * validates that the certificate that is being uploaded contains valid data
   */
  @SneakyThrows
  public static void validateCertificateUpload(CertificateUpload certificateUpload, ValidationContext validationContext)
  {
    byte[] decodedCertificateFile;
    try
    {
      decodedCertificateFile = Base64.getDecoder().decode(certificateUpload.getCertificateFile());
    }
    catch (IllegalArgumentException ex)
    {
      validationContext.addError("certificateUpload.certificateFile",
                                 String.format("Certificate file is not Base64 encoded: %s", ex.getMessage()));
      return;
    }

    if (decodedCertificateFile == null || decodedCertificateFile.length == 0)
    {
      validationContext.addError("certificateUpload.certificateFile", "Certificate file must not be empty");
      return;
    }


    X509Certificate certificate = null;
    try
    {
      certificate = KeyReader.readX509Certificate(decodedCertificateFile);
    }
    catch (Exception ex)
    {
      Throwable current = ex;
      while (current != null)
      {
        String errormessage = Optional.ofNullable(current.getMessage()).orElse("NullPointerException");
        log.debug(errormessage);
        validationContext.addError("certificateUpload.certificateFile", errormessage);
        current = current.getCause();
      }
    }

    TruststoreDao truststoreDao = WebAppConfig.getApplicationContext().getBean(TruststoreDao.class);
    Truststore truststore = truststoreDao.getTruststore();

    if (truststore.getTruststoreAliases().contains(certificateUpload.getAlias()))
    {
      String errormessage = String.format("Cannot add certificate for alias '%s' is already taken",
                                          certificateUpload.getAlias());
      log.debug(errormessage);
      validationContext.addError("certificateUpload.alias", errormessage);
    }

    if (certificate == null)
    {
      return;
    }

    String existingCertEntryAlias = truststore.getTruststore().getCertificateAlias(certificate);
    if (existingCertEntryAlias != null)
    {
      String errormessage = String.format("Cannot add certificate for certificate is already present under alias '%s'",
                                          existingCertEntryAlias);
      log.debug(errormessage);
      validationContext.addError("certificateUpload.certificateFile", errormessage);
    }
  }
}
