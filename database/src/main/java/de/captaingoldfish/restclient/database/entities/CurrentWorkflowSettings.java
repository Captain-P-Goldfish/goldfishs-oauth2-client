package de.captaingoldfish.restclient.database.entities;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author Pascal Knueppel
 * @since 19.08.2021
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "CURRENT_WORKFLOW_SETTINGS")
public class CurrentWorkflowSettings
{

  /**
   * the primary key of this table
   */
  @Id
  @GeneratedValue
  @Column(name = "ID")
  private long id;

  /**
   * the openid client parent reference.
   */
  @OneToOne
  @JoinColumn(name = "OPENID_CLIENT_ID")
  private OpenIdClient openIdClient;

  /**
   * the redirect uri that was used in the last authorization code grant request for the parent client
   */
  @Column(name = "REDIRECT_URI")
  private String redirectUri;

  /**
   * the query parameters that were used in the last authorization code grant request for the parent client
   */
  @Column(name = "QUERY_PARAMETER")
  private String queryParameters;

  /**
   * The optional scope parameter to set the scope of the access token. This field is explicitly stored for the
   * client-credentials-grant
   */
  @Column(name = "CLIENT_CREDENTIALS_GRANT_SCOPE")
  private String clientCredentialsGrantScope;

  /**
   * the username that was used in the last resource owner password credentials grant request for the parent
   * client
   */
  @Column(name = "USERNAME")
  private String username;

  /**
   * the password that was used in the last resource owner password credentials grant request for the parent
   * client
   */
  @Column(name = "USER_PASSWORD")
  private String userPassword;

  /**
   * The optional scope parameter to set the scope of the access token. This field is explicitly stored for the
   * resource-owner-password-credentials-grant
   */
  @Column(name = "RESOURCE_PASSWORD_GRANT_SCOPE")
  private String resourcePasswordGrantScope;

  /**
   * the key that should be used for DPoP binding
   */
  @Column(name = "DPOP_KEY_ID")
  private String dpopKeyId;

  /**
   * the signature algorithm to use with DPoP
   */
  @Column(name = "DPOP_JWS_ALGORITHM")
  private String dpopJwsAlgorithm;

  /**
   * the nonce to use in the next request
   */
  @Column(name = "DPOP_NONCE")
  private String dpopNonce;

  /**
   * the jti to use in the next request
   */
  @Column(name = "DPOP_JTI")
  private String dpopJti;

  /**
   * the htm value to use in the next request
   */
  @Column(name = "DPOP_HTM")
  private String dpopHtm;

  /**
   * the htu value to use in the next request
   */
  @Column(name = "DPOP_HTU")
  private String dpopHtu;

  /**
   * If PKCE should be used or not
   */
  @Column(name = "PKCE_USE")
  private boolean pkceUse;

  /**
   * optional value. If present this value is used as code_verifier. If missing a value will be generated.
   */
  @Column(name = "PKCE_CODE_VERIFIER")
  private String pkceCodeVerifier;

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

  @Builder
  public CurrentWorkflowSettings(OpenIdClient openIdClient,
                                 String redirectUri,
                                 String queryParameters,
                                 String clientCredentialsGrantScope,
                                 String username,
                                 String userPassword,
                                 String resourcePasswordGrantScope,
                                 String dpopKeyId,
                                 String dpopJwsAlgorithm,
                                 String dpopNonce,
                                 String dpopJti,
                                 String dpopHtm,
                                 String dpopHtu,
                                 boolean pkceUse,
                                 String pkceCodeVerifier)
  {
    this.openIdClient = openIdClient;
    this.redirectUri = redirectUri;
    this.queryParameters = queryParameters;
    this.clientCredentialsGrantScope = clientCredentialsGrantScope;
    this.username = username;
    this.userPassword = userPassword;
    this.resourcePasswordGrantScope = resourcePasswordGrantScope;
    this.dpopKeyId = dpopKeyId;
    this.dpopJwsAlgorithm = dpopJwsAlgorithm;
    this.dpopNonce = dpopNonce;
    this.dpopJti = dpopJti;
    this.dpopHtm = dpopHtm;
    this.dpopHtu = dpopHtu;
    this.pkceUse = pkceUse;
    this.pkceCodeVerifier = pkceCodeVerifier;
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
