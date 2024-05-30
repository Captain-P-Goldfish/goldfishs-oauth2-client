package de.captaingoldfish.restclient.scim.resources;

import java.util.Collections;
import java.util.Optional;

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
                                  String authorizationCodeGrantUrl,
                                  String authorizationResponseUrl,
                                  String metaDataJson,
                                  ScimCurrentWorkflowSettings currentWorkflowSettings,
                                  Meta meta)
  {
    setSchemas(Collections.singletonList(FieldNames.SCHEMA_ID));
    setId(id);
    setPkce(pkce);
    setAuthorizationCodeGrantUrl(authorizationCodeGrantUrl);
    setAuthorizationResponseUrl(authorizationResponseUrl);
    setMetaDataJson(metaDataJson);
    setCurrentWorkflowSettings(currentWorkflowSettings);
    setMeta(meta);
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

  public static class FieldNames
  {

    public static final String SCHEMA_ID = "urn:ietf:params:scim:schemas:captaingoldfish:2.0:AuthCodeGrantRequest";

    public static final String PKCE = "pkce";

    public static final String USE = "use";

    public static final String CODEVERIFIER = "codeVerifier";

    public static final String AUTHORIZATION_CODE_GRANT_URL = "authorizationCodeGrantUrl";

    public static final String ID = "id";

    public static final String AUTHORIZATION_RESPONSE_URL = "authorizationResponseUrl";

    public static final String META_DATA_JSON = "metaDataJson";
  }
}
