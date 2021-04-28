package de.captaingoldfish.restclient.database.entities;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author Pascal Knueppel
 * @since 30.03.2021
 */
@Data
@NoArgsConstructor
@Embeddable
public class KeystoreEntry
{

  /**
   * a an entry within the keystore entry of this application
   */
  @Column(name = "ALIAS")
  private String alias;

  /**
   * the password to access the private key of this entry
   */
  @Column(name = "PRIVATE_KEY_PASSWORD")
  private String privateKeyPassword;


  public KeystoreEntry(String alias, String privateKeyPassword)
  {
    this.alias = alias;
    this.privateKeyPassword = privateKeyPassword;
  }
}
