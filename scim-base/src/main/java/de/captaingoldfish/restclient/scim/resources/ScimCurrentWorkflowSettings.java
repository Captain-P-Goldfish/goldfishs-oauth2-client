package de.captaingoldfish.restclient.scim.resources;

import java.util.Collections;
import java.util.Optional;

import de.captaingoldfish.scim.sdk.common.resources.ResourceNode;
import de.captaingoldfish.scim.sdk.common.resources.base.ScimObjectNode;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import lombok.Builder;


/**
 * Represents the last settings representation that have been used for a specific client
 *
 * @author Pascal Knueppel
 * @since 19.08.2021
 */
public class ScimCurrentWorkflowSettings extends ResourceNode
{

  public ScimCurrentWorkflowSettings()
  {}

  @Builder
  public ScimCurrentWorkflowSettings(String id,
                                     Long openIdClientId,
                                     String grantType,
                                     Dpop dpop,
                                     ScimAuthCodeGrantRequest.Pkce pkce,
                                     AuthCodeParameters authCodeParameters,
                                     ClientCredentialsParameters clientCredentialsParameters,
                                     ResourceOwnerPasswordParameters resourceOwnerPasswordParameters,
                                     Meta meta)
  {
    setSchemas(Collections.singletonList(FieldNames.SCHEMA_ID));
    setId(id);
    setOpenIdClientId(openIdClientId);
    setGrantType(grantType);
    setDpop(dpop);
    setPkce(pkce);
    setAuthCodeParameters(authCodeParameters);
    setClientCredentialsParameters(clientCredentialsParameters);
    setResourceOwnerPasswordParameters(resourceOwnerPasswordParameters);
    setMeta(meta);
  }

  /** The grant_type to use in the tokenRequest. */
  public String getGrantType()
  {
    return getStringAttribute(FieldNames.GRANT_TYPE).orElse(null);
  }

  /** The grant_type to use in the tokenRequest. */
  public void setGrantType(String grantType)
  {
    setAttribute(FieldNames.GRANT_TYPE, grantType);
  }

  /** The foreign key reference to an open-id-client resource. */
  public Long getOpenIdClientId()
  {
    return getLongAttribute(FieldNames.OPENID_CLIENT_ID).orElse(null);
  }

  /** The foreign key reference to an open-id-client resource. */
  public void setOpenIdClientId(Long openIdClientId)
  {
    setAttribute(FieldNames.OPENID_CLIENT_ID, openIdClientId);
  }

  /**
   * If the AccessToken should be bound to a DPoP token (https://datatracker.ietf.org/doc/html/rfc9449).
   */
  public Optional<Dpop> getDpop()
  {

    return getObjectAttribute(FieldNames.DPOP, Dpop.class);
  }

  /**
   * If the AccessToken should be bound to a DPoP token (https://datatracker.ietf.org/doc/html/rfc9449).
   */
  public void setDpop(Dpop dpop)
  {
    setAttribute(FieldNames.DPOP, dpop);
  }

  /**
   * Tells us if PKCE should be used for the authorization-request and token-request and what value to use
   */
  public Optional<ScimAuthCodeGrantRequest.Pkce> getPkce()
  {

    return getObjectAttribute(ScimAuthCodeGrantRequest.FieldNames.PKCE, ScimAuthCodeGrantRequest.Pkce.class);
  }

  /**
   * Tells us if PKCE should be used for the authorization-request and token-request and what value to use
   */
  public void setPkce(ScimAuthCodeGrantRequest.Pkce pkce)
  {
    setAttribute(ScimAuthCodeGrantRequest.FieldNames.PKCE, pkce);
  }

  /** The settings that have been used for the authorization code grant in the last request. */
  public Optional<AuthCodeParameters> getAuthCodeParameters()
  {
    return getObjectAttribute(FieldNames.AUTH_CODE_PARAMETERS, AuthCodeParameters.class);
  }

  /** The settings that have been used for the authorization code grant in the last request. */
  public void setAuthCodeParameters(AuthCodeParameters authCodeParameters)
  {
    setAttribute(FieldNames.AUTH_CODE_PARAMETERS, authCodeParameters);
  }

  /** The settings that have been used for the client credentials grant in the last request. */
  public Optional<ClientCredentialsParameters> getClientCredentialsParameters()
  {
    return getObjectAttribute(FieldNames.CLIENT_CREDENTIALS_PARAMETERS, ClientCredentialsParameters.class);
  }

  /** The settings that have been used for the client credentials grant in the last request. */
  public void setClientCredentialsParameters(ClientCredentialsParameters clientCredentialsParameters)
  {
    setAttribute(FieldNames.CLIENT_CREDENTIALS_PARAMETERS, clientCredentialsParameters);
  }

