package de.captaingoldfish.oauthrestclient.database.entities;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import de.captaingoldfish.oauthrestclient.commons.keyhelper.KeyStoreSupporter;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


/**
 * author Pascal Knueppel <br>
 * created at: 29.04.2018 - 22:46 <br>
 * <br>
 */
@Slf4j
@Data
@Entity
@Table(name = "KEYSTORE")
@NoArgsConstructor
public class Keystore
{

  /**
   * the primary key of this table
   */
  @Id
  @GeneratedValue
  @Column(name = "ID")
  private long id;

  /**
   * a unique identifier for the keystores
   */
  @Column(name = "NAME")
  private String name;

  /**
   * the bytes representing this keystore
   */
  @Column(name = "KEYSTORE_BYTES")
  private byte[] keystoreBytes;

  /**
   * the type of this keystore
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "KEYSTORE_TYPE")
  private KeyStoreSupporter.KeyStoreType keystoreType;

  /**
   * the alias of the entry that should be used with this keystore
   */
  @Column(name = "ALIAS")
  private String alias;

  /**
   * the keystore password for the {@link #alias}
   */
  @Column(name = "KEYSTORE_PASSWORD")
  private String keystorePassword;


  /**
   * the private key password for the {@link #alias}
   */
  @Column(name = "PRIVATE_KEY_PASSWORD")
  private String privateKeyPassword;

  /**
   * the keystore that is the main object of this class
   */
  @Transient
  private KeyStore keyStore;

  @SneakyThrows
  public Keystore(String name,
                  InputStream keystoreInputStream,
                  KeyStoreSupporter.KeyStoreType keyStoreType,
                  String alias,
                  String keystorePassword,
                  String privateKeyPassword)
  {
    this.name = name;
    this.keystoreBytes = IOUtils.toByteArray(keystoreInputStream);
    this.keyStore = KeyStoreSupporter.readKeyStore(new ByteArrayInputStream(this.keystoreBytes),
                                                   keyStoreType,
                                                   keystorePassword);
    this.keystoreType = keyStoreType;
    this.alias = alias;
    this.keystorePassword = keystorePassword;
    this.privateKeyPassword = privateKeyPassword;
  }

  /**
   * will load the keystore instance
   */
  @PostLoad
  public final void loadKeystore()
  {
    try
    {
      this.keyStore = KeyStoreSupporter.readKeyStore(getKeystoreBytes(), getKeystoreType(), getKeystorePassword());
    }
    catch (Exception ex)
    {
      log.error(ex.getMessage(), ex);
    }
  }

  /**
   * will extract the private key for the given alias
   *
   * @return the private key of the alias
   */
  public PrivateKey getPrivateKey()
  {
    if (keystoreEntryExists())
    {
      throw new IllegalStateException(String.format("Could not find key entry for alias: %s", alias));
    }
    try
    {
      PrivateKey privateKey;
      char[] privateKeyPasswordCharArray = Optional.ofNullable(privateKeyPassword)
                                                   .map(String::toCharArray)
                                                   .orElse(null);
      char[] keystorePasswordCharArray = Optional.ofNullable(keystorePassword).map(String::toCharArray).orElse(null);
      privateKey = (PrivateKey)keyStore.getKey(alias,
                                               Optional.ofNullable(privateKeyPasswordCharArray)
                                                       .orElse(keystorePasswordCharArray));
      if (privateKey == null && log.isWarnEnabled())
      {
        log.warn("no private key found for alias: {}", alias);
      }
      return privateKey;
    }
    catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e)
    {
      throw new IllegalStateException("could not read keystore entry with alias: " + alias, e);
    }
  }

  /**
   * will read the certificate from the given alias
   *
   * @return the certificate under the given keystore entry
   */
  public X509Certificate getCertificate()
  {
    if (keystoreEntryExists())
    {
      throw new IllegalStateException(String.format("Could not find certificate entry for alias: %s", alias));
    }
    try
    {
      X509Certificate x509Certificate = (X509Certificate)keyStore.getCertificate(alias);
      if (x509Certificate == null && log.isWarnEnabled())
      {
        log.warn("no certificate entry found for alias: {}", alias);
      }
      return x509Certificate;
    }
    catch (KeyStoreException e)
    {
      throw new IllegalStateException("could not read certificate with alias: " + alias, e);
    }
  }

  /**
   * checks if the configured entry does exist
   */
  private boolean keystoreEntryExists()
  {
    return keyStore == null || StringUtils.isBlank(alias);
  }
}
