package de.captaingoldfish.restclient.scim.resources;

import java.util.Collections;
import java.util.Optional;

import de.captaingoldfish.scim.sdk.common.resources.ResourceNode;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import lombok.Builder;


/**
 * A representation that is used to create signed and encrypted JWTs
 *
 * @author Pascal Knueppel
 * @since 27.06.2021
 */
public class ScimJwtBuilder extends ResourceNode
{

  public ScimJwtBuilder()
  {}

  @Builder
  public ScimJwtBuilder(String id,
                        String keyId,
                        String header,
                        String body,
                        String jwt,
                        Boolean addX5tSHa256Header,
                        Boolean addPublicKeyHeader,
                        Meta meta)
  {
    setSchemas(Collections.singletonList(FieldNames.SCHEMA_ID));
    setId(id);
    setKeyId(keyId);
    setHeader(header);
    setBody(body);
    setJwt(jwt);
    setAddX5tSha256tHeader(Optional.ofNullable(addX5tSHa256Header).orElse(false));
    setAddPublicKeyHeader(Optional.ofNullable(addPublicKeyHeader).orElse(false));
    setMeta(meta);
  }

  /** The header of the JWT that should be created */
  public String getHeader()
  {
    return getStringAttribute(FieldNames.HEADER).orElse(null);
  }

  /** The header of the JWT that should be created */
  public void setHeader(String header)
  {
    setAttribute(FieldNames.HEADER, header);
  }

  /**
   * The id of the key that should be used to sign, encrypt the current data. If left empty the keyId from the
   * header will be used instead as fallback. The keyId must match an existing alias within the application
   * keystore
   */
  public String getKeyId()
  {
    return getStringAttribute(FieldNames.KEY_ID).orElse(null);
  }

  /**
   * The id of the key that should be used to sign, encrypt the current data. If left empty the keyId from the
   * header will be used instead as fallback. The keyId must match an existing alias within the application
   * keystore
   */
  public void setKeyId(String keyId)
  {
    setAttribute(FieldNames.KEY_ID, keyId);
  }

  /**
   * Tells if the SHA-256 of the certificate corresponding to the key used to sign the JWS should be added to
   * the header
   */
  public Boolean isAddX5tSha256tHeader()
  {
    return getBooleanAttribute(FieldNames.ADD_X5T_SHA_256_HEADER).orElse(false);
  }

  /**
   * Tells if the SHA-256 of the certificate corresponding to the key used to sign the JWS should be added to
   * the header
   */
  public void setAddX5tSha256tHeader(boolean addX5tSHa256Header)
  {
    setAttribute(FieldNames.ADD_X5T_SHA_256_HEADER, addX5tSHa256Header);
  }

  /**
   * Tells if the SHA-256 of the certificate corresponding to the key used to sign the JWS should be added to
   * the header
   */
  public Boolean isAddPublicKeyHeader()
  {
    return getBooleanAttribute(FieldNames.ADD_PUBLIC_KEY_HEADER).orElse(false);
  }

  /**
   * Tells if the SHA-256 of the certificate corresponding to the key used to sign the JWS should be added to
   * the header
   */
  public void setAddPublicKeyHeader(boolean addPublicKeyHeader)
  {
    setAttribute(FieldNames.ADD_PUBLIC_KEY_HEADER, addPublicKeyHeader);
  }

  /** The body of a plain JWT */
  public String getBody()
  {
    return getStringAttribute(FieldNames.BODY).orElse(null);
  }

  /** The body of a plain JWT */
  public void setBody(String body)
  {
    setAttribute(FieldNames.BODY, body);
  }

  /** The generated JWT in case of response or the JWT to be verified or decrypted in case of request */
  public String getJwt()
  {
    return getStringAttribute(FieldNames.JWT).orElse(null);
  }

  /** The generated JWT in case of response or the JWT to be verified or decrypted in case of request */
  public void setJwt(String jwt)
  {
    setAttribute(FieldNames.JWT, jwt);
  }


  public static class FieldNames
  {

    public static final String SCHEMA_ID = "urn:ietf:params:scim:schemas:captaingoldfish:2.0:JwtBuilder";

    public static final String HEADER = "header";

    public static final String ID = "id";

    public static final String BODY = "body";

    public static final String KEY_ID = "keyId";

    public static final String JWT = "jwt";

    public static final String ADD_X5T_SHA_256_HEADER = "addX5Sha256tHeader";

    public static final String ADD_PUBLIC_KEY_HEADER = "addPublicKeyHeader";
  }
}
