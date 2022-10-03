package de.captaingoldfish.restclient.application.endpoints;

import java.util.Map;
import java.util.Set;

import org.springframework.web.util.UriComponentsBuilder;

import de.captaingoldfish.scim.sdk.server.endpoints.Context;
import de.captaingoldfish.scim.sdk.server.endpoints.authorize.Authorization;
import lombok.Getter;


/**
 * @author Pascal Knueppel
 * @since 18.08.2021
 */
public class ScimRequestContext extends Context
{

  /**
   * can be used to build relevant urls to the application
   */
  @Getter
  private final UriComponentsBuilder uriComponentsBuilder;

  public ScimRequestContext(UriComponentsBuilder uriComponentsBuilder)
  {
    super(new Authorization()
    {

      @Override
      public Set<String> getClientRoles()
      {
        return null;
      }

      @Override
      public boolean authenticate(Map<String, String> map, Map<String, String> map1)
      {
        return true;
      }
    });
    this.uriComponentsBuilder = uriComponentsBuilder;
  }
}
