package de.captaingoldfish.restclient.application.exceptions;

import org.springframework.validation.BindingResult;

import lombok.Getter;


/**
 * used in case of bean validation errors
 * 
 * @author Pascal Knueppel
 * @since 27.03.2021
 */
public class RequestException extends RuntimeException
{

  /**
   * the http status that should be returned to the react application
   */
  @Getter
  private final int status;

  /**
   * contains the field validation errors
   */
  @Getter
  private final BindingResult bindingResult;

  public RequestException(String message, int status, BindingResult responseObject)
  {
    super(message);
    this.status = status;
    this.bindingResult = responseObject;
  }
}
