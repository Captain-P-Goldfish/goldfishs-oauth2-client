package de.captaingoldfish.restclient.application.endpoints.tokenrequest;

import java.util.UUID;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriComponentsBuilder;

import de.captaingoldfish.restclient.application.endpoints.BrowserEntryController;
import de.captaingoldfish.restclient.application.endpoints.provider.TestIdentityProvider;
import de.captaingoldfish.restclient.application.setup.AbstractScimClientConfig;
import de.captaingoldfish.restclient.application.setup.OAuthRestClientTest;
import de.captaingoldfish.restclient.application.utils.OAuthConstants;
import de.captaingoldfish.restclient.database.entities.HttpClientSettings;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;
import de.captaingoldfish.restclient.database.entities.OpenIdProvider;
import de.captaingoldfish.restclient.scim.resources.ScimAccessTokenRequest;
import de.captaingoldfish.restclient.scim.resources.ScimAccessTokenRequest.HttpHeaders;
import de.captaingoldfish.restclient.scim.resources.ScimAccessTokenRequest.RequestHeaders;
import de.captaingoldfish.restclient.scim.resources.ScimAccessTokenRequest.RequestParams;
import de.captaingoldfish.scim.sdk.client.response.ServerResponse;
import de.captaingoldfish.scim.sdk.common.constants.HttpStatus;
import lombok.extern.slf4j.Slf4j;


/**
 * @author Pascal Knueppel
 * @since 21.08.2021
 */
@Slf4j
@OAuthRestClientTest
public class AccessTokenRequestHandlerTest extends AbstractScimClientConfig
{

  /**
   * the scim endpoint for the access token request endpoint
   */
  private static final String ACCESS_TOKEN_REQUEST_ENDPOINT = "/AccessTokenRequest";

  /**
   * the discovery endpoint from the {@link TestIdentityProvider}
   */
  private String discoveryUrl;

  @BeforeEach
  public void initialize()
  {
    discoveryUrl = getApplicationUrl(TestIdentityProvider.IDP_PATH + TestIdentityProvider.DISCOVERY_ENDPOINT);
  }

  @Test
  public void testAccessTokenRequestHandlerTest()
  {
    // prepare
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getApplicationUrl());
    OpenIdProvider openIdProvider = OpenIdProvider.builder().discoveryEndpoint(discoveryUrl).build();
    openIdProvider = openIdProviderDao.save(openIdProvider);
    OpenIdClient openIdClient = OpenIdClient.builder()
                                            .openIdProvider(openIdProvider)
                                            .authenticationType("basic")
                                            .clientId("goldfish")
                                            .clientSecret("123456")
                                            .build();
    openIdClient = openIdClientDao.save(openIdClient);
    HttpClientSettings httpClientSettings = HttpClientSettings.builder().openIdClient(openIdClient).build();
    httpClientSettingsDao.save(httpClientSettings);

    // do
    String authorizationCode = UUID.randomUUID().toString();
    String redirectUri = BrowserEntryController.getAuthorizationCodeEntryPoint(uriComponentsBuilder);
    ScimAccessTokenRequest scimAccessTokenRequest = ScimAccessTokenRequest.builder()
                                                                          .openIdClientId(openIdClient.getId())
                                                                          .grantType(OAuthConstants.AUTH_CODE_GRANT_TYPE)
                                                                          .authorizationCode(authorizationCode)
                                                                          .redirectUri(redirectUri)
                                                                          .build();
    ServerResponse<ScimAccessTokenRequest> response = scimRequestBuilder.create(ScimAccessTokenRequest.class,
                                                                                ACCESS_TOKEN_REQUEST_ENDPOINT)
                                                                        .setResource(scimAccessTokenRequest)
                                                                        .sendRequest();

    // then
    Assertions.assertEquals(HttpStatus.CREATED, response.getHttpStatus());
    ScimAccessTokenRequest accessTokenRequest = response.getResource();
    // validate request headers
    {
      Assertions.assertEquals(2, accessTokenRequest.getRequestHeaders().size());
      RequestHeaders authorization = accessTokenRequest.getRequestHeaders().get(0);
      Assertions.assertEquals(org.apache.http.HttpHeaders.AUTHORIZATION, authorization.getName());
      Assertions.assertEquals("Basic Z29sZGZpc2g6MTIzNDU2", authorization.getValue());
      RequestHeaders contentType = accessTokenRequest.getRequestHeaders().get(1);
      Assertions.assertEquals(org.apache.http.HttpHeaders.CONTENT_TYPE, contentType.getName());
      Assertions.assertEquals(MediaType.APPLICATION_FORM_URLENCODED_VALUE, contentType.getValue());
    }
    // validate request parameters
    {
      Assertions.assertEquals(4, accessTokenRequest.getRequestParams().size());
      RequestParams code = accessTokenRequest.getRequestParams().get(0);
      Assertions.assertEquals(OAuthConstants.CODE, code.getName());
      Assertions.assertEquals(authorizationCode, code.getValue());
      RequestParams grantType = accessTokenRequest.getRequestParams().get(1);
      Assertions.assertEquals("grant_type", grantType.getName());
      Assertions.assertEquals(OAuthConstants.AUTH_CODE_GRANT_TYPE, grantType.getValue());
      RequestParams redirectUriParam = accessTokenRequest.getRequestParams().get(2);
      Assertions.assertEquals(OAuthConstants.REDIRECT_URI, redirectUriParam.getName());
      Assertions.assertEquals(redirectUri, redirectUriParam.getValue());
      RequestParams clientIdParam = accessTokenRequest.getRequestParams().get(3);
      Assertions.assertEquals(OAuthConstants.CLIENT_ID, clientIdParam.getName());
      Assertions.assertEquals(openIdClient.getClientId(), clientIdParam.getValue());
    }
    // validate response code from idp
    Assertions.assertEquals(HttpStatus.OK, accessTokenRequest.getStatusCode());
    // validate response headers
    {
      HttpHeaders contentType = accessTokenRequest.getResponseHeaders()
                                                  .stream()
                                                  .filter(header -> header.getName()
                                                                          .equals(org.apache.http.HttpHeaders.CONTENT_TYPE))
                                                  .findAny()
                                                  .orElseThrow();
      MatcherAssert.assertThat(contentType.getValue(),
                               Matchers.containsString(TestIdentityProvider.accessTokenResponseContentTypeSupplier.get()));
    }
    // validate idp response
    Assertions.assertDoesNotThrow(() -> UUID.fromString(accessTokenRequest.getPlainResponse()));
  }
}
