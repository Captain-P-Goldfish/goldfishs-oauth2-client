package de.captaingoldfish.oauthrestclient.application.endpoints.proxy.forms;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author Pascal Knueppel
 * @since 07.04.2021
 */
@Data
@NoArgsConstructor
public class ProxyResponseForm
{

  private String id;

  private String host;

  private String port;

  private String username;

  private String password;

  @Builder
  public ProxyResponseForm(String id, String host, String port, String username, String password)
  {
    this.id = id;
    this.host = host;
    this.port = port;
    this.username = username;
    this.password = password;
  }
}
