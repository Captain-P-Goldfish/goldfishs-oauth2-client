package de.captaingoldfish.restclient.scim.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;


/**
 * @author Pascal Knueppel
 * @since 09.05.2021
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClasspathReferences
{

  private static final String BASE_PATH = "/de/captaingoldfish/restclient/scim";

  public static final String KEYSTORE_RESOURCE_TYPE = BASE_PATH + "/resourcetypes/keystore-resource-type.json";

  public static final String KEYSTORE_SCHEMA = BASE_PATH + "/resource/keystore.json";

  public static final String TRUSTSTORE_RESOURCE_TYPE = BASE_PATH + "/resourcetypes/truststore-resource-type.json";

  public static final String TRUSTSTORE_SCHEMA = BASE_PATH + "/resource/truststore.json";

  public static final String PROXY_RESOURCE_TYPE = BASE_PATH + "/resourcetypes/proxy-resource-type.json";

  public static final String PROXY_SCHEMA = BASE_PATH + "/resource/proxy.json";

  public static final String OPEN_ID_PROVIDER_RESOURCE_TYPE = BASE_PATH
                                                              + "/resourcetypes/openid-provider-resource-type.json";

  public static final String OPEN_ID_PROVIDER_SCHEMA = BASE_PATH + "/resource/openid-provider.json";

  public static final String CERTIFICATE_INFO_SCHEMA = BASE_PATH + "/resource/certificate-info.json";

}
