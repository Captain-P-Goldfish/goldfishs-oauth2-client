package de.captaingoldfish.restclient.database.entities;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

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
@Table(name = "HTTP_REQUESTS")
public class HttpRequest
{

  /**
   * the unique identifier of this http request
   */
  @Id
  @GeneratedValue
  @Column(name = "ID")
  private long id;

  /**
   * the foreign key reference to the group of this http request
   */
  @ManyToOne
  @JoinColumn(name = "HTTP_REQUEST_GROUPS_ID", referencedColumnName = "ID")
  private HttpRequestGroup httpRequestGroup;

  /**
   * a name that will be unique within the requests category
   */
  @Column(name = "NAME")
  private String name;

  /**
   * the http client settings for this specific request
   */
  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "HTTP_CLIENT_SETTINGS_ID", referencedColumnName = "ID")
  private HttpClientSettings httpClientSettings;

  /**
   * the http method used by this request
   */
  @Column(name = "HTTP_METHOD")
  private String httpMethod;

  /**
   * the url of the http request
   */
  @Column(name = "URL")
  private String url;

  /**
   * the http headers associated with this http request
   */
  @LazyCollection(LazyCollectionOption.FALSE)
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  // @formatter:off
  @JoinTable(name = "HTTP_HEADERS_REQUEST_MAPPING",
             joinColumns = @JoinColumn(name = "HTTP_REQUEST_ID", referencedColumnName = "ID"),
             inverseJoinColumns = @JoinColumn(name = "HTTP_HEADER_ID", referencedColumnName = "ID"))
  // @formatter:on
  private List<HttpHeader> httpHeaders;

  /**
   * the body of the http request
   */
  @Column(name = "REQUEST_BODY")
  private String requestBody;

  /**
   * the http response history of this request
   */
  @LazyCollection(LazyCollectionOption.FALSE)
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  // @formatter:off
  @JoinTable(name = "HTTP_REQUEST_RESPONSE_MAPPING",
             joinColumns = @JoinColumn(name = "HTTP_REQUEST_ID", referencedColumnName = "ID"),
             inverseJoinColumns = @JoinColumn(name = "HTTP_RESPONSE_ID", referencedColumnName = "ID"))
  // @formatter:on
  private List<HttpResponse> httpResponses;

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
  public HttpRequest(long id,
                     HttpRequestGroup httpRequestGroup,
                     String name,
                     HttpClientSettings httpClientSettings,
                     String httpMethod,
                     String url,
                     List<HttpHeader> httpHeaders,
                     String requestBody,
                     List<HttpResponse> httpResponses,
                     Instant created,
                     Instant lastModified)
  {
    this.id = id;
    this.httpRequestGroup = httpRequestGroup;
    this.name = name;
    this.httpClientSettings = httpClientSettings;
    this.httpMethod = httpMethod;
    this.url = url;
    this.httpHeaders = httpHeaders;
    this.requestBody = requestBody;
    this.httpResponses = httpResponses;
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
