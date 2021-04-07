package de.captaingoldfish.oauthrestclient.application.endpoints.proxy.forms;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

import de.captaingoldfish.oauthrestclient.application.projectconfig.WebAppConfig;
import de.captaingoldfish.oauthrestclient.database.repositories.ProxyDao;
import lombok.extern.slf4j.Slf4j;


/**
 * validates the content of {@link ProxyDeleteForm}
 * 
 * @author Pascal Knueppel
 * @since 07.04.2021
 */
@Slf4j
public class ProxyDeleteValidator implements ConstraintValidator<ProxyDeleteValidation, ProxyDeleteForm>
{

  /**
   * checks that the data in the {@link ProxyDeleteForm} is valid and is able to produce valid results during
   * further processing
   */
  @Override
  public boolean isValid(ProxyDeleteForm proxyForm, ConstraintValidatorContext context)
  {
    boolean isValid = true;


    if (StringUtils.isBlank(proxyForm.getId()))
    {
      context.buildConstraintViolationWithTemplate("Cannot delete proxy for empty ID").addConstraintViolation();
      return false;
    }

    Long id = null;
    try
    {
      id = Long.parseLong(proxyForm.getId());
    }
    catch (NumberFormatException ex)
    {
      context.buildConstraintViolationWithTemplate("Cannot delete proxy for illegal ID '" + proxyForm.getId() + "'")
             .addConstraintViolation();
      isValid = false;
    }

    ProxyDao proxyDao = WebAppConfig.getApplicationContext().getBean(ProxyDao.class);
    if (id != null && proxyDao.findById(id).orElse(null) == null)
    {
      context.buildConstraintViolationWithTemplate("Cannot delete proxy for entry with ID '" + proxyForm.getId()
                                                   + "' does not exist")
             .addConstraintViolation();
      isValid = false;
    }

    return isValid;
  }
}
