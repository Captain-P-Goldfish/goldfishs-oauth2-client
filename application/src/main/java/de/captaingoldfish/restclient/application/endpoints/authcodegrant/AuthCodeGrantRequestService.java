package de.captaingoldfish.restclient.application.endpoints.authcodegrant;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;

import de.captaingoldfish.restclient.application.endpoints.BrowserEntryController;
import de.captaingoldfish.restclient.application.utils.HttpClientBuilder;
import de.captaingoldfish.restclient.application.utils.HttpResponseDetails;
import de.captaingoldfish.restclient.application.utils.OAuthConstants;
import de.captaingoldfish.restclient.application.utils.Utils;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;
import de.captaingoldfish.restclient.scim.constants.AuthCodeGrantType;
import de.captaingoldfish.restclient.scim.resources.ScimAuthCodeGrantRequest.Pkce;
import de.captaingoldfish.restclient.scim.resources.ScimCurrentWorkflowSettings;
import de.captaingoldfish.restclient.scim.resources.ScimCurrentWorkflowSettings.AuthCodeParameters;
import de.captaingoldfish.scim.sdk.common.exceptions.BadRequestException;
import de.captaingoldfish.scim.sdk.common.exceptions.PreconditionFailedException;
import de.captaingoldfish.scim.sdk.common.utils.JsonHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;


/**
 * @author Pascal Knueppel
 * @since 20.08.2021
 */
@RequiredArgsConstructor
@Service
public class AuthCodeGrantRequestService
{

  private final AuthCodeGrantRequestCache authCodeGrantRequestCache;

  private final AuthCodeGrantResponseCache authCodeGrantResponseCache;

  private final PkceCodeVerifierCache pkceCodeVerifierCache;

  /**
   * generates the authorization code grant request and caches it in {@link #authCodeGrantRequestCache} under
   * the state parameter as key
   *
   * @param openIdClient the owner of the authorization code grant request
   * @param workflowSettings the dynamic settings from the javascript frontend
   * @param authenticationType tells us if we should use a Pushed Authorization Request or a standard
   *          Authorization Request
   * @return the authorization code grant url, the query parameters and optionally a response body if present
   */
  public Triple<String, String, String> generateAuthCodeRequestUrl(OpenIdClient openIdClient,
                                                                   ScimCurrentWorkflowSettings workflowSettings,
                                                                   AuthCodeGrantType authenticationType)
  {
    OIDCProviderMetadata metadata = Utils.loadDiscoveryEndpointInfos(openIdClient.getOpenIdProvider());
    String authorizationEndpointUri = metadata.getAuthorizationEndpointURI().toString();

    final String redirectUri = workflowSettings.getAuthCodeParameters()
                                               .flatMap(AuthCodeParameters::getRedirectUri)
                                               .map(StringUtils::stripToNull)
                                               .orElse(null);
    final String additionalQueryParams = workflowSettings.getAuthCodeParameters()
                                                         .flatMap(AuthCodeParameters::getQueryParameters)
                                                         .orElse(null);
    Pair<String, UriComponentsBuilder> requestPair = //
      this.addRequiredQueryParams(UriComponentsBuilder.fromHttpUrl(authorizationEndpointUri),
                                  openIdClient.getClientId(),
                                  redirectUri,
                                  additionalQueryParams);

    final String state = requestPair.getLeft();
    UriComponentsBuilder requestUrlBuilder = requestPair.getRight();
    if (workflowSettings.getPkce().map(Pkce::isUse).orElse(false))
    {
      final String pkceCodeVerifier = workflowSettings.getPkce()
                                                      .flatMap(Pkce::getCodeVerifier)
                                                      .map(StringUtils::stripToNull)
                                                      .orElseGet(() -> RandomStringUtils.randomAlphabetic(50));
      final String pkceCodeChallenge = Utils.toSha256Base64UrlEncoded(pkceCodeVerifier);
      requestUrlBuilder.queryParam(OAuthConstants.CODE_CHALLENGE, pkceCodeChallenge);
      requestUrlBuilder.queryParam(OAuthConstants.CODE_CHALLENGE_METHOD, "S256");
      pkceCodeVerifierCache.setCodeVerifier(state, pkceCodeVerifier);
    }
    final UriComponents requestUrl = requestUrlBuilder.build();
    final String authCodeRequestUrl = requestUrl.toString();


    if (AuthCodeGrantType.AUTHORIZATION_CODE.equals(authenticationType))
    {
      authCodeGrantRequestCache.setAuthorizationRequestUrl(state, authCodeRequestUrl);
      return Triple.of(authCodeRequestUrl, requestUrl.getQuery(), null);
    }
    else
    {
      HttpResponseDetails parResponseDetails = sendPushedAuthorizationRequest(openIdClient, metadata, requestUrl);
      ObjectNode responseNode = JsonHelper.readJsonDocument(parResponseDetails.getBody(), ObjectNode.class);

      UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(authorizationEndpointUri);
      responseNode.fieldNames().forEachRemaining(fieldName -> {
        builder.queryParam(fieldName,
                           Optional.ofNullable(responseNode.get(fieldName)).map(JsonNode::textValue).orElse(null));
      });

      if (HttpStatus.SC_CREATED == parResponseDetails.getStatusCode())
      {
        builder.queryParam(OAuthConstants.CLIENT_ID, openIdClient.getClientId());
      }
      String authCodeUrl = builder.build().toString();
      authCodeGrantRequestCache.setAuthorizationRequestUrl(state, authCodeUrl);
      return Triple.of(authCodeUrl, requestUrl.getQuery(), parResponseDetails.getBody());
    }
  }

