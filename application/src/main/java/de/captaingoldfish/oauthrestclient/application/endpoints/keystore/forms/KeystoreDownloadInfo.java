package de.captaingoldfish.oauthrestclient.application.endpoints.keystore.forms;

import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author Pascal Knueppel
 * @since 05.04.2021
 */
@Data
@NoArgsConstructor
public class KeystoreDownloadInfo
{

  private byte[] keystoreBytes;

  /**
   * the name of the download file
   */
  private String filename;

  public KeystoreDownloadInfo(byte[] keystoreBytes, String filename)
  {
    this.keystoreBytes = keystoreBytes;
    this.filename = filename;
  }

}
