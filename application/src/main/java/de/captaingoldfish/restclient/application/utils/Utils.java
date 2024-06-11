package de.captaingoldfish.restclient.application.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;

import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;

import de.captaingoldfish.restclient.application.endpoints.OpenIdProviderMetdatdataCache;
import de.captaingoldfish.restclient.application.projectconfig.WebAppConfig;
import de.captaingoldfish.restclient.database.entities.OpenIdClient;
import de.captaingoldfish.restclient.database.entities.OpenIdProvider;
import de.captaingoldfish.scim.sdk.common.exceptions.BadRequestException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


/**
 * @author Pascal Knueppel
 * @since 21.05.2021
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Utils
{

  /**
   * tries to parse a given id for a SCIM [get, update, delete] to a long value
   */
  public static Long parseId(String id)
  {
    try
    {
      return Long.parseLong(id);
    }
    catch (NumberFormatException ex)
    {
      throw new BadRequestException("Invalid ID format: " + id);
    }
  }

  /**
   * loads the OpenID Connect metadata from the identity provider
   *
   * @param openIdClient the OpenID Provider definition
   * @return the metadata of the OpenID Provider
   */
  @SneakyThrows
  public synchronized static OIDCProviderMetadata loadDiscoveryEndpointInfos(OpenIdClient openIdClient)
  {
    final OpenIdProvider openIdProvider = openIdClient.getOpenIdProvider();
    OpenIdProviderMetdatdataCache metadataCache = WebAppConfig.getApplicationContext()
                                                              .getBean(OpenIdProviderMetdatdataCache.class);
    OIDCProviderMetadata metadata = metadataCache.getProviderMetadata(openIdProvider.getId());
    if (metadata != null)
    {
      return metadata;
    }
    String discoveryUrl = openIdProvider.getDiscoveryEndpoint();
    final String responseBody;

    HttpGet httpGet = new HttpGet(discoveryUrl);
    try (CloseableHttpClient httpClient = HttpClientBuilder.getHttpClient(openIdClient);
      CloseableHttpResponse response = httpClient.execute(httpGet))
    {
      responseBody = Utils.getBody(response);
      if (response.getCode() != 200)
      {
        throw new BadRequestException(String.format("Failed to load meta-data from OpenID Discovery endpoint: %s",
                                                    responseBody));
      }
    }
    catch (Exception ex)
    {
      throw new BadRequestException(String.format("Failed to load meta-data from OpenID Discovery endpoint: %s",
                                                  ex.getMessage()),
                                    ex);
    }
    metadata = OIDCProviderMetadata.parse(responseBody);
    metadataCache.setProviderMetadata(openIdProvider.getId(), metadata);
    return metadata;
  }

  @SneakyThrows
  public static String getBody(CloseableHttpResponse response)
  {
    return IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
  }

  /**
   * uses the given value calculates a SHA-256 sum of it and encodes it base64Url-encoding
   */
  @SneakyThrows
  public static String toSha256Base64UrlEncoded(String value)
  {
    MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
    byte[] sha256 = messageDigest.digest(value.getBytes(StandardCharsets.UTF_8));
    return Base64.getUrlEncoder().withoutPadding().encodeToString(sha256);
  }
}
