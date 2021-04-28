package de.captaingoldfish.restclient.application.endpoints.proxy.forms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author Pascal Knueppel
 * @since 07.04.2021
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ProxyDeleteValidation
public class ProxyDeleteForm
{

  private String id;

}
