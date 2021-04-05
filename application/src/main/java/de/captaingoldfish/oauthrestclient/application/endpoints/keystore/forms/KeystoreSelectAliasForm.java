package de.captaingoldfish.oauthrestclient.application.endpoints.keystore.forms;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;


/**
 * This form is used in the second step of uploading a keystore entry. It is used as response after uploading
 * a keystore file and request data structure for saving a new keystore entry within the application keystore
 * 
 * @author Pascal Knueppel
 * @since 26.03.2021
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@KeystoreAliasFormValidation
public class KeystoreSelectAliasForm
{

  /**
   * the stateId references the keystore-cache-entry that tells us where to find the uploaded file. REQUIRED
   */
  private String stateId;

  /**
   * on response it is the list of all aliases that are present within the uploaded keystore. On request only a
   * single entry must be present or a validation error will occur. REQUIRED
   */
  private List<String> aliases;

  /**
   * if the alias of the uploaded keystore is already present in the application keystore it is possible to
   * assign a new alias for this specific key entry with this value. OPTIONAL
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String aliasOverride;

  /**
   * only for request: the password of the private key for the alias set in {@link #aliases}. If not present the
   * application assumes the keystore password to be also the key-password. OPTIONAL
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String privateKeyPassword;

  @SneakyThrows
  public KeystoreSelectAliasForm(String stateId, KeyStore keyStore)
  {
    this(stateId, null, keyStore);
  }

  @SneakyThrows
  public KeystoreSelectAliasForm(String stateId, String privateKeyPassword, KeyStore keyStore)
  {
    this.stateId = stateId;
    this.privateKeyPassword = privateKeyPassword;
    this.aliases = new ArrayList<>();

    Enumeration<String> aliasesEnumeration = keyStore.aliases();
    while (aliasesEnumeration.hasMoreElements())
    {
      this.aliases.add(aliasesEnumeration.nextElement());
    }
  }
}
