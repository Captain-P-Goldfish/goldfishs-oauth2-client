package de.captaingoldfish.restclient.scim.resources;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import de.captaingoldfish.scim.sdk.common.resources.ResourceNode;
import de.captaingoldfish.scim.sdk.common.resources.base.ScimObjectNode;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import lombok.Builder;
import lombok.NoArgsConstructor;


/**
 * @author Pascal Knueppel
 * @since 28.04.2021
 */
@NoArgsConstructor
public class ScimKeystore extends ResourceNode
{

  @Builder
  public ScimKeystore(List<String> aliasesList,
                      FileUpload fileUpload,
                      AliasSelection aliasSelection,
                      CertificateInfo certificateInfo,
                      Meta meta)
  {
    setSchemas(Collections.singletonList(FieldNames.SCHEMA_ID));
    setId("1");
    setAliases(aliasesList);
    setFileUpload(fileUpload);
    setAliasSelection(aliasSelection);
    setCertificateInfo(certificateInfo);
    setMeta(meta);
  }

  /**
   * Contains all aliases present within the application keystore.
   */
  public Set<String> getAliases()
  {
    return getSimpleArrayAttributeSet(FieldNames.ALIASES);
  }

  /**
   * Contains all aliases present within the application keystore.
   */
  public void setAliases(List<String> aliasesList)
  {
    setAttributeList(FieldNames.ALIASES, aliasesList);
  }

  /**
   * Used to upload an existing keystore that will be merged with the application keystore.
   */
  public FileUpload getFileUpload()
  {
    return getObjectAttribute(FieldNames.FILE_UPLOAD, FileUpload.class).orElse(null);
  }

  /**
   * Used to upload an existing keystore that will be merged with the application keystore.
   */
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

  /** Used to upload an existing keystore that will be merged with the application keystore. */
  @NoArgsConstructor
  public static class FileUpload extends ScimObjectNode
  {

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
  @NoArgsConstructor
  public static class AliasSelection extends ScimObjectNode
  {

    @Builder
    public AliasSelection(String stateId, List<String> aliases, String aliasOverride, String privateKeyPassword)
    {
      setStateId(stateId);
      setAliases(aliases);
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
    public void setAliases(List<String> aliasOverride)
    {
      setAttributeList(FieldNames.ALIASES, aliasOverride);
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

    public static final String ALIASES = "aliases";

    public static final String KEYSTORE_PASSWORD = "keystorePassword";

    public static final String PRIVATE_KEY_PASSWORD = "privateKeyPassword";

    public static final String STATE_ID = "stateId";

    public static final String KEYSTORE_FILE = "keystoreFile";

    public static final String ID = "id";

    public static final String ALIAS_OVERRIDE = "aliasOverride";

    public static final String FILE_UPLOAD = "fileUpload";

    public static final String KEYSTORE_FILE_NAME = "keystoreFileName";
  }
}
