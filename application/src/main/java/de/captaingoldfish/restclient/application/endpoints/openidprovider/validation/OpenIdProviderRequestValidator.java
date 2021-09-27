package de.captaingoldfish.restclient.application.endpoints.openidprovider.validation;

import java.util.Optional;
import java.util.function.Supplier;

import de.captaingoldfish.restclient.application.endpoints.openidprovider.OpenIdProviderConverter;
import de.captaingoldfish.restclient.database.entities.OpenIdProvider;
import de.captaingoldfish.restclient.database.repositories.OpenIdProviderDao;
import de.captaingoldfish.restclient.scim.resources.ScimOpenIdProvider;
import de.captaingoldfish.scim.sdk.server.endpoints.Context;
import de.captaingoldfish.scim.sdk.server.endpoints.validation.RequestValidator;
import de.captaingoldfish.scim.sdk.server.endpoints.validation.ValidationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * @author Pascal Knueppel
 * @since 21.05.2021
 */
@Slf4j
@RequiredArgsConstructor
public class OpenIdProviderRequestValidator implements RequestValidator<ScimOpenIdProvider>
{

  /**
   * to check that are no conflicting entities are within the database
   */
  private final OpenIdProviderDao openIdProviderDao;

  /**
   * checks that the name of the {@link OpenIdProvider} is not already taken and more
   */
  @Override
  public void validateCreate(ScimOpenIdProvider resource, ValidationContext validationContext, Context requestContext)
  {
    if (validationContext.hasErrors())
    {
      return;
    }
    Optional<OpenIdProvider> duplicateProvider = openIdProviderDao.findByName(resource.getName());
    if (duplicateProvider.isPresent())
    {
      validationContext.addError(ScimOpenIdProvider.FieldNames.NAME,
                                 String.format("A provider with name '%s' does already exist", resource.getName()));
      return;
    }
    validateFields(resource, validationContext);
  }

  /**
   * @see #validateFields(ScimOpenIdProvider, ValidationContext)
   */
  @Override
  public void validateUpdate(Supplier<ScimOpenIdProvider> oldResourceSupplier,
                             ScimOpenIdProvider newResource,
                             ValidationContext validationContext,
                             Context requestContext)
  {
    validateFields(newResource, validationContext);
  }

  /**
   * verifies that at least the discovery endpoint or the authorization endpoint and the token endpoint are
   * given and validates the content of the signature verification key
   */
  protected void validateFields(ScimOpenIdProvider newResource, ValidationContext validationContext)
  {
    Optional<String> discoveryEndpoint = newResource.getDiscoveryEndpoint();
    Optional<String> authorizationEndpoint = newResource.getAuthorizationEndpoint();
    Optional<String> tokenEndpoint = newResource.getTokenEndpoint();

    if (discoveryEndpoint.isEmpty() && (authorizationEndpoint.isEmpty() || tokenEndpoint.isEmpty()))
    {
      validationContext.addError(String.format("Either the '%s' field or the '%s' and '%s' fields must be present.",
                                               ScimOpenIdProvider.FieldNames.DISCOVERY_ENDPOINT,
                                               ScimOpenIdProvider.FieldNames.AUTHORIZATION_ENDPOINT,
                                               ScimOpenIdProvider.FieldNames.TOKEN_ENDPOINT));
    }

    validateSignatureVerificationKey(newResource, validationContext);
  }

  /**
   * checks that the signature verification key is either a certificate with a public key or a plain public key
   * instance
   */
  private void validateSignatureVerificationKey(ScimOpenIdProvider newResource, ValidationContext validationContext)
  {
    Optional<String> b64PublicKey = newResource.getSignatureVerificationKey();
    if (b64PublicKey.isEmpty())
    {
      return;
    }
    try
    {
      OpenIdProviderConverter.parseSignatureVerificationKey(newResource);
    }
    catch (IllegalArgumentException ex)
    {
      validationContext.addError(ScimOpenIdProvider.FieldNames.SIGNATURE_VERIFICATION_KEY,
                                 String.format(ex.getMessage(), ex.getMessage()));
    }
  }
}
