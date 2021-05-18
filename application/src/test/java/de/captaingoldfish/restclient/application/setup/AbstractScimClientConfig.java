package de.captaingoldfish.restclient.application.setup;

import org.junit.jupiter.api.BeforeEach;

import de.captaingoldfish.scim.sdk.client.ScimClientConfig;
import de.captaingoldfish.scim.sdk.client.ScimRequestBuilder;


/**
 * @author Pascal Knueppel
 * @since 14.05.2021
 */
public class AbstractScimClientConfig extends AbstractOAuthRestClientTest
{

  /**
   * the scim client
   */
  protected ScimRequestBuilder scimRequestBuilder;

  /**
   * initializes the scim client
   */
  @BeforeEach
  public void initializeScimClient()
  {
    final String baseUrl = getApplicationUrl("/scim/v2");
    final ScimClientConfig scimClientConfig = ScimClientConfig.builder()
                                                              .socketTimeout(6000)
                                                              .requestTimeout(6000)
                                                              .build();
    scimRequestBuilder = new ScimRequestBuilder(baseUrl, scimClientConfig);
  }
}
