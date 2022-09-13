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

  public static final String OPEN_ID_CLIENT_RESOURCE_TYPE = BASE_PATH
                                                            + "/resourcetypes/openid-client-resource-type.json";

  public static final String OPEN_ID_CLIENT_SCHEMA = BASE_PATH + "/resource/openid-client.json";

  public static final String HTTP_CLIENT_SETTINGS_RESOURCE_TYPE = BASE_PATH
                                                                  + "/resourcetypes/http-client-settings-resource-type.json";

  public static final String HTTP_CLIENT_SETTINGS_SCHEMA = BASE_PATH + "/resource/http-client-settings.json";

  public static final String JWT_BUILDER_RESOURCE_TYPE = BASE_PATH + "/resourcetypes/jwt-builder-resource-type.json";

  public static final String JWT_BUILDER_SCHEMA = BASE_PATH + "/resource/jwt-builder.json";

  public static final String APP_INFO_RESOURCE_TYPE = BASE_PATH + "/resourcetypes/app-info-resource-type.json";

  public static final String APP_INFO_SCHEMA = BASE_PATH + "/resource/app-infos.json";

  public static final String AUTH_CODE_GRANT_REQUEST_RESOURCE_TYPE = BASE_PATH
                                                                     + "/resourcetypes/auth-code-grant-request-resource-type.json";

  public static final String AUTH_CODE_GRANT_REQUEST_SCHEMA = BASE_PATH + "/resource/auth-code-grant-request.json";

  public static final String ACCESS_TOKEN_REQUEST_RESOURCE_TYPE = BASE_PATH
                                                                  + "/resourcetypes/access-token-request-resource-type.json";

  public static final String ACCESS_TOKEN_REQUEST_SCHEMA = BASE_PATH + "/resource/access-token-request.json";

  public static final String CURRENT_WORKFLOW_SETTINGS_RESOURCE_TYPE = BASE_PATH
                                                                       + "/resourcetypes/current-workflow-settings-resource-type.json";

  public static final String CURRENT_WORKFLOW_SETTINGS_SCHEMA = BASE_PATH + "/resource/current-workflow-settings.json";

  public static final String TOKEN_STORE_RESOURCE_TYPE = BASE_PATH + "/resourcetypes/token-store-resource-type.json";

  public static final String TOKEN_STORE_SCHEMA = BASE_PATH + "/resource/token-store.json";

  public static final String TOKEN_CATEGORY_RESOURCE_TYPE = BASE_PATH
                                                            + "/resourcetypes/token-category-resource-type.json";

  public static final String TOKEN_CATEGORY_SCHEMA = BASE_PATH + "/resource/token-category.json";

  public static final String CERTIFICATE_INFO_SCHEMA = BASE_PATH + "/resource/certificate-info.json";


  public static final String HTTP_REQUESTS_RESOURCE_TYPE = BASE_PATH + "/resourcetypes/http-request-resource-type.json";

  public static final String HTTP_REQUESTS_SCHEMA = BASE_PATH + "/resource/http-request.json";

  public static final String HTTP_REQUESTS_CATEGORY_RESOURCE_TYPE = BASE_PATH
                                                                    + "/resourcetypes/http-request-category-resource-type.json";

  public static final String HTTP_REQUESTS_CATEGORY_SCHEMA = BASE_PATH + "/resource/http-request-category.json";

}
