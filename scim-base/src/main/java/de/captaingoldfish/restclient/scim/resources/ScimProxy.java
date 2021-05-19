package de.captaingoldfish.restclient.scim.resources;

import java.util.Collections;
import java.util.Optional;

import de.captaingoldfish.scim.sdk.common.resources.ResourceNode;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import lombok.Builder;


/**
 * a connection representation to a simple HTTP proxy
 *
 * @author Pascal Knueppel
 * @since 19.05.2021
 */
public class ScimProxy extends ResourceNode
{

  public ScimProxy()
  {}

  @Builder
  public ScimProxy(String id, String hostname, Integer port, String username, String password, Meta meta)
  {
    setSchemas(Collections.singletonList(FieldNames.SCHEMA_ID));
    setId(id);
    setHostname(hostname);
    setPort(port);
    setUsername(username);
    setPassword(password);
    setMeta(meta);
  }

  /** The hostname or IP-address of the proxy */
  public String getHostname()
  {
    return getStringAttribute(FieldNames.HOSTNAME).orElse(null);
  }

  /** The hostname or IP-address of the proxy */
  public void setHostname(String hostname)
  {
    setAttribute(FieldNames.HOSTNAME, hostname);
  }

  /** The port of the proxy. If not set the default will be 8888 (Telerik Fiddler Proxy) */
  public Optional<Integer> getPort()
  {
    return getIntegerAttribute(FieldNames.PORT);
  }

  /** The port of the proxy. If not set the default will be 8888 (Telerik Fiddler Proxy) */
  public void setPort(Integer port)
  {
    setAttribute(FieldNames.PORT, port);
  }

  /** The username to authenticate at the proxy if the proxy requires authentication */
  public Optional<String> getUsername()
  {
    return getStringAttribute(FieldNames.USERNAME);
  }

  /** The username to authenticate at the proxy if the proxy requires authentication */
  public void setUsername(String username)
  {
    setAttribute(FieldNames.USERNAME, username);
  }

  /** The password to authenticate at the proxy if the proxy requires authentication */
  public Optional<String> getPassword()
  {
    return getStringAttribute(FieldNames.PASSWORD);
  }

  /** The password to authenticate at the proxy if the proxy requires authentication */
  public void setPassword(String password)
  {
    setAttribute(FieldNames.PASSWORD, password);
  }


  public static class FieldNames
  {

    public static final String SCHEMA_ID = "urn:ietf:params:scim:schemas:captaingoldfish:2.0:Proxy";

    public static final String HOSTNAME = "hostname";

    public static final String PASSWORD = "password";

    public static final String PORT = "port";

    public static final String ID = "id";

    public static final String USERNAME = "username";
  }
}
