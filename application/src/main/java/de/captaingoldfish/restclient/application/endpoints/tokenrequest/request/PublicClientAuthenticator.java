package de.captaingoldfish.restclient.application.endpoints.tokenrequest.request;

import java.util.HashMap;
import java.util.Map;


/**
 * @author Pascal Knueppel
 * @since 31.05.2024
 */
public class PublicClientAuthenticator implements Authenticator
{

  @Override
  public Map<String, String> getRequestHeader()
  {
    return new HashMap<>();
  }

  @Override
  public Map<String, String> getRequestParameter()
  {
    return new HashMap<>();
  }
}
