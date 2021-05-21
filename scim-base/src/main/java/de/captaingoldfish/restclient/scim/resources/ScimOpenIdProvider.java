package de.captaingoldfish.restclient.scim.resources;


import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import de.captaingoldfish.scim.sdk.common.resources.ResourceNode;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import lombok.Builder;


/**
 * represents the connection details to a specific OpenID provider
 *
 * @author Pascal Knueppel
 * @since 21.05.2021
 */
public class ScimOpenIdProvider extends ResourceNode
{

  public ScimOpenIdProvider()
  {}

  @Builder
  public ScimOpenIdProvider(String id,
                            String name,
                            String discoveryEndpoint,
                            String authorizationEndpoint,
                            String tokenEndpoint,
                            Set<String> resourceEndpointsSet,
                            String signatureVerificationKey,
                            Meta meta)
  {
    setSchemas(Collections.singletonList(FieldNames.SCHEMA_ID));
    setId(id);
    setName(name);
    setDiscoveryEndpoint(discoveryEndpoint);
    setAuthorizationEndpoint(authorizationEndpoint);
    setTokenEndpoint(tokenEndpoint);
    setResourceEndpoints(resourceEndpointsSet);
    setSignatureVerificationKey(signatureVerificationKey);
    setMeta(meta);
  }

  /** A name that is used as human readable identifier that will be displayed in the UI */
  public String getName()
  {
    return getStringAttribute(FieldNames.NAME).orElse(null);
  }

  /** A name that is used as human readable identifier that will be displayed in the UI */
  public void setName(String name)
  {
    setAttribute(FieldNames.NAME, name);
  }

  /** The url to the discovery endpoint of an OpenID Provider */
  public Optional<String> getDiscoveryEndpoint()
  {
    return getStringAttribute(FieldNames.DISCOVERY_ENDPOINT);
  }

  /** The url to the discovery endpoint of an OpenID Provider */
  public void setDiscoveryEndpoint(String discoveryEndpoint)
  {
    setAttribute(FieldNames.DISCOVERY_ENDPOINT, discoveryEndpoint);
  }

  /**
   * The url to the authorization endpoint. This is an optional value in case that no discovery endpoint is
   * provided
   */
  public Optional<String> getAuthorizationEndpoint()
  {
    return getStringAttribute(FieldNames.AUTHORIZATION_ENDPOINT);
  }

  /**
   * The url to the authorization endpoint. This is an optional value in case that no discovery endpoint is
   * provided
   */
  public void setAuthorizationEndpoint(String authorizationEndpoint)
  {
    setAttribute(FieldNames.AUTHORIZATION_ENDPOINT, authorizationEndpoint);
  }

  /** The url to the token endpoint. This is an optional value in case that no discovery endpoint is provided */
  public Optional<String> getTokenEndpoint()
  {
    return getStringAttribute(FieldNames.TOKEN_ENDPOINT);
  }

  /** The url to the token endpoint. This is an optional value in case that no discovery endpoint is provided */
  public void setTokenEndpoint(String tokenEndpoint)
  {
    setAttribute(FieldNames.TOKEN_ENDPOINT, tokenEndpoint);
  }

  /** A list of urls to defined resource endpoints that may be accessible with an acquired token */
  public Set<String> getResourceEndpoints()
  {
    return getSimpleArrayAttributeSet(FieldNames.RESOURCE_ENDPOINTS);
  }

  /** A list of urls to defined resource endpoints that may be accessible with an acquired token */
  public void setResourceEndpoints(Set<String> resourceEndpointsList)
  {
    setStringAttributeList(FieldNames.RESOURCE_ENDPOINTS, resourceEndpointsList);
  }

  /**
   * Base64 encoded public RSA key or X509 certificate that holds the public key for signature verification.
   * This is an optional value in case that no JWKS endpoint is provided via discovery endpoint
   */
  public Optional<String> getSignatureVerificationKey()
  {
    return getStringAttribute(FieldNames.SIGNATURE_VERIFICATION_KEY);
  }

  /**
   * Base64 encoded public RSA key or X509 certificate that holds the public key for signature verification.
   * This is an optional value in case that no JWKS endpoint is provided via discovery endpoint
   */
  public void setSignatureVerificationKey(String signatureVerificationKey)
  {
    setAttribute(FieldNames.SIGNATURE_VERIFICATION_KEY, signatureVerificationKey);
  }


  public static class FieldNames
  {

    public static final String SCHEMA_ID = "urn:ietf:params:scim:schemas:captaingoldfish:2.0:OpenIdProvider";

    public static final String TOKEN_ENDPOINT = "tokenEndpoint";

    public static final String RESOURCE_ENDPOINTS = "resourceEndpoints";

    public static final String SIGNATURE_VERIFICATION_KEY = "signatureVerificationKey";

    public static final String NAME = "name";

    public static final String DISCOVERY_ENDPOINT = "discoveryEndpoint";

    public static final String ID = "id";

    public static final String AUTHORIZATION_ENDPOINT = "authorizationEndpoint";
  }
}
