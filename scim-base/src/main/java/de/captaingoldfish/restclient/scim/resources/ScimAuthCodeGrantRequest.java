package de.captaingoldfish.restclient.scim.resources;

import java.util.Collections;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import de.captaingoldfish.restclient.scim.constants.AuthCodeGrantType;
import de.captaingoldfish.scim.sdk.common.resources.ResourceNode;
import de.captaingoldfish.scim.sdk.common.resources.base.ScimObjectNode;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import lombok.Builder;


/**
 * Can be used to get the authorization code grant url to redirect the user to the identity provider and to
 * retrieve the authorization code after it was received by the backend
 *
 * @author Pascal Knueppel
 * @since 20.08.2021
 */

public class ScimAuthCodeGrantRequest extends ResourceNode
{

  public ScimAuthCodeGrantRequest()
  {}

  @Builder
  public ScimAuthCodeGrantRequest(String id,
                                  Pkce pkce,
                                  Jar jar,
                                  AuthCodeGrantType authenticationType,
                                  String state,
                                  String redirectUri,
                                  String authorizationCodeGrantUrl,
                                  String authorizationCodeGrantParameters,
                                  String authorizationResponseUrl,
                                  String pushedAuthorizationResponse,
                                  String metaDataJson,
                                  ScimCurrentWorkflowSettings currentWorkflowSettings,
                                  Meta meta)
  {
    setSchemas(Collections.singletonList(FieldNames.SCHEMA_ID));
    setId(id);
    setAuthenticationType(authenticationType);
    setState(state);
    setRedirectUri(redirectUri);
    setPkce(pkce);
    setJar(jar);
    setAuthorizationCodeGrantUrl(authorizationCodeGrantUrl);
    setAuthorizationCodeGrantParameters(authorizationCodeGrantParameters);
    setAuthorizationResponseUrl(authorizationResponseUrl);
    setPushedAuthorizationResponse(pushedAuthorizationResponse);
    setMetaDataJson(metaDataJson);
    setCurrentWorkflowSettings(currentWorkflowSettings);
    setMeta(meta);
  }

  /**
   * If the normal Authorization Code Grant or a Pushed Authorization Code Grant should be executed.
   */
  public AuthCodeGrantType getAuthenticationType()
  {
    return getStringAttribute(FieldNames.AUTHENTICATION_TYPE).map(StringUtils::stripToNull)
                                                             .map(StringUtils::toRootUpperCase)
                                                             .map(AuthCodeGrantType::valueOf)
                                                             .orElseThrow();
  }

  /**
   * If the normal Authorization Code Grant or a Pushed Authorization Code Grant should be executed.
   */
  public void setAuthenticationType(AuthCodeGrantType authenticationType)
  {
    setAttribute(FieldNames.AUTHENTICATION_TYPE, Optional.ofNullable(authenticationType).map(Enum::name).orElse(null));
  }

  /**
   * The state parameter that was used in the AuthorizationRequest.
   */
  public Optional<String> getState()
  {
    return getStringAttribute(FieldNames.STATE);
  }

  /**
   * The state parameter that was used in the AuthorizationRequest.
   */
  public void setState(String state)
  {
    setAttribute(FieldNames.STATE, state);
  }

  /**
   * The redirectUri parameter that was used in the AuthorizationRequest.
   */
  public Optional<String> getRedirectUri()
  {
    return getStringAttribute(FieldNames.REDIRECTURI);
  }

  /**
   * The redirectUri parameter that was used in the AuthorizationRequest.
   */
  public void setRedirectUri(String redirectUri)
  {
    setAttribute(FieldNames.REDIRECTURI, redirectUri);
  }

  /**
   * Tells us if PKCE should be used for the authorization-request and token-request and what value to use
   */
  public Optional<Pkce> getPkce()
  {

    return getObjectAttribute(FieldNames.PKCE, Pkce.class);
  }

  /**
   * Tells us if PKCE should be used for the authorization-request and token-request and what value to use
   */
  public void setPkce(Pkce pkce)
  {
    setAttribute(FieldNames.PKCE, pkce);
  }

  /**
   * Used if we want to send the AuthorizationRequest as JWT Secured Authorization Request (JAR)
   */
  public Optional<Jar> getJar()
  {

    return getObjectAttribute(FieldNames.JAR, Jar.class);
  }

