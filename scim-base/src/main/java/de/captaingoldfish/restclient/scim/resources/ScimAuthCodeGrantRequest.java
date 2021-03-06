package de.captaingoldfish.restclient.scim.resources;

import java.util.Collections;
import java.util.Optional;

import de.captaingoldfish.scim.sdk.common.resources.ResourceNode;
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
                                  String authorizationCodeGrantUrl,
                                  String authorizationResponseUrl,
                                  ScimCurrentWorkflowSettings currentWorkflowSettings,
                                  Meta meta)
  {
    setSchemas(Collections.singletonList(FieldNames.SCHEMA_ID));
    setId(id);
    setAuthorizationCodeGrantUrl(authorizationCodeGrantUrl);
    setAuthorizationResponseUrl(authorizationResponseUrl);
    setCurrentWorkflowSettings(currentWorkflowSettings);
    setMeta(meta);
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

  public static class FieldNames
  {

    public static final String SCHEMA_ID = "urn:ietf:params:scim:schemas:captaingoldfish:2.0:AuthCodeGrantRequest";

    public static final String AUTHORIZATION_CODE_GRANT_URL = "authorizationCodeGrantUrl";

    public static final String ID = "id";

    public static final String AUTHORIZATION_RESPONSE_URL = "authorizationResponseUrl";
  }
}
