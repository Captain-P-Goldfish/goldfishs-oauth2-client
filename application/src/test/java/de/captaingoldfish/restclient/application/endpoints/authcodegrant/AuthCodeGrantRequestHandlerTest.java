package de.captaingoldfish.restclient.application.endpoints.authcodegrant;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.web.util.UriComponentsBuilder;

import de.captaingoldfish.restclient.application.endpoints.BrowserEntryController;
import de.captaingoldfish.restclient.application.endpoints.provider.TestIdentityProvider;
import de.captaingoldfish.restclient.application.setup.AbstractScimClientConfig;
import de.captaingoldfish.restclient.application.setup.OAuthRestClientTest;
import de.captaingoldfish.restclient.application.utils.OAuthConstants;
import de.captaingoldfish.restclient.database.entities.HttpClientSettings;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;
import de.captaingoldfish.restclient.database.entities.OpenIdProvider;
import de.captaingoldfish.restclient.scim.resources.ScimAuthCodeGrantRequest;
import de.captaingoldfish.restclient.scim.resources.ScimCurrentWorkflowSettings;
import de.captaingoldfish.restclient.scim.resources.ScimCurrentWorkflowSettings.AuthCodeParameters;
import de.captaingoldfish.scim.sdk.client.response.ServerResponse;
import de.captaingoldfish.scim.sdk.common.constants.HttpStatus;
import de.captaingoldfish.scim.sdk.common.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;


/**
 * @author Pascal Knueppel
 * @since 20.08.2021
 */
@Slf4j
@OAuthRestClientTest
public class AuthCodeGrantRequestHandlerTest extends AbstractScimClientConfig
{

  /**
   * the scim endpoint for the application keystore
   */
  private static final String AUTH_CODE_GRANT_ENDPOINT = "/AuthCodeGrantRequest";

  /**
   * used to check if the handler did call the service
   */
  @SpyBean
  private AuthCodeGrantRequestService authCodeGrantRequestService;

  /**
   * the discovery endpoint from the {@link TestIdentityProvider}
   */
  private String discoveryUrl;

  @BeforeEach
  public void initialize()
  {
    discoveryUrl = getApplicationUrl(TestIdentityProvider.IDP_PATH + TestIdentityProvider.DISCOVERY_ENDPOINT);
  }

  @AfterEach
  public void tearDown()
  {
    Mockito.clearInvocations(authCodeGrantRequestService);
  }

  /**
   * gets an authorization code grant request from the scim endpoint
   */
  @Test
  public void testGetValidAuthorizationCodeGrantRequest()
  {
    // prepare
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getApplicationUrl());
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
    String redirectUri = BrowserEntryController.getAuthorizationCodeEntryPoint(uriComponentsBuilder);
    AuthCodeParameters authCodeParameters = AuthCodeParameters.builder().redirectUri(redirectUri).build();
    ScimCurrentWorkflowSettings workflowSettings = ScimCurrentWorkflowSettings.builder()
                                                                              .openIdClientId(openIdClient.getId())
                                                                              .authCodeParameters(authCodeParameters)
                                                                              .build();
    ScimAuthCodeGrantRequest grantRequest = ScimAuthCodeGrantRequest.builder()
                                                                    .currentWorkflowSettings(workflowSettings)
                                                                    .build();
    ServerResponse<ScimAuthCodeGrantRequest> response = scimRequestBuilder.create(ScimAuthCodeGrantRequest.class,
                                                                                  AUTH_CODE_GRANT_ENDPOINT)
                                                                          .setResource(grantRequest)
                                                                          .sendRequest();

    // then
    Assertions.assertEquals(HttpStatus.CREATED, response.getHttpStatus());
    ScimAuthCodeGrantRequest responseResource = response.getResource();
    Assertions.assertNotNull(responseResource);

