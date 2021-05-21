package de.captaingoldfish.restclient.application.endpoints.openidprovider;

import java.util.Base64;
import java.util.Optional;

import org.apache.commons.lang3.exception.ExceptionUtils;

import de.captaingoldfish.restclient.commons.keyhelper.KeyReader;
import de.captaingoldfish.restclient.database.entities.OpenIdProvider;
import de.captaingoldfish.restclient.scim.resources.ScimOpenIdProvider;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * @author Pascal Knueppel
 * @since 21.05.2021
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OpenIdProviderConverter
{

  public static OpenIdProvider toOpenIdProvider(ScimOpenIdProvider scimOpenIdProvider)
  {
    byte[] signatureVerificationKey = parseSignatureVerificationKey(scimOpenIdProvider);
    return OpenIdProvider.builder()
                         .name(scimOpenIdProvider.getName())
                         .discoveryEndpoint(scimOpenIdProvider.getDiscoveryEndpoint().orElse(null))
                         .authorizationEndpoint(scimOpenIdProvider.getAuthorizationEndpoint().orElse(null))
                         .tokenEndpoint(scimOpenIdProvider.getTokenEndpoint().orElse(null))
                         .resourceEndpoints(scimOpenIdProvider.getResourceEndpoints())
                         .signatureVerificationKey(signatureVerificationKey)
                         .build();
  }

  public static byte[] parseSignatureVerificationKey(ScimOpenIdProvider scimOpenIdProvider)
  {
    String b64KeyOrCert = scimOpenIdProvider.getSignatureVerificationKey().orElse(null);
    if (b64KeyOrCert == null)
    {
      return null;
    }

    byte[] keyOrCert;
    try
    {
      keyOrCert = Base64.getDecoder().decode(b64KeyOrCert);
    }
    catch (IllegalArgumentException ex)
    {
      throw new IllegalArgumentException(String.format("Uploaded key is not Base64 encoded: %s", ex.getMessage()), ex);
    }

    try
    {
      return KeyReader.readX509Certificate(keyOrCert).getPublicKey().getEncoded();
    }
    catch (Exception ex)
    {
      ExceptionUtils.getThrowableList(ex).forEach(e -> log.trace(e.getMessage()));
    }

    try
    {
      return KeyReader.readPublicRSAKey(keyOrCert).getEncoded();
    }
    catch (Exception ex)
    {
      ExceptionUtils.getThrowableList(ex).forEach(e -> log.trace(e.getMessage()));
      throw new IllegalArgumentException("Uploaded key is neither a public RSA key nor a X509 certificate", ex);
    }
  }

  public static ScimOpenIdProvider toScimOpenIdProvider(OpenIdProvider openIdProvider)
  {
    String signatureVerificationKey = Optional.ofNullable(openIdProvider.getSignatureVerificationKey())
                                              .map(b64Key -> Base64.getEncoder().encodeToString(b64Key))
                                              .orElse(null);
    return ScimOpenIdProvider.builder()
                             .id(String.valueOf(openIdProvider.getId()))
                             .name(openIdProvider.getName())
                             .discoveryEndpoint(openIdProvider.getDiscoveryEndpoint())
                             .authorizationEndpoint(openIdProvider.getAuthorizationEndpoint())
                             .tokenEndpoint(openIdProvider.getTokenEndpoint())
                             .resourceEndpointsSet(openIdProvider.getResourceEndpoints())
                             .signatureVerificationKey(signatureVerificationKey)
                             .meta(Meta.builder()
                                       .created(openIdProvider.getCreated())
                                       .lastModified(openIdProvider.getLastModified())
                                       .build())
                             .build();
  }

}
