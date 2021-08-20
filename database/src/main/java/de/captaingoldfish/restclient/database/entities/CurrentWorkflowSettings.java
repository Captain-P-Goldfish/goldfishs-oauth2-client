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
                                 String username,
                                 String userPassword)
  {
    this.openIdClient = openIdClient;
    this.redirectUri = redirectUri;
    this.queryParameters = queryParameters;
    this.username = username;
    this.userPassword = userPassword;
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
