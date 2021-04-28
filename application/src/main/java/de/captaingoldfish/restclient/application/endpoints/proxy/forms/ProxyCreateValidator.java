package de.captaingoldfish.restclient.application.endpoints.proxy.forms;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import lombok.extern.slf4j.Slf4j;


/**
 * validates the content of {@link ProxyCreateForm}
 * 
 * @author Pascal Knueppel
 * @since 07.04.2021
 */
@Slf4j
public class ProxyCreateValidator extends AbstractProxySaveValidator
  implements ConstraintValidator<ProxyCreateValidation, ProxyCreateForm>
{

  /**
   * checks that the data in the {@link ProxyCreateForm} is valid and is able to produce valid results during
   * further processing
   */
  @Override
  public boolean isValid(ProxyCreateForm proxyForm, ConstraintValidatorContext context)
  {
    return validateIsSavable(proxyForm.getHost(), proxyForm.getPort(), context);
  }
}