  /**
   * Used if we want to send the AuthorizationRequest as JWT Secured Authorization Request (JAR)
   */
  public void setJar(Jar jar)
  {
    setAttribute(FieldNames.JAR, jar);
  }

  /**
   * The authorization code grant request url that is used by the frontend to delegate the user to the identity
   * provider and to show the specific request details
   */
  public Optional<String> getAuthorizationCodeGrantUrl()
  {
    return getStringAttribute(FieldNames.AUTHORIZATION_CODE_GRANT_URL);
  }

  /**
   * The authorization code grant request url that is used by the frontend to delegate the user to the identity
   * provider and to show the specific request details
   */
  public void setAuthorizationCodeGrantUrl(String authorizationCodeGrantUrl)
  {
    setAttribute(FieldNames.AUTHORIZATION_CODE_GRANT_URL, authorizationCodeGrantUrl);
  }

  /**
   * This string will contain the query-string of the authorization Code Grant Url. This is added as an
   * additional parameter because the 'authorizationCodeGrantUrl' may not contain the query parameters in case
   * of Pushed Authorization Requests
   */
  public Optional<String> getAuthorizationCodeGrantParameters()
  {
    return getStringAttribute(FieldNames.AUTHORIZATION_CODE_GRANT_PARAMETERS);
  }

  /**
   * This string will contain the query-string of the authorization Code Grant Url. This is added as an
   * additional parameter because the 'authorizationCodeGrantUrl' may not contain the query parameters in case
   * of Pushed Authorization Requests
   */
  public void setAuthorizationCodeGrantParameters(String authorizationCodeGrantParameters)
  {
    setAttribute(FieldNames.AUTHORIZATION_CODE_GRANT_PARAMETERS, authorizationCodeGrantParameters);
  }

  /**
   * The authorization code response that was built by the identity provider to return the user to the
   * application
   */
  public Optional<String> getAuthorizationResponseUrl()
  {
    return getStringAttribute(FieldNames.AUTHORIZATION_RESPONSE_URL);
  }

  /**
   * The authorization code response that was built by the identity provider to return the user to the
   * application
   */
  public void setAuthorizationResponseUrl(String authorizationResponseUrl)
  {
    setAttribute(FieldNames.AUTHORIZATION_RESPONSE_URL, authorizationResponseUrl);
  }

  /**
   * shall contain the body of the Pushed Authorization Response
   */
  public Optional<String> getPushedAuthorizationResponse()
  {
    return getStringAttribute(FieldNames.PUSHEDAUTHORIZATIONRESPONSE);
  }

  /**
   * shall contain the body of the Pushed Authorization Response
   */
  public void setPushedAuthorizationResponse(String pushedAuthorizationResponse)
  {
    setAttribute(FieldNames.PUSHEDAUTHORIZATIONRESPONSE, pushedAuthorizationResponse);
  }

  /**
   * the json string that represents the metadata of the openId provider
   */
  public Optional<String> getMetaDataJson()
  {
    return getStringAttribute(FieldNames.META_DATA_JSON);
  }

  /**
   * the json string that represents the metadata of the openId provider
   */
  public void setMetaDataJson(String metaDataJson)
  {
    setAttribute(FieldNames.META_DATA_JSON, metaDataJson);
  }

  /**
   * only returned if the get-resource method is called. It contains the last used settings of previous OpenID
   * Connect workflows for this client.
   */
  public Optional<ScimCurrentWorkflowSettings> getCurrentWorkflowSettings()
  {
    return getObjectAttribute(ScimCurrentWorkflowSettings.FieldNames.SCHEMA_ID, ScimCurrentWorkflowSettings.class);
  }

  /**
   * only returned if the get-resource method is called. It contains the last used settings of previous OpenID
   * Connect workflows for this client.
   */
  public void setCurrentWorkflowSettings(ScimCurrentWorkflowSettings currentWorkflowSettings)
  {
    setAttribute(ScimCurrentWorkflowSettings.FieldNames.SCHEMA_ID, currentWorkflowSettings);
  }

  /**
   * Tells us if PKCE should be used for the authorization-request and token-request and what value to use
   */
  public static class Pkce extends ScimObjectNode
  {

    public Pkce()
    {}

