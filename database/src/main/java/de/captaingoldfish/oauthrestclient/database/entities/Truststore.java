package de.captaingoldfish.oauthrestclient.database.entities;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.io.IOUtils;

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
  }

  /**
   * will load the keystore instance
   */
  @PostLoad
  public final void loadKeystore()
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

  /**
   * @return all certificates of the given truststore
   */
  @SneakyThrows
  public List<X509Certificate> getCertificates()
  {
    List<X509Certificate> aliasList = new ArrayList<>();
    Enumeration<String> aliases = truststore.aliases();
    while (aliases.hasMoreElements())
    {
      String alias = aliases.nextElement();
      X509Certificate certificate = (X509Certificate)truststore.getCertificate(alias);
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
    Enumeration<String> aliasesEnumeration = truststore.aliases();
    List<String> aliases = new ArrayList<>();
    while (aliasesEnumeration.hasMoreElements())
    {
      aliases.add(aliasesEnumeration.nextElement());
    }
    return aliases;
  }
}
