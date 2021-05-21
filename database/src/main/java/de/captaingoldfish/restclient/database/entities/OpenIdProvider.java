package de.captaingoldfish.restclient.database.entities;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

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
  @Column(name = "NAME")
  private String name;

  /**
   * if this url is present and reachable it will be used to access the authorization endpoint, token endpoint
   * and user-info-endpoint if present in the configuration
   */
  @Column(name = "DISCOVERY_ENDPOINT")
  private String discoveryEndpoint;

  /**
   * the authorization endpoint -> can be used if a discovery endpoint is not provided
   */
  @Column(name = "AUTHORIZATION_ENDPOINT")
  private String authorizationEndpoint;

  /**
   * the token endpoint -> can be used if a discovery endpoint is not provided
   */
  @Column(name = "TOKEN_ENDPOINT")
  private String tokenEndpoint;

  /**
   * resource endpoints that should be accessible after a token was acquired
   */
  @ElementCollection
  @CollectionTable(name = "RESOURCE_ENDPOINTS", joinColumns = @JoinColumn(name = "OPENID_PROVIDER_ID"))
  @Column(name = "ENDPOINT")
  private List<String> resourceEndpoints;

  /**
   * a public key that can be used to verify the provided signatures of this open id provider
   */
  @Column(name = "SIGNATURE_VERIFICATION_CERT")
  private byte[] signatureVerificationCert;

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
  public OpenIdProvider(String name,
                        String discoveryEndpointUrl,
                        String authorizationEndpointUrl,
                        String tokenEndpointUrl,
                        List<String> resourceEndpoints,
                        byte[] signatureVerificationCert)
  {
    this.name = name;
    this.discoveryEndpoint = discoveryEndpointUrl;
    this.authorizationEndpoint = authorizationEndpointUrl;
    this.tokenEndpoint = tokenEndpointUrl;
    this.resourceEndpoints = resourceEndpoints;
    this.signatureVerificationCert = signatureVerificationCert;
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
