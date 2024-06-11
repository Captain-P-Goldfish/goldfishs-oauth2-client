package de.captaingoldfish.restclient.database.entities;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

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
  @GeneratedValue(generator = "TOKEN_STORE_SEQ")
  @SequenceGenerator(name = "TOKEN_STORE_SEQ", sequenceName = "hibernate_sequence", allocationSize = 1)
  @Column(name = "ID")
  private long id;

  /**
   * the owning category under which this token will be stored
   */
  @ManyToOne
  @JoinColumn(name = "TOKEN_CATEGORY_ID")
  private TokenCategory tokenCategory;

  /**
   * the name of the token
   */
  @Column(name = "NAME")
  private String name;

  /**
   * the stored token
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
  public TokenStore(long id, TokenCategory tokenCategory, String name, String token)
  {
    this.id = id;
    this.tokenCategory = tokenCategory;
    this.name = name;
    this.token = token;
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
