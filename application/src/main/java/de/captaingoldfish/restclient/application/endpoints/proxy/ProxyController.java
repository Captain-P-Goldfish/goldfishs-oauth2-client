package de.captaingoldfish.restclient.application.endpoints.proxy;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import de.captaingoldfish.restclient.application.endpoints.proxy.forms.ProxyCreateForm;
import de.captaingoldfish.restclient.application.endpoints.proxy.forms.ProxyDeleteForm;
import de.captaingoldfish.restclient.application.endpoints.proxy.forms.ProxyResponseForm;
import de.captaingoldfish.restclient.application.endpoints.proxy.forms.ProxyUpdateForm;
import de.captaingoldfish.restclient.application.exceptions.RequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * @author Pascal Knueppel
 * @since 07.04.2021
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/proxy")
public class ProxyController
{

  private final ProxyService proxyService;

  @PostMapping(path = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public ProxyResponseForm createProxy(@RequestBody @Valid ProxyCreateForm proxyForm, BindingResult bindingResult)
  {
    if (bindingResult.hasErrors())
    {
      throw new RequestException("could not create proxy", HttpStatus.BAD_REQUEST.value(), bindingResult);
    }
    return proxyService.create(proxyForm);
  }

  @GetMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<ProxyResponseForm> listProxies()
  {
    return proxyService.list();
  }

  @PutMapping(path = "/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ProxyResponseForm updateProxy(@RequestBody @Valid ProxyUpdateForm proxyForm, BindingResult bindingResult)
  {
    if (bindingResult.hasErrors())
    {
      throw new RequestException("could not update proxy", HttpStatus.BAD_REQUEST.value(), bindingResult);
    }
    return proxyService.update(proxyForm);
  }

  @DeleteMapping(path = "/delete", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteProxy(@RequestBody @Valid ProxyDeleteForm proxyForm, BindingResult bindingResult)
  {
    if (bindingResult.hasErrors())
    {
      throw new RequestException("could not delete proxy", HttpStatus.BAD_REQUEST.value(), bindingResult);
    }
    proxyService.delete(proxyForm);
  }
}
