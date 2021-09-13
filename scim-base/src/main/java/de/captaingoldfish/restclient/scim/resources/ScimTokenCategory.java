package de.captaingoldfish.restclient.scim.resources;

import java.util.Collections;

import de.captaingoldfish.scim.sdk.common.resources.ResourceNode;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import lombok.Builder;


/**
 * @author Pascal Knueppel
 * @since 28.08.2021
 */
public class ScimTokenCategory extends ResourceNode
{

  public ScimTokenCategory()
  {}

  @Builder
  public ScimTokenCategory(String id, String name, Meta meta)
  {
    setSchemas(Collections.singletonList(FieldNames.SCHEMA_ID));
    setId(id);
    setName(name);
    setMeta(meta);
  }

  /**
   * the name of the category.
   */
  public String getName()
  {
    return getStringAttribute(FieldNames.NAME).orElse(null);
  }

  /**
   * the name of the category.
   */
  public void setName(String name)
  {
    setAttribute(FieldNames.NAME, name);
  }

  public static class FieldNames
  {

    public static final String SCHEMA_ID = "urn:ietf:params:scim:schemas:captaingoldfish:2.0:TokenStore";

    public static final String NAME = "name";

    public static final String ID = "id";
  }
}
