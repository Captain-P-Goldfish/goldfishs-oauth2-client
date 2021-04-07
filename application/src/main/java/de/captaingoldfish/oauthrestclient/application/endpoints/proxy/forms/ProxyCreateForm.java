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
@ProxyCreateValidation
public class ProxyCreateForm
{

  private String host;

  private String port;

  private String username;

  private String password;

  @Builder
  public ProxyCreateForm(String host, String port, String username, String password)
  {
    this.host = host;
    this.port = port;
    this.username = username;
    this.password = password;
  }
}
