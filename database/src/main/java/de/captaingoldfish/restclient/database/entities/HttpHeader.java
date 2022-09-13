package de.captaingoldfish.restclient.database.entities;

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
@Table(name = "HTTP_HEADERS")
public class HttpHeader
{

  /**
   * the unique identifier of this http request
   */
  @Id
  @GeneratedValue
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
