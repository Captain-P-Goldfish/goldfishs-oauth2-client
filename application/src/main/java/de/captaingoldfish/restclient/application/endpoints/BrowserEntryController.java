package de.captaingoldfish.restclient.application.endpoints;

import java.io.StringReader;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import de.captaingoldfish.restclient.application.endpoints.authcodegrant.AuthCodeGrantRequestService;
import de.captaingoldfish.scim.sdk.common.constants.HttpStatus;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;


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
   * redirects to load the view
   */
  @GetMapping("/")
  public ModelAndView redirectToLoadViews()
  {
    return new ModelAndView("redirect:/views");
  }

  /**
   * loads the main page of this project
   */
  @GetMapping("/views/**")
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
  public ModelAndView acceptAuthorizationCode(HttpServletRequest request, HttpServletResponse response)
  {
    try
    {
      final String query = request.getQueryString() == null ? "" : "?" + request.getQueryString();
      final String fullRequestUrl = request.getRequestURL().toString() + query;
      authCodeGrantRequestService.handleAuthorizationResponse(fullRequestUrl);
      return new ModelAndView("auth-success.html");
    }
    catch (Exception ex)
    {
      response.setStatus(HttpStatus.BAD_REQUEST);
      ModelAndView modelAndView = new ModelAndView("auth-success.html");
      modelAndView.addObject("error", ex.getMessage());
      return modelAndView;
    }
  }

  @SneakyThrows
  @PostMapping("/pretty-print-xml")
  public @ResponseBody String prettyPrintXml(@RequestBody String xml, HttpServletResponse response)
  {
    try
    {
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      StreamResult result = new StreamResult(new StringWriter());
      DOMSource source = new DOMSource(parseXmlFile(xml));
      transformer.transform(source, result);
      return result.getWriter().toString();
    }
    catch (Exception ex)
    {
      response.setStatus(HttpStatus.BAD_REQUEST);
      return ex.getMessage();
    }
  }

  @SneakyThrows
  private Document parseXmlFile(String xml)
  {
    String preparedXml = xml.replaceAll("<!.*?>", "");
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    InputSource is = new InputSource(new StringReader(preparedXml));
    return db.parse(is);
  }
}
