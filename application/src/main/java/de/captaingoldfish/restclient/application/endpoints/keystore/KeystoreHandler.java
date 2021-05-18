package de.captaingoldfish.restclient.application.endpoints.keystore;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import de.captaingoldfish.restclient.application.endpoints.keystore.validation.ScimKeystoreRequestValidator;
import de.captaingoldfish.restclient.commons.keyhelper.KeyStoreSupporter;
import de.captaingoldfish.restclient.database.entities.Keystore;
import de.captaingoldfish.restclient.database.entities.KeystoreEntry;
import de.captaingoldfish.restclient.database.repositories.KeystoreDao;
import de.captaingoldfish.restclient.scim.resources.CertificateInfo;
import de.captaingoldfish.restclient.scim.resources.ScimKeystore;
import de.captaingoldfish.scim.sdk.common.constants.enums.SortOrder;
import de.captaingoldfish.scim.sdk.common.exceptions.ResourceNotFoundException;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import de.captaingoldfish.scim.sdk.common.schemas.SchemaAttribute;
import de.captaingoldfish.scim.sdk.server.endpoints.ResourceHandler;
import de.captaingoldfish.scim.sdk.server.endpoints.authorize.Authorization;
import de.captaingoldfish.scim.sdk.server.endpoints.validation.RequestValidator;
import de.captaingoldfish.scim.sdk.server.filter.FilterNode;
import de.captaingoldfish.scim.sdk.server.response.PartialListResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


/**
 * @author Pascal Knueppel
 * @since 09.05.2021
 */
@Slf4j
@RequiredArgsConstructor
public class KeystoreHandler extends ResourceHandler<ScimKeystore>
{

  /**
   * to cache an uploaded keystore file
   */
  private final KeystoreFileCache keystoreFileCache;

  /**
   * to save an altered application keystore or to load the existing application keystore
   */
  private final KeystoreDao keystoreDao;

  /**
   * the create part is separated into two steps. Uploading a keystore and selecting the entries that should be
   * added to the application keystore
   */
  @Override
  public ScimKeystore createResource(ScimKeystore scimKeystore, Authorization authorization)
  {
    ScimKeystore.FileUpload fileUpload = scimKeystore.getFileUpload();
    ScimKeystore scimKeystoreResponse;
    if (fileUpload != null)
    {
      scimKeystoreResponse = handleKeystoreUpload(fileUpload);
    }
    else
    {
      scimKeystoreResponse = handleAliasSelection(scimKeystore.getAliasSelection());
    }
    return scimKeystoreResponse;
  }

  /**
   * handles the upload of a keystore by storing it within the file cache and returns the present aliases from
   * the keystore and a stateId that is the reference-key to the uploaded keystore
   * 
   * @param fileUpload the keystore upload structure
   * @return the response for the client with the aliases and a stateId that references the uploaded keystore
   */
  @SneakyThrows
  protected ScimKeystore handleKeystoreUpload(ScimKeystore.FileUpload fileUpload)
  {
    final String filename = fileUpload.getKeystoreFileName().orElse(null);
    final byte[] keystoreData = Base64.getDecoder().decode(fileUpload.getKeystoreFile());
    final String keystorePassword = fileUpload.getKeystorePassword();
    final KeyStoreSupporter.KeyStoreType type = Optional.ofNullable(filename)
                                                        .flatMap(KeyStoreSupporter.KeyStoreType::byFileExtension)
                                                        .orElse(KeyStoreSupporter.KeyStoreType.JKS);
    final String stateId = UUID.randomUUID().toString();
    Keystore tmpKeystore = new Keystore(new ByteArrayInputStream(keystoreData), type, keystorePassword);
    keystoreFileCache.setKeystoreFile(stateId, tmpKeystore);

    List<String> aliases = new ArrayList<>();

    Enumeration<String> aliasesEnumeration = tmpKeystore.getKeyStore().aliases();
    while (aliasesEnumeration.hasMoreElements())
    {
      aliases.add(aliasesEnumeration.nextElement());
    }

    ScimKeystore.AliasSelection aliasSelection = ScimKeystore.AliasSelection.builder()
                                                                            .stateId(stateId)
                                                                            .aliases(aliases)
                                                                            .build();
    return ScimKeystore.builder()
                       .aliasSelection(aliasSelection)
                       .meta(Meta.builder().created(Instant.now()).lastModified(Instant.now()).build())
                       .build();
  }

