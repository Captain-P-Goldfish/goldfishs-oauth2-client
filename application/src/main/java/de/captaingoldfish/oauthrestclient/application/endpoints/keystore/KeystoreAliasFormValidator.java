package de.captaingoldfish.oauthrestclient.application.endpoints.keystore;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Optional;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import de.captaingoldfish.oauthrestclient.application.projectconfig.WebAppConfig;
import de.captaingoldfish.oauthrestclient.database.entities.Keystore;
import de.captaingoldfish.oauthrestclient.database.entities.KeystoreEntry;
import de.captaingoldfish.oauthrestclient.database.repositories.KeystoreDao;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


/**
 * validates the content of {@link KeystoreAliasForm}
 * 
 * @author Pascal Knueppel
 * @since 27.03.2021
 */
@Slf4j
public class KeystoreAliasFormValidator implements ConstraintValidator<KeystoreAliasFormValidation, KeystoreAliasForm>
{

  /**
   * checks that the data in the {@link KeystoreAliasForm} is valid and is able to produce valid results during
   * further processing
   */
  @SneakyThrows
  @Override
  public boolean isValid(KeystoreAliasForm keystoreAliasForm, ConstraintValidatorContext context)
  {
    boolean isValid = true;

    final String stateId = keystoreAliasForm.getStateId();
    if (StringUtils.isBlank(stateId))
    {
      String errorMessage = "Ups the required stateId parameter was missing in the request...";
      log.debug(errorMessage);
      context.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();
      return false;
    }

    List<String> aliases = keystoreAliasForm.getAliases();
    if (aliases == null || aliases.isEmpty())
    {
      String errorMessage = "No alias was selected";
      log.debug(errorMessage);
      context.buildConstraintViolationWithTemplate(errorMessage).addPropertyNode("aliases").addConstraintViolation();
      return false;
    }

    if (aliases.size() != 1)
    {
      String errorMessage = "Only a single alias can be selected but found: " + aliases;
      log.debug(errorMessage);
      context.buildConstraintViolationWithTemplate(errorMessage).addPropertyNode("aliases").addConstraintViolation();
      return false;
    }

    KeystoreFileCache keystoreFileCache = WebAppConfig.getApplicationContext().getBean(KeystoreFileCache.class);
    Keystore keystore = keystoreFileCache.getKeystoreFile(stateId);
    if (keystore == null)
    {
      String errorMessage = "The stateId '" + stateId + "' could not be resolved to a previously uploaded keystore";
      log.debug(errorMessage);
      context.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();
      return false;
    }

    KeyStore javaKeystore = keystore.getKeyStore();
    final String alias = aliases.get(0);
    if (!javaKeystore.containsAlias(alias))
    {
      String errorMessage = "Unknown alias selected: " + alias;
      log.debug(errorMessage);
      context.buildConstraintViolationWithTemplate(errorMessage).addPropertyNode("aliases").addConstraintViolation();
      isValid = false;
    }

    final String privateKeyPassword = Optional.ofNullable(keystoreAliasForm.getPrivateKeyPassword())
                                              .map(StringUtils::stripToNull)
                                              .orElse(keystore.getKeystorePassword());
    PrivateKey privateKey = null;
    try
    {
      char[] password = Optional.ofNullable(privateKeyPassword).map(String::toCharArray).orElse(new char[0]);
      privateKey = (PrivateKey)javaKeystore.getKey(alias, password);
    }
    catch (Exception ex)
    {
      log.debug(ex.getMessage(), ex);
      ExceptionUtils.getThrowableList(ex).forEach(exception -> {
        context.buildConstraintViolationWithTemplate(ex.getMessage())
               .addPropertyNode("aliases")
               .addConstraintViolation();
        context.buildConstraintViolationWithTemplate(ex.getMessage())
               .addPropertyNode("privateKeyPassword")
               .addConstraintViolation();
      });
      isValid = false;
    }

    if (privateKey == null)
    {
      String errorMessage = "Could not access private key of alias '" + alias + "'";
      log.debug(errorMessage);
      context.buildConstraintViolationWithTemplate(errorMessage)
             .addPropertyNode("privateKeyPassword")
             .addConstraintViolation();
      isValid = false;
    }

    KeystoreDao keystoreDao = WebAppConfig.getApplicationContext().getBean(KeystoreDao.class);
    Keystore applicationKeystore = keystoreDao.getKeystore();
    final String newAlias = Optional.ofNullable(keystoreAliasForm.getAliasOverride())
                                    .map(StringUtils::stripToNull)
                                    .orElse(keystoreAliasForm.getAliases().get(0));
    boolean aliasNameAlreadyTaken = applicationKeystore.getKeystoreEntries()
                                                       .stream()
                                                       .map(KeystoreEntry::getAlias)
                                                       .anyMatch(newAlias::equals);
    if (aliasNameAlreadyTaken)
    {
      String errorMessage = "Alias '" + newAlias + "' is already used. Please override this alias with another name";
      log.debug(errorMessage);
      if (newAlias.equals(keystoreAliasForm.getAliasOverride()))
      {
        context.buildConstraintViolationWithTemplate(errorMessage)
               .addPropertyNode("aliasOverride")
               .addConstraintViolation();
      }
      else
      {
        context.buildConstraintViolationWithTemplate(errorMessage).addPropertyNode("aliases").addConstraintViolation();
      }
      isValid = false;
    }

    X509Certificate certificate = (X509Certificate)keystore.getKeyStore().getCertificate(alias);
    String existingUnderAlias = applicationKeystore.getKeyStore().getCertificateAlias(certificate);
    if (existingUnderAlias != null)
    {
      String errorMessage = "The selected Key is already present under the alias '" + existingUnderAlias + "'";
      log.debug(errorMessage);
      context.buildConstraintViolationWithTemplate(errorMessage).addPropertyNode("aliases").addConstraintViolation();
      isValid = false;
    }

    return isValid;
  }
}
