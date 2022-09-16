package de.captaingoldfish.restclient.scim.resources;

import java.util.Arrays;

import de.captaingoldfish.scim.sdk.common.resources.ResourceNode;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import lombok.Builder;


/**
 * A category that groups http requests together
 */
public class ScimHttpRequestGroup extends ResourceNode
{

  public ScimHttpRequestGroup()
  {
    setSchemas(Arrays.asList(FieldNames.SCHEMA));
  }

  @Builder
  public ScimHttpRequestGroup(String id, Meta meta, String name)
  {
    setSchemas(Arrays.asList(FieldNames.SCHEMA));
    setId(id);
    setMeta(meta);
    setName(name);
  }

  /**
   * the name of the http request category.
   */
  public String getName()
  {
    return getStringAttribute(FieldNames.NAME).get();
  }

  /**
   * the name of the http request category.
   */
  public void setName(String name)
  {
    setAttribute(FieldNames.NAME, name);
  }


  /**
   * contains the attribute names of the resource representation
   */
  public static class FieldNames
  {

    public static final String SCHEMA = "urn:ietf:params:scim:schemas:captaingoldfish:2.0:HttpRequestGroup";


    public static final String NAME = "name";

  }
}
