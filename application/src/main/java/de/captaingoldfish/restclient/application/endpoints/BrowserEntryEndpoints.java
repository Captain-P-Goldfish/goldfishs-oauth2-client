package de.captaingoldfish.restclient.application.endpoints;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


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
