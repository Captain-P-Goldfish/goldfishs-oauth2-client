package de.captaingoldfish.restclient.database.entities;

import java.time.Instant;

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
@Table(name = "HTTP_RESPONSES")
public class HttpResponse
{

  /**
   * the unique identifier of this category
   */
  @Id
  @GeneratedValue
  @Column(name = "ID")
  private long id;

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
  public HttpResponse(long id,
                      String requestDetails,
                      int responseStatus,
                      String responseHeaders,
                      String responseBody,
                      Instant created)
  {
    this.id = id;
    this.requestDetails = requestDetails;
    this.responseStatus = responseStatus;
    this.responseHeaders = responseHeaders;
    this.responseBody = responseBody;
    this.created = created;
  }
}
