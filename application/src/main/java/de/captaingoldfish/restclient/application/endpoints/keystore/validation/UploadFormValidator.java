package de.captaingoldfish.restclient.application.endpoints.keystore.validation;

import java.io.EOFException;
import java.security.KeyStore;
import java.util.Base64;
import java.util.Enumeration;
import java.util.Optional;

import de.captaingoldfish.restclient.commons.keyhelper.KeyStoreSupporter;
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
public final class UploadFormValidator
{

  /**
   * validates the the {@link de.captaingoldfish.restclient.scim.resources.ScimKeystore.FileUpload} object
   * contains valid content
   */
  @SneakyThrows
  public static void validateUploadForm(ScimKeystore.FileUpload fileUpload, ValidationContext validationContext)
  {
    byte[] decodedKeystoreFile;
    try
    {
      decodedKeystoreFile = Base64.getDecoder().decode(fileUpload.getKeystoreFile());
    }
    catch (IllegalArgumentException ex)
    {
      validationContext.addError("fileUpload.keystoreFile",
                                 String.format("Keystore file is not Base64 encoded: %s", ex.getMessage()));
      return;
    }

    if (decodedKeystoreFile == null || decodedKeystoreFile.length == 0)
    {
      validationContext.addError("fileUpload.keystoreFile", "Keystore file must not be empty");
      return;
    }

    KeyStore keyStore;
    try
    {
      final byte[] keystoreBytes = decodedKeystoreFile;
      final String fileName = fileUpload.getKeystoreFileName().orElse(null);
      final KeyStoreSupporter.KeyStoreType keyStoreType = KeyStoreSupporter.KeyStoreType.byFileExtension(fileName)
                                                                                        .orElse(KeyStoreSupporter.KeyStoreType.JKS);
      final String password = fileUpload.getKeystorePassword();
      keyStore = KeyStoreSupporter.readKeyStore(keystoreBytes, keyStoreType, password);
    }
    catch (Exception ex)
    {
      log.debug(ex.getMessage(), ex);
      if (EOFException.class.equals(ex.getClass()))
      {
        validationContext.addError("fileUpload.keystoreFile", "File is not a valid keystore file");
        return;
      }
      Throwable current = ex;
      while (current != null)
      {
        String errormessage = Optional.ofNullable(current.getMessage()).orElse("NullPointerException");
        if (errormessage.contains("exception unwrapping private key"))
        {
          String errormessage2 = "key-passwords must match keystore-password. This is a restriction of the "
                                 + "BouncyCastle PKCS12 implementation. As a workaround rename the file extension "
                                 + "to '.jks' this may work based on the used JDK";
          log.debug(errormessage2);
          validationContext.addError("fileUpload.keystoreFile", errormessage2);
        }
        log.debug(errormessage);
        validationContext.addError("fileUpload.keystoreFile", errormessage);
        current = current.getCause();
      }
      return;
    }

    Enumeration<String> aliases = keyStore.aliases();
    boolean aliasErrorFound = false;
    while (aliases.hasMoreElements())
    {
      String alias = aliases.nextElement();
      if (alias.contains("/"))
      {
        if (!aliasErrorFound)
        {
          validationContext.addError("fileUpload.keystoreFile",
                                     "This keystore cannot be handled for illegal character in alias");
        }
        aliasErrorFound = true;
        validationContext.addError("fileUpload.keystoreFile",
                                   String.format("The alias '%s' contains an illegal character '/'", alias));
      }
    }
  }
}
