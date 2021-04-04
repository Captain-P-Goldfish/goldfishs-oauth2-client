package de.captaingoldfish.oauthrestclient.application.endpoints.truststore;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.captaingoldfish.oauthrestclient.application.exceptions.RequestException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


/**
 * accepts http requests to modify the application truststore that is used to trust external services
 * 
 * @author Pascal Knueppel
 * @since 04.04.2021
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/truststore")
public class TruststoreController
{

  /**
   * the service that allows us to add new certificate entries to the application truststore
   */
  private final TruststoreService truststoreService;

  /**
   * uploads a truststore and validates the given data
   */
  @PostMapping(path = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public TruststoreUploadResponseForm uploadKeystore(@Valid @ModelAttribute TruststoreUploadForm truststoreUploadForm,
                                                     BindingResult bindingResult)
  {
    if (bindingResult.hasErrors())
    {
      throw new RequestException("TruststoreUpload validation failed", HttpStatus.BAD_REQUEST.value(), bindingResult);
    }
    return truststoreService.saveCertificateEntries(truststoreUploadForm);
  }

  /**
   * @return the number of entries in the truststore and a download link
   */
  @GetMapping("/infos")
  public TruststoreInfoForm getTruststoreInfos()
  {
    return truststoreService.getTruststoreInfos();
  }

  /**
   * downloads the truststore
   */
  @SneakyThrows
  @GetMapping("/download")
  public void downloadTruststore(HttpServletResponse response)
  {
    TruststoreDownloadInfo truststoreDownloadInfo = truststoreService.getDownloadInfos();
    String contentDisposition = ContentDisposition.builder("attachment")
                                                  .filename(truststoreDownloadInfo.getFilename())
                                                  .build()
                                                  .toString();
    response.addHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);
    response.setContentType(MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE);
    response.setContentLength(truststoreDownloadInfo.getTruststoreBytes().length);
    try (OutputStream bodyStream = response.getOutputStream())
    {
      StreamUtils.copy(truststoreDownloadInfo.getTruststoreBytes(), bodyStream);
    }
  }

}
