package de.captaingoldfish.restclient.application.endpoints.proxy.forms;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author Pascal Knueppel
 * @since 07.04.2021
 */
@Data
@NoArgsConstructor
@ProxyUpdateValidation
public class ProxyUpdateForm
{

  private String id;

  private String host;

  private String port;

  private String username;

  private String password;

  @Builder
  public ProxyUpdateForm(String id, String host, String port, String username, String password)
  {
    this.id = id;
    this.host = host;
    this.port = port;
    this.username = username;
    this.password = password;
  }
}
