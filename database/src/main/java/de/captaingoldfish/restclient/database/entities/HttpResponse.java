package de.captaingoldfish.restclient.database.entities;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
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
@Table(name = "HTTP_RESPONSES")
public class HttpResponse
{

  /**
   * the unique identifier of this category
   */
  @Id
  @Column(name = "ID")
  private String id;

  /**
   * this transition to httprequest is needed to be able to directly delete {@link HttpResponse} objects when
   * not needed anymore. It is not used within the sourcecode but hibernate needs the information about the
   * join-table in order to delete the foreign-key relation.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  // @formatter:off
  @JoinTable(name = "HTTP_REQUEST_RESPONSE_MAPPING",
             joinColumns = @JoinColumn(name = "HTTP_RESPONSE_ID", referencedColumnName = "ID"),
             inverseJoinColumns = @JoinColumn(name = "HTTP_REQUEST_ID", referencedColumnName = "ID"))
  // @formatter:on
  private HttpRequest httpRequest;

  /**
   * the request details merged into a single string representation that have led to this response (the parent
   * object might have been altered already)
   */
  @Column(name = "REQUEST_DETAILS")
  private String requestDetails;

  /**
   * the http response status.
   */
  @Column(name = "RESPONSE_STATUS")
  private int responseStatus;

  /**
   * the response headers as simple string representation
   */
  @Column(name = "RESPONSE_HEADERS")
  private String responseHeaders;

  /**
   * the response body that belongs to the request details
   */
  @Column(name = "RESPONSE_BODY")
  private String responseBody;

  /**
   * the moment this instance was created
   */
  @Column(name = "CREATED")
  private Instant created;


  @Builder
  public HttpResponse(String id,
                      String requestDetails,
                      int responseStatus,
                      String responseHeaders,
                      String responseBody,
                      Instant created)
  {
    this.id = Optional.ofNullable(id).orElseGet(() -> UUID.randomUUID().toString());
    this.requestDetails = requestDetails;
    this.responseStatus = responseStatus;
    this.responseHeaders = responseHeaders;
    this.responseBody = responseBody;
    this.created = created;
  }
}
