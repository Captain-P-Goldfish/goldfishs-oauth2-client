package de.captaingoldfish.restclient.database.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

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

  /**
   * the key type that might be RSA or EC
   */
  @Column(name = "KEY_ALGORITHM")
  private String keyAlgorithm;

  /**
   * the length of the specific key entry
   */
  @Column(name = "KEY_LENGTH")
  private Integer keyLength;


  public KeystoreEntry(String alias, String privateKeyPassword)
  {
    this.alias = alias;
    this.privateKeyPassword = privateKeyPassword;
  }


  public KeystoreEntry(String alias, String privateKeyPassword, String keyAlgorithm, Integer keyLength)
  {
    this.alias = alias;
    this.privateKeyPassword = privateKeyPassword;
    this.keyAlgorithm = keyAlgorithm;
    this.keyLength = keyLength;
  }
}
