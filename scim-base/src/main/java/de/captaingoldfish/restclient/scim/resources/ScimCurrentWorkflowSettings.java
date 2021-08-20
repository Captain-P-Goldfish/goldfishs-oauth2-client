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
                                     AuthCodeParameters authCodeParameters,
                                     ResourceOwnerPasswordParameters resourceOwnerPasswordParameters,
                                     Meta meta)
  {
    setSchemas(Collections.singletonList(FieldNames.SCHEMA_ID));
    setId(id);
    setOpenIdClientId(openIdClientId);
    setAuthCodeParameters(authCodeParameters);
    setResourceOwnerPasswordParameters(resourceOwnerPasswordParameters);
    setMeta(meta);
  }

  /** The foreign key reference to an open-id-client resource. */
  public Long getOpenIdClientId()
  {
    return getLongAttribute(FieldNames.OPENID_CLIENT_ID).orElse(null);
  }

  /** The foreign key reference to an open-id-client resource. */
  public void setOpenIdClientId(Long idOfClient)
  {
    setAttribute(FieldNames.OPENID_CLIENT_ID, idOfClient);
  }

  /** The settings that have been used for the authorization code grant in the last request. */
  public Optional<AuthCodeParameters> getAuthCodeParameters()
  {
    return getObjectAttribute(FieldNames.AUTH_CODE_PARAMETERS, AuthCodeParameters.class);
  }

  /** The settings that have been used for the authorization code grant in the last request. */
  public void setAuthCodeParameters(AuthCodeParameters authCodeParameters)
  {
    setAttribute(FieldNames.AUTH_CODE_PARAMETERS,
                 Optional.ofNullable(authCodeParameters).map(ScimObjectNode::isEmpty).orElse(false) ? null
                   : authCodeParameters);
  }

  /** The settings that have been used for the resource owner password credentials grant in the last request. */
  public Optional<ResourceOwnerPasswordParameters> getResourceOwnerPasswordParameters()
  {
    return getObjectAttribute(FieldNames.RESOURCE_OWNER_PASSWORD_PARAMETERS, ResourceOwnerPasswordParameters.class);
  }

  /** The settings that have been used for the resource owner password credentials grant in the last request. */
  public void setResourceOwnerPasswordParameters(ResourceOwnerPasswordParameters resourceOwnerPasswordParameters)
  {
    setAttribute(FieldNames.RESOURCE_OWNER_PASSWORD_PARAMETERS,
                 Optional.ofNullable(resourceOwnerPasswordParameters).map(ScimObjectNode::isEmpty).orElse(false) ? null
                   : resourceOwnerPasswordParameters);
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
    public String getRedirectUri()
    {
      return getStringAttribute(FieldNames.REDIRECT_URI).orElse(null);
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

  /** The settings that have been used for the resource owner password credentials grant in the last request. */
  public static class ResourceOwnerPasswordParameters extends ScimObjectNode
  {

    public ResourceOwnerPasswordParameters()
    {}

    @Builder
    public ResourceOwnerPasswordParameters(String username, String password)
    {
      setUsername(username);
      setPassword(password);
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


  }

  public static class FieldNames
  {

    public static final String SCHEMA_ID = "urn:ietf:params:scim:schemas:captaingoldfish:2.0:CurrentWorkflowSettings";

    public static final String REDIRECT_URI = "redirectUri";

    public static final String PASSWORD = "password";

    public static final String QUERY_PARAMETERS = "queryParameters";

    public static final String AUTH_CODE_PARAMETERS = "authCodeParameters";

    public static final String ID = "id";

    public static final String OPENID_CLIENT_ID = "openIdClientId";

    public static final String RESOURCE_OWNER_PASSWORD_PARAMETERS = "resourceOwnerPasswordParameters";

    public static final String USERNAME = "username";
  }
}
