package de.captaingoldfish.restclient.application.endpoints.httprequests;

import java.time.Instant;

import de.captaingoldfish.restclient.application.utils.Utils;
import de.captaingoldfish.restclient.database.entities.HttpRequestGroup;
import de.captaingoldfish.restclient.scim.resources.ScimHttpRequestGroup;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;


/**
 * author Pascal Knueppel <br>
 * created at: 13.09.2022 - 14:20 <br>
 * <br>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpRequestGroupConverter
{

  /**
   * converts the database representation into its SCIM representation
   */
  public static ScimHttpRequestGroup toScimHttpRequestCategory(HttpRequestGroup httpRequestGroup)
  {
    return ScimHttpRequestGroup.builder()
                               .id(String.valueOf(httpRequestGroup.getId()))
                               .name(httpRequestGroup.getName())
                               .meta(Meta.builder()
                                         .created(httpRequestGroup.getCreated())
                                         .lastModified(httpRequestGroup.getLastModified())
                                         .build())
                               .build();
  }

  /**
   * converts the SCIM representation into its datbase representation
   */
  public static HttpRequestGroup toHttpRequestCategory(ScimHttpRequestGroup scimHttpRequestGroup)
  {
    return HttpRequestGroup.builder()
                           .id(scimHttpRequestGroup.getId().map(Utils::parseId).orElse(0L))
                           .name(scimHttpRequestGroup.getName())
                           .created(scimHttpRequestGroup.getMeta().flatMap(Meta::getCreated).orElse(Instant.now()))
                           .lastModified(scimHttpRequestGroup.getMeta()
                                                             .flatMap(Meta::getLastModified)
                                                             .orElse(Instant.now()))
                           .build();
  }

}
