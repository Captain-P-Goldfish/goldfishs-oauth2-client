package de.captaingoldfish.oauthrestclient.application.endpoints.keystore;

import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author Pascal Knueppel
 * @since 31.03.2021
 */
@Data
@NoArgsConstructor
@KeystoreDeleteEntryValidation
public class KeystoreDeleteEntryForm
{

  /**
   * the alias that should be deleted
   */
  private String alias;
}
