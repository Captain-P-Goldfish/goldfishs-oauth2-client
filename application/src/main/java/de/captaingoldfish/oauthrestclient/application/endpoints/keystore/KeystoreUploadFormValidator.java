package de.captaingoldfish.oauthrestclient.application.endpoints.keystore;

import java.util.Optional;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

import de.captaingoldfish.oauthrestclient.commons.keyhelper.KeyStoreSupporter;
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
      context.buildConstraintViolationWithTemplate("Not accepting empty passwords")
             .addPropertyNode("keystorePassword")
             .addConstraintViolation();
      isValid = false;
    }

    if (keystoreUploadForm.getKeystoreFile() == null || keystoreUploadForm.getKeystoreFile().isEmpty())
    {
      context.buildConstraintViolationWithTemplate("Keystore file must not be empty")
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
        context.buildConstraintViolationWithTemplate(Optional.ofNullable(current.getMessage())
                                                             .orElse("NullPointerException"))
               .addPropertyNode("keystoreFile")
               .addConstraintViolation();
        current = current.getCause();
      }
      isValid = false;
    }

    return isValid;
  }
}
