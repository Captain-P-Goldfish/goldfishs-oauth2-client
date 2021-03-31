package de.captaingoldfish.oauthrestclient.commons.exceptions;

/**
 * @author Pascal Knueppel
 * @since 30.03.2021
 */
public class KeystoreEntryExistsException extends RuntimeException
{

  public KeystoreEntryExistsException(String message)
  {
    super(message);
  }
}
