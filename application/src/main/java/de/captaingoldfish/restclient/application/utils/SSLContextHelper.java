package de.captaingoldfish.restclient.application.utils;

import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import de.captaingoldfish.restclient.application.projectconfig.WebAppConfig;
import de.captaingoldfish.restclient.database.entities.HttpClientSettings;
import de.captaingoldfish.restclient.database.entities.Keystore;
import de.captaingoldfish.restclient.database.entities.KeystoreEntry;
import de.captaingoldfish.restclient.database.entities.Truststore;
import de.captaingoldfish.restclient.database.repositories.KeystoreDao;
import de.captaingoldfish.restclient.database.repositories.TruststoreDao;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;


/**
 * @author Pascal Knueppel
 * @since 21.08.2021
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SSLContextHelper
{

  /**
   * this method will build the {@link SSLContext} that will be used to access the identity provider based on
   * the key within the http client settings
   *
   * @return the {@link SSLContext} that configured the TLS connection
   */
  public static SSLContext getSslContext(HttpClientSettings httpClientSettings)
  {
    SSLContext sslContext;
    try
    {
      sslContext = SSLContext.getInstance("TLS");
    }
    catch (NoSuchAlgorithmException e)
    {
      throw new IllegalStateException(e);
    }
    try
    {
      sslContext.init(getKeyManagers(httpClientSettings.getTlsClientAuthKeyRef()), getTrustmanager(), null);
    }
    catch (KeyManagementException e)
    {
      throw new IllegalStateException(e);
    }
    return sslContext;
  }

  /**
   * builds the application truststore manager based on the application truststore
   *
   * @return null if {@code truststore} is null or the explicit truststore configuration
   */
  private static TrustManager[] getTrustmanager()
  {
    TruststoreDao truststoreDao = WebAppConfig.getApplicationContext().getBean(TruststoreDao.class);
    Truststore truststore = truststoreDao.getTruststore();
    TrustManagerFactory trustManagerFactory;
    try
    {
      trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init(truststore.getTruststore());
    }
    catch (NoSuchAlgorithmException | KeyStoreException e)
    {
      throw new IllegalStateException(e.getMessage(), e);
    }
    return trustManagerFactory.getTrustManagers();
  }

  /**
   * will load the key material from the application keystore and will setup mutual TLS client authentication
   * with this key
   */
  private static KeyManager[] getKeyManagers(String tlsClientKeyReference)
  {
    if (tlsClientKeyReference == null)
    {
      return null;
    }
    KeystoreDao keystoreDao = WebAppConfig.getApplicationContext().getBean(KeystoreDao.class);
    Keystore applicationKeystore = keystoreDao.getKeystore();
    KeyManagerFactory keyManagerFactory;
    try
    {
      keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    }
    catch (NoSuchAlgorithmException e)
    {
      throw new IllegalStateException(e);
    }
    try
    {
      KeystoreEntry keystoreEntry = applicationKeystore.getKeystoreEntry(tlsClientKeyReference);
      KeyPair keyPair = applicationKeystore.getKeyPair(keystoreEntry);
      Certificate certificate = applicationKeystore.getCertificate(tlsClientKeyReference);
      KeyStore tlsKeystore = buildTlsKeystore(keyPair, certificate, applicationKeystore.getKeystorePassword());
      keyManagerFactory.init(tlsKeystore, applicationKeystore.getKeystorePassword().toCharArray());
    }
    catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e)
    {
      throw new IllegalStateException("keystore could not be accessed", e);
    }
    return keyManagerFactory.getKeyManagers();
  }

  /**
   * builds a temporary keystore that contains a single key entry. This is necessary because the
   * KeyManagerFactory requires a keystore with a single entry only
   * 
   * @param keyPair contains the private key
   * @param certificate the certificate that belongs to the private key
   * @param keystorePassword the password for the keystore that is also used for the private key within the
   *          keystore
   * @return a new setup keystore for mutual tls client authentication
   */
  @SneakyThrows
  private static KeyStore buildTlsKeystore(KeyPair keyPair, Certificate certificate, String keystorePassword)
  {
    KeyStore keyStore = KeyStore.getInstance("JKS");
    keyStore.load(null, keystorePassword.toCharArray());
    keyStore.setKeyEntry("tls", keyPair.getPrivate(), keystorePassword.toCharArray(), new Certificate[]{certificate});
    return keyStore;
  }


}
