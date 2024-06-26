package de.captaingoldfish.restclient.database.entities;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.apache.commons.io.IOUtils;

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
@NoArgsConstructor
@Entity
@Table(name = "TRUSTSTORE")
public class Truststore
{

  /**
   * the primary key of this table
   */
  @Id
  @Column(name = "ID")
  private long id = 1L;

  /**
   * the bytes representing this keystore
   */
  @Column(name = "TRUSTSTORE_BYTES")
  private byte[] truststoreBytes;

  /**
   * the password to open the truststore. Is only necessary for pkcs PKCS2 keystores
   */
  @Column(name = "TRUSTSTORE_PASSWORD")
  private String truststorePassword;

  /**
   * the type of the given truststore
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "TRUSTSTORE_TYPE")
  private KeyStoreSupporter.KeyStoreType truststoreType;

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
   * the truststore that is the main object of this class
   */
  @Transient
  private KeyStore truststore;

  @SneakyThrows
  public Truststore(InputStream truststoreInputStream,
                    KeyStoreSupporter.KeyStoreType truststoreType,
                    String truststorePassword)
  {
    this.truststoreBytes = IOUtils.toByteArray(truststoreInputStream);
    this.truststorePassword = truststorePassword;
    this.truststoreType = truststoreType;
    this.truststore = KeyStoreSupporter.readTruststore(truststoreBytes, truststoreType, truststorePassword);
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
  public KeyStore getTruststore()
  {
    if (this.truststore == null)
    {
      try
      {
        this.truststore = KeyStoreSupporter.readKeyStore(getTruststoreBytes(),
                                                         getTruststoreType(),
                                                         getTruststorePassword());
      }
      catch (Exception ex)
      {
        log.error(ex.getMessage(), ex);
      }
    }
    return this.truststore;
  }

  /**
   * @return all certificates of the given truststore
   */
  @SneakyThrows
  public List<X509Certificate> getCertificates()
  {
    List<X509Certificate> aliasList = new ArrayList<>();
    Enumeration<String> aliases = getTruststore().aliases();
    while (aliases.hasMoreElements())
    {
      String alias = aliases.nextElement();
      X509Certificate certificate = (X509Certificate)getTruststore().getCertificate(alias);
      aliasList.add(certificate);
    }
    return aliasList;
  }

  /**
   * mainly used for unit tests. This method returns the keystore entries that are really present in the
   * application keystore
   */
  @SneakyThrows
  public List<String> getTruststoreAliases()
  {
    Enumeration<String> aliasesEnumeration = getTruststore().aliases();
    List<String> aliases = new ArrayList<>();
    while (aliasesEnumeration.hasMoreElements())
    {
      aliases.add(aliasesEnumeration.nextElement());
    }
    return aliases;
  }
}
