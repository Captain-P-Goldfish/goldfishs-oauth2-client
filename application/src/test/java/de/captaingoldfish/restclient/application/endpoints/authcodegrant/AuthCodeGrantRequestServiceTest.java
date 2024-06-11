package de.captaingoldfish.restclient.application.endpoints.authcodegrant;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import de.captaingoldfish.restclient.application.endpoints.BrowserEntryController;
import de.captaingoldfish.restclient.application.endpoints.provider.TestIdentityProvider;
import de.captaingoldfish.restclient.application.setup.AbstractScimClientConfig;
import de.captaingoldfish.restclient.application.setup.OAuthRestClientTest;
import de.captaingoldfish.restclient.application.utils.OAuthConstants;
import de.captaingoldfish.restclient.database.entities.HttpClientSettings;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;
import de.captaingoldfish.restclient.database.entities.OpenIdProvider;
import de.captaingoldfish.restclient.scim.resources.ScimCurrentWorkflowSettings;
import de.captaingoldfish.restclient.scim.resources.ScimCurrentWorkflowSettings.AuthCodeParameters;
import lombok.extern.slf4j.Slf4j;


/**
 * @author Pascal Knueppel
 * @since 20.08.2021
 */
@Slf4j
@OAuthRestClientTest
public class AuthCodeGrantRequestServiceTest extends AbstractScimClientConfig
{

  /**
   * the discovery endpoint from the {@link TestIdentityProvider}
   */
  private String discoveryUrl;

  /**
   * bean under test
   */
  @Autowired
  private AuthCodeGrantRequestService authCodeGrantRequestService;

  /**
   * to check that the request urls are correctly stored within the cache
   */
  @Autowired
  private AuthCodeGrantRequestCache authCodeGrantRequestCache;

  @BeforeEach
  public void initialize()
  {
    discoveryUrl = getApplicationUrl(TestIdentityProvider.IDP_PATH + TestIdentityProvider.DISCOVERY_ENDPOINT);
  }

  /**
   * verifies that the authorization code request is correctly setup if no additional query parameters were
   * added
   */
  @Test
  public void testGenerateAuthCodeRequestUrlWithoutAdditionalQuery()
  {
    // prepare
    OpenIdProvider openIdProvider = OpenIdProvider.builder().discoveryEndpoint(discoveryUrl).build();
    openIdProvider = openIdProviderDao.save(openIdProvider);

    OpenIdClient openIdClient = OpenIdClient.builder()
                                            .openIdProvider(openIdProvider)
                                            .authenticationType("basic")
                                            .clientId("goldfish")
                                            .clientSecret(UUID.randomUUID().toString())
                                            .build();
    openIdClient = openIdClientDao.save(openIdClient);
    HttpClientSettings httpClientSettings = HttpClientSettings.builder().openIdClient(openIdClient).build();
    httpClientSettingsDao.save(httpClientSettings);

    // do
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getApplicationUrl());
    String redirectUri = BrowserEntryController.getAuthorizationCodeEntryPoint(uriComponentsBuilder);
    AuthCodeParameters authCodeParameters = AuthCodeParameters.builder().redirectUri(redirectUri).build();
    ScimCurrentWorkflowSettings workflowSettings = ScimCurrentWorkflowSettings.builder()
                                                                              .openIdClientId(openIdClient.getId())
                                                                              .authCodeParameters(authCodeParameters)
                                                                              .build();
    Pair<String, String> authorizationCodeRequestUrl = //
      authCodeGrantRequestService.generateAuthCodeRequestUrl(openIdClient, workflowSettings);

    String expectedAuthorizationUrl = String.format("%s?response_type=%s&client_id=%s&redirect_uri=%s&state=",
                                                    getApplicationUrl(TestIdentityProvider.IDP_PATH
                                                                      + TestIdentityProvider.AUTHORIZATION_ENDPOINT),
                                                    OAuthConstants.CODE,
                                                    openIdClient.getClientId(),
                                                    URLEncoder.encode(redirectUri, StandardCharsets.UTF_8));
    expectedAuthorizationUrl = Pattern.quote(expectedAuthorizationUrl) + ".*";
    MatcherAssert.assertThat(authorizationCodeRequestUrl.getLeft(), Matchers.matchesPattern(expectedAuthorizationUrl));

    UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(authorizationCodeRequestUrl.getLeft()).build();
    final String state = uriComponents.getQueryParams().getFirst(OAuthConstants.STATE);
    Assertions.assertNotNull(authCodeGrantRequestCache.getAuthorizationRequestUrl(state));
  }

  /**
   * verifies that the authorization code request is correctly setup if additional query parameters were added
   * that are also part of the required attributes set
   */
  @Test
  public void testGenerateAuthCodeRequestUrlWithAdditionalQuery()
  {
    // prepare
    OpenIdProvider openIdProvider = OpenIdProvider.builder().discoveryEndpoint(discoveryUrl).build();
    openIdProvider = openIdProviderDao.save(openIdProvider);

    OpenIdClient openIdClient = OpenIdClient.builder()
                                            .openIdProvider(openIdProvider)
                                            .authenticationType("basic")
                                            .clientId("goldfish")
                                            .clientSecret(UUID.randomUUID().toString())
                                            .build();
    openIdClient = openIdClientDao.save(openIdClient);
    HttpClientSettings httpClientSettings = HttpClientSettings.builder().openIdClient(openIdClient).build();
    httpClientSettingsDao.save(httpClientSettings);

    // do
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getApplicationUrl());
    String redirectUri = BrowserEntryController.getAuthorizationCodeEntryPoint(uriComponentsBuilder);
    final String client_id = "test-me";
    final String state = "some-state-param";
    final String queryParameters = String.format("client_id=%s&state=%s", client_id, state);
    AuthCodeParameters authCodeParameters = AuthCodeParameters.builder()
                                                              .redirectUri(redirectUri)
                                                              .queryParameters(queryParameters)
                                                              .build();
    ScimCurrentWorkflowSettings workflowSettings = ScimCurrentWorkflowSettings.builder()
                                                                              .openIdClientId(openIdClient.getId())
                                                                              .authCodeParameters(authCodeParameters)
                                                                              .build();
    Pair<String, String> authorizationCodeRequestUrl = //
      authCodeGrantRequestService.generateAuthCodeRequestUrl(openIdClient, workflowSettings);

    String expectedAuthorizationUrl = String.format("%s?client_id=%s&state=%s&response_type=%s&redirect_uri=%s",
                                                    getApplicationUrl(TestIdentityProvider.IDP_PATH
                                                                      + TestIdentityProvider.AUTHORIZATION_ENDPOINT),
                                                    client_id,
                                                    state,
                                                    OAuthConstants.CODE,
                                                    URLEncoder.encode(redirectUri, StandardCharsets.UTF_8));
    Assertions.assertEquals(expectedAuthorizationUrl, authorizationCodeRequestUrl.getLeft());
    Assertions.assertNotNull(authCodeGrantRequestCache.getAuthorizationRequestUrl(state));
  }

  /**
   * verifies that the authorization code request is correctly setup if the redirect uri is missing
   */
  @Test
  public void testGenerateAuthCodeRequestUrlWithMissingRedirectUri()
  {
    // prepare
    OpenIdProvider openIdProvider = OpenIdProvider.builder().discoveryEndpoint(discoveryUrl).build();
    openIdProvider = openIdProviderDao.save(openIdProvider);

    OpenIdClient openIdClient = OpenIdClient.builder()
                                            .openIdProvider(openIdProvider)
                                            .authenticationType("basic")
                                            .clientId("goldfish")
                                            .clientSecret(UUID.randomUUID().toString())
                                            .build();
    openIdClient = openIdClientDao.save(openIdClient);
    HttpClientSettings httpClientSettings = HttpClientSettings.builder().openIdClient(openIdClient).build();
    httpClientSettingsDao.save(httpClientSettings);

    // do
    final String state = "some-state-param";
    AuthCodeParameters authCodeParameters = AuthCodeParameters.builder()
                                                              .queryParameters(String.format("state=%s", state))
                                                              .build();
    ScimCurrentWorkflowSettings workflowSettings = ScimCurrentWorkflowSettings.builder()
                                                                              .openIdClientId(openIdClient.getId())
                                                                              .authCodeParameters(authCodeParameters)
                                                                              .build();
    Pair<String, String> authorizationCodeRequestUrl = //
      authCodeGrantRequestService.generateAuthCodeRequestUrl(openIdClient, workflowSettings);

    String expectedAuthorizationUrl = String.format("%s?state=%s&response_type=%s&client_id=%s",
                                                    getApplicationUrl(TestIdentityProvider.IDP_PATH
                                                                      + TestIdentityProvider.AUTHORIZATION_ENDPOINT),
                                                    state,
                                                    OAuthConstants.CODE,
                                                    openIdClient.getClientId());
    Assertions.assertEquals(expectedAuthorizationUrl, authorizationCodeRequestUrl.getLeft());
    Assertions.assertNotNull(authCodeGrantRequestCache.getAuthorizationRequestUrl(state));
  }
}
