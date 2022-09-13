package de.captaingoldfish.restclient.database.entities;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import de.captaingoldfish.restclient.commons.keyhelper.KeyStoreSupporter;
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
   * the moment this instance was created
   */
  @Column(name = "CREATED")
  private Instant created;

  /**
   * the moment this instance was last modified
   */
  @Column(name = "LAST_MODIFIED")
  private Instant lastModified;

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
    this.created = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    this.lastModified = this.created;
  }

  /**
   * @see #lastModified
   */
  public void setLastModified(Instant lastModified)
  {
    this.lastModified = lastModified.truncatedTo(ChronoUnit.MILLIS);
  }

  /**
   * will load the keystore instance
   */
  public KeyStore getKeyStore()
  {
    if (this.keyStore == null)
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
    return this.keyStore;
  }

  public KeystoreEntry addKeyEntry(KeystoreEntry aliasEntry)
  {
    return this.addKeyEntry(aliasEntry.getAlias(), aliasEntry.getPrivateKeyPassword());
  }

  public KeystoreEntry addKeyEntry(String alias, String keyPassword)
  {
    String keyAlgorithm = getCertificate(alias).getPublicKey().getAlgorithm();
    KeystoreEntry keystoreEntry = new KeystoreEntry(alias, keyPassword);
    keystoreEntry.setKeyAlgorithm(keyAlgorithm);
    keystoreEntry.setKeyLength(getKeyLength(alias));
    getKeystoreEntries().add(keystoreEntry);
    return keystoreEntry;
  }

  /**
   * gets a key pair by its alias entry
   *
   * @param alias the alias of the keypair
   * @return the public and private key of the given entry
   */
  public KeyPair getKeyPair(String alias)
  {
    return getKeystoreEntries().stream()
                               .filter(entry -> entry.getAlias().equals(alias))
                               .findAny()
                               .map(this::getKeyPair)
                               .orElseThrow();
  }

  /**
   * retrieves the private and the public key of a specific keystore entry
   *
   * @param keystoreEntry the keystore entry for which we would like to get the public and private key
   * @return the private and public key of the given entry
   */
  @SneakyThrows
  public KeyPair getKeyPair(KeystoreEntry keystoreEntry)
  {
    PrivateKey privateKey = getPrivateKey(keystoreEntry);
    PublicKey publicKey = getKeyStore().getCertificate(keystoreEntry.getAlias()).getPublicKey();
    return new KeyPair(publicKey, privateKey);
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
      privateKey = (PrivateKey)getKeyStore().getKey(keystoreEntry.getAlias(),
                                                    Optional.ofNullable(privateKeyPasswordCharArray)
                                                            .orElse(keystorePasswordCharArray));
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
  public X509Certificate getCertificate(String alias)
  {
    return getCertificate(new KeystoreEntry(alias, null));
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
      X509Certificate x509Certificate = (X509Certificate)getKeyStore().getCertificate(keystoreEntry.getAlias());
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
    return StringUtils.isBlank(keystoreEntry.getAlias());
  }

  /**
   * mainly used for unit tests. This method returns the keystore entries that are really present in the
   * application keystore
   */
  @SneakyThrows
  public List<String> getKeyStoreAliases()
  {
    Enumeration<String> aliasesEnumeration = getKeyStore().aliases();
    List<String> aliases = new ArrayList<>();
    while (aliasesEnumeration.hasMoreElements())
    {
      aliases.add(aliasesEnumeration.nextElement());
    }
    return aliases;
  }

  /**
   * gets the key length of a specific key entry
   *
   * @param alias the key entry of which the keylength should be determined
   * @return the length of the key.
   */
  private Integer getKeyLength(String alias)
  {
    Certificate certificate = getCertificate(alias);
    PublicKey publicKey = certificate.getPublicKey();
    switch (publicKey.getAlgorithm())
    {
      case "RSA":
        return ((RSAPublicKey)publicKey).getModulus().bitLength();
      case "DSA":
        return ((DSAPublicKey)publicKey).getParams().getP().bitLength();
      case "EC":
        return ((ECPublicKey)publicKey).getParams().getOrder().bitLength();
      default:
        // keys of other types should never be stored so this should not happen
        return null;
    }
  }

  /**
   * gets the keystore entry infos for the given alias
   */
  public KeystoreEntry getKeystoreEntry(String alias)
  {
    return getKeystoreEntries().stream().filter(entry -> entry.getAlias().equals(alias)).findAny().orElseThrow();
  }
}
