package de.captaingoldfish.restclient.scim.resources;

import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.time.Instant;

import org.apache.tomcat.util.buf.HexUtils;

import de.captaingoldfish.scim.sdk.common.resources.base.ScimObjectNode;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;


/**
 * Represents a single certificate entry of a keystore / truststore represents a reusable object that holds
 * certificate data that should be displayed in the html view
 *
 * @author Pascal Knueppel
 * @since 29.04.2021
 */
@NoArgsConstructor
public class CertificateInfo extends ScimObjectNode
{

  @SneakyThrows
  @Builder
  public CertificateInfo(String alias, X509Certificate certificate)
  {
    setAlias(alias);
    if (certificate != null)
    {
      Info info = new Info(certificate.getIssuerDN().toString(), certificate.getSubjectDN().toString(),
                           HexUtils.toHexString(MessageDigest.getInstance("SHA-256").digest(certificate.getEncoded()))
                                   .toLowerCase(),
                           certificate.getNotBefore().toInstant(), certificate.getNotAfter().toInstant());
      setInfo(info);
    }
  }

  /** The alias that holds this certificate entry. */
  public String getAlias()
  {
    return getStringAttribute(FieldNames.ALIAS).orElse(null);
  }

  /** The alias that holds this certificate entry. */
  public void setAlias(String alias)
  {
    setAttribute(FieldNames.ALIAS, alias);
  }

  /** The data of the certificate. */
  public Info getInfo()
  {
    return getObjectAttribute(FieldNames.INFO, Info.class).orElse(null);
  }

  /** The data of the certificate. */
  public void setInfo(Info info)
  {
    setAttribute(FieldNames.INFO, info);
  }

  /** The data of the certificate. */
  @NoArgsConstructor
  public static class Info extends ScimObjectNode
  {

    public Info(String issuerDn, String subjectDn, String sha256Fingerprint, Instant validFrom, Instant validTo)
    {
      setIssuerDn(issuerDn);
      setSubjectDn(subjectDn);
      setSha256Fingerprint(sha256Fingerprint);
      setValidFrom(validFrom);
      setValidTo(validTo);
    }

    /** The distinguished name of the issuer */
    public String getIssuerDn()
    {
      return getStringAttribute(FieldNames.ISSUER_DN).orElse(null);
    }

    /** The distinguished name of the issuer */
    public void setIssuerDn(String issuerDn)
    {
      setAttribute(FieldNames.ISSUER_DN, issuerDn);
    }

    /** The distinguished name of the subject */
    public String getSubjectDn()
    {
      return getStringAttribute(FieldNames.SUBJECT_DN).orElse(null);
    }

    /** The distinguished name of the subject */
    public void setSubjectDn(String subjectDn)
    {
      setAttribute(FieldNames.SUBJECT_DN, subjectDn);
    }

    /** The SHA-256 fingerprint of the certificate */
    public String getSha256Fingerprint()
    {
      return getStringAttribute(FieldNames.SHA256_FINGERPRINT).orElse(null);
    }

    /** The SHA-256 fingerprint of the certificate */
    public void setSha256Fingerprint(String sha256Fingerprint)
    {
      setAttribute(FieldNames.SHA256_FINGERPRINT, sha256Fingerprint);
    }

    /** The date from which this certificate will be valid */
    public Instant getValidFrom()
    {
      return getDateTimeAttribute(FieldNames.VALID_FROM).orElse(null);
    }

    /** The date from which this certificate will be valid */
    public void setValidFrom(Instant validFrom)
    {
      setDateTimeAttribute(FieldNames.VALID_FROM, validFrom);
    }

    /** The date until this certificate will be valid */
    public Instant getValidTo()
    {
      return getDateTimeAttribute(FieldNames.VALID_TO).orElse(null);
    }

    /** The date until this certificate will be valid */
    public void setValidTo(Instant validTo)
    {
      setDateTimeAttribute(FieldNames.VALID_TO, validTo);
    }
  }

  public static class FieldNames
  {

    public static final String SCHEMA_ID = "urn:ietf:params:scim:schemas:captaingoldfish:2.0:CertificateInfo";

    public static final String ISSUER_DN = "issuerDn";

    public static final String SHA256_FINGERPRINT = "sha256Fingerprint";

    public static final String ALIAS = "alias";

    public static final String VALID_FROM = "validFrom";

    public static final String INFO = "info";

    public static final String SUBJECT_DN = "subjectDn";

    public static final String VALID_TO = "validTo";
  }
}
