package de.captaingoldfish.oauthrestclient.application.endpoints.proxy;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import de.captaingoldfish.oauthrestclient.application.endpoints.proxy.forms.ProxyCreateForm;
import de.captaingoldfish.oauthrestclient.application.endpoints.proxy.forms.ProxyDeleteForm;
import de.captaingoldfish.oauthrestclient.application.endpoints.proxy.forms.ProxyResponseForm;
import de.captaingoldfish.oauthrestclient.application.endpoints.proxy.forms.ProxyUpdateForm;
import de.captaingoldfish.oauthrestclient.database.entities.Proxy;
import de.captaingoldfish.oauthrestclient.database.repositories.ProxyDao;
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
                       .proxyHost(proxyForm.getHost())
                       .proxyPort(Integer.parseInt(proxyForm.getPort()))
                       .proxyUsername(proxyForm.getUsername())
                       .proxyPassword(proxyForm.getPassword())
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
    proxy.setProxyHost(proxyForm.getHost());
    proxy.setProxyPort(Integer.parseInt(proxyForm.getPort()));
    proxy.setProxyUsername(proxyForm.getUsername());
    proxy.setProxyPassword(proxyForm.getPassword());
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
                            .host(proxy.getProxyHost())
                            .port(String.valueOf(proxy.getProxyPort()))
                            .username(proxy.getProxyUsername())
                            .password(proxy.getProxyPassword())
                            .build();
  }
}
