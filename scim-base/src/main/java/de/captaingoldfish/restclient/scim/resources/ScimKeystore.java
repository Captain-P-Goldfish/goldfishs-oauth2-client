package de.captaingoldfish.restclient.scim.resources;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import de.captaingoldfish.scim.sdk.common.resources.ResourceNode;
import de.captaingoldfish.scim.sdk.common.resources.base.ScimObjectNode;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import lombok.Builder;
import lombok.NoArgsConstructor;


/**
 * Structure to load and modify the application keystore. The entries can be used to encrypt, sign or for TLS
 * client authentication
 * 
 * @author Pascal Knueppel
 * @since 28.04.2021
 */
@NoArgsConstructor
public class ScimKeystore extends ResourceNode
{

  @Builder
  public ScimKeystore(List<KeyInfos> keyInfosList,
                      FileUpload fileUpload,
                      AliasSelection aliasSelection,
                      CertificateInfo certificateInfo,
                      Meta meta)
  {
    setSchemas(Collections.singletonList(FieldNames.SCHEMA_ID));
    setId("1");
    setKeyInfos(keyInfosList);
    setFileUpload(fileUpload);
    setAliasSelection(aliasSelection);
    setCertificateInfo(certificateInfo);
    setMeta(meta);
  }

  /** A list of all key entries with some minor infos that are present within this keystore. */
  public List<KeyInfos> getKeyInfos()
  {
    return getArrayAttribute(FieldNames.KEY_INFOS, KeyInfos.class);
  }

  /** A list of all key entries with some minor infos that are present within this keystore. */
  public void setKeyInfos(List<KeyInfos> keyInfosList)
  {
    setAttribute(FieldNames.KEY_INFOS, keyInfosList);
  }

  /** Used to upload an existing keystore that will be merged with the application keystore. */
  public FileUpload getFileUpload()
  {
    return getObjectAttribute(FieldNames.FILE_UPLOAD, FileUpload.class).orElse(null);
  }

  /** Used to upload an existing keystore that will be merged with the application keystore. */
  public void setFileUpload(FileUpload fileUpload)
  {
    setAttribute(FieldNames.FILE_UPLOAD, fileUpload);
  }

  /**
   * If a keystore was uploaded the user will be able to select an alias of the keystore that should be merged
   * into the application keystore. This complex type represents both the response that shows the user what can
   * be merged into the application keystore and the request that tells the application what the user wants to
   * merge into the application keystore.
   */
  public AliasSelection getAliasSelection()
  {
    return getObjectAttribute(FieldNames.ALIAS_SELECTION, AliasSelection.class).orElse(null);
  }

  /**
   * If a keystore was uploaded the user will be able to select an alias of the keystore that should be merged
   * into the application keystore. This complex type represents both the response that shows the user what can
   * be merged into the application keystore and the request that tells the application what the user wants to
   * merge into the application keystore.
   */
  public void setAliasSelection(AliasSelection aliasSelection)
  {
    setAttribute(FieldNames.ALIAS_SELECTION, aliasSelection);
  }

  /**
   * Represents a single certificate entry of a keystore / truststore
   */
  public CertificateInfo getCertificateInfo()
  {
    return getObjectAttribute(CertificateInfo.FieldNames.SCHEMA_ID, CertificateInfo.class).orElse(null);
  }

  /**
   * Represents a single certificate entry of a keystore / truststore
   */
  public void setCertificateInfo(CertificateInfo certificateInfo)
  {
    setAttribute(CertificateInfo.FieldNames.SCHEMA_ID, certificateInfo);
  }

  /** A list of all key entries with some minor infos that are present within this keystore. */
  public static class KeyInfos extends ScimObjectNode
  {

    public KeyInfos()
    {}

    @Builder
    public KeyInfos(String alias, String keyAlgorithm, Boolean hasPrivateKey, Integer keyLength)
    {
      setAlias(alias);
      setKeyAlgorithm(keyAlgorithm);
      setKeyLength(keyLength);
      setHasPrivateKey(hasPrivateKey);
    }

