package de.captaingoldfish.restclient.database.entities;

import java.time.Instant;

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
@Table(name = "HTTP_RESPONSES")
public class HttpResponse
{

  /**
   * the unique identifier of this category
   */
  @Id
  @GeneratedValue(generator = "HTTP_RESPONSES_SEQ")
  @SequenceGenerator(name = "HTTP_RESPONSES_SEQ", sequenceName = "hibernate_sequence", allocationSize = 1)
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
