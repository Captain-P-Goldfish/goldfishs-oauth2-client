package de.captaingoldfish.restclient.application.endpoints.truststore;

import java.io.ByteArrayOutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;

import de.captaingoldfish.restclient.application.endpoints.truststore.validation.ScimTruststoreRequestValidator;
import de.captaingoldfish.restclient.commons.keyhelper.KeyReader;
import de.captaingoldfish.restclient.commons.keyhelper.KeyStoreSupporter;
import de.captaingoldfish.restclient.database.entities.Truststore;
import de.captaingoldfish.restclient.database.repositories.TruststoreDao;
import de.captaingoldfish.restclient.scim.resources.CertificateInfo;
import de.captaingoldfish.restclient.scim.resources.ScimTruststore;
import de.captaingoldfish.restclient.scim.resources.ScimTruststore.CertificateUpload;
import de.captaingoldfish.restclient.scim.resources.ScimTruststore.CertificateUploadResponse;
import de.captaingoldfish.restclient.scim.resources.ScimTruststore.TruststoreUpload;
import de.captaingoldfish.restclient.scim.resources.ScimTruststore.TruststoreUploadResponse;
import de.captaingoldfish.scim.sdk.common.constants.enums.SortOrder;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import de.captaingoldfish.scim.sdk.common.schemas.SchemaAttribute;
import de.captaingoldfish.scim.sdk.server.endpoints.Context;
import de.captaingoldfish.scim.sdk.server.endpoints.ResourceHandler;
import de.captaingoldfish.scim.sdk.server.endpoints.validation.RequestValidator;
import de.captaingoldfish.scim.sdk.server.filter.FilterNode;
import de.captaingoldfish.scim.sdk.server.response.PartialListResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;


/**
 * @author Pascal Knueppel
 * @since 19.05.2021
 */
@RequiredArgsConstructor
public class TruststoreHandler extends ResourceHandler<ScimTruststore>
{

  private final TruststoreDao truststoreDao;

  /**
   * merges all entries of a truststore into the application keystore or a single certificate entry
   */
  @Override
  public ScimTruststore createResource(ScimTruststore scimTruststore, Context context)
  {
    Optional<TruststoreUpload> truststoreUpload = scimTruststore.getTruststoreUpload();
    if (truststoreUpload.isPresent())
    {
      return handleTruststoreUpload(truststoreUpload.get());
    }
    else
    {
      // the case that the getCertificateUpload method returns an empty is already handled by the
      // ScimTruststoreRequestValidator. So we should never get an exception due to the get-call
      return handleCertificateUpload(scimTruststore.getCertificateUpload().get());
    }
  }

  /**
   * merges a truststore into the application truststore
   */
  private ScimTruststore handleTruststoreUpload(TruststoreUpload truststoreUpload)
  {
    Truststore truststore = truststoreDao.getTruststore();
    final String filename = truststoreUpload.getTruststoreFileName().orElse(null);
    KeyStoreSupporter.KeyStoreType type = KeyStoreSupporter.KeyStoreType.byFileExtension(filename)
                                                                        .orElse(KeyStoreSupporter.KeyStoreType.JKS);
    final byte[] truststoreBytes = Base64.getDecoder().decode(truststoreUpload.getTruststoreFile());
    final String truststorePassword = truststoreUpload.getTruststorePassword().orElse(null);
    KeyStore keyStore = KeyStoreSupporter.readTruststore(truststoreBytes, type, truststorePassword);
    KeyStore mergedKeystore = KeyStoreSupporter.mergeTruststores(truststore.getTruststore(),
                                                                 truststore.getTruststorePassword(),
                                                                 keyStore,
                                                                 truststorePassword,
                                                                 truststore.getTruststoreType(),
                                                                 truststore.getTruststorePassword());
    byte[] mergeKeystoreBytes = KeyStoreSupporter.getBytes(mergedKeystore, truststore.getTruststorePassword());
    truststore.setTruststoreBytes(mergeKeystoreBytes);
    truststore.setLastModified(Instant.now());
    truststoreDao.save(truststore);
    return getTruststoreUploadResponse(truststore.getTruststore(), keyStore);
  }

  /**
   * retrieves the aliases from the uploaded keystore that have not been added to the application truststore
   * because of a duplicate alias
   */
  @SneakyThrows
  private ScimTruststore getTruststoreUploadResponse(KeyStore keystoreToMergeIn, KeyStore keystoreToMergeFrom)
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

