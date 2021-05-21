package de.captaingoldfish.restclient.application.utils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import org.bouncycastle.crypto.KeyGenerationParameters;

import de.captaingoldfish.restclient.commons.keyhelper.SecurityProvider;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;


/**
 * @author Pascal Knueppel
 * @since 21.05.2021
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestUtils
{

  /**
   * generates an asymmetric RSA key
   *
   * @param keyGenerationParameters The parameters to generate the key
   * @return an RSA keypair
   */
  public static KeyPair generateKey(KeyGenerationParameters keyGenerationParameters)
  {
    KeyPairGenerator keyPairGenerator = null;
    try
    {
      keyPairGenerator = KeyPairGenerator.getInstance("RSA", SecurityProvider.BOUNCY_CASTLE_PROVIDER);
    }
    catch (NoSuchAlgorithmException e)
    {
      throw new IllegalStateException("RSA-Schluessel konnte nicht erzeugt werden", e);
    }
    keyPairGenerator.initialize(keyGenerationParameters.getStrength(), keyGenerationParameters.getRandom());
    return keyPairGenerator.generateKeyPair();
  }
}
