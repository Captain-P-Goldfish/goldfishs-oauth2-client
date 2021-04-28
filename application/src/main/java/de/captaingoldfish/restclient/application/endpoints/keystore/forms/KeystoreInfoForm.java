package de.captaingoldfish.restclient.application.endpoints.keystore.forms;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * a response only form
 * 
 * @author Pascal Knueppel
 * @since 04.04.2021
 */
@Data
@NoArgsConstructor
public class KeystoreInfoForm
{

  /**
   * tells us how many entries can be found within the truststore
   */
  private int numberOfEntries;

  /**
   * the aliases present in the application keystore
   */
  private List<String> certificateAliases;

  public KeystoreInfoForm(int numberOfEntries, List<String> certificateAliases)
  {
    this.numberOfEntries = numberOfEntries;
    this.certificateAliases = certificateAliases;
  }
}
