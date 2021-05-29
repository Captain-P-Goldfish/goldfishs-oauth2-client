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
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

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
   * an optional keystore to provide signatures that can be used in case of JWT authentication or SSL client
   * authentication
   */
  @Column(name = "SIGNATURE_KEY_REF")
  private String signatureKeyRef;

  /**
   * the audience is an optional field that becomes necessary if the {@link #signatureKeyRef} is present and the
   * client is going to use JWT authentication
   */
  @Column(name = "AUDIENCE")
  private String audience;

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
                      String signatureKeyRef,
                      String audience)
  {
    this.id = Optional.ofNullable(id).orElse(0L);
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.openIdProvider = openIdProvider;
    this.signatureKeyRef = signatureKeyRef;
    this.audience = audience;
  }

  @PrePersist
  public final void setCreated()
  {
    this.created = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    this.lastModified = this.created;
  }

  @PreUpdate
  public final void setLastModified()
  {
    this.lastModified = Instant.now().truncatedTo(ChronoUnit.MILLIS);
  }

}
