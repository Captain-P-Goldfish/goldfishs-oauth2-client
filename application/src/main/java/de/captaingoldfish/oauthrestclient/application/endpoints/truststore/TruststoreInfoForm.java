package de.captaingoldfish.oauthrestclient.application.endpoints.truststore;

import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author Pascal Knueppel
 * @since 04.04.2021
 */
@Data
@NoArgsConstructor
public class TruststoreInfoForm
{

  /**
   * tells us how many entries can be found within the truststore
   */
  private int numberOfEntries;

  public TruststoreInfoForm(int numberOfEntries)
  {
    this.numberOfEntries = numberOfEntries;
  }
}
