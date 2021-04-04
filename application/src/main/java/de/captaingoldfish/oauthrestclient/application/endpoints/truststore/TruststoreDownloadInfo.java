package de.captaingoldfish.oauthrestclient.application.endpoints.truststore;

import lombok.Getter;


/**
 * @author Pascal Knueppel
 * @since 04.04.2021
 */
@Getter
public class TruststoreDownloadInfo
{

  /**
   * the bytes of the truststore to download
   */
  private byte[] truststoreBytes;

  /**
   * the name of the download file
   */
  private String filename;

  public TruststoreDownloadInfo(byte[] truststoreBytes, String filename)
  {
    this.truststoreBytes = truststoreBytes;
    this.filename = filename;
  }
}
