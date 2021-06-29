package de.captaingoldfish.restclient.application.endpoints.appinfo;

import java.util.List;
import java.util.stream.Collectors;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;

import de.captaingoldfish.restclient.scim.resources.ScimApplicationInfo;
import de.captaingoldfish.restclient.scim.resources.ScimApplicationInfo.JwtInfo;
import de.captaingoldfish.scim.sdk.common.constants.enums.SortOrder;
import de.captaingoldfish.scim.sdk.common.schemas.SchemaAttribute;
import de.captaingoldfish.scim.sdk.server.endpoints.Context;
import de.captaingoldfish.scim.sdk.server.endpoints.ResourceHandler;
import de.captaingoldfish.scim.sdk.server.filter.FilterNode;
import de.captaingoldfish.scim.sdk.server.response.PartialListResponse;


/**
 * @author Pascal Knueppel
 * @since 29.06.2021
 */
public class AppInfoHandler extends ResourceHandler<ScimApplicationInfo>
{

  /**
   * disabled endpoint
   */
  @Override
  public ScimApplicationInfo createResource(ScimApplicationInfo resource, Context context)
  {
    return null;
  }

  /**
   * provides currently available application information for the UI
   */
  @Override
  public ScimApplicationInfo getResource(String id,
                                         List<SchemaAttribute> attributes,
                                         List<SchemaAttribute> excludedAttributes,
                                         Context context)
  {
    JwtInfo jwtInfo = getJwtInfo();
    return ScimApplicationInfo.builder().jwtInfo(jwtInfo).build();
  }

  /**
   * creates information about JWT algorithms that are currently supported by this application
   */
  private JwtInfo getJwtInfo()
  {
    List<String> jwtSignatureAlgorithms = JWSAlgorithm.Family.RSA.stream()
                                                                 .map(Algorithm::getName)
                                                                 .collect(Collectors.toList());
    jwtSignatureAlgorithms.addAll(JWSAlgorithm.Family.EC.stream().map(Algorithm::getName).collect(Collectors.toList()));

    List<String> jwtKeyWrapAlgorithms = JWEAlgorithm.Family.RSA.stream()
                                                               .map(Algorithm::getName)
                                                               .collect(Collectors.toList());
    jwtKeyWrapAlgorithms.addAll(JWEAlgorithm.Family.ECDH_ES.stream()
                                                           .map(Algorithm::getName)
                                                           .collect(Collectors.toList()));

    List<String> jwtEncryptionAlgorithms = EncryptionMethod.Family.AES_GCM.stream()
                                                                          .map(Algorithm::getName)
                                                                          .collect(Collectors.toList());
    jwtEncryptionAlgorithms.addAll(EncryptionMethod.Family.AES_CBC_HMAC_SHA.stream()
                                                                           .map(Algorithm::getName)
                                                                           .collect(Collectors.toList()));

    return JwtInfo.builder()
                  .signatureAlgorithmsList(jwtSignatureAlgorithms)
                  .keyWrapAlgorithmsList(jwtKeyWrapAlgorithms)
                  .encryptionAlgorithmsList(jwtEncryptionAlgorithms)
                  .build();
  }

  /**
   * disabled endpoint
   */
  @Override
  public PartialListResponse<ScimApplicationInfo> listResources(long startIndex,
                                                                int count,
                                                                FilterNode filter,
                                                                SchemaAttribute sortBy,
                                                                SortOrder sortOrder,
                                                                List<SchemaAttribute> attributes,
                                                                List<SchemaAttribute> excludedAttributes,
                                                                Context context)
  {
    return null;
  }

  /**
   * disabled endpoint
   */
  @Override
  public ScimApplicationInfo updateResource(ScimApplicationInfo resourceToUpdate, Context context)
  {
    return null;
  }

  /**
   * disabled endpoint
   */
  @Override
  public void deleteResource(String id, Context context)
  {

  }
}
