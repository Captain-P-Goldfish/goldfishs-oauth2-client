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
 * author: Pascal Knueppel <br>
 * created at: 30.04.2018 <br>
 * <br>
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "PROXY")
public class Proxy
{

  /**
   * the primary key of this table
   */
  @Id
  @GeneratedValue
  @Column(name = "ID")
  private long id;

  /**
   * the hostname of the proxy
   */
  @Column(name = "HOST")
  private String host;

  /**
   * the port under which the proxy is accessible
   */
  @Column(name = "PORT")
  private int port;

  /**
   * the username for basic authentication with the proxy
   */
  @Column(name = "USERNAME")
  private String username;

  /**
   * the password for basic authentication with the proxy
   */
  @Column(name = "PROXY_PASSWORD")
  private String password;

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
  public Proxy(String host, int port, String username, String password)
  {
    this.host = host;
    this.port = port;
    this.username = username;
    this.password = password;
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
