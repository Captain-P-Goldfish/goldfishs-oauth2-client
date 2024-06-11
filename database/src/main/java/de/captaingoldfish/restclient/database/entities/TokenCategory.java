package de.captaingoldfish.restclient.database.entities;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author Pascal Knueppel
 * @since 02.09.2021
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "TOKEN_CATEGORY")
public class TokenCategory
{

  /**
   * the primary key of this table
   */
  @Id
  @GeneratedValue(generator = "TOKEN_CATEGORY_SEQ")
  @SequenceGenerator(name = "TOKEN_CATEGORY_SEQ", sequenceName = "hibernate_sequence", allocationSize = 1)
  @Column(name = "ID")
  private long id;

  /**
   * the name of the category
   */
  @Column(name = "NAME")
  private String name;

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
  public TokenCategory(long id, String name, Instant created, Instant lastModified)
  {
    this.id = id;
    this.name = name;
    this.created = created;
    this.lastModified = lastModified;
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
