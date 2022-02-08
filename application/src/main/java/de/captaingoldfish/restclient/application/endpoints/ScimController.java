package de.captaingoldfish.restclient.application.endpoints;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import de.captaingoldfish.scim.sdk.common.constants.HttpHeader;
import de.captaingoldfish.scim.sdk.common.constants.enums.HttpMethod;
import de.captaingoldfish.scim.sdk.common.response.ScimResponse;
import de.captaingoldfish.scim.sdk.server.endpoints.ResourceEndpoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * @author Pascal Knueppel
 * @since 09.05.2021
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/scim/v2")
public class ScimController
{

  /**
   * the resource endpoint that handles ALL SCIM requests
   */
  private final ResourceEndpoint resourceEndpoint;

  /**
   * the rest-endpoint for SCIM that is accessible under the path ${basepath}/scim/v2/**
   *
   * @param request the request object created by the underlying tomcat
   * @param requestBody the request body
   * @return the scim response that will automatically be converted to json by spring
   */
  @Transactional
  @RequestMapping(value = "/**", method = {RequestMethod.POST, RequestMethod.GET, RequestMethod.PUT,
                                           RequestMethod.PATCH,
                                           RequestMethod.DELETE}, produces = HttpHeader.SCIM_CONTENT_TYPE)
  public @ResponseBody String handleScimRequest(HttpServletRequest request,
                                                HttpServletResponse response,
                                                UriComponentsBuilder uriComponentsBuilder,
                                                @RequestBody(required = false) String requestBody)
  {
    Map<String, String> httpHeaders = getHttpHeaders(request);
    String query = request.getQueryString() == null ? "" : "?" + request.getQueryString();
    ScimResponse scimResponse = resourceEndpoint.handleRequest(request.getRequestURL().toString() + query,
                                                               HttpMethod.valueOf(request.getMethod()),
                                                               requestBody,
                                                               httpHeaders,
                                                               new ScimRequestContext(uriComponentsBuilder));
    response.setContentType(HttpHeader.SCIM_CONTENT_TYPE);
    scimResponse.getHttpHeaders().forEach(response::setHeader);
    response.setStatus(scimResponse.getHttpStatus());
    return scimResponse.toPrettyString();
  }

  /**
   * extracts the http headers from the request and puts them into a map
   *
   * @param request the request object
   * @return a map with the http-headers
   */
  private Map<String, String> getHttpHeaders(HttpServletRequest request)
  {
    Map<String, String> httpHeaders = new HashMap<>();
    Enumeration<String> enumeration = request.getHeaderNames();
    while (enumeration != null && enumeration.hasMoreElements())
    {
      String headerName = enumeration.nextElement();
      httpHeaders.put(headerName, request.getHeader(headerName));
    }
    return httpHeaders;
  }
}
