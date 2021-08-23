package de.captaingoldfish.restclient.application.endpoints.authcodegrant;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;

import de.captaingoldfish.restclient.application.endpoints.workflowsettings.CurrentWorkflowSettingsService;
import de.captaingoldfish.restclient.application.utils.OAuthConstants;
import de.captaingoldfish.restclient.application.utils.Utils;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;
import de.captaingoldfish.restclient.scim.resources.ScimCurrentWorkflowSettings;
import de.captaingoldfish.restclient.scim.resources.ScimCurrentWorkflowSettings.AuthCodeParameters;
import de.captaingoldfish.scim.sdk.common.exceptions.BadRequestException;
import lombok.RequiredArgsConstructor;


/**
 * @author Pascal Knueppel
 * @since 20.08.2021
 */
@RequiredArgsConstructor
@Service
public class AuthCodeGrantRequestService
{

  private final CurrentWorkflowSettingsService currentWorkflowSettingsService;

  private final OpenIdProviderMetdatdataCache openIdProviderMetaDataCache;

  private final AuthCodeGrantRequestCache authCodeGrantRequestCache;

  private final AuthCodeGrantResponseCache authCodeGrantResponseCache;

  /**
   * generates the authorization code grant request and caches it in {@link #authCodeGrantRequestCache} under
   * the state parameter as key
   * 
   * @param openIdClient the owner of the authorization code grant request
   * @param workflowSettings the dynamic settings from the javascript frontend
   * @return the authorization code grant url
   */
  public String generateAuthCodeRequestUrl(OpenIdClient openIdClient, ScimCurrentWorkflowSettings workflowSettings)
  {
    OIDCProviderMetadata metadata = Utils.loadDiscoveryEndpointInfos(openIdClient);
    String authorizationEndpointUri = metadata.getAuthorizationEndpointURI().toString();

    final String redirectUri = workflowSettings.getAuthCodeParameters()
                                               .map(AuthCodeParameters::getRedirectUri)
                                               .map(StringUtils::stripToNull)
                                               .orElse(null);
    final String additionalQueryParams = workflowSettings.getAuthCodeParameters()
                                                         .flatMap(AuthCodeParameters::getQueryParameters)
                                                         .orElse(null);
    UriComponents requestUrl = this.addRequiredQueryParams(UriComponentsBuilder.fromHttpUrl(authorizationEndpointUri),
                                                           openIdClient.getClientId(),
                                                           redirectUri,
                                                           additionalQueryParams)
                                   .build();
    final String authCodeRequestUrl = requestUrl.toString();

    cacheAuthCodeRequestUrl(authCodeRequestUrl);
    currentWorkflowSettingsService.overrideCurrentWorkflowSettings(workflowSettings);
    return authCodeRequestUrl;
  }

  /**
   * will cache the authCodeRequestUrl under the given state parameter that should be present within the request
   * 
   * @param authCodeRequestUrl the url to put into the cache
   */
  private void cacheAuthCodeRequestUrl(String authCodeRequestUrl)
  {
    UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(authCodeRequestUrl).build();
    List<String> stateParams = uriComponents.getQueryParams().get(OAuthConstants.STATE);
    final String state = stateParams.get(0);
    authCodeGrantRequestCache.setAuthorizationRequestUrl(state, authCodeRequestUrl);
  }

  /**
   * will add the required query parameters if they have not been added yet by the field
   * {@code additionalQueryParams}
   * 
   * @param uriComponentsBuilder the uri-components builder to which the parameters should be added
   * @param clientId the clientId parameter
   * @param redirectUri the redirect uri parameter
   * @param additionalQueryParams the optional additional query that might contain some required parameters
   * @return the extended {@link UriComponentsBuilder}
   */
  private UriComponentsBuilder addRequiredQueryParams(UriComponentsBuilder uriComponentsBuilder,
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
    queryParamMap.putIfAbsent(OAuthConstants.STATE,
                              Collections.singletonList(URLEncoder.encode(UUID.randomUUID().toString(),
                                                                          StandardCharsets.UTF_8)));
    return uriComponentsBuilder.queryParams(queryParamMap);
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
   * @see de.captaingoldfish.restclient.application.endpoints.BrowserEntryEndpoints#acceptAuthorizationCode(HttpServletRequest,
   *      HttpServletResponse)
   */
  public Optional<String> getAuthorizationCodeResponseUrl(String state)
  {
    return Optional.ofNullable(authCodeGrantResponseCache.getAuthorizationResponseUrl(state));
  }
}
