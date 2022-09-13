package de.captaingoldfish.restclient.application.endpoints.httprequests;

import java.time.Instant;

import de.captaingoldfish.restclient.application.utils.Utils;
import de.captaingoldfish.restclient.database.entities.HttpRequestCategory;
import de.captaingoldfish.restclient.scim.resources.ScimHttpRequestCategory;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;


/**
 * author Pascal Knueppel <br>
 * created at: 13.09.2022 - 14:20 <br>
 * <br>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpRequestCategoryConverter
{

  /**
   * converts the database representation into its SCIM representation
   */
  public static ScimHttpRequestCategory toScimHttpRequestCategory(HttpRequestCategory httpRequestCategory)
  {
    return ScimHttpRequestCategory.builder()
                                  .id(String.valueOf(httpRequestCategory.getId()))
                                  .name(httpRequestCategory.getName())
                                  .meta(Meta.builder()
                                            .created(httpRequestCategory.getCreated())
                                            .lastModified(httpRequestCategory.getLastModified())
                                            .build())
                                  .build();
  }

  /**
   * converts the SCIM representation into its datbase representation
   */
  public static HttpRequestCategory toHttpRequestCategory(ScimHttpRequestCategory scimHttpRequestCategory)
  {
    return HttpRequestCategory.builder()
                              .id(scimHttpRequestCategory.getId().map(Utils::parseId).orElse(0L))
                              .name(scimHttpRequestCategory.getName())
                              .created(scimHttpRequestCategory.getMeta()
                                                              .flatMap(Meta::getCreated)
                                                              .orElse(Instant.now()))
                              .lastModified(scimHttpRequestCategory.getMeta()
                                                                   .flatMap(Meta::getLastModified)
                                                                   .orElse(Instant.now()))
                              .build();
  }

}
