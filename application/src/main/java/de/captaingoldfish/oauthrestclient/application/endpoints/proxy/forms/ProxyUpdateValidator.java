package de.captaingoldfish.oauthrestclient.application.endpoints.proxy.forms;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

import de.captaingoldfish.oauthrestclient.application.projectconfig.WebAppConfig;
import de.captaingoldfish.oauthrestclient.database.repositories.ProxyDao;
import lombok.extern.slf4j.Slf4j;


/**
 * validates the content of {@link ProxyUpdateForm}
 * 
 * @author Pascal Knueppel
 * @since 07.04.2021
 */
@Slf4j
public class ProxyUpdateValidator extends AbstractProxySaveValidator
  implements ConstraintValidator<ProxyUpdateValidation, ProxyUpdateForm>
{

  /**
   * checks that the data in the {@link ProxyUpdateForm} is valid and is able to produce valid results during
   * further processing
   */
  @Override
  public boolean isValid(ProxyUpdateForm proxyForm, ConstraintValidatorContext context)
  {
    boolean isValid = true;


    if (StringUtils.isBlank(proxyForm.getId()))
    {
      context.buildConstraintViolationWithTemplate("Cannot update proxy for empty ID").addConstraintViolation();
      isValid = false;
    }

    isValid = isValid && validateIsSavable(proxyForm.getHost(), proxyForm.getPort(), context);

    if (!isValid)
    {
      return false;
    }

    ProxyDao proxyDao = WebAppConfig.getApplicationContext().getBean(ProxyDao.class);
    long id = Long.parseLong(proxyForm.getId());
    if (proxyDao.findById(id).orElse(null) == null)
    {
      context.buildConstraintViolationWithTemplate("Cannot update proxy for entry with ID '" + proxyForm.getId()
                                                   + "' does not exist")
             .addConstraintViolation();
      isValid = false;
    }

    return isValid;
  }
}
