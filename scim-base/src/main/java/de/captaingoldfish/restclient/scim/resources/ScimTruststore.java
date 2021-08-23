package de.captaingoldfish.restclient.scim.resources;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import de.captaingoldfish.scim.sdk.common.resources.ResourceNode;
import de.captaingoldfish.scim.sdk.common.resources.base.ScimObjectNode;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import lombok.Builder;


/**
 * Represents the whole endpoint. The application truststore itself is actually a singleton but has several
 * entries. This structure acts as a singleton structure that is used to return different data based on the
 * current request type.
 * 
 * @author Pascal Knueppel
 * @since 19.05.2021
 */
public class ScimTruststore extends ResourceNode
{

  public ScimTruststore()
  {
    setId("1");
  }

  @Builder
  public ScimTruststore(String applicationTruststore,
                        List<String> aliasesList,
                        TruststoreUpload truststoreUpload,
                        TruststoreUploadResponse truststoreUploadResponse,
                        CertificateUpload certificateUpload,
                        CertificateUploadResponse certificateUploadResponse,
                        CertificateInfo certificateInfo,
                        Meta meta)
  {
    this();
    setSchemas(Collections.singletonList(FieldNames.SCHEMA_ID));
    setApplicationTruststore(applicationTruststore);
    setAliases(aliasesList);
    setTruststoreUpload(truststoreUpload);
    setTruststoreUploadResponse(truststoreUploadResponse);
    setCertificateUpload(certificateUpload);
    setCertificateUploadResponse(certificateUploadResponse);
    setCertificateInfo(certificateInfo);
    setMeta(meta);
  }

  /**
   * A base64 encoded representation of the application truststore that is only returned if directly requested.
   */
  public String getApplicationTruststore()
  {
    return getStringAttribute(FieldNames.APPLICATION_TRUSTSTORE).orElse(null);
  }

  /**
   * A base64 encoded representation of the application truststore that is only returned if directly requested
   */
  public void setApplicationTruststore(String applicationTruststore)
  {
    setAttribute(FieldNames.APPLICATION_TRUSTSTORE, applicationTruststore);
  }

  /**
   * a list of the entries that are present within the application truststore. Will be returned from the
   * list-endpoint
   */
  public List<String> getAliases()
  {
    return getSimpleArrayAttribute(FieldNames.ALIASES);
  }

  /**
   * a list of the entries that are present within the application truststore. Will be returned from the
   * list-endpoint
   */
  public void setAliases(List<String> aliasesList)
  {
    setAttributeList(FieldNames.ALIASES, aliasesList);
  }

  /** Used to upload a truststore whose entries will be merged into the application keystore if possible. */
  public Optional<TruststoreUpload> getTruststoreUpload()
  {
    return getObjectAttribute(FieldNames.TRUSTSTORE_UPLOAD, TruststoreUpload.class);
  }

  /** Used to upload a truststore whose entries will be merged into the application keystore if possible. */
  public void setTruststoreUpload(TruststoreUpload truststoreUpload)
  {
    setAttribute(FieldNames.TRUSTSTORE_UPLOAD, truststoreUpload);
  }

  /** The response that is returned after a successful upload of a truststore. */
  public Optional<TruststoreUploadResponse> getTruststoreUploadResponse()
  {
    return getObjectAttribute(FieldNames.TRUSTSTORE_UPLOAD_RESPONSE, TruststoreUploadResponse.class);
  }

  /** The response that is returned after a successful upload of a truststore. */
  public void setTruststoreUploadResponse(TruststoreUploadResponse truststoreUploadResponse)
  {
    setAttribute(FieldNames.TRUSTSTORE_UPLOAD_RESPONSE, truststoreUploadResponse);
  }

  /** Used to upload a certificate file that will be added into the application keystore if possible. */
  public Optional<CertificateUpload> getCertificateUpload()
  {
    return getObjectAttribute(FieldNames.CERTIFICATE_UPLOAD, CertificateUpload.class);
  }