    TruststoreUploadResponse uploadResponse = TruststoreUploadResponse.builder()
                                                                      .aliasesList(addedAliases)
                                                                      .duplicateAliasesList(duplicateAliases)
                                                                      .duplicateCertificateAliasesList(duplicateCertificateAliases)
                                                                      .build();
    return ScimTruststore.builder()
                         .truststoreUploadResponse(uploadResponse)
                         .meta(Meta.builder().created(Instant.now()).lastModified(Instant.now()).build())
                         .build();
  }

  /**
   * merges a single certificate into the application truststore
   */
  @SneakyThrows
  private ScimTruststore handleCertificateUpload(CertificateUpload certificateUpload)
  {
    Truststore truststore = truststoreDao.getTruststore();
    final byte[] certFile = Base64.getDecoder().decode(certificateUpload.getCertificateFile());
    X509Certificate certificate = KeyReader.readX509Certificate(certFile);
    final String alias = certificateUpload.getAlias();
    KeyStoreSupporter.addCertificateEntry(truststore.getTruststore(), alias, certificate);
    byte[] truststoreBytes = KeyStoreSupporter.getBytes(truststore.getTruststore(), truststore.getTruststorePassword());
    truststore.setTruststoreBytes(truststoreBytes);
    truststore.setLastModified(Instant.now());
    truststoreDao.save(truststore);
    CertificateUploadResponse uploadResponse = new CertificateUploadResponse(alias);
    return ScimTruststore.builder()
                         .certificateUploadResponse(uploadResponse)
                         .meta(Meta.builder().created(Instant.now()).lastModified(Instant.now()).build())
                         .build();
  }

  /**
   * gets the certificate information of a specific alias from the application keystore
   */
  @SneakyThrows
  @Override
  public ScimTruststore getResource(String id,
                                    List<SchemaAttribute> attributes,
                                    List<SchemaAttribute> excludedAttributes,
                                    Context context)
  {
    final String alias = URLDecoder.decode(id, StandardCharsets.UTF_8);
    Truststore applicationTruststore = truststoreDao.getTruststore();

    boolean downloadTruststoreFile = attributes.stream().anyMatch(attribute -> {
      return attribute.getName().equals(ScimTruststore.FieldNames.APPLICATION_TRUSTSTORE);
    });

    final ScimTruststore scimTruststore;
    if (downloadTruststoreFile)
    {
      String base64EncodedTruststore = applicationTruststoreToBase64(applicationTruststore);
      scimTruststore = ScimTruststore.builder().applicationTruststore(base64EncodedTruststore).build();
    }
    else
    {
      X509Certificate certificate = (X509Certificate)applicationTruststore.getTruststore().getCertificate(alias);
      CertificateInfo certificateInfo = CertificateInfo.builder().alias(id).certificate(certificate).build();
      scimTruststore = ScimTruststore.builder().certificateInfo(certificateInfo).build();
    }
    scimTruststore.setId(id);
    scimTruststore.setMeta(Meta.builder().created(Instant.now()).lastModified(Instant.now()).build());
    return scimTruststore;
  }

  /**
   * parses the application keystore to a base64 representation
   *
   * @param applicationTruststore the application keystore for download
   * @return the base64 representation of the application keystore if it should be downloaded
   */
  @SneakyThrows
  private String applicationTruststoreToBase64(Truststore applicationTruststore)
  {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream())
    {
      applicationTruststore.getTruststore()
                           .store(outputStream, applicationTruststore.getTruststorePassword().toCharArray());
      byte[] applicationKeystoreBytes = outputStream.toByteArray();
      return Base64.getEncoder().encodeToString(applicationKeystoreBytes);
    }
  }

  /**
   * gets the aliases that are present within the application keystore
   */
  @Override
  public PartialListResponse<ScimTruststore> listResources(long startIndex,
                                                           int count,
                                                           FilterNode filter,
                                                           SchemaAttribute sortBy,
                                                           SortOrder sortOrder,
                                                           List<SchemaAttribute> attributes,
                                                           List<SchemaAttribute> excludedAttributes,
                                                           Context context)
  {
    Truststore truststore = truststoreDao.getTruststore();
    ScimTruststore scimTruststore = ScimTruststore.builder()
                                                  .aliasesList(truststore.getTruststoreAliases())
                                                  .meta(Meta.builder()
                                                            .created(Instant.now())
                                                            .lastModified(Instant.now())
                                                            .build())
                                                  .build();
    return PartialListResponse.<ScimTruststore> builder()
                              .totalResults(1)
                              .resources(Collections.singletonList(scimTruststore))
                              .build();
  }

  /**
   * update not supported
   */
  @Override
  public ScimTruststore updateResource(ScimTruststore resourceToUpdate, Context context)
  {
    return null;
  }

  @SneakyThrows
  @Override
  public void deleteResource(String id, Context context)
  {
    final String alias = URLDecoder.decode(id, StandardCharsets.UTF_8);
    Truststore truststore = truststoreDao.getTruststore();
    truststore.getTruststore().deleteEntry(alias);
    byte[] truststoreBytes = KeyStoreSupporter.getBytes(truststore.getTruststore(), truststore.getTruststorePassword());
    truststore.setTruststoreBytes(truststoreBytes);
    truststoreDao.save(truststore);
  }

  @Override
  public RequestValidator<ScimTruststore> getRequestValidator()
  {
    return new ScimTruststoreRequestValidator();
  }
}