    /** The key length of this key entry. */
    public Integer getKeyLength()
    {
      return getIntegerAttribute(FieldNames.KEY_LENGTH).orElse(null);
    }

    /** The key length of this key entry. */
    public void setKeyLength(Integer keyLength)
    {
      setAttribute(FieldNames.KEY_LENGTH, keyLength);
    }

    /** The alias under which this specific key is stored. */
    public String getAlias()
    {
      return getStringAttribute(FieldNames.ALIAS).orElse(null);
    }

    /** The alias under which this specific key is stored. */
    public void setAlias(String alias)
    {
      setAttribute(FieldNames.ALIAS, alias);
    }

    /** The key algorithm of this key. */
    public String getKeyAlgorithm()
    {
      return getStringAttribute(FieldNames.KEY_ALGORITHM).orElse(null);
    }

    /** The key algorithm of this key. */
    public void setKeyAlgorithm(String keyAlgorithm)
    {
      setAttribute(FieldNames.KEY_ALGORITHM, keyAlgorithm);
    }

    /** If this entry has also a private key entry or a certificate only. */
    public Boolean getHasPrivateKey()
    {
      return getBooleanAttribute(FieldNames.HAS_PRIVATE_KEY).orElse(false);
    }

    /** If this entry has also a private key entry or a certificate only. */
    public void setHasPrivateKey(Boolean hasPrivateKey)
    {
      setAttribute(FieldNames.HAS_PRIVATE_KEY, hasPrivateKey);
    }


  }

  /** Used to upload an existing keystore that will be merged with the application keystore. */
  public static class FileUpload extends ScimObjectNode
  {

    public FileUpload()
    {}

    @Builder
    public FileUpload(String keystorePassword, String keystoreFileName, String keystoreFile)
    {
      setKeystorePassword(keystorePassword);
      setKeystoreFileName(keystoreFileName);
      setKeystoreFile(keystoreFile);
    }

    /** The password to access the keystore */
    public String getKeystorePassword()
    {
      return getStringAttribute(FieldNames.KEYSTORE_PASSWORD).orElse(null);
    }

    /** The password to access the keystore */
    public void setKeystorePassword(String keystorePassword)
    {
      setAttribute(FieldNames.KEYSTORE_PASSWORD, keystorePassword);
    }

    /**
     * The name of the file that is only needed to determine the type of keystore to resolve by its file
     * extension. If missing the JKS keystore type is expected
     */
    public Optional<String> getKeystoreFileName()
    {
      return getStringAttribute(FieldNames.KEYSTORE_FILE_NAME);
    }

    /**
     * The name of the file that is only needed to determine the type of keystore to resolve by its file
     * extension. If missing the JKS keystore type is expected
     */
    public void setKeystoreFileName(String keystoreFileName)
    {
      setAttribute(FieldNames.KEYSTORE_FILE_NAME, keystoreFileName);
    }

    /** The keystore file that is being uploaded */
    public String getKeystoreFile()
    {
      return getStringAttribute(FieldNames.KEYSTORE_FILE).orElse(null);
    }

    /** The keystore file that is being uploaded */
    public void setKeystoreFile(String keystoreFile)
    {
      setAttribute(FieldNames.KEYSTORE_FILE, keystoreFile);
    }


  }

  /**
   * If a keystore was uploaded the user will be able to select an alias of the keystore that should be merged
   * into the application keystore. This complex type represents both the response that shows the user what can
   * be merged into the application keystore and the request that tells the application what the user wants to
   * merge into the application keystore.
   */
  public static class AliasSelection extends ScimObjectNode
  {

    public AliasSelection()
    {}

    @Builder
    public AliasSelection(String stateId, List<String> aliasesList, String aliasOverride, String privateKeyPassword)
    {
      setStateId(stateId);
      setAliases(aliasesList);
      setAliasOverride(aliasOverride);
      setPrivateKeyPassword(privateKeyPassword);
    }

