package de.captaingoldfish.restclient.database.entities;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

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
 * @since 31.05.2021
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "HTTP_CLIENT_SETTINGS")
public class HttpClientSettings
{

  /**
   * the primary key of this table
   */
  @Id
  @GeneratedValue
  @Column(name = "ID")
  private long id;

  /**
   * the openid client reference. This instance is null for the default HTTP client configuration and not null
   * for any customized configuration
   */
  @OneToOne
  @JoinColumn(name = "OPENID_CLIENT_ID")
  private OpenIdClient openIdClient;

  /**
   * the reference to the proxy that should be used with the http client.
   */
  @OneToOne
  @JoinColumn(name = "PROXY_ID")
  private Proxy proxy;

  /**
   * the request timeout in seconds
   */
  @Column(name = "REQUEST_TIMEOUT")
  private int requestTimeout;

  /**
   * the connection timeout in seconds
   */
  @Column(name = "CONNECTION_TIMEOUT")
  private int connectionTimeout;

  /**
   * the socket timeout in seconds
   */
  @Column(name = "SOCKET_TIMEOUT")
  private int socketTimeout;

  /**
   * to ignore the hostname verifier or activate it
   */
  @Column(name = "USE_HOSTNAME_VERIFIER")
  private boolean useHostnameVerifier;

  /**
   * The alias of the key reference within the application keystore to authenticate with TLS client
   * authentication.
   */
  @Column(name = "TLS_CLIENT_AUTH_KEY_REF")
  private String tlsClientAuthKeyRef;

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
  public HttpClientSettings(long id,
                            OpenIdClient openIdClient,
                            Proxy proxy,
                            Integer requestTimeout,
                            Integer connectionTimeout,
                            Integer socketTimeout,
                            boolean useHostnameVerifier,
                            String tlsClientAuthKeyRef)
  {
    this.id = id;
    this.openIdClient = openIdClient;
    this.proxy = proxy;
    this.requestTimeout = Optional.ofNullable(requestTimeout).orElse(30);
    this.connectionTimeout = Optional.ofNullable(connectionTimeout).orElse(30);
    this.socketTimeout = Optional.ofNullable(socketTimeout).orElse(30);
    this.useHostnameVerifier = useHostnameVerifier;
    this.tlsClientAuthKeyRef = tlsClientAuthKeyRef;
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
