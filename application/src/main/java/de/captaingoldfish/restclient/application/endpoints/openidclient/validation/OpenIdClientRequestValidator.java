package de.captaingoldfish.restclient.application.endpoints.openidclient.validation;

import java.util.Optional;
import java.util.function.Supplier;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.util.ArrayUtils;

import de.captaingoldfish.restclient.application.projectconfig.WebAppConfig;
import de.captaingoldfish.restclient.database.entities.Keystore;
import de.captaingoldfish.restclient.database.entities.OpenIdProvider;
import de.captaingoldfish.restclient.database.repositories.KeystoreDao;
import de.captaingoldfish.restclient.database.repositories.OpenIdClientDao;
import de.captaingoldfish.restclient.database.repositories.OpenIdProviderDao;
import de.captaingoldfish.restclient.scim.resources.ScimOpenIdClient;
import de.captaingoldfish.scim.sdk.common.constants.HttpStatus;
import de.captaingoldfish.scim.sdk.server.endpoints.Context;
import de.captaingoldfish.scim.sdk.server.endpoints.validation.RequestValidator;
import de.captaingoldfish.scim.sdk.server.endpoints.validation.ValidationContext;


/**
 * @author Pascal Knueppel
 * @since 28.05.2021
 */
public class OpenIdClientRequestValidator implements RequestValidator<ScimOpenIdClient>
{

  /**
   * validates the incoming resource for validity
   */
  @Override
  public void validateCreate(ScimOpenIdClient resource, ValidationContext validationContext, Context requestContext)
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
    validateAuthenticationDetails(resource, validationContext);
  }

  /**
   * validates the incoming resource for validity
   */
  @Override
  public void validateUpdate(Supplier<ScimOpenIdClient> oldResourceSupplier,
                             ScimOpenIdClient newResource,
                             ValidationContext validationContext,
                             Context requestContext)
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
    validateAuthenticationDetails(newResource, validationContext);
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
    validateSignatureAlgorithm(resource, validationContext);
    return provider;
  }

  /**
   * verifies that the given signature algorithm is either empty to use a default algorithm or is set to a valid
   * supported algorithm. The check does not include a check if the algorithm and the key will fit together
   */
  private void validateSignatureAlgorithm(ScimOpenIdClient resource, ValidationContext validationContext)
  {
    if (resource.getSignatureAlgorithm().isPresent())
    {
      // @formatter:off
      JWSAlgorithm.Family supportedAlgorithms = new JWSAlgorithm.Family(ArrayUtils
        .concat(
          JWSAlgorithm.Family.RSA.toArray(new JWSAlgorithm[]{}),
          JWSAlgorithm.Family.EC.toArray(new JWSAlgorithm[]{})
        )
      );
      // @formatter:on
      boolean isNotSupportedAlgorithm = supportedAlgorithms.stream().noneMatch(supportedAlgorithm -> {
        return supportedAlgorithm.getName().equals(resource.getSignatureAlgorithm().get());
      });
      if (isNotSupportedAlgorithm)
      {
        validationContext.addError(ScimOpenIdClient.FieldNames.SIGNATURE_ALGORITHM,
                                   String.format("Unsupported algorithm found '%s'. Only RSA and EC algorithms "
                                                 + "are supported",
                                                 resource.getSignatureAlgorithm().get()));
      }
    }
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

  /**
   * checks that the setup authentication details are valid
   *
   * @param resource the resource to create or update
   */
  private void validateAuthenticationDetails(ScimOpenIdClient resource, ValidationContext validationContext)
  {
    if ("basic".equals(resource.getAuthenticationType()))
    {
      if (resource.getClientSecret().isEmpty())
      {
        validationContext.addError(ScimOpenIdClient.FieldNames.CLIENT_SECRET, "ClientSecret must be present");
      }
    }

    if ("jwt".equals(resource.getAuthenticationType()))
    {
      if (resource.getSigningKeyRef().isEmpty())
      {
        validationContext.addError(ScimOpenIdClient.FieldNames.SIGNING_KEY_REF, "SigningKey must be present");
      }
    }
  }
}
