package de.captaingoldfish.restclient.application.endpoints.authcodegrant;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import de.captaingoldfish.restclient.application.endpoints.authcodegrant.validation.AuthCodeGrantRequestValidator;
import de.captaingoldfish.restclient.application.projectconfig.WebAppConfig;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;
import de.captaingoldfish.restclient.database.repositories.OpenIdClientDao;
import de.captaingoldfish.restclient.scim.resources.ScimAuthCodeGrantRequest;
import de.captaingoldfish.restclient.scim.resources.ScimCurrentWorkflowSettings;
import de.captaingoldfish.scim.sdk.common.constants.enums.SortOrder;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import de.captaingoldfish.scim.sdk.common.schemas.SchemaAttribute;
import de.captaingoldfish.scim.sdk.server.endpoints.Context;
import de.captaingoldfish.scim.sdk.server.endpoints.ResourceHandler;
import de.captaingoldfish.scim.sdk.server.endpoints.validation.RequestValidator;
import de.captaingoldfish.scim.sdk.server.filter.FilterNode;
import de.captaingoldfish.scim.sdk.server.response.PartialListResponse;
import lombok.RequiredArgsConstructor;


/**
 * @author Pascal Knueppel
 * @since 20.08.2021
 */
@RequiredArgsConstructor
public class AuthCodeGrantRequestHandler extends ResourceHandler<ScimAuthCodeGrantRequest>
{

  private final AuthCodeGrantRequestService requestService;

  /**
   * builds an authorization code grant request url and returns it to the client
   */
  @Override
  public ScimAuthCodeGrantRequest createResource(ScimAuthCodeGrantRequest resource, Context context)
  {
    OpenIdClientDao openIdClientDao = WebAppConfig.getApplicationContext().getBean(OpenIdClientDao.class);
    OpenIdClient openIdClient = resource.getCurrentWorkflowSettings()
                                        .map(ScimCurrentWorkflowSettings::getOpenIdClientId)
                                        .flatMap(openIdClientDao::findById)
                                        .orElseThrow();
    final String id = UUID.randomUUID().toString(); // random id that has no actual meaning
    final String authCodeGrantUrl = requestService.generateAuthCodeRequestUrl(openIdClient,
                                                                              resource.getCurrentWorkflowSettings()
                                                                                      .orElseThrow());

    return ScimAuthCodeGrantRequest.builder()
                                   .id(id)
                                   .authorizationCodeGrantUrl(authCodeGrantUrl)
                                   .meta(Meta.builder().created(Instant.now()).build())
                                   .build();
  }

  /**
   * returns the authorization code grant response url as it was received from the user after the user was
   * redirected from the identity provider
   */
  @Override
  public ScimAuthCodeGrantRequest getResource(String id,
                                              List<SchemaAttribute> attributes,
                                              List<SchemaAttribute> excludedAttributes,
                                              Context context)
  {
    return null;
  }

  /**
   * endpoint is disabled
   */
  @Override
  public PartialListResponse<ScimAuthCodeGrantRequest> listResources(long startIndex,
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
   * endpoint is disabled
   */
  @Override
  public ScimAuthCodeGrantRequest updateResource(ScimAuthCodeGrantRequest resourceToUpdate, Context context)
  {
    return null;
  }

  /**
   * endpoint is disabled
   */
  @Override
  public void deleteResource(String id, Context context)
  {
    // do nothing
  }

  /**
   * the validator to check that the request contains valid parameters
   */
  @Override
  public RequestValidator<ScimAuthCodeGrantRequest> getRequestValidator()
  {
    return new AuthCodeGrantRequestValidator();
  }
}
