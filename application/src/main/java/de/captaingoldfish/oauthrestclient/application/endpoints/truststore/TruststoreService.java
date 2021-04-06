package de.captaingoldfish.oauthrestclient.application.endpoints.truststore;

import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import de.captaingoldfish.oauthrestclient.application.endpoints.models.CertificateInfo;
import de.captaingoldfish.oauthrestclient.application.endpoints.truststore.forms.TruststoreAliasRequestForm;
import de.captaingoldfish.oauthrestclient.application.endpoints.truststore.forms.TruststoreDownloadInfo;
import de.captaingoldfish.oauthrestclient.application.endpoints.truststore.forms.TruststoreInfoForm;
import de.captaingoldfish.oauthrestclient.application.endpoints.truststore.forms.TruststoreUploadForm;
import de.captaingoldfish.oauthrestclient.application.endpoints.truststore.forms.TruststoreUploadResponseForm;
import de.captaingoldfish.oauthrestclient.commons.keyhelper.KeyReader;
import de.captaingoldfish.oauthrestclient.commons.keyhelper.KeyStoreSupporter;
import de.captaingoldfish.oauthrestclient.database.entities.Truststore;
import de.captaingoldfish.oauthrestclient.database.repositories.TruststoreDao;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;


/**
 * allows to modify the application truststore that is used to trust external services
 * 
 * @author Pascal Knueppel
 * @since 04.04.2021
 */
@RequiredArgsConstructor
@Service
public class TruststoreService
{

  /**
   * the database access object for the application truststore
   */
  private final TruststoreDao truststoreDao;


  /**
   * merges either an uploaded keystore with the application keystore or adds a single certificate entry to the
   * truststore
   */
  public TruststoreUploadResponseForm saveCertificateEntries(TruststoreUploadForm truststoreUploadForm)
  {
    if (truststoreUploadForm.getTruststoreFile() != null && !truststoreUploadForm.getTruststoreFile().isEmpty())
    {
      return mergeKeystore(truststoreUploadForm.getTruststoreFile(), truststoreUploadForm.getTruststorePassword());
    }
    else
    {
      return addCertificate(truststoreUploadForm.getCertificateFile(), truststoreUploadForm.getAlias());
    }
  }

  /**
   * merges the application truststore with the uploaded truststore
   */
  @SneakyThrows
  private TruststoreUploadResponseForm mergeKeystore(MultipartFile keystoreFile, String keystorePassword)
  {
    Truststore truststore = truststoreDao.getTruststore();
    KeyStoreSupporter.KeyStoreType type = KeyStoreSupporter.KeyStoreType.byFileExtension(keystoreFile.getName())
                                                                        .orElse(KeyStoreSupporter.KeyStoreType.JKS);
    KeyStore keyStore = KeyStoreSupporter.readTruststore(keystoreFile.getBytes(), type, keystorePassword);
    KeyStore mergedKeystore = KeyStoreSupporter.mergeTruststores(truststore.getTruststore(),
                                                                 truststore.getTruststorePassword(),
                                                                 keyStore,
                                                                 keystorePassword,
                                                                 truststore.getTruststoreType(),
                                                                 truststore.getTruststorePassword());
    byte[] mergeKeystoreBytes = KeyStoreSupporter.getBytes(mergedKeystore, truststore.getTruststorePassword());
    truststore.setTruststoreBytes(mergeKeystoreBytes);
    truststoreDao.save(truststore);
    return getTruststoreUploadResponse(truststore.getTruststore(), keyStore);
  }

  /**
   * retrieves the aliases from the uploaded keystore that have not been added to the application truststore
   * because of a duplicate alias
   */
  @SneakyThrows
  private TruststoreUploadResponseForm getTruststoreUploadResponse(KeyStore keystoreToMergeIn,
                                                                   KeyStore keystoreToMergeFrom)
  {
    List<String> addedAliases = new ArrayList<>();
    List<String> duplicateAliases = new ArrayList<>();
    List<String> duplicateCertificateAliases = new ArrayList<>();
    Enumeration<String> aliasesToMerge = KeyStoreSupporter.getAliases(keystoreToMergeFrom);

    while (aliasesToMerge.hasMoreElements())
    {
      String aliasToMerge = aliasesToMerge.nextElement();
      if (keystoreToMergeIn.containsAlias(aliasToMerge))
      {
        duplicateAliases.add(aliasToMerge);
      }
      else
      {
        X509Certificate certificate = (X509Certificate)keystoreToMergeFrom.getCertificate(aliasToMerge);
        if (keystoreToMergeIn.getCertificateAlias(certificate) != null)
        {
          duplicateCertificateAliases.add(aliasToMerge);
        }
        else
        {
          addedAliases.add(aliasToMerge);
        }
      }
    }

    return TruststoreUploadResponseForm.builder()
                                       .aliases(addedAliases)
                                       .duplicateAliases(duplicateAliases)
                                       .duplicateCertificates(duplicateCertificateAliases)
                                       .build();
  }

  /**
   * adds an uploaded certificate to the application truststore
   */
  @SneakyThrows
  private TruststoreUploadResponseForm addCertificate(MultipartFile certFile, String alias)
  {
    Truststore truststore = truststoreDao.getTruststore();
    X509Certificate certificate = KeyReader.readX509Certificate(certFile.getBytes());
    KeyStoreSupporter.addCertificateEntry(truststore.getTruststore(), alias, certificate);
    byte[] truststoreBytes = KeyStoreSupporter.getBytes(truststore.getTruststore(), truststore.getTruststorePassword());
    truststore.setTruststoreBytes(truststoreBytes);
    truststoreDao.save(truststore);
    return TruststoreUploadResponseForm.builder().aliases(Collections.singletonList(alias)).build();
  }

  @SneakyThrows
  public TruststoreInfoForm getTruststoreInfos()
  {
    Truststore truststore = truststoreDao.getTruststore();
    return new TruststoreInfoForm(truststore.getTruststore().size(), truststore.getTruststoreAliases());
  }

  public TruststoreDownloadInfo getDownloadInfos()
  {
    Truststore truststore = truststoreDao.getTruststore();
    String filename = "application-truststore." + truststore.getTruststoreType().getFileExtension();
    return new TruststoreDownloadInfo(truststore.getTruststoreBytes(), filename);
  }

  @SneakyThrows
  public void deleteAlias(TruststoreAliasRequestForm truststoreAliasRequestForm)
  {
    Truststore truststore = truststoreDao.getTruststore();
    truststore.getTruststore().deleteEntry(truststoreAliasRequestForm.getAlias());
    byte[] truststoreBytes = KeyStoreSupporter.getBytes(truststore.getTruststore(), truststore.getTruststorePassword());
    truststore.setTruststoreBytes(truststoreBytes);
    truststoreDao.save(truststore);
  }

  @SneakyThrows
  public CertificateInfo loadAlias(TruststoreAliasRequestForm truststoreAliasRequestForm)
  {
    Truststore truststore = truststoreDao.getTruststore();
    X509Certificate certificate = (X509Certificate)truststore.getTruststore()
                                                             .getCertificate(truststoreAliasRequestForm.getAlias());
    return new CertificateInfo(certificate);
  }
}
