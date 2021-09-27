package de.captaingoldfish.restclient.database.entities;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
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
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "RESOURCE_ENDPOINTS", joinColumns = @JoinColumn(name = "OPENID_PROVIDER_ID"))
  @Column(name = "ENDPOINT")
  private Set<String> resourceEndpoints;

  /**
   * a public key that can be used to verify the provided signatures of this open id provider
   */
  @Column(name = "SIGNATURE_VERIFICATION_KEY")
  private byte[] signatureVerificationKey;

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
                        String discoveryEndpoint,
                        String authorizationEndpoint,
                        String tokenEndpoint,
                        Set<String> resourceEndpoints,
                        byte[] signatureVerificationKey)
  {
    this.name = name;
    this.discoveryEndpoint = discoveryEndpoint;
    this.authorizationEndpoint = authorizationEndpoint;
    this.tokenEndpoint = tokenEndpoint;
    this.resourceEndpoints = resourceEndpoints;
    this.signatureVerificationKey = signatureVerificationKey;
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
