package de.captaingoldfish.restclient.application.endpoints.keystore;

import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StreamUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import de.captaingoldfish.restclient.application.endpoints.keystore.forms.KeystoreAliasRequestForm;
import de.captaingoldfish.restclient.application.endpoints.keystore.forms.KeystoreDownloadInfo;
import de.captaingoldfish.restclient.application.endpoints.keystore.forms.KeystoreEntryInfoForm;
import de.captaingoldfish.restclient.application.endpoints.keystore.forms.KeystoreInfoForm;
import de.captaingoldfish.restclient.application.endpoints.keystore.forms.KeystoreSelectAliasForm;
import de.captaingoldfish.restclient.application.endpoints.keystore.forms.KeystoreUploadForm;
import de.captaingoldfish.restclient.application.endpoints.models.CertificateInfo;
import de.captaingoldfish.restclient.application.exceptions.RequestException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


/**
 * the controller for controlling loading and saving keystore information. The application handles only a
 * single keystore that will receive additional key entries
 * 
 * @author Pascal Knueppel
 * @since 26.03.2021
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/keystore")
public class KeystoreController
{

  /**
   * the service that is used for uploading and loading data for the application keystore
   */
  private final KeystoreService keystoreService;

  /**
   * uploads a keystore and validates the given data
   */
  @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public KeystoreSelectAliasForm uploadKeystore(@Valid KeystoreUploadForm keystoreUploadForm,
                                                BindingResult bindingResult)
  {
    if (bindingResult.hasErrors())
    {
      throw new RequestException("KeystoreUpload validation failed", HttpStatus.BAD_REQUEST.value(), bindingResult);
    }
    return keystoreService.uploadKeystore(keystoreUploadForm);
  }

  /**
   * saves the selected alias into the application keystore
   */
  @PostMapping(path = "/select-alias", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public KeystoreEntryInfoForm selectAlias(@Valid KeystoreSelectAliasForm keystoreSelectAliasForm,
                                           BindingResult bindingResult)
  {
    if (bindingResult.hasErrors())
    {
      throw new RequestException("KeystoreUpload validation failed", HttpStatus.BAD_REQUEST.value(), bindingResult);
    }
    return keystoreService.mergeNewEntryIntoApplicationKeystore(keystoreSelectAliasForm);
  }

  /**
   * loads the certificate information of the application keystore
   */
  @GetMapping(path = "/infos", produces = MediaType.APPLICATION_JSON_VALUE)
  public KeystoreInfoForm getAliases()
  {
    return keystoreService.getKeystoreInfos();
  }

  /**
   * loads the certificate information of the given alias
   */
  @GetMapping(path = "/load-alias", produces = MediaType.APPLICATION_JSON_VALUE)
  public CertificateInfo loadCertificateInfo(@Valid KeystoreAliasRequestForm keystoreAliasRequestForm,
                                             BindingResult bindingResult)
  {
    if (bindingResult.hasErrors())
    {
      throw new RequestException("Cannot load keystore information for alias: " + keystoreAliasRequestForm.getAlias(),
                                 HttpStatus.BAD_REQUEST.value(), bindingResult);
    }
    return keystoreService.loadCertificateInfo(keystoreAliasRequestForm.getAlias());
  }

  /**
   * deletes the key entry of the given alias
   */
  @DeleteMapping(path = "/delete-alias", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteAliases(@Valid KeystoreAliasRequestForm keystoreAliasRequestForm, BindingResult bindingResult)
  {
    if (bindingResult.hasErrors())
    {
      throw new RequestException("Cannot delete alias: " + keystoreAliasRequestForm.getAlias(),
                                 HttpStatus.BAD_REQUEST.value(), bindingResult);
    }
    keystoreService.deleteKeystoreEntry(keystoreAliasRequestForm.getAlias());
  }

  /**
   * downloads the application keystore
   */
  @SneakyThrows
  @GetMapping("/download")
  public void downloadTruststore(HttpServletResponse response)
  {
    KeystoreDownloadInfo truststoreDownloadInfo = keystoreService.getDownloadInfos();
    String contentDisposition = ContentDisposition.builder("attachment")
                                                  .filename(truststoreDownloadInfo.getFilename())
                                                  .build()
                                                  .toString();
    response.addHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);
    response.setContentType(MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE);
    response.setContentLength(truststoreDownloadInfo.getKeystoreBytes().length);
    try (OutputStream bodyStream = response.getOutputStream())
    {
      StreamUtils.copy(truststoreDownloadInfo.getKeystoreBytes(), bodyStream);
    }
  }
}
