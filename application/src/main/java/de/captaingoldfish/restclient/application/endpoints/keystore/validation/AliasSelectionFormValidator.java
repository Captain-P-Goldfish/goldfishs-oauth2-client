package de.captaingoldfish.restclient.application.endpoints.keystore.validation;

import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import de.captaingoldfish.restclient.application.endpoints.keystore.KeystoreFileCache;
import de.captaingoldfish.restclient.application.projectconfig.WebAppConfig;
import de.captaingoldfish.restclient.database.entities.Keystore;
import de.captaingoldfish.restclient.database.entities.KeystoreEntry;
import de.captaingoldfish.restclient.database.repositories.KeystoreDao;
import de.captaingoldfish.restclient.scim.resources.ScimKeystore;
import de.captaingoldfish.scim.sdk.server.endpoints.validation.ValidationContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


/**
 * @author Pascal Knueppel
 * @since 17.05.2021
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AliasSelectionFormValidator
{


  /**
   * validates the the {@link de.captaingoldfish.restclient.scim.resources.ScimKeystore.AliasSelection} object
   * contains valid content
   */
  @SneakyThrows
  public static void validateAliasSelectionForm(ScimKeystore.AliasSelection aliasSelection,
                                                ValidationContext validationContext)
  {
    List<String> aliases = aliasSelection.getAliases();
    if (aliases.isEmpty())
    {
      String errorMessage = "No alias was selected";
      log.debug(errorMessage);
      validationContext.addError("aliasSelection.aliases", errorMessage);
      return;
    }

    if (aliases.size() != 1)
    {
      String errorMessage = String.format("Only a single alias can be selected but found: %s", aliases);
      log.debug(errorMessage);
      validationContext.addError("aliasSelection.aliases", errorMessage);
      return;
    }

    final String stateId = aliasSelection.getStateId();
    KeystoreFileCache keystoreFileCache = WebAppConfig.getApplicationContext().getBean(KeystoreFileCache.class);
    Keystore keystore = keystoreFileCache.getKeystoreFile(stateId);
    if (keystore == null)
    {
      String errorMessage = String.format("The stateId '%s' is not related to any previously uploaded keystore files",
                                          stateId);
      log.debug(errorMessage);
      validationContext.addError("aliasSelection.stateId", errorMessage);
      return;
    }

    KeyStore javaKeystore = keystore.getKeyStore();
    final String alias = aliases.get(0);
    if (!javaKeystore.containsAlias(alias))
    {
      String errorMessage = String.format("Unknown alias selected: %s", alias);
      log.debug(errorMessage);
      validationContext.addError("aliasSelection.aliases", errorMessage);
    }

    final String privateKeyPassword = Optional.ofNullable(aliasSelection.getPrivateKeyPassword())
                                              .map(StringUtils::stripToNull)
                                              .orElse(keystore.getKeystorePassword());
    try
    {
      char[] password = Optional.ofNullable(privateKeyPassword).map(String::toCharArray).orElse(new char[0]);
      javaKeystore.getKey(alias, password);
    }
    catch (Exception ex)
    {
      log.debug(ex.getMessage(), ex);
      ExceptionUtils.getThrowableList(ex).forEach(exception -> {
        validationContext.addError("aliasSelection.aliases", ex.getMessage());
        validationContext.addError("aliasSelection.privateKeyPassword", ex.getMessage());
      });
    }

    KeystoreDao keystoreDao = WebAppConfig.getApplicationContext().getBean(KeystoreDao.class);
    Keystore applicationKeystore = keystoreDao.getKeystore();
    final String newAlias = Optional.ofNullable(aliasSelection.getAliasOverride())
                                    .map(StringUtils::stripToNull)
                                    .orElse(alias);

    final String aliasPattern = "[A-Za-z0-9_-]+";
    if (!newAlias.matches(aliasPattern))
    {
      String errorMessage = String.format("Invalid alias with value '%s'. The alias is used as url path-parameter "
                                          + "so please use the aliasOverride field to override the alias with a value "
                                          + "that matches the following pattern: %s",
                                          newAlias,
                                          aliasPattern);
      validationContext.addError("aliasSelection.aliases", errorMessage);
      validationContext.addError("aliasSelection.aliasOverride", errorMessage);
    }

    boolean aliasNameAlreadyTaken = applicationKeystore.getKeystoreEntries()
                                                       .stream()
                                                       .map(KeystoreEntry::getAlias)
                                                       .anyMatch(newAlias::equals);
    if (aliasNameAlreadyTaken)
    {
      String errorMessage = String.format("Alias '%s' is already used. Please override this alias with another name",
                                          newAlias);
      log.debug(errorMessage);
      if (newAlias.equals(aliasSelection.getAliasOverride()))
      {
        validationContext.addError("aliasSelection.aliasOverride", errorMessage);
      }
      else
      {
        validationContext.addError("aliasSelection.aliases", errorMessage);
      }
    }

    X509Certificate certificate = (X509Certificate)keystore.getKeyStore().getCertificate(alias);
    String existingUnderAlias = applicationKeystore.getKeyStore().getCertificateAlias(certificate);
    if (existingUnderAlias != null)
    {
      String errorMessage = String.format("The selected Key is already present under the alias '%s'",
                                          existingUnderAlias);
      log.debug(errorMessage);
      validationContext.addError("aliasSelection.aliases", errorMessage);
    }
  }
}
