package de.captaingoldfish.restclient.application.endpoints.proxy;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import de.captaingoldfish.restclient.application.endpoints.proxy.forms.ProxyCreateForm;
import de.captaingoldfish.restclient.application.endpoints.proxy.forms.ProxyDeleteForm;
import de.captaingoldfish.restclient.application.endpoints.proxy.forms.ProxyResponseForm;
import de.captaingoldfish.restclient.application.endpoints.proxy.forms.ProxyUpdateForm;
import de.captaingoldfish.restclient.database.entities.Proxy;
import de.captaingoldfish.restclient.database.repositories.ProxyDao;
import lombok.RequiredArgsConstructor;


/**
 * @author Pascal Knueppel
 * @since 07.04.2021
 */
@RequiredArgsConstructor
@Service
public class ProxyService
{

  /**
   * the database access object to do CRUD operations
   */
  private final ProxyDao proxyDao;

  public ProxyResponseForm create(ProxyCreateForm proxyForm)
  {
    Proxy proxy = Proxy.builder()
                       .host(proxyForm.getHost())
                       .port(Integer.parseInt(proxyForm.getPort()))
                       .username(proxyForm.getUsername())
                       .password(proxyForm.getPassword())
                       .build();
    proxy = proxyDao.save(proxy);
    return proxyToResponseForm(proxy);
  }

  public List<ProxyResponseForm> list()
  {
    return proxyDao.findAll().stream().map(this::proxyToResponseForm).collect(Collectors.toList());
  }

  public ProxyResponseForm update(ProxyUpdateForm proxyForm)
  {
    // existence already checked by bean validation
    Proxy proxy = proxyDao.findById(Long.parseLong(proxyForm.getId())).get();
    proxy.setHost(proxyForm.getHost());
    proxy.setPort(Integer.parseInt(proxyForm.getPort()));
    proxy.setUsername(proxyForm.getUsername());
    proxy.setPassword(proxyForm.getPassword());
    proxy = proxyDao.save(proxy);
    return proxyToResponseForm(proxy);
  }

  public void delete(ProxyDeleteForm proxyForm)
  {
    proxyDao.deleteById(Long.parseLong(proxyForm.getId()));
  }

  private ProxyResponseForm proxyToResponseForm(Proxy proxy)
  {
    return ProxyResponseForm.builder()
                            .id(String.valueOf(proxy.getId()))
                            .host(proxy.getHost())
                            .port(String.valueOf(proxy.getPort()))
                            .username(proxy.getUsername())
                            .password(proxy.getPassword())
                            .build();
  }
}
