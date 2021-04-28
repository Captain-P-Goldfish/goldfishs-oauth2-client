package de.captaingoldfish.restclient.database.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

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
  @NotBlank(message = "{proxy.no.hostname}")
  @Column(name = "PROXY_HOST")
  private String proxyHost;

  /**
   * the port under which the proxy is accessible
   */
  @Min(value = 1, message = "{proxy.no.port}")
  @Column(name = "PROXY_PORT")
  private int proxyPort;

  /**
   * the username for basic authentication with the proxy
   */
  @Column(name = "PROXY_USERNAME")
  private String proxyUsername;

  /**
   * the password for basic authentication with the proxy
   */
  @Column(name = "PROXY_PASSWORD")
  private String proxyPassword;

  /**
   * lombok builder
   */
  @Builder
  public Proxy(String proxyHost, int proxyPort, String proxyUsername, String proxyPassword)
  {
    this.proxyHost = proxyHost;
    this.proxyPort = proxyPort;
    this.proxyUsername = proxyUsername;
    this.proxyPassword = proxyPassword;
  }

}
