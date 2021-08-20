package de.captaingoldfish.restclient.application.endpoints;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponentsBuilder;


/**
 * @author Pascal Knueppel
 * @since 18.08.2021
 */
@Controller
@RequestMapping("/")
public class BrowserEntryEndpoints
{

  /**
   * describes the endpoint where authorization codes for OpenID Connect are accepted
   */
  public static final String AUTH_CODE_ENDPOINT = "/authcode";

  /**
   * @param uriComponentsBuilder the uri builder to user that should be preconfigured with the url
   * @return the url to the authorization code response endpoint
   */
  public static String getAuthorizationCodeEntryPoint(UriComponentsBuilder uriComponentsBuilder)
  {
    return uriComponentsBuilder.cloneBuilder().path(BrowserEntryEndpoints.AUTH_CODE_ENDPOINT).build().toString();
  }

  /**
   * the endpoint that accepts OpenID Connect authorization codes.
   * 
   * @return a note for the user to return to the main-window
   */
  @GetMapping
  @RequestMapping(AUTH_CODE_ENDPOINT)
  public @ResponseBody String acceptAuthorizationCode()
  {
    return "Authorization Code accepted. Please return to the main-window";
  }
}