    String expectedAuthorizationUrl = String.format("%s?response_type=%s&client_id=%s&redirect_uri=%s&state=",
                                                    getApplicationUrl(TestIdentityProvider.IDP_PATH
                                                                      + TestIdentityProvider.AUTHORIZATION_ENDPOINT),
                                                    OAuthConstants.CODE,
                                                    openIdClient.getClientId(),
                                                    URLEncoder.encode(redirectUri, StandardCharsets.UTF_8));
    expectedAuthorizationUrl = Pattern.quote(expectedAuthorizationUrl) + ".*";
    MatcherAssert.assertThat(responseResource.getAuthorizationCodeGrantUrl().orElseThrow(),
                             Matchers.matchesPattern(expectedAuthorizationUrl));
    Mockito.verify(authCodeGrantRequestService).generateAuthCodeRequestUrl(Mockito.any(), Mockito.any(), Mockito.any());
  }

  /**
   * Assures that an appropriate error is returned if the workflow-settings are missing
   */
  @Test
  public void testGetAccessEndpointWithMissingWorkflowSettings()
  {
    // do
    ScimAuthCodeGrantRequest grantRequest = ScimAuthCodeGrantRequest.builder().build();
    ServerResponse<ScimAuthCodeGrantRequest> response = scimRequestBuilder.create(ScimAuthCodeGrantRequest.class,
                                                                                  AUTH_CODE_GRANT_ENDPOINT)
                                                                          .setResource(grantRequest)
                                                                          .sendRequest();

    // then
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();
    // verify error messages
    {
      List<String> errorMessages = errorResponse.getErrorMessages();
      Assertions.assertEquals(1, errorMessages.size());
      String expectedError = "The resource type extension for the workflow settings must be "
                             + "present in request. This is an implementation error in the javascript frontend.";
      Assertions.assertEquals(expectedError, errorMessages.get(0));
    }

    // verify field error messages
    {
      Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
      Assertions.assertEquals(0, fieldErrors.size());
    }
  }

  /**
   * shows that an appropriate error is returned if the user tries to add several state values to the request
   */
  @Test
  public void testGetAccessEndpointWithSeveralStateValues()
  {
    // prepare
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getApplicationUrl());
    String redirectUri = BrowserEntryController.getAuthorizationCodeEntryPoint(uriComponentsBuilder);
    OpenIdProvider openIdProvider = OpenIdProvider.builder().discoveryEndpoint(discoveryUrl).build();
    openIdProvider = openIdProviderDao.save(openIdProvider);
    OpenIdClient openIdClient = OpenIdClient.builder()
                                            .openIdProvider(openIdProvider)
                                            .authenticationType("basic")
                                            .clientId("goldfish")
                                            .clientSecret(UUID.randomUUID().toString())
                                            .build();
    openIdClient = openIdClientDao.save(openIdClient);

    // do
    final String illegalQuery = String.format("state=%s&state=%s", UUID.randomUUID(), "123456");
    AuthCodeParameters authCodeParameters = AuthCodeParameters.builder()
                                                              .redirectUri(redirectUri)
                                                              .queryParameters(illegalQuery)
                                                              .build();
    ScimCurrentWorkflowSettings workflowSettings = ScimCurrentWorkflowSettings.builder()
                                                                              .openIdClientId(openIdClient.getId())
                                                                              .authCodeParameters(authCodeParameters)
                                                                              .build();
    ScimAuthCodeGrantRequest grantRequest = ScimAuthCodeGrantRequest.builder()
                                                                    .currentWorkflowSettings(workflowSettings)
                                                                    .build();
    ServerResponse<ScimAuthCodeGrantRequest> response = scimRequestBuilder.create(ScimAuthCodeGrantRequest.class,
                                                                                  AUTH_CODE_GRANT_ENDPOINT)
                                                                          .setResource(grantRequest)
                                                                          .sendRequest();

    // then
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();
    // verify error messages
    {
      List<String> errorMessages = errorResponse.getErrorMessages();
      Assertions.assertEquals(0, errorMessages.size());
    }

    // verify field error messages
    {
      Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
      Assertions.assertEquals(1, fieldErrors.size());
      List<String> queryParamErrors = fieldErrors.get(String.format("%s.%s",
                                                                    ScimCurrentWorkflowSettings.FieldNames.AUTH_CODE_PARAMETERS,
                                                                    ScimCurrentWorkflowSettings.FieldNames.QUERY_PARAMETERS));
      Assertions.assertEquals(1, queryParamErrors.size());
      String expectedError = "Only a single state parameter may be added. The state parameter is used to identify the "
                             + "authorization response. If you use several values unpredictable results may occur.";
      Assertions.assertEquals(expectedError, queryParamErrors.get(0));
    }
  }

  /**
   * shows that an appropriate error is returned if the javascript frontend misses to send the openId client id
   */
  @Test
  public void testGetAccessEndpointWithMissingOpenIdClientId()
  {
    // prepare
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getApplicationUrl());
    String redirectUri = BrowserEntryController.getAuthorizationCodeEntryPoint(uriComponentsBuilder);
    OpenIdProvider openIdProvider = OpenIdProvider.builder().discoveryEndpoint(discoveryUrl).build();
    openIdProvider = openIdProviderDao.save(openIdProvider);
    OpenIdClient openIdClient = OpenIdClient.builder()
                                            .openIdProvider(openIdProvider)
                                            .authenticationType("basic")
                                            .clientId("goldfish")
                                            .clientSecret(UUID.randomUUID().toString())
                                            .build();
    openIdClientDao.save(openIdClient);

    // do
    AuthCodeParameters authCodeParameters = AuthCodeParameters.builder().redirectUri(redirectUri).build();
    ScimCurrentWorkflowSettings workflowSettings = ScimCurrentWorkflowSettings.builder()
                                                                              .authCodeParameters(authCodeParameters)
                                                                              .build();
    ScimAuthCodeGrantRequest grantRequest = ScimAuthCodeGrantRequest.builder()
                                                                    .currentWorkflowSettings(workflowSettings)
                                                                    .build();
    ServerResponse<ScimAuthCodeGrantRequest> response = scimRequestBuilder.create(ScimAuthCodeGrantRequest.class,
                                                                                  AUTH_CODE_GRANT_ENDPOINT)
                                                                          .setResource(grantRequest)
                                                                          .sendRequest();

    // then
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();
    // verify error messages
    {
      List<String> errorMessages = errorResponse.getErrorMessages();
      Assertions.assertEquals(0, errorMessages.size());
    }

    // verify field error messages
    {
      Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
      Assertions.assertEquals(1, fieldErrors.size());
      List<String> clientIdErrors = fieldErrors.get(ScimCurrentWorkflowSettings.FieldNames.OPENID_CLIENT_ID);
      Assertions.assertEquals(1, clientIdErrors.size());
      String expectedError = "Required 'READ_WRITE' attribute 'urn:ietf:params:scim:schemas:captaingoldfish:"
                             + "2.0:CurrentWorkflowSettings:openIdClientId' is missing";
      Assertions.assertEquals(expectedError, clientIdErrors.get(0));
    }
  }

  /**
   * shows that an appropriate error is returned if the javascript frontend sends an unknown OpenId Client ID
   */
  @Test
  public void testGetAccessEndpointWithUnknownOpenIdClientId()
  {
    // prepare
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getApplicationUrl());
    String redirectUri = BrowserEntryController.getAuthorizationCodeEntryPoint(uriComponentsBuilder);
    OpenIdProvider openIdProvider = OpenIdProvider.builder().discoveryEndpoint(discoveryUrl).build();
    openIdProvider = openIdProviderDao.save(openIdProvider);
    OpenIdClient openIdClient = OpenIdClient.builder()
                                            .openIdProvider(openIdProvider)
                                            .authenticationType("basic")
                                            .clientId("goldfish")
                                            .clientSecret(UUID.randomUUID().toString())
                                            .build();
    openIdClientDao.save(openIdClient);

    // do
    final Long unknownClientId = -1L;
    AuthCodeParameters authCodeParameters = AuthCodeParameters.builder().redirectUri(redirectUri).build();
    ScimCurrentWorkflowSettings workflowSettings = ScimCurrentWorkflowSettings.builder()
                                                                              .openIdClientId(unknownClientId)
                                                                              .authCodeParameters(authCodeParameters)
                                                                              .build();
    ScimAuthCodeGrantRequest grantRequest = ScimAuthCodeGrantRequest.builder()
                                                                    .currentWorkflowSettings(workflowSettings)
                                                                    .build();
    ServerResponse<ScimAuthCodeGrantRequest> response = scimRequestBuilder.create(ScimAuthCodeGrantRequest.class,
                                                                                  AUTH_CODE_GRANT_ENDPOINT)
                                                                          .setResource(grantRequest)
                                                                          .sendRequest();

    // then
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();
    // verify error messages
    {
      List<String> errorMessages = errorResponse.getErrorMessages();
      Assertions.assertEquals(0, errorMessages.size());
    }

    // verify field error messages
    {
      Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
      Assertions.assertEquals(1, fieldErrors.size());
      List<String> clientIdErrors = fieldErrors.get(ScimCurrentWorkflowSettings.FieldNames.OPENID_CLIENT_ID);
      Assertions.assertEquals(1, clientIdErrors.size());
      String expectedError = String.format("Unknown OpenID Client ID '%s'", unknownClientId);
      Assertions.assertEquals(expectedError, clientIdErrors.get(0));
    }
  }
}