  /**
   * handles the selection of an alias from the uploaded keystore
   * 
   * @param aliasSelection the selection of the client for the keystore entry that should be added to the
   *          application keystore
   * @return the response for the client with the certificate information of the entry that was added to the
   *         keystore
   */
  protected ScimKeystore handleAliasSelection(ScimKeystore.AliasSelection aliasSelection)
  {
    final String stateId = aliasSelection.getStateId();
    Keystore applicationKeystore = keystoreDao.getKeystore();
    Keystore uploadedKeystore = keystoreFileCache.getKeystoreFile(stateId);

    final String aliasToUse = aliasSelection.getAliases().get(0);
    final String newAlias = Optional.ofNullable(aliasSelection.getAliasOverride())
                                    .map(StringUtils::stripToNull)
                                    .orElse(aliasSelection.getAliases().get(0));
    KeystoreEntry aliasEntry = new KeystoreEntry(aliasToUse, aliasSelection.getPrivateKeyPassword());
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

    X509Certificate x509Certificate = applicationKeystore.getCertificate(aliasPasswords);
    CertificateInfo certificateInfo = CertificateInfo.builder().alias(newAlias).certificate(x509Certificate).build();

    return ScimKeystore.builder()
                       .certificateInfo(certificateInfo)
                       .meta(Meta.builder().created(Instant.now()).lastModified(Instant.now()).build())
                       .build();
  }

  @Override
  public ScimKeystore getResource(String alias,
                                  Authorization authorization,
                                  List<SchemaAttribute> list,
                                  List<SchemaAttribute> list1)
  {
    Keystore keystore = keystoreDao.getKeystore();
    Meta meta = Meta.builder().created(Instant.now()).lastModified(Instant.now()).build();
    X509Certificate x509Certificate = keystore.getCertificate(alias);
    CertificateInfo certificateInfo = new CertificateInfo(alias, x509Certificate);
    ScimKeystore scimKeystore = ScimKeystore.builder().certificateInfo(certificateInfo).meta(meta).build();
    scimKeystore.setId(alias);
    return scimKeystore;
  }

  @Override
  public PartialListResponse<ScimKeystore> listResources(long startIndex,
                                                         int count,
                                                         FilterNode filter,
                                                         SchemaAttribute sortBy,
                                                         SortOrder sortOrder,
                                                         List<SchemaAttribute> attributes,
                                                         List<SchemaAttribute> excludedAttributes,
                                                         Authorization authorization)
  {
    Keystore keystore = keystoreDao.getKeystore();
    List<ScimKeystore> keystoreList = new ArrayList<>();
    Meta meta = Meta.builder().created(Instant.now()).lastModified(Instant.now()).build();
    ScimKeystore scimKeystore = ScimKeystore.builder().aliasesList(keystore.getKeyStoreAliases()).meta(meta).build();
    keystoreList.add(scimKeystore);
    return PartialListResponse.<ScimKeystore> builder().totalResults(1).resources(keystoreList).build();
  }

  /**
   * not supported
   */
  @Override
  public ScimKeystore updateResource(ScimKeystore resourceNode, Authorization authorization)
  {
    return null;
  }

  /**
   * deletes the entry with the given alias from the application keystore
   */
  @SneakyThrows
  @Override
  public void deleteResource(String alias, Authorization authorization)
  {
    Keystore keystore = keystoreDao.getKeystore();
    Optional<KeystoreEntry> keystoreEntryOptional = keystore.getKeystoreEntries()
                                                            .stream()
                                                            .filter(entry -> entry.getAlias().equals(alias))
                                                            .findAny();
    if (keystoreEntryOptional.isEmpty())
    {
      throw new ResourceNotFoundException(String.format("No entry found with alias %s", alias));
    }
    KeystoreEntry keystoreEntry = keystoreEntryOptional.get();
    keystore.getKeystoreEntries().remove(keystoreEntry);
    keystore.getKeyStore().deleteEntry(alias);
    byte[] newKeystoreBytes = KeyStoreSupporter.getBytes(keystore.getKeyStore(), keystore.getKeystorePassword());
    keystore.setKeystoreBytes(newKeystoreBytes);
    keystoreDao.save(keystore);
  }

  @Override
  public RequestValidator<ScimKeystore> getRequestValidator()
  {
    return new ScimKeystoreRequestValidator();
  }
}