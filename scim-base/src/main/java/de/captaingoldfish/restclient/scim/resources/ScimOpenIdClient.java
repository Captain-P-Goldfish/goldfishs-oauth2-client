package de.captaingoldfish.restclient.scim.resources;


import java.util.Collections;
import java.util.Optional;

import de.captaingoldfish.scim.sdk.common.resources.ResourceNode;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import lombok.Builder;


/**
 * the client details that was registered at a specific OpenID Provider
 *
 * @author Pascal Knueppel
 * @since 28.05.2021
 */
public class ScimOpenIdClient extends ResourceNode
{

  public ScimOpenIdClient()
  {}

  @Builder
  public ScimOpenIdClient(String id,
                          Long openIdProviderId,
                          String clientId,
                          String clientSecret,
                          String audience,
                          String signingKeyRef,
                          String decryptionKeyRef,
                          String tlsClientAuthKeyRef,
                          Meta meta)
  {
    setSchemas(Collections.singletonList(FieldNames.SCHEMA_ID));
    setId(id);
    setOpenIdProviderId(openIdProviderId);
    setClientId(clientId);
    setClientSecret(clientSecret);
    setAudience(audience);
    setSigningKeyRef(signingKeyRef);
    setDecryptionKeyRef(decryptionKeyRef);
    setTlsClientAuthKeyRef(tlsClientAuthKeyRef);
    setMeta(meta);
  }

  /** The ID reference to the OpenID Provider that is the owner of this client. */
  public Long getOpenIdProviderId()
  {
    return getLongAttribute(FieldNames.OPEN_ID_PROVIDER_ID).orElse(null);
  }

  /** The ID reference to the OpenID Provider that is the owner of this client. */
  public void setOpenIdProviderId(Long openIdProviderId)
  {
    setAttribute(FieldNames.OPEN_ID_PROVIDER_ID, openIdProviderId);
  }

  /** The unique identifier of the client */
  public String getClientId()
  {
    return getStringAttribute(FieldNames.CLIENT_ID).orElse(null);
  }

  /** The unique identifier of the client */
  public void setClientId(String clientId)
  {
    setAttribute(FieldNames.CLIENT_ID, clientId);
  }

  /**
   * The password of the client. This is an optional value that may be used as an alternative to JWT
   * authentication
   */
  public Optional<String> getClientSecret()
  {
    return getStringAttribute(FieldNames.CLIENT_SECRET);
  }

  /**
   * The password of the client. This is an optional value that may be used as an alternative to JWT
   * authentication
   */
  public void setClientSecret(String clientSecret)
  {
    setAttribute(FieldNames.CLIENT_SECRET, clientSecret);
  }

  /**
   * The audience is an optional field that becomes necessary if JWT authentication is used. This field will be
   * entered into the created JWTs audience value.
   */
  public Optional<String> getAudience()
  {
    return getStringAttribute(FieldNames.AUDIENCE);
  }

  /**
   * The audience is an optional field that becomes necessary if JWT authentication is used. This field will be
   * entered into the created JWTs audience value.
   */
  public void setAudience(String audience)
  {
    setAttribute(FieldNames.AUDIENCE, audience);
  }

  /** The alias of the key reference within the application keystore to sign JWTs for authentication. */
  public Optional<String> getSigningKeyRef()
  {
    return getStringAttribute(FieldNames.SIGNING_KEY_REF);
  }

  /** The alias of the key reference within the application keystore to sign JWTs for authentication. */
  public void setSigningKeyRef(String signingKeyRef)
  {
    setAttribute(FieldNames.SIGNING_KEY_REF, signingKeyRef);
  }

  /**
   * The alias of the key reference within the application keystore to decrypt JWTs on responses e.g. the ID
   * Token.
   */
  public Optional<String> getDecryptionKeyRef()
  {
    return getStringAttribute(FieldNames.DECRYPTION_KEY_REF);
  }

  /**
   * The alias of the key reference within the application keystore to decrypt JWTs on responses e.g. the ID
   * Token.
   */
  public void setDecryptionKeyRef(String decryptionKeyRef)
  {
    setAttribute(FieldNames.DECRYPTION_KEY_REF, decryptionKeyRef);
  }

  /**
   * The alias of the key reference within the application keystore to authenticate with alternative TLS client
   * authentication instead of JWT of basic authentication.
   */
  public Optional<String> getTlsClientAuthKeyRef()
  {
    return getStringAttribute(FieldNames.TLS_CLIENT_AUTH_KEY_REF);
  }

  /**
   * The alias of the key reference within the application keystore to authenticate with alternative TLS client
   * authentication instead of JWT of basic authentication.
   */
  public void setTlsClientAuthKeyRef(String tlsClientAuthKeyRef)
  {
    setAttribute(FieldNames.TLS_CLIENT_AUTH_KEY_REF, tlsClientAuthKeyRef);
  }


  public static class FieldNames
  {

    public static final String SCHEMA_ID = "urn:ietf:params:scim:schemas:captaingoldfish:2.0:OpenIdClient";

    public static final String AUDIENCE = "audience";

    public static final String CLIENT_ID = "clientId";

    public static final String OPEN_ID_PROVIDER_ID = "openIdProviderId";

    public static final String DECRYPTION_KEY_REF = "decryptionKeyRef";

    public static final String TLS_CLIENT_AUTH_KEY_REF = "tlsClientAuthKeyRef";

    public static final String CLIENT_SECRET = "clientSecret";

    public static final String ID = "id";

    public static final String SIGNING_KEY_REF = "signingKeyRef";
  }
}
