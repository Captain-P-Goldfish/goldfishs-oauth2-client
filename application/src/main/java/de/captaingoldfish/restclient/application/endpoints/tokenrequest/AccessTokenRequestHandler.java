package de.captaingoldfish.restclient.application.endpoints.tokenrequest;

import java.util.List;

import de.captaingoldfish.restclient.application.endpoints.tokenrequest.validation.AccessTokenRequestValidator;
import de.captaingoldfish.restclient.application.projectconfig.WebAppConfig;
import de.captaingoldfish.restclient.scim.resources.ScimAccessTokenRequest;
import de.captaingoldfish.scim.sdk.common.constants.enums.SortOrder;
import de.captaingoldfish.scim.sdk.common.schemas.SchemaAttribute;
import de.captaingoldfish.scim.sdk.server.endpoints.Context;
import de.captaingoldfish.scim.sdk.server.endpoints.ResourceHandler;
import de.captaingoldfish.scim.sdk.server.endpoints.validation.RequestValidator;
import de.captaingoldfish.scim.sdk.server.filter.FilterNode;
import de.captaingoldfish.scim.sdk.server.response.PartialListResponse;


/**
 * @author Pascal Knueppel
 * @since 21.08.2021
 */
public class AccessTokenRequestHandler extends ResourceHandler<ScimAccessTokenRequest>
{


  /**
   * used to initiate an access token request, to gather all information about the request and response and to
   * return this information to the frontend for display purposes
   */
  @Override
  public ScimAccessTokenRequest createResource(ScimAccessTokenRequest resource, Context context)
  {
    AuthCodeAccessTokenRequestHandler requestHandler = WebAppConfig.getApplicationContext()
                                                                   .getBean(AuthCodeAccessTokenRequestHandler.class);
    return requestHandler.handleAccessTokenRequest(resource.getOpenIdClientId(),
                                                   resource.getAuthorizationCode().orElse(null),
                                                   resource.getRedirectUri().orElse(null));
  }

  /**
   * endpoint disabled
   */
  @Override
  public ScimAccessTokenRequest getResource(String id,
                                            List<SchemaAttribute> attributes,
                                            List<SchemaAttribute> excludedAttributes,
                                            Context context)
  {
    return null;
  }

  /**
   * endpoint disabled
   */
  @Override
  public PartialListResponse<ScimAccessTokenRequest> listResources(long startIndex,
                                                                   int count,
                                                                   FilterNode filter,
                                                                   SchemaAttribute sortBy,
                                                                   SortOrder sortOrder,
                                                                   List<SchemaAttribute> attributes,
                                                                   List<SchemaAttribute> excludedAttributes,
                                                                   Context context)
  {
    return null;
  }

  /**
   * endpoint disabled
   */
  @Override
  public ScimAccessTokenRequest updateResource(ScimAccessTokenRequest resourceToUpdate, Context context)
  {
    return null;
  }

  /**
   * endpoint disabled
   */
  @Override
  public void deleteResource(String id, Context context)
  {
    // do nothing
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RequestValidator<ScimAccessTokenRequest> getRequestValidator()
  {
    return new AccessTokenRequestValidator();
  }
}
