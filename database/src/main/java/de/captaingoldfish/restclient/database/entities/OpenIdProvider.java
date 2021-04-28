package de.captaingoldfish.restclient.database.entities;

import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Base64;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import de.captaingoldfish.restclient.commons.keyhelper.KeyReader;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * author Pascal Knueppel <br>
 * created at: 29.04.2018 - 22:47 <br>
 * <br>
 */
@Slf4j
@Data
@NoArgsConstructor
@Entity
@Table(name = "OPENID_PROVIDER")
public class OpenIdProvider
{

  /**
   * primary key of this table
   */
  @Id
  @GeneratedValue
  @Column(name = "ID")
  private long id;

  /**
   * this name can be used to identify this provider in the view
   */
  @Column(name = "PROVIDER_NAME")
  private String name;

  /**
   * if this url is present and reachable it will be used to access the authorization endpoint, token endpoint
   * and user-info-endpoint if present in the configuration
   */
  @Column(name = "DISCOVERY_ENDPOINT")
  private String discoveryEndpointUrl;

  /**
   * the authorization endpoint -> can be used if a discovery endpoint is not provided
   */
  @Column(name = "AUTHORIZATION_ENDPOINT")
  private String authorizationEndpointUrl;

  /**
   * the token endpoint -> can be used if a discovery endpoint is not provided
   */
  @Column(name = "TOKEN_ENDPOINT")
  private String tokenEndpointUrl;

  /**
   * the user-info endpoint -> can be used if a discovery endpoint is not provided
   */
  @Column(name = "USER_INFO_ENDPOINT")
  private String userInfoEndpointUrl;

  /**
   * a public key that can be used to verify the provided signatures of this open id provider
   */
  @Column(name = "SIGNATURE_VERIFICATION_KEY")
  private byte[] signatureVerificationKey;

  /**
   * lombok builder
   */
  @Builder
  public OpenIdProvider(String name,
                        String discoveryEndpointUrl,
                        String authorizationEndpointUrl,
                        String tokenEndpointUrl,
                        String userInfoEndpointUrl,
                        byte[] signatureVerificationKey)
  {
    this.name = name;
    this.discoveryEndpointUrl = discoveryEndpointUrl;
    this.authorizationEndpointUrl = authorizationEndpointUrl;
    this.tokenEndpointUrl = tokenEndpointUrl;
    this.userInfoEndpointUrl = userInfoEndpointUrl;
    this.signatureVerificationKey = signatureVerificationKey;
  }

  /**
   * @return the {@link #signatureVerificationKey} as a base64 string
   */
  public String getSignatureVerificationKeyString()
  {
    if (signatureVerificationKey == null || signatureVerificationKey.length == 0)
    {
      return null;
    }
    return Base64.getEncoder().encodeToString(signatureVerificationKey);
  }

  /**
   * @see #signatureVerificationKey
   */
  public void setSignatureVerificationKey(byte[] signatureVerificationKeyFile)
  {
    if (signatureVerificationKeyFile != null && signatureVerificationKeyFile.length > 0)
    {
      try
      {
        X509Certificate certificate = KeyReader.readX509Certificate(signatureVerificationKeyFile);
        this.signatureVerificationKey = certificate.getPublicKey().getEncoded();
      }
      catch (IllegalStateException ex)
      {
        log.debug(ex.getMessage(), ex);
        PublicKey publicKey = KeyReader.readPublicRSAKey(signatureVerificationKeyFile);
        this.signatureVerificationKey = publicKey.getEncoded();
      }
    }
  }
}
