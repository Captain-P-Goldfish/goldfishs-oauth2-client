package de.captaingoldfish.restclient.database.entities;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * author Pascal Knueppel <br>
 * created at: 29.04.2018 - 22:46 <br>
 * <br>
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "OPENID_CLIENT")
public class OpenIdClient
{

  /**
   * the primary key of this client
   */
  @Id
  @GeneratedValue
  @Column(name = "ID")
  private long id;

  /**
   * the openid provider on which this client was registered
   */
  @OneToOne
  @JoinColumn(name = "OPENID_PROVIDER_ID", referencedColumnName = "ID")
  private OpenIdProvider openIdProvider;

  /**
   * the client_id to authenticate against the {@link OpenIdProvider}
   */
  @Column(name = "CLIENT_ID")
  private String clientId;

  /**
   * the client_secret credentials fot authentication at the {@link OpenIdProvider}
   */
  @Column(name = "CLIENT_SECRET")
  private String clientSecret;

  /**
   * the configured type of authentication that is to be used by this client. Either one of ["basic", "jwt",
   * "other"]
   */
  @Column(name = "AUTHENTICATION_TYPE")
  private String authenticationType;

  /**
   * The alias of the key reference within the application keystore to sign JWTs for authentication.
   */
  @Column(name = "SIGNING_KEY_REF")
  private String signingKeyRef;

  /**
   * The algorithm that should be used to sign the JWT during authentication at an identity provider
   */
  @Column(name = "SIGNATURE_ALGORITHM")
  private String signatureAlgorithm;

  /**
   * the audience is an optional field that becomes necessary if the {@link #signingKeyRef} is present and the
   * client is going to use JWT authentication
   */
  @Column(name = "AUDIENCE")
  private String audience;

  /**
   * The alias of the key reference within the application keystore to decrypt JWTs on responses e.g. the ID
   * Token.
   */
  @Column(name = "DECRYPTION_KEY_REF")
  private String decryptionKeyRef;

  /**
   * the moment this instance was created
   */
  @Column(name = "CREATED")
  private Instant created;

  /**
   * the moment this instance was last modified
   */
  @Column(name = "LAST_MODIFIED")
  private Instant lastModified;

  /**
   * lombok builder
   */
  @Builder
  public OpenIdClient(Long id,
                      OpenIdProvider openIdProvider,
                      String clientId,
                      String clientSecret,
                      String authenticationType,
                      String signingKeyRef,
                      String signatureAlgorithm,
                      String audience,
                      String decryptionKeyRef)
  {
    this.id = Optional.ofNullable(id).orElse(0L);
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.openIdProvider = openIdProvider;
    this.signingKeyRef = signingKeyRef;
    this.signatureAlgorithm = signatureAlgorithm;
    this.authenticationType = authenticationType;
    this.audience = audience;
    this.decryptionKeyRef = decryptionKeyRef;
    this.created = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    this.lastModified = this.created;
  }

  /**
   * @see #lastModified
   */
  public void setLastModified(Instant lastModified)
  {
    this.lastModified = lastModified.truncatedTo(ChronoUnit.MILLIS);
  }

}