  /** The settings that have been used for the resource owner password credentials grant in the last request. */
  public Optional<ResourceOwnerPasswordParameters> getResourceOwnerPasswordParameters()
  {
    return getObjectAttribute(FieldNames.RESOURCE_OWNER_PASSWORD_PARAMETERS, ResourceOwnerPasswordParameters.class);
  }

  /** The settings that have been used for the resource owner password credentials grant in the last request. */
  public void setResourceOwnerPasswordParameters(ResourceOwnerPasswordParameters resourceOwnerPasswordParameters)
  {
    setAttribute(FieldNames.RESOURCE_OWNER_PASSWORD_PARAMETERS, resourceOwnerPasswordParameters);
  }

  /**
   * If the AccessToken should be bound to a DPoP token (https://datatracker.ietf.org/doc/html/rfc9449).
   */
  public static class Dpop extends ScimObjectNode
  {

    public Dpop()
    {}

    @Builder
    public Dpop(String keyId, String signatureAlgorithm, String nonce, String jti, String htm, String htu, String ath)
    {
      setKeyId(keyId);
      setSignatureAlgorithm(signatureAlgorithm);
      setNonce(nonce);
      setJti(jti);
      setHtm(htm);
      setHtu(htu);
      setAth(ath);
    }

    /**
     * If a DPoP should be sent in the access-token request or not.
     */
    public boolean isUseDpop()
    {
      return getBooleanAttribute(FieldNames.USE_DPOP).orElse(false);
    }

    /**
     * If a DPoP should be sent in the access-token request or not.
     */
    public void setUseDpop(boolean useDpop)
    {
      setAttribute(FieldNames.USE_DPOP, useDpop);
    }

    /**
     * The ID of the key the DPoP should reference.
     */
    public Optional<String> getKeyId()
    {
      return getStringAttribute(FieldNames.KEYID);
    }

    /**
     * The ID of the key the DPoP should reference.
     */
    public void setKeyId(String keyId)
    {
      setAttribute(FieldNames.KEYID, keyId);
    }

    /**
     * The algorithm to sign the DPoP JWT.
     */
    public Optional<String> getSignatureAlgorithm()
    {
      return getStringAttribute(FieldNames.SIGNATUREALGORITHM);
    }

    /**
     * The algorithm to sign the DPoP JWT.
     */
    public void setSignatureAlgorithm(String signatureAlgorithm)
    {
      setAttribute(FieldNames.SIGNATUREALGORITHM, signatureAlgorithm);
    }

    /**
     * The expected nonce of the remote system that must be added to the DPoP.
     */
    public Optional<String> getNonce()
    {
      return getStringAttribute(FieldNames.NONCE);
    }

    /**
     * The expected nonce of the remote system that must be added to the DPoP.
     */
    public void setNonce(String nonce)
    {
      setAttribute(FieldNames.NONCE, nonce);
    }

    /**
     * Optional value to use as 'jti'. Will be auto-generated if left empty.
     */
    public Optional<String> getJti()
    {
      return getStringAttribute(FieldNames.JTI);
    }

    /**
     * Optional value to use as 'jti'. Will be auto-generated if left empty.
     */
    public void setJti(String jti)
    {
      setAttribute(FieldNames.JTI, jti);
    }

    /**
     * The value of the HTTP method (Section 9.1 of [RFC9110]) of the request to which the JWT is attached.
     */
    public Optional<String> getHtm()
    {
      return getStringAttribute(FieldNames.HTM);
    }

    /**
     * The value of the HTTP method (Section 9.1 of [RFC9110]) of the request to which the JWT is attached.
     */
    public void setHtm(String htm)
    {
      setAttribute(FieldNames.HTM, htm);
    }

    /**
     * The HTTP target URI (Section 7.1 of [RFC9110]) of the request to which the JWT is attached, without query
     * and fragment parts.
     */
    public Optional<String> getHtu()
    {
      return getStringAttribute(FieldNames.HTU);
    }

    /**
     * The HTTP target URI (Section 7.1 of [RFC9110]) of the request to which the JWT is attached, without query
     * and fragment parts.
     */
    public void setHtu(String htu)
    {
      setAttribute(FieldNames.HTU, htu);
    }

    /**
     * Hash of the access token. The value MUST be the result of a base64url encoding (as defined in Section 2 of
     * [RFC7515]) the SHA-256 [SHS] hash of the ASCII encoding of the associated access token's value.
     */
    public Optional<String> getAth()
    {
      return getStringAttribute(FieldNames.ATH);
    }

