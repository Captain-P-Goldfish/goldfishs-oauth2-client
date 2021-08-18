package de.captaingoldfish.restclient.application.endpoints;

import org.springframework.web.util.UriComponentsBuilder;

import de.captaingoldfish.scim.sdk.server.endpoints.Context;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * @author Pascal Knueppel
 * @since 18.08.2021
 */
@RequiredArgsConstructor
public class ScimRequestContext extends Context
{

  /**
   * can be used to build relevant urls to the application
   */
  @Getter
  private final UriComponentsBuilder uriComponentsBuilder;

}
