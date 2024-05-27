package de.captaingoldfish.restclient.application.crypto;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;

import de.captaingoldfish.restclient.application.utils.OAuthConstants;
import de.captaingoldfish.restclient.application.utils.Utils;
import de.captaingoldfish.restclient.scim.resources.ScimCurrentWorkflowSettings;
import de.captaingoldfish.restclient.scim.resources.ScimCurrentWorkflowSettings.Dpop;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;


/**
 * @author Pascal Knueppel
 * @since 22.05.2024
 */
@RequiredArgsConstructor
public class DpopBuilder
{

  private final JwtHandler jwtHandler;


  @SneakyThrows
  public Optional<String> generateDpopAccessTokenHeader(ScimCurrentWorkflowSettings currentWorkflowSettings,
                                                        OIDCProviderMetadata metadata)
  {
    return generateDpopAccessTokenHeader(currentWorkflowSettings, metadata, null);
  }

  @SneakyThrows
  public Optional<String> generateDpopAccessTokenHeader(ScimCurrentWorkflowSettings currentWorkflowSettings,
                                                        OIDCProviderMetadata metadata,
                                                        String accessToken)
  {
    if (!Optional.ofNullable(currentWorkflowSettings)
                 .flatMap(ScimCurrentWorkflowSettings::getDpop)
                 .map(Dpop::isUseDpop)
                 .orElse(false))
    {
      return Optional.empty();
    }
    final String jwsAlgorithmString = currentWorkflowSettings.getDpop()
                                                             .flatMap(Dpop::getSignatureAlgorithm)
                                                             .orElse(null);
    if (StringUtils.isBlank(jwsAlgorithmString))
    {
      return Optional.empty();
    }
    JWSHeader.Builder jwsHeaderBuilder = new JWSHeader.Builder(JWSAlgorithm.parse(jwsAlgorithmString));
    jwsHeaderBuilder.type(new JOSEObjectType(OAuthConstants.DPOP_JWT_TYPE));

    Dpop dpop = currentWorkflowSettings.getDpop().get();
    JWTClaimsSet.Builder jwtClaimsSetBuilder = new JWTClaimsSet.Builder();
    jwtClaimsSetBuilder.jwtID(dpop.getJti().map(StringUtils::stripToNull).orElse(UUID.randomUUID().toString()));
    jwtClaimsSetBuilder.issueTime(new Date());
    jwtClaimsSetBuilder.claim(OAuthConstants.HTM_CLAIIM,
                              currentWorkflowSettings.getDpop()
                                                     .flatMap(Dpop::getHtm)
                                                     .map(StringUtils::stripToNull)
                                                     .orElseGet(() -> {
                                                       if (StringUtils.isBlank(accessToken))
                                                       {
                                                         return "POST";
                                                       }
                                                       else
                                                       {
                                                         return "GET";
                                                       }
                                                     }));
    jwtClaimsSetBuilder.claim(OAuthConstants.HTU_CLAIIM,
                              currentWorkflowSettings.getDpop()
                                                     .flatMap(Dpop::getHtu)
                                                     .map(StringUtils::stripToNull)
                                                     .orElseGet(() -> {
                                                       if (StringUtils.isBlank(accessToken))
                                                       {
                                                         return metadata.getTokenEndpointURI().toString();
                                                       }
                                                       else
                                                       {
                                                         return metadata.getUserInfoEndpointURI().toString();
                                                       }
                                                     }));
    jwtClaimsSetBuilder.claim(OAuthConstants.JTI,
                              currentWorkflowSettings.getDpop()
                                                     .flatMap(Dpop::getJti)
                                                     .map(StringUtils::stripToNull)
                                                     .orElse(UUID.randomUUID().toString()));
    currentWorkflowSettings.getDpop().flatMap(Dpop::getNonce).map(StringUtils::stripToNull).ifPresent(nonce -> {
      jwtClaimsSetBuilder.claim(OAuthConstants.NONCE, nonce);
    });

    Optional.ofNullable(accessToken).ifPresent(value -> {
      jwtClaimsSetBuilder.claim(OAuthConstants.ATH_CLAIM, Utils.toSha256Base64UrlEncoded(value));
    });

    return Optional.of(jwtHandler.createJwt(dpop.getKeyId().orElseThrow(),
                                            jwsHeaderBuilder.build().toString(),
                                            jwtClaimsSetBuilder.build().toString(),
                                            JwtHandler.JwtAttribute.ADD_PUBLIC_KEY));
  }
}
