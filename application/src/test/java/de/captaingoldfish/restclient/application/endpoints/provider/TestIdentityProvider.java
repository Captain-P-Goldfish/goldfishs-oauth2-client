package de.captaingoldfish.restclient.application.endpoints.provider;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Supplier;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderConfigurationRequest;

import de.captaingoldfish.restclient.application.setup.FileReferences;
import de.captaingoldfish.scim.sdk.common.constants.HttpStatus;
import de.captaingoldfish.scim.sdk.common.utils.JsonHelper;


/**
 * @author Pascal Knueppel
 * @since 20.08.2021
 */
@RestController
@RequestMapping(TestIdentityProvider.IDP_PATH)
public class TestIdentityProvider implements FileReferences
{

  public static final String IDP_PATH = "/test-idp";

  public static final String DISCOVERY_ENDPOINT = OIDCProviderConfigurationRequest.OPENID_PROVIDER_WELL_KNOWN_PATH;

  public static final String AUTHORIZATION_ENDPOINT = "/auth";

  public static final String TOKEN_ENDPOINT = "/token";

  public static Supplier<Integer> accessTokenResponseStatusSupplier = () -> HttpStatus.OK;

  public static Supplier<String> accessTokenResponseSupplier = () -> UUID.randomUUID().toString();

  public static Supplier<String> accessTokenResponseContentTypeSupplier = () -> MediaType.TEXT_PLAIN;

  public static void resetAccessTokenResponseSupplier()
  {
    accessTokenResponseStatusSupplier = () -> HttpStatus.OK;
    accessTokenResponseSupplier = () -> UUID.randomUUID().toString();
    accessTokenResponseContentTypeSupplier = () -> MediaType.TEXT_PLAIN;
  }

  @GetMapping(value = DISCOVERY_ENDPOINT, produces = MediaType.APPLICATION_JSON)
  public String discoveryEndpoint(UriComponentsBuilder uriComponentsBuilder)
  {
    String discoveryJson = new String(readAsBytes(TEST_IDP_OIDC_DISCOVERY_JSON), StandardCharsets.UTF_8);
    ObjectNode discoveryJsonNode = (ObjectNode)JsonHelper.readJsonDocument(discoveryJson);

    String authEndpoint = uriComponentsBuilder.cloneBuilder()
                                              .path(IDP_PATH + AUTHORIZATION_ENDPOINT)
                                              .build()
                                              .toString();
    discoveryJsonNode.set("authorization_endpoint", new TextNode(authEndpoint));

    String tokenEndpoint = uriComponentsBuilder.cloneBuilder().path(IDP_PATH + TOKEN_ENDPOINT).build().toString();
    discoveryJsonNode.set("token_endpoint", new TextNode(tokenEndpoint));

    return discoveryJsonNode.toString();
  }

  @GetMapping(AUTHORIZATION_ENDPOINT)
  public RedirectView authorizationEndpoint(@RequestParam(name = "redirect_uri") String redirectUri,
                                            @RequestParam(name = "state") String state)
  {
    return new RedirectView(String.format("%s?code=%s&state=%s", redirectUri, UUID.randomUUID(), state));
  }

  @PostMapping(TOKEN_ENDPOINT)
  public String tokenEndpoint(HttpServletResponse response)
  {
    response.setStatus(accessTokenResponseStatusSupplier.get());
    response.setContentType(accessTokenResponseContentTypeSupplier.get());
    String accessToken = accessTokenResponseSupplier.get();
    resetAccessTokenResponseSupplier();
    return accessToken;
  }

}