  /**
   * sends a pushed authorization request to the remote system and retrieves the responseBody with the
   * redirectUri to the authorizationCode endpoint for the pushed authorization request
   *
   * @param openIdClient required to generate a http client with its corresponding settings
   * @param metadata needed to get the pushed-authorization-code endpoint
   * @param authCodeRequestUrl the authorization code grant URL that would be normally used to access the
   *          standard authorization endpoint
   * @return
   */
  @SneakyThrows
  public HttpResponseDetails sendPushedAuthorizationRequest(OpenIdClient openIdClient,
                                                            OIDCProviderMetadata metadata,
                                                            UriComponents authCodeRequestUrl)
  {
    URI parEndpoint = metadata.getPushedAuthorizationRequestEndpointURI();
    if (parEndpoint == null)
    {
      throw new PreconditionFailedException("Remote Provider does not offer a Pushed Authorization Code Endpoint");
    }
    HttpPost httpPost = new HttpPost(parEndpoint);
    httpPost.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
    httpPost.setEntity(new StringEntity(authCodeRequestUrl.getQuery()));
    try (CloseableHttpClient httpClient = HttpClientBuilder.getHttpClient(openIdClient);
      CloseableHttpResponse response = httpClient.execute(httpPost))
    {
      return new HttpResponseDetails(response);
    }
  }

  /**
   * will add the required query parameters if they have not been added yet by the field
   * {@code additionalQueryParams}
   *
   * @param uriComponentsBuilder the uri-components builder to which the parameters should be added
   * @param clientId the clientId parameter
   * @param redirectUri the redirect uri parameter
   * @param additionalQueryParams the optional additional query that might contain some required parameters
   * @return {@code Pair<state, the extended {@link UriComponentsBuilder}>}
   */
  private Pair<String, UriComponentsBuilder> addRequiredQueryParams(UriComponentsBuilder uriComponentsBuilder,
                                                                    String clientId,
                                                                    String redirectUri,
                                                                    String additionalQueryParams)
  {
    UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl("http://localhost")
                                                      .query(additionalQueryParams)
                                                      .build();
    MultiValueMap<String, String> queryParamMap = new LinkedMultiValueMap<>(uriComponents.getQueryParams());
    queryParamMap.putIfAbsent("response_type", Collections.singletonList(OAuthConstants.CODE));
    queryParamMap.putIfAbsent(OAuthConstants.CLIENT_ID,
                              Collections.singletonList(URLEncoder.encode(clientId, StandardCharsets.UTF_8)));
    Optional.ofNullable(redirectUri).ifPresent(uri -> {
      queryParamMap.putIfAbsent(OAuthConstants.REDIRECT_URI,
                                Collections.singletonList(URLEncoder.encode(uri, StandardCharsets.UTF_8)));
    });
    String state = Optional.ofNullable(queryParamMap.getFirst(OAuthConstants.STATE))
                           .orElseGet(() -> URLEncoder.encode(UUID.randomUUID().toString(), StandardCharsets.UTF_8));
    queryParamMap.put(OAuthConstants.STATE, List.of(state));
    return Pair.of(state, uriComponentsBuilder.queryParams(queryParamMap));
  }

  /**
   * this method will handle the authorization response from an identity provider by identifying the necessary
   * data based on the given state parameter that should be present within the request
   *
   * @param fullRequestUrl the full url that should contain the authorization code and the state parameter or
   *          maybe some error details
   */
  public void handleAuthorizationResponse(String fullRequestUrl)
  {
    UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(fullRequestUrl).build();
    final String state = getStateFromAuthorizationResponseUrl(uriComponents);
    String originalRequestUrl = authCodeGrantRequestCache.getAuthorizationRequestUrl(state);
    if (originalRequestUrl == null)
    {
      throw new BadRequestException(String.format("The state parameter '%s' cannot be mapped to a previous sent request",
                                                  state));
    }
    authCodeGrantResponseCache.setAuthorizationResponseUrl(state, fullRequestUrl);
  }

  /**
   * retrieves the state-parameter from an authorization code grant response url
   *
   * @param uriComponents the parsed response url
   * @return the state-parameter from the url
   */
  private String getStateFromAuthorizationResponseUrl(UriComponents uriComponents)
  {
    List<String> stateParams = uriComponents.getQueryParams().get(OAuthConstants.STATE);
    final int numberOfStateParams = Optional.ofNullable(stateParams).map(List::size).orElse(0);
    if (numberOfStateParams != 1)
    {
      throw new BadRequestException(String.format("Got an unexpected number of state-parameters '%s'. "
                                                  + "Exactly '1' state-parameter must be present in the response.",
                                                  numberOfStateParams));
    }
    return stateParams.get(0);
  }

  /**
   * simply returns the authorization code response url from the cache if it is already present
   *
   * @param state the state parameter from the request that was used to save the response url
   * @return the full authorization code response
   * @see #handleAuthorizationResponse(String)
   * @see BrowserEntryController#acceptAuthorizationCode(HttpServletRequest, HttpServletResponse)
   */
  public Optional<String> getAuthorizationCodeResponseUrl(String state)
  {
    return Optional.ofNullable(authCodeGrantResponseCache.getAuthorizationResponseUrl(state));
  }
}