    /**
     * An identifier to find the previously uploaded keystore. After an upload of a keystore the user can choose
     * which entry to merge into the application keystore. But to find the keystore when requesting this stateId
     * is necessary.
     */
    public String getStateId()
    {
      return getStringAttribute(FieldNames.STATE_ID).orElse(null);
    }

    /**
     * An identifier to find the previously uploaded keystore. After an upload of a keystore the user can choose
     * which entry to merge into the application keystore. But to find the keystore when requesting this stateId
     * is necessary.
     */
    public void setStateId(String stateId)
    {
      setAttribute(FieldNames.STATE_ID, stateId);
    }

    /**
     * In a request this array must hold only a single entry which represents the entry to be merged into the
     * application keystore. In a response this array holds all aliases that are present within the uploaded
     * keystore.
     */
    public List<String> getAliases()
    {
      return getSimpleArrayAttribute(FieldNames.ALIASES);
    }

    /**
     * In a request this array must hold only a single entry which represents the entry to be merged into the
     * application keystore. In a response this array holds all aliases that are present within the uploaded
     * keystore.
     */
    public void setAliases(List<String> aliasesList)
    {
      setAttributeList(FieldNames.ALIASES, aliasesList);
    }

    /**
     * Normally the alias that should be merged into the application keystore will be kept from the original
     * keystore. If this is not wanted this attribute can be used to define an alias alternative. This might be
     * useful if the alias that should be merged is already present within the application keystore.
     */
    public String getAliasOverride()
    {
      return getStringAttribute(FieldNames.ALIAS_OVERRIDE).orElse(null);
    }

    /**
     * Normally the alias that should be merged into the application keystore will be kept from the original
     * keystore. If this is not wanted this attribute can be used to define an alias alternative. This might be
     * useful if the alias that should be merged is already present within the application keystore.
     */
    public void setAliasOverride(String aliasOverride)
    {
      setAttribute(FieldNames.ALIAS_OVERRIDE, aliasOverride);
    }

    /**
     * If the private key uses a different password than the keystore, the user can set the private key password
     * to access the key. If this field is missing in the request the application will assume that the keystore
     * password itself must be used to access the private key.
     */
    public String getPrivateKeyPassword()
    {
      return getStringAttribute(FieldNames.PRIVATE_KEY_PASSWORD).orElse(null);
    }

    /**
     * If the private key uses a different password than the keystore, the user can set the private key password
     * to access the key. If this field is missing in the request the application will assume that the keystore
     * password itself must be used to access the private key.
     */
    public void setPrivateKeyPassword(String privateKeyPassword)
    {
      setAttribute(FieldNames.PRIVATE_KEY_PASSWORD, privateKeyPassword);
    }

  }

  public static class FieldNames
  {

    public static final String SCHEMA_ID = "urn:ietf:params:scim:schemas:captaingoldfish:2.0:Keystore";

    public static final String ALIAS_SELECTION = "aliasSelection";

    public static final String KEY_INFOS = "keyInfos";

    public static final String KEY_ALGORITHM = "keyAlgorithm";

    public static final String ALIASES = "aliases";

    public static final String KEYSTORE_PASSWORD = "keystorePassword";

    public static final String STATE_ID = "stateId";

    public static final String FILE_UPLOAD = "fileUpload";

    public static final String HAS_PRIVATE_KEY = "hasPrivateKey";

    public static final String PRIVATE_KEY_PASSWORD = "privateKeyPassword";

    public static final String ALIAS = "alias";

    public static final String KEY_LENGTH = "keyLength";

    public static final String KEYSTORE_FILE = "keystoreFile";

    public static final String ID = "id";

    public static final String ALIAS_OVERRIDE = "aliasOverride";

    public static final String KEYSTORE_FILE_NAME = "keystoreFileName";
  }
}