    @Builder
    public Pkce(Boolean use, String codeVerifier)
    {
      setUse(use);
      setCodeVerifier(codeVerifier);
    }

    /**
     * If PKCE should be used or not
     */
    public boolean isUse()
    {
      return getBooleanAttribute(FieldNames.USE).orElse(false);
    }

    /**
     * If PKCE should be used or not
     */
    public void setUse(Boolean use)
    {
      setAttribute(FieldNames.USE, use);
    }

    /**
     * optional value. If present this value is used as code_verifier. If missing a value will be generated.
     */
    public Optional<String> getCodeVerifier()
    {
      return getStringAttribute(FieldNames.CODEVERIFIER);
    }

    /**
     * optional value. If present this value is used as code_verifier. If missing a value will be generated.
     */
    public void setCodeVerifier(String codeVerifier)
    {
      setAttribute(FieldNames.CODEVERIFIER, codeVerifier);
    }

  }

  /**
   * Used if we want to send the AuthorizationRequest as JWT Secured Authorization Request (JAR)
   */
  public static class Jar extends ScimObjectNode
  {

    public Jar()
    {}

    @Builder
    public Jar(Boolean use, String requestType, String signatureKey, String signatureAlgorithm)
    {
      setUse(use);
      setRequestType(requestType);
      setSignatureKey(signatureKey);
      setSignatureAlgorithm(signatureAlgorithm);
    }

    /**
     * If JAR should be used or not
     */
    public boolean isUse()
    {
      return getBooleanAttribute(FieldNames.USE).orElse(false);
    }

    /**
     * If JAR should be used or not
     */
    public void setUse(Boolean use)
    {
      setAttribute(FieldNames.USE, use);
    }

    /**
     * How the JAR request should be sent to the remote system. Either ['request', 'request_uri'].
     */
    public Optional<String> getRequestType()
    {
      return getStringAttribute(FieldNames.REQUESTTYPE);
    }

    /**
     * How the JAR request should be sent to the remote system. Either ['request', 'request_uri'].
     */
    public void setRequestType(String requestType)
    {
      setAttribute(FieldNames.REQUESTTYPE, requestType);
    }

    /**
     * The key alias that should be used to sign the JAR request.
     */
    public Optional<String> getSignatureKey()
    {
      return getStringAttribute(FieldNames.SIGNATUREKEY);
    }

    /**
     * The key alias that should be used to sign the JAR request.
     */
    public void setSignatureKey(String signatureKey)
    {
      setAttribute(FieldNames.SIGNATUREKEY, signatureKey);
    }

    /**
     * The signature algorithm that should be used to sign the JAR request.
     */
    public Optional<String> getSignatureAlgorithm()
    {
      return getStringAttribute(FieldNames.SIGNATUREALGORITHM);
    }

    /**
     * The signature algorithm that should be used to sign the JAR request.
     */
    public void setSignatureAlgorithm(String signatureAlgorithm)
    {
      setAttribute(FieldNames.SIGNATUREALGORITHM, signatureAlgorithm);
    }

  }


  public static class FieldNames
  {

    public static final String SCHEMA_ID = "urn:ietf:params:scim:schemas:captaingoldfish:2.0:AuthCodeGrantRequest";

    public static final String AUTHENTICATION_TYPE = "authenticationType";

    public static final String STATE = "state";

    public static final String REDIRECTURI = "redirectUri";

    public static final String PKCE = "pkce";

    public static final String JAR = "jar";

    public static final String USE = "use";

    public static final String CODEVERIFIER = "codeVerifier";

    public static final String REQUESTTYPE = "requestType";

    public static final String SIGNATUREKEY = "signatureKey";

    public static final String SIGNATUREALGORITHM = "signatureAlgorithm";

    public static final String AUTHORIZATION_CODE_GRANT_URL = "authorizationCodeGrantUrl";

    public static final String AUTHORIZATION_CODE_GRANT_PARAMETERS = "authorizationCodeGrantParameters";

    public static final String ID = "id";

    public static final String AUTHORIZATION_RESPONSE_URL = "authorizationResponseUrl";

    public static final String PUSHEDAUTHORIZATIONRESPONSE = "pushedAuthorizationResponse";

    public static final String META_DATA_JSON = "metaDataJson";
  }
}
