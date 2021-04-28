package de.captaingoldfish.restclient.application.endpoints.keystore.forms;

import java.util.Optional;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

import de.captaingoldfish.restclient.commons.keyhelper.KeyStoreSupporter;
import lombok.extern.slf4j.Slf4j;


/**
 * validates the content of {@link KeystoreUploadForm}
 * 
 * @author Pascal Knueppel
 * @since 27.03.2021
 */
@Slf4j
public class KeystoreUploadFormValidator
  implements ConstraintValidator<KeystoreUploadFormValidation, KeystoreUploadForm>
{

  /**
   * checks that the data in the {@link KeystoreUploadForm} is valid and is able to produce valid results during
   * further processing
   */
  @Override
  public boolean isValid(KeystoreUploadForm keystoreUploadForm, ConstraintValidatorContext context)
  {
    boolean isValid = true;

    if (StringUtils.isEmpty(keystoreUploadForm.getKeystorePassword()))
    {
      String errormessage = "Not accepting empty passwords";
      log.debug(errormessage);
      context.buildConstraintViolationWithTemplate(errormessage)
             .addPropertyNode("keystorePassword")
             .addConstraintViolation();
      isValid = false;
    }

    if (keystoreUploadForm.getKeystoreFile() == null || keystoreUploadForm.getKeystoreFile().isEmpty())
    {
      String errormessage = "Keystore file must not be empty";
      log.debug(errormessage);
      context.buildConstraintViolationWithTemplate(errormessage)
             .addPropertyNode("keystoreFile")
             .addConstraintViolation();
      return false;
    }
    try
    {
      final byte[] keystoreBytes = keystoreUploadForm.getKeystoreFile().getBytes();
      final String fileName = keystoreUploadForm.getKeystoreFile().getOriginalFilename();
      final KeyStoreSupporter.KeyStoreType keyStoreType = KeyStoreSupporter.KeyStoreType.byFileExtension(fileName)
                                                                                        .orElse(KeyStoreSupporter.KeyStoreType.JKS);
      final String password = keystoreUploadForm.getKeystorePassword();
      KeyStoreSupporter.readKeyStore(keystoreBytes, keyStoreType, password);
    }
    catch (Exception ex)
    {
      Throwable current = ex;
      while (current != null)
      {
        String errormessage = Optional.ofNullable(current.getMessage()).orElse("NullPointerException");
        if (errormessage.contains("exception unwrapping private key"))
        {
          String errormessage2 = "key-passwords must match keystore-password. This is a restriction of the "
                                 + "BouncyCastle PKCS12 implementation. As a workaround rename the file extension "
                                 + "to '.jks' this may work based on the used JDK";
          log.debug(errormessage2);
          context.buildConstraintViolationWithTemplate(errormessage2)
                 .addPropertyNode("keystoreFile")
                 .addConstraintViolation();
        }
        log.debug(errormessage);
        context.buildConstraintViolationWithTemplate(errormessage)
               .addPropertyNode("keystoreFile")
               .addConstraintViolation();
        current = current.getCause();
      }
      isValid = false;
    }

    return isValid;
  }
}
