package de.captaingoldfish.restclient.application.endpoints.proxy;

import de.captaingoldfish.restclient.database.entities.Proxy;
import de.captaingoldfish.restclient.scim.resources.ScimProxy;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;


/**
 * @author Pascal Knueppel
 * @since 19.05.2021
 */
public class ProxyConverter
{

  public static Proxy toProxy(ScimProxy scimProxy)
  {
    return Proxy.builder()
                .host(scimProxy.getHostname())
                .port(scimProxy.getPort().orElse(8888))
                .username(scimProxy.getUsername().orElse(null))
                .password(scimProxy.getPassword().orElse(null))
                .build();
  }

  public static ScimProxy toScimProxy(Proxy proxy)
  {
    return ScimProxy.builder()
                    .id(String.valueOf(proxy.getId()))
                    .hostname(proxy.getHost())
                    .port(proxy.getPort())
                    .username(proxy.getUsername())
                    .password(proxy.getPassword())
                    .meta(Meta.builder().created(proxy.getCreated()).lastModified(proxy.getLastModified()).build())
                    .build();
  }
}
