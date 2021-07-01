package de.captaingoldfish.restclient.scim.resources;

import java.util.Collections;

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
  public ScimJwtBuilder(String id, String keyId, String header, String body, Meta meta)
  {
    setSchemas(Collections.singletonList(FieldNames.SCHEMA_ID));
    setId(id);
    setKeyId(keyId);
    setHeader(header);
    setBody(body);
    setMeta(meta);
  }

  /** The header of a parsed JWT */
  public String getHeader()
  {
    return getStringAttribute(FieldNames.HEADER).orElse(null);
  }

  /** The header of a parsed JWT */
  public void setHeader(String header)
  {
    setAttribute(FieldNames.HEADER, header);
  }

  /** The header of a parsed JWT */
  public String getKeyId()
  {
    return getStringAttribute(FieldNames.KEY_ID).orElse(null);
  }

  /** The header of a parsed JWT */
  public void setKeyId(String keyId)
  {
    setAttribute(FieldNames.KEY_ID, keyId);
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


  public static class FieldNames
  {

    public static final String SCHEMA_ID = "urn:ietf:params:scim:schemas:captaingoldfish:2.0:JwtBuilder";

    public static final String HEADER = "header";

    public static final String ID = "id";

    public static final String BODY = "body";

    public static final String KEY_ID = "keyId";
  }
}
