package de.captaingoldfish.restclient.application.endpoints;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponentsBuilder;

import de.captaingoldfish.restclient.application.endpoints.authcodegrant.AuthCodeGrantRequestService;
import de.captaingoldfish.scim.sdk.common.constants.HttpStatus;
import lombok.RequiredArgsConstructor;


/**
 * @author Pascal Knueppel
 * @since 18.08.2021
 */
@RequiredArgsConstructor
@Controller
@RequestMapping("/")
public class BrowserEntryController
{

  /**
   * describes the endpoint where authorization codes for OpenID Connect are accepted
   */
  public static final String AUTH_CODE_ENDPOINT = "/authcode";

  /**
   * used to handle the authorization code grant response
   */
  private final AuthCodeGrantRequestService authCodeGrantRequestService;

  /**
   * @param uriComponentsBuilder the uri builder to user that should be preconfigured with the url
   * @return the url to the authorization code response endpoint
   */
  public static String getAuthorizationCodeEntryPoint(UriComponentsBuilder uriComponentsBuilder)
  {
    return uriComponentsBuilder.cloneBuilder().path(BrowserEntryController.AUTH_CODE_ENDPOINT).build().toString();
  }

  /**
   * loads the main page of this project
   */
  @GetMapping
  public ModelAndView loadIndex()
  {
    return new ModelAndView("index");
  }

  /**
   * the endpoint that accepts OpenID Connect authorization codes.
   * 
   * @return a note for the user to return to the main-window
   */
  @GetMapping
  @RequestMapping(value = AUTH_CODE_ENDPOINT, produces = "text/html")
  public @ResponseBody String acceptAuthorizationCode(HttpServletRequest request, HttpServletResponse response)
  {
    try
    {
      final String query = request.getQueryString() == null ? "" : "?" + request.getQueryString();
      final String fullRequestUrl = request.getRequestURL().toString() + query;
      authCodeGrantRequestService.handleAuthorizationResponse(fullRequestUrl);
      return "<html><body><h3>Authorization Code accepted. Please return to the main-window</h3></body></html>";
    }
    catch (Exception ex)
    {
      response.setStatus(HttpStatus.BAD_REQUEST);
      return String.format("<html><body><span style='color: \"red\"'>%s</span></body></html>", ex.getMessage());
    }
  }
}
