package de.captaingoldfish.oauthrestclient.application.endpoints.keystore;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import de.captaingoldfish.oauthrestclient.application.endpoints.models.CertificateInfo;
import de.captaingoldfish.oauthrestclient.commons.keyhelper.KeyStoreSupporter;
import de.captaingoldfish.oauthrestclient.database.entities.Keystore;
import de.captaingoldfish.oauthrestclient.database.entities.KeystoreEntry;
import de.captaingoldfish.oauthrestclient.database.repositories.KeystoreDao;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


/**
 * provides the business logic for the application keystore
 * 
 * @author Pascal Knueppel
 * @since 26.03.2021
 */
@Slf4j
@RequiredArgsConstructor
@Service
class KeystoreService
{

  /**
   * to access the application keystore
   */
  private final KeystoreDao keystoreDao;

  /**
   * the cache that is used to hold uploaded keystore files for a specific amount of time
   */
  private final KeystoreFileCache keystoreFileCache;

  /**
   * caches an uploaded keystore and returns all aliases of the keystore for key selection
   */
  @SneakyThrows
  public KeystoreAliasForm uploadKeystore(KeystoreUploadForm keystoreUploadForm)
  {
    final String filename = keystoreUploadForm.getKeystoreFile().getName();
    final byte[] keystoreData = keystoreUploadForm.getKeystoreFile().getBytes();
    final String keystorePassword = keystoreUploadForm.getKeystorePassword();
    final KeyStoreSupporter.KeyStoreType type = KeyStoreSupporter.KeyStoreType.byFileExtension(filename)
                                                                              .orElse(KeyStoreSupporter.KeyStoreType.JKS);
    final String stateId = UUID.randomUUID().toString();
    Keystore tmpKeystore = new Keystore(new ByteArrayInputStream(keystoreData), type, keystorePassword);
    keystoreFileCache.setKeystoreFile(stateId, tmpKeystore);
    return new KeystoreAliasForm(stateId, tmpKeystore.getKeyStore());
  }

  /**
   * merges a new key entry into the application keystore and saves its entry to the database
   */
  public KeystoreEntryInfoForm mergeNewEntryIntoApplicationKeystore(KeystoreAliasForm keystoreAliasForm)
  {
    final String stateId = keystoreAliasForm.getStateId();
    Keystore applicationKeystore = keystoreDao.getKeystore();
    Keystore uploadedKeystore = keystoreFileCache.getKeystoreFile(stateId);

    final String aliasToUse = keystoreAliasForm.getAliases().get(0);
    final String newAlias = Optional.ofNullable(keystoreAliasForm.getAliasOverride())
                                    .map(StringUtils::stripToNull)
                                    .orElse(keystoreAliasForm.getAliases().get(0));
    KeystoreEntry aliasEntry = new KeystoreEntry(aliasToUse, keystoreAliasForm.getPrivateKeyPassword());
    PrivateKey privateKey = uploadedKeystore.getPrivateKey(aliasEntry);
    Certificate certificate = uploadedKeystore.getCertificate(aliasEntry);
    aliasEntry.setAlias(newAlias);
    aliasEntry.setPrivateKeyPassword(Optional.ofNullable(keystoreAliasForm.getPrivateKeyPassword())
                                             .orElse(applicationKeystore.getKeystorePassword()));

    KeyStore mergedKeystore = KeyStoreSupporter.addEntryToKeystore(applicationKeystore.getKeyStore(),
                                                                   newAlias,
                                                                   privateKey,
                                                                   new Certificate[]{certificate},
                                                                   aliasEntry.getPrivateKeyPassword());
    byte[] newKeystoreBytes = KeyStoreSupporter.getBytes(mergedKeystore, applicationKeystore.getKeystorePassword());
    applicationKeystore.setKeystoreBytes(newKeystoreBytes);
    KeystoreEntry aliasPasswords = applicationKeystore.addAliasEntry(aliasEntry);
    keystoreDao.save(applicationKeystore);

    CertificateInfo certificateInfo = new CertificateInfo(applicationKeystore.getCertificate(aliasPasswords));
    return new KeystoreEntryInfoForm(newAlias, certificateInfo);
  }

  /**
   * @return all certificate infos from the application keystore to show which private keys are present
   */
  public List<KeystoreEntryInfoForm> getKeystoreInfos()
  {
    Keystore keystore = keystoreDao.getKeystore();
    return keystore.getKeystoreEntries().stream().map(keystoreAliasPasswords -> {
      CertificateInfo certificateInfo = new CertificateInfo(keystore.getCertificate(keystoreAliasPasswords));
      return new KeystoreEntryInfoForm(keystoreAliasPasswords.getAlias(), certificateInfo);
    }).collect(Collectors.toList());
  }

  /**
   * deletes an entry from the application keystore
   * 
   * @param alias the entry that should be deleted
   */
  @SneakyThrows
  public void deleteKeystoreEntry(String alias)
  {
    Keystore keystore = keystoreDao.getKeystore();
    Optional<KeystoreEntry> keystoreEntryOptional = keystore.getKeystoreEntries()
                                                            .stream()
                                                            .filter(entry -> entry.getAlias().equals(alias))
                                                            .findAny();
    if (keystoreEntryOptional.isPresent())
    {
      KeystoreEntry keystoreEntry = keystoreEntryOptional.get();
      keystore.getKeystoreEntries().remove(keystoreEntry);
      keystore.getKeyStore().deleteEntry(alias);
      byte[] newKeystoreBytes = KeyStoreSupporter.getBytes(keystore.getKeyStore(), keystore.getKeystorePassword());
      keystore.setKeystoreBytes(newKeystoreBytes);
      keystoreDao.save(keystore);
    }
  }
}
