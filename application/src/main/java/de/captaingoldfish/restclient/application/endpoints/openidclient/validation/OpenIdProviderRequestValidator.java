package de.captaingoldfish.restclient.application.endpoints.openidclient.validation;

import java.util.Optional;
import java.util.function.Supplier;

import de.captaingoldfish.restclient.application.projectconfig.WebAppConfig;
import de.captaingoldfish.restclient.database.entities.Keystore;
import de.captaingoldfish.restclient.database.entities.OpenIdProvider;
import de.captaingoldfish.restclient.database.repositories.KeystoreDao;
import de.captaingoldfish.restclient.database.repositories.OpenIdClientDao;
import de.captaingoldfish.restclient.database.repositories.OpenIdProviderDao;
import de.captaingoldfish.restclient.scim.resources.ScimOpenIdClient;
import de.captaingoldfish.scim.sdk.common.constants.HttpStatus;
import de.captaingoldfish.scim.sdk.server.endpoints.validation.RequestValidator;
import de.captaingoldfish.scim.sdk.server.endpoints.validation.ValidationContext;


/**
 * @author Pascal Knueppel
 * @since 28.05.2021
 */
public class OpenIdProviderRequestValidator implements RequestValidator<ScimOpenIdClient>
{

  /**
   * validates the incoming resource for validity
   */
  @Override
  public void validateCreate(ScimOpenIdClient resource, ValidationContext validationContext)
  {
    if (validationContext.hasErrors())
    {
      return;
    }
    validateAliasReferences(resource, validationContext);

    Optional<OpenIdProvider> provider = validateOpenIdProvider(resource, validationContext);
    if (provider.isEmpty())
    {
      return;
    }

    OpenIdClientDao openIdClientDao = WebAppConfig.getApplicationContext().getBean(OpenIdClientDao.class);
    openIdClientDao.findByClientIdAndOpenIdProvider(resource.getClientId(), provider.get()).ifPresent(openIdClient -> {
      validationContext.addError("clientId",
                                 String.format("A client with this clientId '%s' was already registered",
                                               resource.getClientId()));
    });
  }

  /**
   * validates the incoming resource for validity
   */
  @Override
  public void validateUpdate(Supplier<ScimOpenIdClient> oldResourceSupplier,
                             ScimOpenIdClient newResource,
                             ValidationContext validationContext)
  {
    if (validationContext.hasErrors())
    {
      return;
    }

    validateAliasReferences(newResource, validationContext);

    Optional<OpenIdProvider> provider = validateOpenIdProvider(newResource, validationContext);
    if (provider.isEmpty())
    {
      return;
    }

    ScimOpenIdClient oldClient = oldResourceSupplier.get();
    final boolean changedClientId = !oldClient.getClientId().equals(newResource.getClientId());
    if (changedClientId)
    {
      OpenIdClientDao openIdClientDao = WebAppConfig.getApplicationContext().getBean(OpenIdClientDao.class);
      openIdClientDao.findByClientIdAndOpenIdProvider(newResource.getClientId(), provider.get())
                     .ifPresent(openIdClient -> {
                       validationContext.setHttpResponseStatus(HttpStatus.CONFLICT);
                       validationContext.addError("clientId",
                                                  String.format("A client with this clientId '%s' was already registered. "
                                                                + "Failed to alter attribute clientId",
                                                                newResource.getClientId()));
                     });
    }
  }

  /**
   * checks if the ID of the given OpenID Provider that acts as parent of the given client does exist and
   * returns an error if it does not exist
   */
  private Optional<OpenIdProvider> validateOpenIdProvider(ScimOpenIdClient resource,
                                                          ValidationContext validationContext)
  {
    OpenIdProviderDao openIdProviderDao = WebAppConfig.getApplicationContext().getBean(OpenIdProviderDao.class);
    Optional<OpenIdProvider> provider = openIdProviderDao.findById(resource.getOpenIdProviderId());
    if (provider.isEmpty())
    {
      validationContext.addError("openIdProviderId",
                                 String.format("No OpenID Provider with ID '%s' does exist",
                                               resource.getOpenIdProviderId()));
    }
    return provider;
  }

  /**
   * checks if the referenced aliases in from the resource do exist and can be used
   * 
   * @param resource the resource that may contain alias references to the application keystore
   */
  private void validateAliasReferences(ScimOpenIdClient resource, ValidationContext validationContext)
  {
    KeystoreDao keystoreDao = WebAppConfig.getApplicationContext().getBean(KeystoreDao.class);
    Keystore applicationKeystore = keystoreDao.getKeystore();
    validateAliasReference(applicationKeystore,
                           resource.getSigningKeyRef(),
                           ScimOpenIdClient.FieldNames.SIGNING_KEY_REF,
                           validationContext);
    validateAliasReference(applicationKeystore,
                           resource.getDecryptionKeyRef(),
                           ScimOpenIdClient.FieldNames.DECRYPTION_KEY_REF,
                           validationContext);
  }

  /**
   * checks if the referenced alias does exist within the application keystore. If not an error will be returned
   */
  private void validateAliasReference(Keystore applicationKeystore,
                                      Optional<String> aliasReference,
                                      String fieldNameForError,
                                      ValidationContext validationContext)
  {
    if (aliasReference.isEmpty())
    {
      return;
    }
    final String alias = aliasReference.get();
    final boolean hasAlias = applicationKeystore.getKeyStoreAliases().contains(alias);
    if (!hasAlias)
    {
      validationContext.addError(fieldNameForError,
                                 String.format("Alias '%s' does not exist within application keystore", alias));
    }
  }
}
