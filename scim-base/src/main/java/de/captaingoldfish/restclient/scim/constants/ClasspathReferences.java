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

  public static final String CERTIFICATE_INFO_SCHEMA = BASE_PATH + "/resource/certificate-info.json";

}
