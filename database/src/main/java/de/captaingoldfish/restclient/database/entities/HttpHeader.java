package de.captaingoldfish.restclient.database.entities;

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
@Table(name = "HTTP_HEADERS")
public class HttpHeader
{

  /**
   * the unique identifier of this http request
   */
  @Id
  @GeneratedValue(generator = "HTTP_HEADERS_SEQ")
  @SequenceGenerator(name = "HTTP_HEADERS_SEQ", sequenceName = "hibernate_sequence", allocationSize = 1)
  @Column(name = "ID")
  private long id;

  /**
   * a name that will be unique within the requests category
   */
  @Column(name = "NAME")
  private String name;

  /**
   * a name that will be unique within the requests category
   */
  @Column(name = "VALUE")
  private String value;

  @Builder
  public HttpHeader(long id, String name, String value)
  {
    this.id = id;
    this.name = name;
    this.value = value;
  }
}
