package de.captaingoldfish.restclient.application.oidc;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import lombok.Getter;


/**
 * @author Pascal Knueppel
 * @since 10.06.2021
 */
@Getter
public class AuthorizationCodeResponseHandler
{

  /**
   * the authorization code from an OpenID Provider
   */
  private final String authorizationCode;

  /**
   * the optional state parameter from the request
   */
  private final String state;

  /**
   * the error code if the authorization request has failed
   */
  private final String error;

  /**
   * a human readable error description
   */
  private final String errorDescription;

  public AuthorizationCodeResponseHandler(HttpServletRequest httpServletRequest)
  {
    this.authorizationCode = httpServletRequest.getParameter("code");
    this.state = httpServletRequest.getParameter("state");
    this.error = httpServletRequest.getParameter("error");
    this.errorDescription = httpServletRequest.getParameter("error_description");
  }

  /**
   * @return true if the authorization code is present
   */
  public boolean isSuccess()
  {
    return Optional.ofNullable(authorizationCode).isPresent();
  }
}
