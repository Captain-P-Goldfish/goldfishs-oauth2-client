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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.io.IOUtils;

import de.captaingoldfish.oauthrestclient.commons.keyhelper.KeyStoreSupporter;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;


/**
 * author Pascal Knueppel <br>
 * created at: 29.04.2018 - 22:46 <br>
 * <br>
 */
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
  @GeneratedValue
  @Column(name = "ID")
  private long id;

  /**
   * a unique identifier for this truststore
   */
  @Column(name = "NAME")
  private String name;

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
  public Truststore(String name,
                    InputStream truststoreInputStream,
                    String truststorePassword,
                    KeyStoreSupporter.KeyStoreType truststoreType)
  {
    this.name = name;
    this.truststoreBytes = IOUtils.toByteArray(truststoreInputStream);
    this.truststorePassword = truststorePassword;
    this.truststoreType = truststoreType;
    this.truststore = KeyStoreSupporter.readTruststore(truststoreBytes, truststoreType, truststorePassword);
  }

  /**
   * will load the truststore instance of this class
   */
  @PostLoad
  public final void loadKTruststore()
  {
    if (KeyStoreSupporter.KeyStoreType.PKCS12.equals(truststoreType))
    {
      setTruststore(KeyStoreSupporter.readTruststore(getTruststoreBytes(),
                                                     getTruststoreType(),
                                                     getTruststorePassword()));
    }
    else
    {
      setTruststore(KeyStoreSupporter.readTruststore(getTruststoreBytes(),
                                                     getTruststoreType(),
                                                     getTruststorePassword()));
    }
  }

  /**
   * will set the values of the keystore based on the given multipartfile
   *
   * @param filename the name of the file
   * @param truststoreFile the keystore file
   */
  public void setTruststore(String filename, byte[] truststoreFile)
  {
    if (truststoreFile == null || truststoreFile.length == 0)
    {
      return;
    }
    final KeyStoreSupporter.KeyStoreType defaultType = KeyStoreSupporter.KeyStoreType.JKS;
    KeyStoreSupporter.KeyStoreType keyStoreType = KeyStoreSupporter.KeyStoreType.byFileExtension(filename)
                                                                                .orElse(defaultType);
    this.truststoreType = keyStoreType;
    this.truststoreBytes = truststoreFile;
    loadKTruststore();
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

}
