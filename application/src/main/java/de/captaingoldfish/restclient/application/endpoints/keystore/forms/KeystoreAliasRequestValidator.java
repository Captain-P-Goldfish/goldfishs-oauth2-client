package de.captaingoldfish.restclient.application.endpoints.keystore.forms;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

import de.captaingoldfish.restclient.application.projectconfig.WebAppConfig;
import de.captaingoldfish.restclient.database.entities.Keystore;
import de.captaingoldfish.restclient.database.repositories.KeystoreDao;
import lombok.extern.slf4j.Slf4j;


/**
 * validates the content of {@link KeystoreAliasRequestForm}
 * 
 * @author Pascal Knueppel
 * @since 31.03.2021
 */
@Slf4j
public class KeystoreAliasRequestValidator
  implements ConstraintValidator<KeystoreAliasRequestValidation, KeystoreAliasRequestForm>
{

  /**
   * checks that the data in the {@link KeystoreAliasRequestForm} is valid and is able to produce valid results
   * during further processing
   */
  @Override
  public boolean isValid(KeystoreAliasRequestForm form, ConstraintValidatorContext context)
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
