package de.captaingoldfish.oauthrestclient.application.endpoints.truststore.forms;

import java.util.List;

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

  private List<String> certificateAliases;

  public TruststoreInfoForm(int numberOfEntries, List<String> certificateAliases)
  {
    this.numberOfEntries = numberOfEntries;
    this.certificateAliases = certificateAliases;
  }
}