    /**
     * Hash of the access token. The value MUST be the result of a base64url encoding (as defined in Section 2 of
     * [RFC7515]) the SHA-256 [SHS] hash of the ASCII encoding of the associated access token's value.
     */
    public void setAth(String ath)
    {
      setAttribute(FieldNames.ATH, ath);
    }

  }

  /** The settings that have been used for the authorization code grant in the last request. */
  public static class AuthCodeParameters extends ScimObjectNode
  {

    public AuthCodeParameters()
    {}

    @Builder
    public AuthCodeParameters(String redirectUri, String queryParameters)
    {
      setRedirectUri(redirectUri);
      setQueryParameters(queryParameters);
    }

    /** The redirect URI that was entered into the frontend input field. */
    public Optional<String> getRedirectUri()
    {
      return getStringAttribute(FieldNames.REDIRECT_URI);
    }

    /** The redirect URI that was entered into the frontend input field. */
    public void setRedirectUri(String redirectUri)
    {
      setAttribute(FieldNames.REDIRECT_URI, redirectUri);
    }

    /** A string representation of query parameters that should be appended to the authorization request */
    public Optional<String> getQueryParameters()
    {
      return getStringAttribute(FieldNames.QUERY_PARAMETERS);
    }

    /** A string representation of query parameters that should be appended to the authorization request */
    public void setQueryParameters(String queryParameters)
    {
      setAttribute(FieldNames.QUERY_PARAMETERS, queryParameters);
    }


  }

  /** The settings that have been used for the client credentials grant in the last request. */
  public static class ClientCredentialsParameters extends ScimObjectNode
  {

    public ClientCredentialsParameters()
    {}

    public ClientCredentialsParameters(String scope)
    {
      setScope(scope);
    }

    /** The optional scope parameter to set the scope of the access token. */
    public String getScope()
    {
      return getStringAttribute(FieldNames.SCOPE).orElse(null);
    }

    /** The optional scope parameter to set the scope of the access token. */
    public void setScope(String scope)
    {
      setAttribute(FieldNames.SCOPE, scope);
    }


  }

  /** The settings that have been used for the resource owner password credentials grant in the last request. */
  public static class ResourceOwnerPasswordParameters extends ScimObjectNode
  {

    public ResourceOwnerPasswordParameters()
    {}

    @Builder
    public ResourceOwnerPasswordParameters(String username, String password, String scope)
    {
      setUsername(username);
      setPassword(password);
      setScope(scope);
    }

    /** The username that should be used to authenticate at the identity provider. */
    public String getUsername()
    {
      return getStringAttribute(FieldNames.USERNAME).orElse(null);
    }

    /** The username that should be used to authenticate at the identity provider. */
    public void setUsername(String username)
    {
      setAttribute(FieldNames.USERNAME, username);
    }

    /** The password that should be used to authenticate at the identity provider with the given username. */
    public String getPassword()
    {
      return getStringAttribute(FieldNames.PASSWORD).orElse(null);
    }

    /** The password that should be used to authenticate at the identity provider with the given username. */
    public void setPassword(String password)
    {
      setAttribute(FieldNames.PASSWORD, password);
    }

    /** The optional scope parameter to set the scope of the access token. */
    public String getScope()
    {
      return getStringAttribute(FieldNames.SCOPE).orElse(null);
    }

    /** The optional scope parameter to set the scope of the access token. */
    public void setScope(String scope)
    {
      setAttribute(FieldNames.SCOPE, scope);
    }


  }

  public static class FieldNames
  {

    public static final String SCHEMA_ID = "urn:ietf:params:scim:schemas:captaingoldfish:2.0:CurrentWorkflowSettings";

    public static final String REDIRECT_URI = "redirectUri";

    public static final String PASSWORD = "password";

    public static final String QUERY_PARAMETERS = "queryParameters";

    public static final String CLIENT_CREDENTIALS_PARAMETERS = "clientCredentialsParameters";

    public static final String AUTH_CODE_PARAMETERS = "authCodeParameters";

    public static final String SCOPE = "scope";

    public static final String ID = "id";

    public static final String RESOURCE_OWNER_PASSWORD_PARAMETERS = "resourceOwnerPasswordParameters";

    public static final String OPENID_CLIENT_ID = "openIdClientId";

    public static final String USERNAME = "username";

    public static final String GRANT_TYPE = "grantType";

    public static final String DPOP = "dpop";

    public static final String SIGNATUREALGORITHM = "signatureAlgorithm";

    public static final String USE_DPOP = "useDpop";

    public static final String KEYID = "keyId";

    public static final String NONCE = "nonce";

    public static final String JTI = "jti";

    public static final String HTM = "htm";

    public static final String HTU = "htu";

    public static final String ATH = "ath";
  }
}
