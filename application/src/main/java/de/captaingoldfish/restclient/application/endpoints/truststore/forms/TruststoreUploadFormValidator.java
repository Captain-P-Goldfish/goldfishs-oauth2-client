package de.captaingoldfish.restclient.application.endpoints.truststore.forms;

import java.security.cert.X509Certificate;
import java.util.Optional;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import de.captaingoldfish.restclient.application.projectconfig.WebAppConfig;
import de.captaingoldfish.restclient.commons.keyhelper.KeyReader;
import de.captaingoldfish.restclient.commons.keyhelper.KeyStoreSupporter;
import de.captaingoldfish.restclient.commons.keyhelper.KeyStoreSupporter.KeyStoreType;
import de.captaingoldfish.restclient.database.entities.Truststore;
import de.captaingoldfish.restclient.database.repositories.TruststoreDao;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


/**
 * validates the content of {@link TruststoreUploadForm}
 * 
 * @author Pascal Knueppel
 * @since 04.042021
 */
@Slf4j
public class TruststoreUploadFormValidator
  implements ConstraintValidator<TruststoreUploadFormValidation, TruststoreUploadForm>
{

  /**
   * checks that the data in the {@link TruststoreUploadForm} is valid and is able to produce valid results
   * during further processing
   */
  @Override
  public boolean isValid(TruststoreUploadForm truststoreUploadForm, ConstraintValidatorContext context)
  {
    boolean isValid = true;

    boolean bothFilesAreEmpty = Optional.ofNullable(truststoreUploadForm.getTruststoreFile())
                                        .map(MultipartFile::isEmpty)
                                        .orElse(true)
                                && Optional.ofNullable(truststoreUploadForm.getCertificateFile())
                                           .map(MultipartFile::isEmpty)
                                           .orElse(true);
    boolean bothFilesUploaded = Optional.ofNullable(truststoreUploadForm.getTruststoreFile())
                                        .map(multipartFile -> !multipartFile.isEmpty())
                                        .orElse(false)
                                && Optional.ofNullable(truststoreUploadForm.getCertificateFile())
                                           .map(multipartFile -> !multipartFile.isEmpty())
                                           .orElse(false);
    boolean keystoreFileIsEmpty = Optional.ofNullable(truststoreUploadForm.getTruststoreFile())
                                          .map(MultipartFile::isEmpty)
                                          .orElse(true);
    if (bothFilesAreEmpty)
    {
      String errorMessage = "No file was uploaded";
      log.debug(errorMessage);
      context.buildConstraintViolationWithTemplate(errorMessage)
             .addPropertyNode("truststoreFile")
             .addConstraintViolation();
      context.buildConstraintViolationWithTemplate(errorMessage)
             .addPropertyNode("certificateFile")
             .addConstraintViolation();
      isValid = false;
    }

    if (bothFilesUploaded)
    {
      String errorMessage = "Accepting only a single upload file: keystore or certificate file";
      log.debug(errorMessage);
      context.buildConstraintViolationWithTemplate(errorMessage)
             .addPropertyNode("truststoreFile")
             .addConstraintViolation();
      context.buildConstraintViolationWithTemplate(errorMessage)
             .addPropertyNode("certificateFile")
             .addConstraintViolation();
      isValid = false;
    }

    if (keystoreFileIsEmpty)
    {
      isValid = isValid && validateCertificateUpload(truststoreUploadForm, context);
    }
    else
    {
      isValid = isValid && validateTruststoreUpload(truststoreUploadForm, context);
    }

    return isValid;
  }

  /**
   * validates the form for an uploaded keystore that is interpreted as a truststore
   */
  private boolean validateTruststoreUpload(TruststoreUploadForm truststoreUploadForm,
                                           ConstraintValidatorContext context)
  {
    boolean isValid = true;

    final String fileName = truststoreUploadForm.getTruststoreFile().getOriginalFilename();
    final KeyStoreType keyStoreType = KeyStoreType.byFileExtension(fileName).orElse(KeyStoreType.JKS);

    if (KeyStoreType.PKCS12.equals(keyStoreType) && StringUtils.isEmpty(truststoreUploadForm.getTruststorePassword()))
    {
      String errormessage = "Not accepting empty passwords for PKCS12 keystore type";
      log.debug(errormessage);
      context.buildConstraintViolationWithTemplate(errormessage)
             .addPropertyNode("truststorePassword")
             .addConstraintViolation();
      isValid = false;
    }

    try
    {
      final byte[] keystoreBytes = truststoreUploadForm.getTruststoreFile().getBytes();
      final String password = truststoreUploadForm.getTruststorePassword();
      KeyStoreSupporter.readTruststore(keystoreBytes, keyStoreType, password);
    }
    catch (Exception ex)
    {
      Throwable current = ex;
      while (current != null)
      {
        String errormessage = Optional.ofNullable(current.getMessage()).orElse("NullPointerException");
        log.debug(errormessage);
        context.buildConstraintViolationWithTemplate(errormessage)
               .addPropertyNode("truststoreFile")
               .addConstraintViolation();
        current = current.getCause();
      }
      isValid = false;
    }

    return isValid;
  }

  /**
   * validates the the form for an uploaded certificate file that should be added to the application truststore
   */
  @SneakyThrows
  private boolean validateCertificateUpload(TruststoreUploadForm truststoreUploadForm,
                                            ConstraintValidatorContext context)
  {
    boolean isValid = true;

    if (StringUtils.isBlank(truststoreUploadForm.getAlias()))
    {
      String errormessage = "Alias must not be blank";
      log.debug(errormessage);
      context.buildConstraintViolationWithTemplate(errormessage).addPropertyNode("alias").addConstraintViolation();
      isValid = false;
    }

    X509Certificate certificate = null;
    try
    {
      certificate = KeyReader.readX509Certificate(truststoreUploadForm.getCertificateFile().getBytes());
    }
    catch (Exception ex)
    {
      Throwable current = ex;
      while (current != null)
      {
        String errormessage = Optional.ofNullable(current.getMessage()).orElse("NullPointerException");
        log.debug(errormessage);
        context.buildConstraintViolationWithTemplate(errormessage)
               .addPropertyNode("certificateFile")
               .addConstraintViolation();
        current = current.getCause();
      }
      isValid = false;
    }

    TruststoreDao truststoreDao = WebAppConfig.getApplicationContext().getBean(TruststoreDao.class);
    Truststore truststore = truststoreDao.getTruststore();

    if (truststore.getTruststoreAliases().contains(truststoreUploadForm.getAlias()))
    {
      String errormessage = String.format("Cannot add certificate for alias '%s' is already taken",
                                          truststoreUploadForm.getAlias());
      log.debug(errormessage);
      context.buildConstraintViolationWithTemplate(errormessage)
             .addPropertyNode("certificateFile")
             .addConstraintViolation();
      isValid = false;
    }

    if (certificate == null)
    {
      return isValid;
    }

    String existingCertEntryAlias = truststore.getTruststore().getCertificateAlias(certificate);
    if (existingCertEntryAlias != null)
    {
      String errormessage = String.format("Cannot add certificate for certificate is already present under alias '%s'",
                                          existingCertEntryAlias);
      log.debug(errormessage);
      context.buildConstraintViolationWithTemplate(errormessage)
             .addPropertyNode("certificateFile")
             .addConstraintViolation();
      isValid = false;
    }

    return isValid;
  }
}
