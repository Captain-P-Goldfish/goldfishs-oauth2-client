package de.captaingoldfish.oauthrestclient.application.endpoints.models;

import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.time.Instant;

import org.apache.tomcat.util.buf.HexUtils;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;


/**
 * Represents the data of a certificate that can be displayed in the view
 *
 * @author Pascal Knueppel
 * @since 30.03.2021
 */
@Data
@NoArgsConstructor
public class CertificateInfo
{

  /**
   * the DN of the issuer
   */
  private String issuerDn;

  /**
   * the DN of the subject
   */
  private String subjectDn;

  /**
   * the sha-256 fingerprint of the certificate
   */
  private String sha256fingerprint;

  /**
   * the timestamp when this certificate will be valid from
   */
  private Instant validFrom;

  /**
   * the expiration timestamp of the certificate
   */
  private Instant validUntil;

  @SneakyThrows
  public CertificateInfo(X509Certificate certificate)
  {
    this.issuerDn = certificate.getIssuerDN().toString();
    this.subjectDn = certificate.getSubjectDN().toString();
    this.sha256fingerprint = HexUtils.toHexString(MessageDigest.getInstance("SHA-256").digest(certificate.getEncoded()))
                                     .toLowerCase();
    this.validFrom = certificate.getNotBefore().toInstant();
    this.validUntil = certificate.getNotAfter().toInstant();
  }
}
