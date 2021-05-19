package de.captaingoldfish.restclient.database.entities;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.validation.constraints.AssertTrue;

import org.apache.commons.lang3.StringUtils;

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
@Table(name = "CLIENT")
public class Client
{

  /**
   * the primary key of this client
   */
  @Id
  @GeneratedValue
  @Column(name = "ID")
  private long id;

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
   * the redirect URI to send in the request
   */
  @Column(name = "REDIRECT_URI")
  private String redirectUri;

  /**
   * the openid provider on which this client was registered
   */
  @OneToOne
  @JoinColumn(name = "OPENID_PROVIDER_ID", referencedColumnName = "ID")
  private OpenIdProvider openIdProvider;

  /**
   * an optional keystore to provide signatures that can be used in case of JWT authentication or SSL client
   * authentication
   */
  @OneToOne
  @JoinColumn(name = "SIGANTURE_KEYSTORE_ID", referencedColumnName = "ID")
  private Keystore signatureKeystore;

  /**
   * the audience is an optional field that becomes necessary if the {@link #signatureKeystore} is present and
   * the client is going to use JWT authentication
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
  public Client(String clientId, String clientSecret, String redirectUri, Keystore signatureKeystore, String audience)
  {
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.redirectUri = redirectUri;
    this.signatureKeystore = signatureKeystore;
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

  /**
   * adds a bean validation that requires the {@link #audience} value to be not empty if the
   * {@link #signatureKeystore} is not null
   */
  @AssertTrue(message = "{validation.database.entity.client.audience.not.blank}")
  public boolean isAudienceValid()
  {
    return signatureKeystore == null || StringUtils.isNotBlank(audience);
  }

}
