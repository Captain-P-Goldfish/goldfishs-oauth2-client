package de.captaingoldfish.restclient.application.endpoints.truststore.forms;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * This form is used in the first step of uploading a truststore or certificate file Is is used to upload a
 * {@link MultipartFile} that hopefully contains a keystore or a certificate
 * 
 * @author Pascal Knueppel
 * @since 04.04.2021
 */
@Data
@NoArgsConstructor
@TruststoreUploadFormValidation
public class TruststoreUploadForm
{

  /**
   * the password to open the truststore
   */
  private String truststorePassword;

  /**
   * the uploaded file that hopefully is a truststore file
   */
  private MultipartFile truststoreFile;

  /**
   * the uploaded file that hopefully is a certificate file
   */
  private MultipartFile certificateFile;

  /**
   * the alias name under which the new certificate should be added
   */
  private String alias;

}
