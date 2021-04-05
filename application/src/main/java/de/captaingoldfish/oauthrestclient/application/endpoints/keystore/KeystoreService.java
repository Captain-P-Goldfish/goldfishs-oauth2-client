package de.captaingoldfish.oauthrestclient.application.endpoints.keystore;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Optional;
import java.util.UUID;

import de.captaingoldfish.oauthrestclient.application.endpoints.keystore.forms.KeystoreSelectAliasForm;
import de.captaingoldfish.oauthrestclient.application.endpoints.keystore.forms.KeystoreDownloadInfo;
import de.captaingoldfish.oauthrestclient.application.endpoints.keystore.forms.KeystoreEntryInfoForm;
import de.captaingoldfish.oauthrestclient.application.endpoints.keystore.forms.KeystoreInfoForm;
import de.captaingoldfish.oauthrestclient.application.endpoints.keystore.forms.KeystoreUploadForm;
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
  public KeystoreSelectAliasForm uploadKeystore(KeystoreUploadForm keystoreUploadForm)
  {
    final String filename = keystoreUploadForm.getKeystoreFile().getName();
    final byte[] keystoreData = keystoreUploadForm.getKeystoreFile().getBytes();
    final String keystorePassword = keystoreUploadForm.getKeystorePassword();
    final KeyStoreSupporter.KeyStoreType type = KeyStoreSupporter.KeyStoreType.byFileExtension(filename)
                                                                              .orElse(KeyStoreSupporter.KeyStoreType.JKS);
    final String stateId = UUID.randomUUID().toString();
    Keystore tmpKeystore = new Keystore(new ByteArrayInputStream(keystoreData), type, keystorePassword);
    keystoreFileCache.setKeystoreFile(stateId, tmpKeystore);
    return new KeystoreSelectAliasForm(stateId, tmpKeystore.getKeyStore());
  }

  /**
   * merges a new key entry into the application keystore and saves its entry to the database
   */
  public KeystoreEntryInfoForm mergeNewEntryIntoApplicationKeystore(KeystoreSelectAliasForm keystoreSelectAliasForm)
  {
    final String stateId = keystoreSelectAliasForm.getStateId();
    Keystore applicationKeystore = keystoreDao.getKeystore();
    Keystore uploadedKeystore = keystoreFileCache.getKeystoreFile(stateId);

    final String aliasToUse = keystoreSelectAliasForm.getAliases().get(0);
    final String newAlias = Optional.ofNullable(keystoreSelectAliasForm.getAliasOverride())
                                    .map(StringUtils::stripToNull)
                                    .orElse(keystoreSelectAliasForm.getAliases().get(0));
    KeystoreEntry aliasEntry = new KeystoreEntry(aliasToUse, keystoreSelectAliasForm.getPrivateKeyPassword());
    PrivateKey privateKey = uploadedKeystore.getPrivateKey(aliasEntry);
    Certificate certificate = uploadedKeystore.getCertificate(aliasEntry);
    aliasEntry.setAlias(newAlias);
    aliasEntry.setPrivateKeyPassword(applicationKeystore.getKeystorePassword());

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
  @SneakyThrows
  public KeystoreInfoForm getKeystoreInfos()
  {
    Keystore keystore = keystoreDao.getKeystore();
    return new KeystoreInfoForm(keystore.getKeyStore().size(), keystore.getKeyStoreAliases());
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

  @SneakyThrows
  public CertificateInfo loadCertificateInfo(String alias)
  {
    Keystore keystore = keystoreDao.getKeystore();
    X509Certificate certificate = (X509Certificate)keystore.getKeyStore().getCertificate(alias);
    return new CertificateInfo(certificate);
  }

  public KeystoreDownloadInfo getDownloadInfos()
  {
    Keystore keystore = keystoreDao.getKeystore();
    String filename = "application-keystore." + keystore.getKeystoreType().getFileExtension();
    byte[] keystoreBytes = KeyStoreSupporter.getBytes(keystore.getKeyStore(), "123456");
    return new KeystoreDownloadInfo(keystoreBytes, filename);
  }
}
