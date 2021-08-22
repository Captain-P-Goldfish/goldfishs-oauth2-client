package de.captaingoldfish.restclient.application.endpoints.tokenrequest.request;

import java.util.Map;


/**
 * @author Pascal Knueppel
 * @since 21.08.2021
 */
public interface Authenticator
{

  /**
   * @return some optional request header values that might be necessary for authentication
   */
  public Map<String, String> getRequestHeader();

  /**
   * @return some request parameters that might be necessary for authentication
   */
  public Map<String, String> getRequestParameter();

}
