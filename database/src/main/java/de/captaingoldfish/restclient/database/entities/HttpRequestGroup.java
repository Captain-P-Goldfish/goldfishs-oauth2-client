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
 * author Pascal Knueppel <br>
 * created at: 12.09.2022 - 18:13 <br>
 * <br>
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "HTTP_REQUEST_GROUPS")
public class HttpRequestGroup
{

  /**
   * the unique identifier of this group
   */
  @Id
  @GeneratedValue(generator = "HTTP_REQUEST_GROUPS_SEQ")
  @SequenceGenerator(name = "HTTP_REQUEST_GROUPS_SEQ", sequenceName = "hibernate_sequence", allocationSize = 1)
  @Column(name = "ID")
  private long id;

  /**
   * the name of this group
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
  public HttpRequestGroup(long id, String name, Instant created, Instant lastModified)
  {
    this.id = id;
    this.name = name;
    this.created = created;
    this.lastModified = lastModified;
  }

  /**
   * @see #lastModified
   */
  public void setLastModified(Instant lastModified)
  {
    this.lastModified = lastModified.truncatedTo(ChronoUnit.MILLIS);
  }
}
