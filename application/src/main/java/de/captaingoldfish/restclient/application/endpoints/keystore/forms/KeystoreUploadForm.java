package de.captaingoldfish.restclient.application.endpoints.keystore.forms;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * This form is used in the first step of uploading a keystore Is is used to upload a {@link MultipartFile}
 * that hopefully contains keystore data that will be validated and cached for the second step
 * 
 * @see KeystoreSelectAliasForm
 * @author Pascal Knueppel
 * @since 26.03.2021
 */
@Data
@NoArgsConstructor
@KeystoreUploadFormValidation
public class KeystoreUploadForm
{

  /**
   * the password to open the keystore
   */
  private String keystorePassword;

  /**
   * the uploaded file that hopefully is a keystore file
   */
  private MultipartFile keystoreFile;

}
