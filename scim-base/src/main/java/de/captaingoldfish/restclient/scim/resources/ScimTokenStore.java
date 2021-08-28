package de.captaingoldfish.restclient.scim.resources;

import java.util.Collections;
import java.util.Optional;

import de.captaingoldfish.scim.sdk.common.resources.ResourceNode;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import lombok.Builder;


/**
 * @author Pascal Knueppel
 * @since 28.08.2021
 */
public class ScimTokenStore extends ResourceNode
{

  public ScimTokenStore()
  {}

  @Builder
  public ScimTokenStore(String id, String origin, String name, String token, Meta meta)
  {
    setSchemas(Collections.singletonList(FieldNames.SCHEMA_ID));
    setId(id);
    setOrigin(origin);
    setName(name);
    setToken(token);
    setMeta(meta);
  }

  /** an optional identifier that can be used to categorize tokens */
  public Optional<String> getOrigin()
  {
    return getStringAttribute(FieldNames.ORIGIN);
  }

  /** an optional identifier that can be used to categorize tokens */
  public void setOrigin(String origin)
  {
    setAttribute(FieldNames.ORIGIN, origin);
  }

  /**
   * a name that is used as identifier. The combination of origin and name should be unique but it is not a
   * requirement.
   */
  public String getName()
  {
    return getStringAttribute(FieldNames.NAME).orElse(null);
  }

  /**
   * a name that is used as identifier. The combination of origin and name should be unique but it is not a
   * requirement.
   */
  public void setName(String name)
  {
    setAttribute(FieldNames.NAME, name);
  }

  /** the token that is the base of this resource. */
  public String getToken()
  {
    return getStringAttribute(FieldNames.TOKEN).orElse(null);
  }

  /** the token that is the base of this resource. */
  public void setToken(String token)
  {
    setAttribute(FieldNames.TOKEN, token);
  }


  public static class FieldNames
  {

    public static final String SCHEMA_ID = "urn:ietf:params:scim:schemas:captaingoldfish:2.0:TokenStore";

    public static final String ORIGIN = "origin";

    public static final String NAME = "name";

    public static final String ID = "id";

    public static final String TOKEN = "token";
  }
}
