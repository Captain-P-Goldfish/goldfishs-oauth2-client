package de.captaingoldfish.oauthrestclient.application.endpoints.keystore;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

import de.captaingoldfish.oauthrestclient.application.projectconfig.WebAppConfig;
import de.captaingoldfish.oauthrestclient.database.entities.Keystore;
import de.captaingoldfish.oauthrestclient.database.repositories.KeystoreDao;
import lombok.extern.slf4j.Slf4j;


/**
 * validates the content of {@link KeystoreDeleteEntryForm}
 * 
 * @author Pascal Knueppel
 * @since 31.03.2021
 */
@Slf4j
public class KeystoreDeleteEntryValidator
  implements ConstraintValidator<KeystoreDeleteEntryValidation, KeystoreDeleteEntryForm>
{

  /**
   * checks that the data in the {@link KeystoreDeleteEntryForm} is valid and is able to produce valid results
   * during further processing
   */
  @Override
  public boolean isValid(KeystoreDeleteEntryForm form, ConstraintValidatorContext context)
  {
    boolean isValid = true;
    if (StringUtils.isBlank(form.getAlias()))
    {
      String errorMessage = "Required parameter 'alias' is missing in request";
      log.debug(errorMessage);
      context.buildConstraintViolationWithTemplate(errorMessage).addPropertyNode("alias").addConstraintViolation();
      return false;
    }
    KeystoreDao keystoreDao = WebAppConfig.getApplicationContext().getBean(KeystoreDao.class);
    Keystore keystore = keystoreDao.getKeystore();
    boolean hasAlias = keystore.getKeyStoreAliases().contains(form.getAlias());
    if (!hasAlias)
    {
      String errorMessage = "Unknown alias '" + form.getAlias() + "'";
      log.debug(errorMessage);
      context.buildConstraintViolationWithTemplate(errorMessage).addPropertyNode("alias").addConstraintViolation();
      isValid = false;
    }
    return isValid;
  }
}
