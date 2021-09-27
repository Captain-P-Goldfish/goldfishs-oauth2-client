package de.captaingoldfish.restclient.application.endpoints.httpclient.validation;

import java.util.Optional;
import java.util.function.Supplier;

import de.captaingoldfish.restclient.application.projectconfig.WebAppConfig;
import de.captaingoldfish.restclient.application.utils.Utils;
import de.captaingoldfish.restclient.database.entities.HttpClientSettings;
import de.captaingoldfish.restclient.database.entities.Keystore;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;
import de.captaingoldfish.restclient.database.repositories.HttpClientSettingsDao;
import de.captaingoldfish.restclient.database.repositories.KeystoreDao;
import de.captaingoldfish.restclient.database.repositories.OpenIdClientDao;
import de.captaingoldfish.restclient.database.repositories.ProxyDao;
import de.captaingoldfish.restclient.scim.resources.ScimHttpClientSettings;
import de.captaingoldfish.scim.sdk.common.constants.HttpStatus;
import de.captaingoldfish.scim.sdk.server.endpoints.Context;
import de.captaingoldfish.scim.sdk.server.endpoints.validation.RequestValidator;
import de.captaingoldfish.scim.sdk.server.endpoints.validation.ValidationContext;


/**
 * @author Pascal Knueppel
 * @since 01.06.2021
 */
public class HttpClientSettingsValidator implements RequestValidator<ScimHttpClientSettings>
{

  /**
   * validates if a {@link HttpClientSettings} resource is valid when it is created
   */
  @Override
  public void validateCreate(ScimHttpClientSettings resource,
                             ValidationContext validationContext,
                             Context requestContext)
  {
    if (validationContext.hasErrors())
    {
      return;
    }

    Optional<Long> openIdClientReference = resource.getOpenIdClientReference();
    if (openIdClientReference.isPresent())
    {
      OpenIdClientDao openIdClientDao = WebAppConfig.getApplicationContext().getBean(OpenIdClientDao.class);
      Optional<OpenIdClient> openIdClientOptional = openIdClientDao.findById(openIdClientReference.get());
      if (openIdClientOptional.isPresent())
      {
        OpenIdClient openIdClient = openIdClientOptional.get();
        HttpClientSettingsDao httpClientSettingsDao = WebAppConfig.getApplicationContext()
                                                                  .getBean(HttpClientSettingsDao.class);
        Optional<HttpClientSettings> optionalHttpClientSettings = httpClientSettingsDao.findByOpenIdClient(openIdClient);
        if (optionalHttpClientSettings.isPresent())
        {
          String errorMessage = String.format("Cannot create a second child for OpenID Client with ID '%s'",
                                              openIdClient.getId());
          validationContext.addError(ScimHttpClientSettings.FieldNames.OPEN_ID_CLIENT_REFERENCE, errorMessage);
        }
      }
      else
      {
        String errorMessage = String.format("No OpenID Client with ID '%s' does exist", openIdClientReference.get());
        validationContext.addError(ScimHttpClientSettings.FieldNames.OPEN_ID_CLIENT_REFERENCE, errorMessage);
      }
    }

    validateProxyReference(resource, validationContext);
    validateTlsClientAuthAliasReference(resource.getTlsClientAuthAliasReference(), validationContext);
  }

  /**
   * validates if a {@link HttpClientSettings} resource is valid when it is updated
   */
  @Override
  public void validateUpdate(Supplier<ScimHttpClientSettings> oldResourceSupplier,
                             ScimHttpClientSettings newResource,
                             ValidationContext validationContext,
                             Context requestContext)
  {
    if (validationContext.hasErrors())
    {
      return;
    }
    HttpClientSettingsDao httpClientSettingsDao = WebAppConfig.getApplicationContext()
                                                              .getBean(HttpClientSettingsDao.class);
    Optional<HttpClientSettings> optionalHttpClientSettings = newResource.getId()
                                                                         .map(Utils::parseId)
                                                                         .flatMap(httpClientSettingsDao::findById);
    if (optionalHttpClientSettings.isEmpty())
    {
      validationContext.setHttpResponseStatus(HttpStatus.NOT_FOUND);
      validationContext.addError(String.format("Resource with ID '%s' does not exist",
                                               newResource.getId().orElseThrow()));
      return;
    }

    ScimHttpClientSettings oldHttpClientSettings = oldResourceSupplier.get();
    boolean isOpenIdClientReferenceEquals = oldHttpClientSettings.getOpenIdClientReference()
                                                                 .orElse(0L)
                                                                 .equals(newResource.getOpenIdClientReference()
                                                                                    .orElse(0L));
    if (!isOpenIdClientReferenceEquals)
    {
      String errorMessage = "The parent reference to the OpenID Client must not be changed";
      validationContext.addError(ScimHttpClientSettings.FieldNames.OPEN_ID_CLIENT_REFERENCE, errorMessage);
      return;
    }
    validateProxyReference(newResource, validationContext);
    validateTlsClientAuthAliasReference(newResource.getTlsClientAuthAliasReference(), validationContext);
  }

  /**
   * checks the proxy reference if it does exist
   */
  private void validateProxyReference(ScimHttpClientSettings resource, ValidationContext validationContext)
  {

    Optional<Long> proxyReference = resource.getProxyReference();
    if (proxyReference.isPresent())
    {
      ProxyDao proxyDao = WebAppConfig.getApplicationContext().getBean(ProxyDao.class);
      if (proxyDao.findById(proxyReference.get()).isEmpty())
      {
        String errorMessage = String.format("No Proxy with ID '%s' does exist", proxyReference.get());
        validationContext.addError(ScimHttpClientSettings.FieldNames.PROXY_REFERENCE, errorMessage);
      }
    }
  }

  /**
   * checks if the referenced alias does exist within the application keystore. If not an error will be returned
   */
  private void validateTlsClientAuthAliasReference(Optional<String> aliasReference, ValidationContext validationContext)
  {
    KeystoreDao keystoreDao = WebAppConfig.getApplicationContext().getBean(KeystoreDao.class);
    Keystore applicationKeystore = keystoreDao.getKeystore();
    if (aliasReference.isEmpty())
    {
      return;
    }
    final String alias = aliasReference.get();
    final boolean hasAlias = applicationKeystore.getKeyStoreAliases().contains(alias);
    if (!hasAlias)
    {
      validationContext.addError(ScimHttpClientSettings.FieldNames.TLS_CLIENT_AUTH_ALIAS_REFERENCE,
                                 String.format("Alias '%s' does not exist within application keystore", alias));
    }
  }
}
