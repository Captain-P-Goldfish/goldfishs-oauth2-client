package de.captaingoldfish.oauthrestclient.application.endpoints.keystore;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.captaingoldfish.oauthrestclient.application.exceptions.RequestException;
import lombok.RequiredArgsConstructor;
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
  public KeystoreAliasForm uploadKeystore(@Valid @ModelAttribute KeystoreUploadForm keystoreUploadForm,
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
  public KeystoreEntryInfoForm selectAlias(@Valid @ModelAttribute KeystoreAliasForm keystoreAliasForm,
                                           BindingResult bindingResult)
  {
    if (bindingResult.hasErrors())
    {
      throw new RequestException("KeystoreUpload validation failed", HttpStatus.BAD_REQUEST.value(), bindingResult);
    }
    return keystoreService.mergeNewEntryIntoApplicationKeystore(keystoreAliasForm);
  }

  /**
   * loads the certificate information of the application keystore
   */
  @GetMapping(path = "/aliases", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<KeystoreEntryInfoForm> getAliases()
  {
    return keystoreService.getKeystoreInfos();
  }

}
