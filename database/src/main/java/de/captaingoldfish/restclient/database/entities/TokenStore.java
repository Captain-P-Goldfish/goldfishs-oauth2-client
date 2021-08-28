package de.captaingoldfish.restclient.database.entities;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author Pascal Knueppel
 * @since 28.08.2021
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "TOKEN_STORE")
public class TokenStore
{

  /**
   * the primary key of this table
   */
  @Id
  @GeneratedValue
  @Column(name = "ID")
  private long id;

  /**
   * the name of the token
   */
  @Column(name = "NAME")
  private String name;

  /**
   * the origin of the token that can be used confine the results in a search
   */
  @Column(name = "ORIGIN")
  private String origin;

  /**
   * the token that as stored
   */
  @Column(name = "TOKEN")
  private String token;

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
  public TokenStore(long id, String name, String origin, String token)
  {
    this.id = id;
    this.name = name;
    this.origin = origin;
    this.token = token;
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
