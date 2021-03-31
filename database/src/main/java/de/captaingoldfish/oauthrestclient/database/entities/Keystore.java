package de.captaingoldfish.oauthrestclient.database.entities;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
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
  @Column(name = "ID")
  private final long id = 1L;

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
   * the keystore password for this keystore
   */
  @Column(name = "KEYSTORE_PASSWORD")
  private String keystorePassword;

  /**
   * the collection of alias key entries within this keystore
   */
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "KEYSTORE_ENTRIES", joinColumns = @JoinColumn(name = "KEYSTORE_ID"))
  private List<KeystoreEntry> keystoreEntries = new ArrayList<>();

  /**
   * the keystore that is the main object of this class
   */
  @Transient
  private KeyStore keyStore;

  @SneakyThrows
  public Keystore(InputStream keystoreInputStream, KeyStoreSupporter.KeyStoreType keyStoreType, String keystorePassword)
  {
    this.keystoreBytes = IOUtils.toByteArray(keystoreInputStream);
    this.keyStore = KeyStoreSupporter.readKeyStore(new ByteArrayInputStream(this.keystoreBytes),
                                                   keyStoreType,
                                                   keystorePassword);
    this.keystoreType = keyStoreType;
    this.keystorePassword = keystorePassword;
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

  public KeystoreEntry addAliasEntry(KeystoreEntry aliasEntry)
  {
    return this.addAliasEntry(aliasEntry.getAlias(), aliasEntry.getPrivateKeyPassword());
  }

  public KeystoreEntry addAliasEntry(String alias, String keyPassword)
  {
    KeystoreEntry keystoreEntry = new KeystoreEntry(alias, keyPassword);
    getKeystoreEntries().add(keystoreEntry);
    return keystoreEntry;
  }

  /**
   * will extract the private key for the given alias
   *
   * @return the private key of the alias
   */
  public PrivateKey getPrivateKey(KeystoreEntry keystoreEntry)
  {
    if (keystoreEntryExists(keystoreEntry))
    {
      throw new IllegalStateException(String.format("Could not find key entry for alias: %s",
                                                    keystoreEntry.getAlias()));
    }
    try
    {
      PrivateKey privateKey;
      char[] privateKeyPasswordCharArray = Optional.ofNullable(keystoreEntry.getPrivateKeyPassword())
                                                   .filter(password -> password.length() > 0)
                                                   .map(String::toCharArray)
                                                   .orElse(null);
      char[] keystorePasswordCharArray = Optional.ofNullable(keystorePassword).map(String::toCharArray).orElse(null);
      privateKey = (PrivateKey)keyStore.getKey(keystoreEntry.getAlias(),
                                               Optional.ofNullable(privateKeyPasswordCharArray)
                                                       .orElse(keystorePasswordCharArray));
      if (privateKey == null && log.isWarnEnabled())
      {
        log.warn("no private key found for alias: {}", keystoreEntry.getAlias());
      }
      return privateKey;
    }
    catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e)
    {
      throw new IllegalStateException("could not read keystore entry with alias: " + keystoreEntry.getAlias(), e);
    }
  }

  /**
   * will read the certificate from the given alias
   *
   * @return the certificate under the given keystore entry
   */
  public X509Certificate getCertificate(KeystoreEntry keystoreEntry)
  {
    if (keystoreEntryExists(keystoreEntry))
    {
      throw new IllegalStateException(String.format("Could not find certificate entry for alias: %s",
                                                    keystoreEntry.getAlias()));
    }
    try
    {
      X509Certificate x509Certificate = (X509Certificate)keyStore.getCertificate(keystoreEntry.getAlias());
      if (x509Certificate == null && log.isWarnEnabled())
      {
        log.warn("no certificate entry found for alias: {}", keystoreEntry.getAlias());
      }
      return x509Certificate;
    }
    catch (KeyStoreException e)
    {
      throw new IllegalStateException("could not read certificate with alias: " + keystoreEntry.getAlias(), e);
    }
  }

  /**
   * checks if the configured entry does exist
   */
  private boolean keystoreEntryExists(KeystoreEntry keystoreEntry)
  {
    return keyStore == null || StringUtils.isBlank(keystoreEntry.getAlias());
  }

  /**
   * mainly used for unit tests. This method returns the keystore entries that are really present in the
   * application keystore
   */
  @SneakyThrows
  public List<String> getKeyStoreAliases()
  {
    Enumeration<String> aliasesEnumeration = keyStore.aliases();
    List<String> aliases = new ArrayList<>();
    while (aliasesEnumeration.hasMoreElements())
    {
      aliases.add(aliasesEnumeration.nextElement());
    }
    return aliases;
  }
}