  /** Used to upload a certificate file that will be added into the application keystore if possible. */
  public void setCertificateUpload(CertificateUpload certificateUpload)
  {
    setAttribute(FieldNames.CERTIFICATE_UPLOAD, certificateUpload);
  }

  /** The response that is returned after a successful upload of a certificate file. */
  public Optional<CertificateUploadResponse> getCertificateUploadResponse()
  {
    return getObjectAttribute(FieldNames.CERTIFICATE_UPLOAD_RESPONSE, CertificateUploadResponse.class);
  }

  /** The response that is returned after a successful upload of a certificate file. */
  public void setCertificateUploadResponse(CertificateUploadResponse certificateUploadResponse)
  {
    setAttribute(FieldNames.CERTIFICATE_UPLOAD_RESPONSE, certificateUploadResponse);
  }

  /**
   * Represents a single certificate entry of a keystore / truststore
   */
  public Optional<CertificateInfo> getCertificateInfo()
  {
    return getObjectAttribute(CertificateInfo.FieldNames.SCHEMA_ID, CertificateInfo.class);
  }

  /**
   * Represents a single certificate entry of a keystore / truststore
   */
  public void setCertificateInfo(CertificateInfo certificateInfo)
  {
    setAttribute(CertificateInfo.FieldNames.SCHEMA_ID, certificateInfo);
  }

  /** Used to upload a truststore whose entries will be merged into the application keystore if possible. */
  public static class TruststoreUpload extends ScimObjectNode
  {

    public TruststoreUpload()
    {}

    @Builder
    public TruststoreUpload(String truststoreFile, String truststoreFileName, String truststorePassword)
    {
      setTruststoreFile(truststoreFile);
      setTruststoreFileName(truststoreFileName);
      setTruststorePassword(truststorePassword);
    }

    /** The truststore file to upload that must be encoded in Base64 */
    public String getTruststoreFile()
    {
      return getStringAttribute(FieldNames.TRUSTSTORE_FILE).orElse(null);
    }

    /** The truststore file to upload that must be encoded in Base64 */
    public void setTruststoreFile(String truststoreFile)
    {
      setAttribute(FieldNames.TRUSTSTORE_FILE, truststoreFile);
    }

    /**
     * The name of the file that is only needed to determine the type of keystore to resolve by its file
     * extension. If missing the JKS keystore type is expected
     */
    public Optional<String> getTruststoreFileName()
    {
      return getStringAttribute(FieldNames.TRUSTSTORE_FILE_NAME);
    }

    /**
     * The name of the file that is only needed to determine the type of keystore to resolve by its file
     * extension. If missing the JKS keystore type is expected
     */
    public void setTruststoreFileName(String truststoreFileName)
    {
      setAttribute(FieldNames.TRUSTSTORE_FILE_NAME, truststoreFileName);
    }

    /** The password to open the truststore. This is only necessary for PKCS12 keystore types */
    public Optional<String> getTruststorePassword()
    {
      return getStringAttribute(FieldNames.TRUSTSTORE_PASSWORD);
    }

    /** The password to open the truststore. This is only necessary for PKCS12 keystore types */
    public void setTruststorePassword(String truststorePassword)
    {
      setAttribute(FieldNames.TRUSTSTORE_PASSWORD, truststorePassword);
    }
  }

  /** The response that is returned after a successful upload of a truststore. */
  public static class TruststoreUploadResponse extends ScimObjectNode
  {

    public TruststoreUploadResponse()
    {}

    @Builder
    public TruststoreUploadResponse(List<String> aliasesList,
                                    List<String> duplicateAliasesList,
                                    List<String> duplicateCertificateAliasesList)
    {
      setAliases(aliasesList);
      setDuplicateAliases(duplicateAliasesList);
      setDuplicateCertificateAliases(duplicateCertificateAliasesList);
    }

    /** The aliases of the truststore that have been added successfully to the application truststore */
    public List<String> getAliases()
    {
      return getSimpleArrayAttribute(FieldNames.ALIASES);
    }

    /** The aliases of the truststore that have been added successfully to the application truststore */
    public void setAliases(List<String> aliasesList)
    {
      setAttributeList(FieldNames.ALIASES, aliasesList);
    }

