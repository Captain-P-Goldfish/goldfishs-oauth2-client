package de.captaingoldfish.oauthrestclient.application.endpoints.proxy.forms;

import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;


/**
 * @author Pascal Knueppel
 * @since 07.04.2021
 */
public abstract class AbstractProxySaveValidator
{

  public boolean validateIsSavable(String host, String proxyPort, ConstraintValidatorContext context)
  {
    boolean isValid = true;
    if (StringUtils.isBlank(host))
    {
      context.buildConstraintViolationWithTemplate("Hostname must not be blank")
             .addPropertyNode("host")
             .addConstraintViolation();
      isValid = false;
    }
    if (StringUtils.isNotBlank(proxyPort))
    {
      Integer port = null;
      try
      {
        port = Integer.parseInt(proxyPort);
      }
      catch (NumberFormatException ex)
      {
        context.buildConstraintViolationWithTemplate("Port is not a number")
               .addPropertyNode("port")
               .addConstraintViolation();
        isValid = false;
      }
      if (port == null || (port < 1 || port > 65535))
      {
        context.buildConstraintViolationWithTemplate("Port must be within range of '1' and '65535'")
               .addPropertyNode("port")
               .addConstraintViolation();
        isValid = false;
      }
    }
    else
    {
      context.buildConstraintViolationWithTemplate("Port must not be blank and within range of '1' and '65535'")
             .addPropertyNode("port")
             .addConstraintViolation();
      isValid = false;
    }
    return isValid;
  }
}
