package de.captaingoldfish.restclient.database.entities;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

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
  @GeneratedValue
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
