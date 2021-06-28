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
 * <p>
 * Copyright &copy; 2009-2020 Governikus GmbH &amp; Co. KG
 * </p>
 *
 * @author Pascal Knüppel
 * @since 25.06.2021
 */
@NoArgsConstructor
@Data
@Entity
@Table(name = "PLAIN_CONTENT")
public class PlainContent
{

  /**
   * the primary key of this table
   */
  @Id
  @GeneratedValue
  @Column(name = "ID")
  private final long id = 1L;

  /**
   * a human readable name to find the token again in the view after it was saved by the user
   */
  @Column(name = "NAME")
  private String name;

  /**
   * a plain string content of any type
   */
  @Column(name = "CONTENT")
  private String content;

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
  public PlainContent(String name, String content)
  {
    this.name = name;
    this.content = content;
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