    /**
     * The aliases that could not be added due to a duplicate alias name that is already present within the
     * application truststore
     */
    public List<String> getDuplicateAliases()
    {
      return getSimpleArrayAttribute(FieldNames.DUPLICATE_ALIASES);
    }

    /**
     * The aliases that could not be added due to a duplicate alias name that is already present within the
     * application truststore
     */
    public void setDuplicateAliases(List<String> duplicateAliasesList)
    {
      setAttributeList(FieldNames.DUPLICATE_ALIASES, duplicateAliasesList);
    }

    /** The aliases that have not been added because the certificate is already present under another alias */
    public List<String> getDuplicateCertificateAliases()
    {
      return getSimpleArrayAttribute(FieldNames.DUPLICATE_CERTIFICATE_ALIASES);
    }

    /** The aliases that have not been added because the certificate is already present under another alias */
    public void setDuplicateCertificateAliases(List<String> duplicateCertificateAliasesList)
    {
      setAttributeList(FieldNames.DUPLICATE_CERTIFICATE_ALIASES, duplicateCertificateAliasesList);
    }
  }

  /** Used to upload a certificate file that will be added into the application keystore if possible. */
  public static class CertificateUpload extends ScimObjectNode
  {

    public CertificateUpload()
    {}

    @Builder
    public CertificateUpload(String certificateFile, String alias)
    {
      setCertificateFile(certificateFile);
      setAlias(alias);
    }

    /** The certificate file to upload that must be encoded in Base64 */
    public String getCertificateFile()
    {
      return getStringAttribute(FieldNames.CERTIFICATE_FILE).orElse(null);
    }

    /** The certificate file to upload that must be encoded in Base64 */
    public void setCertificateFile(String certificateFile)
    {
      setAttribute(FieldNames.CERTIFICATE_FILE, certificateFile);
    }

    /**
     * The alias under which the certificate entry should be stored.
     */
    public String getAlias()
    {
      return getStringAttribute(FieldNames.ALIAS).orElse(null);
    }

    /**
     * The alias under which the certificate entry should be stored.
     */
    public void setAlias(String alias)
    {
      setAttribute(FieldNames.ALIAS, alias);
    }
  }

  /** The response that is returned after a successful upload of a certificate file. */
  public static class CertificateUploadResponse extends ScimObjectNode
  {

    public CertificateUploadResponse()
    {}

    public CertificateUploadResponse(String alias)
    {
      setAlias(alias);
    }

    /** The alias of the entry to which the certificate was added */
    public String getAlias()
    {
      return getStringAttribute(FieldNames.ALIAS).orElse(null);
    }

    /** The alias of the entry to which the certificate was added */
    public void setAlias(String alias)
    {
      setAttribute(FieldNames.ALIAS, alias);
    }
  }

  public static class FieldNames
  {

    public static final String SCHEMA_ID = "urn:ietf:params:scim:schemas:captaingoldfish:2.0:Truststore";

    public static final String TRUSTSTORE_FILE_NAME = "truststoreFileName";

    public static final String TRUSTSTORE_PASSWORD = "truststorePassword";

    public static final String DUPLICATE_ALIASES = "duplicateAliases";

    public static final String ALIASES = "aliases";

    public static final String APPLICATION_TRUSTSTORE = "applicationTruststore";

    public static final String TRUSTSTORE_UPLOAD = "truststoreUpload";

    public static final String TRUSTSTORE_UPLOAD_RESPONSE = "truststoreUploadResponse";

    public static final String CERTIFICATE_UPLOAD = "certificateUpload";

    public static final String ALIAS = "alias";

    public static final String TRUSTSTORE_FILE = "truststoreFile";

    public static final String DUPLICATE_CERTIFICATE_ALIASES = "duplicateCertificateAliases";

    public static final String CERTIFICATE_FILE = "certificateFile";

    public static final String CERTIFICATE_UPLOAD_RESPONSE = "certificateUploadResponse";
  }
}
