package de.captaingoldfish.restclient.scim.resources;

import java.util.Collections;
import java.util.Optional;

import de.captaingoldfish.scim.sdk.common.resources.ResourceNode;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import lombok.Builder;


/**
 * Represents the default settings used by the internal HTTP client
 *
 * @author Pascal Knueppel
 * @since 31.05.2021
 */
public class ScimHttpClientSettings extends ResourceNode
{

  public ScimHttpClientSettings()
  {}

  @Builder
  public ScimHttpClientSettings(String id,
                                Long requestTimeout,
                                Long connectionTimeout,
                                Long socketTimeout,
                                Boolean useHostnameVerifier,
                                Long openIdClientReference,
                                Long proxyReference,
                                Meta meta)
  {
    setSchemas(Collections.singletonList(FieldNames.SCHEMA_ID));
    setId(id);
    setRequestTimeout(requestTimeout);
    setConnectionTimeout(connectionTimeout);
    setSocketTimeout(socketTimeout);
    setUseHostnameVerifier(useHostnameVerifier);
    setOpenIdClientReference(openIdClientReference);
    setProxyReference(proxyReference);
    setMeta(meta);
  }

  /** The request timeout in seconds. */
  public Optional<Long> getRequestTimeout()
  {
    return getLongAttribute(FieldNames.REQUEST_TIMEOUT);
  }

  /** The request timeout in seconds. */
  public void setRequestTimeout(Long requestTimeout)
  {
    setAttribute(FieldNames.REQUEST_TIMEOUT, requestTimeout);
  }

  /** The connection timeout in seconds. */
  public Optional<Long> getConnectionTimeout()
  {
    return getLongAttribute(FieldNames.CONNECTION_TIMEOUT);
  }

  /** The connection timeout in seconds. */
  public void setConnectionTimeout(Long connectionTimeout)
  {
    setAttribute(FieldNames.CONNECTION_TIMEOUT, connectionTimeout);
  }

  /** The socket timeout in seconds. */
  public Optional<Long> getSocketTimeout()
  {
    return getLongAttribute(FieldNames.SOCKET_TIMEOUT);
  }

  /** The socket timeout in seconds. */
  public void setSocketTimeout(Long socketTimeout)
  {
    setAttribute(FieldNames.SOCKET_TIMEOUT, socketTimeout);
  }

  /** If the hostname verifier should be activated or deactivated. */
  public Optional<Boolean> getUseHostnameVerifier()
  {
    return getBooleanAttribute(FieldNames.USE_HOSTNAME_VERIFIER);
  }

  /** If the hostname verifier should be activated or deactivated. */
  public void setUseHostnameVerifier(Boolean useHostnameVerifier)
  {
    setAttribute(FieldNames.USE_HOSTNAME_VERIFIER, useHostnameVerifier);
  }

  /** The id of an OpenID Client that acts as parent of this configuration. */
  public Optional<Long> getOpenIdClientReference()
  {
    return getLongAttribute(FieldNames.OPEN_ID_CLIENT_REFERENCE);
  }

  /** The id of an OpenID Client that acts as parent of this configuration. */
  public void setOpenIdClientReference(Long openIdClientReference)
  {
    setAttribute(FieldNames.OPEN_ID_CLIENT_REFERENCE, openIdClientReference);
  }

  /** The id of a created proxy configuration. */
  public Optional<Long> getProxyReference()
  {
    return getLongAttribute(FieldNames.PROXY_REFERENCE);
  }

  /** The id of a created proxy configuration. */
  public void setProxyReference(Long proxyReference)
  {
    setAttribute(FieldNames.PROXY_REFERENCE, proxyReference);
  }


  public static class FieldNames
  {

    public static final String SCHEMA_ID = "urn:ietf:params:scim:schemas:captaingoldfish:2.0:HttpClientSettings";

    public static final String USE_HOSTNAME_VERIFIER = "useHostnameVerifier";

    public static final String PROXY_REFERENCE = "proxyReference";

    public static final String OPEN_ID_CLIENT_REFERENCE = "openIdClientReference";

    public static final String SOCKET_TIMEOUT = "socketTimeout";

    public static final String CONNECTION_TIMEOUT = "connectionTimeout";

    public static final String REQUEST_TIMEOUT = "requestTimeout";
  }
}
