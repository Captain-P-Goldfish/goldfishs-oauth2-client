package de.captaingoldfish.restclient.application.endpoints.keystore.forms;

import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author Pascal Knueppel
 * @since 31.03.2021
 */
@Data
@NoArgsConstructor
@KeystoreAliasRequestValidation
public class KeystoreAliasRequestForm
{

  /**
   * the alias that should be deleted
   */
  private String alias;
}
