package de.captaingoldfish.restclient.application.endpoints.keystore.forms;

import de.captaingoldfish.restclient.application.endpoints.models.CertificateInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * This form returns the certificate information of the application keystore
 * 
 * @author Pascal Knueppel
 * @since 30.03.2021
 */
@Getter
@RequiredArgsConstructor
public class KeystoreEntryInfoForm
{

  /**
   * the alias of the entry
   */
  private final String alias;

  /**
   * the certificate information of the keystore entry for the given alias
   */
  private final CertificateInfo certificateInfo;
}
