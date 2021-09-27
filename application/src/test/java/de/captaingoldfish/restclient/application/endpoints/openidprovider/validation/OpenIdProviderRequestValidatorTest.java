package de.captaingoldfish.restclient.application.endpoints.openidprovider.validation;

import java.security.KeyPair;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Set;
import java.util.function.Supplier;

import org.bouncycastle.crypto.KeyGenerationParameters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.captaingoldfish.restclient.application.setup.SpringBootInitializer;
import de.captaingoldfish.restclient.application.utils.TestUtils;
import de.captaingoldfish.restclient.database.config.DatabaseConfig;
import de.captaingoldfish.restclient.database.repositories.OpenIdProviderDao;
import de.captaingoldfish.restclient.scim.resources.ScimOpenIdProvider;
import de.captaingoldfish.scim.sdk.server.endpoints.Context;
import de.captaingoldfish.scim.sdk.server.endpoints.validation.ValidationContext;
import de.captaingoldfish.scim.sdk.server.schemas.ResourceType;


/**
 * @author Pascal Knueppel
 * @since 22.05.2021
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DatabaseConfig.class, SpringBootInitializer.class})
@TestPropertySource(properties = "database.url=jdbc:hsqldb:mem:oauth2restclient")
public class OpenIdProviderRequestValidatorTest
{

  /**
   * argument needed for creating the {@link OpenIdProviderRequestValidator}
   */
  @Autowired
  protected OpenIdProviderDao openIdProviderDao;

  /**
   * simply verifies that both methods
   * {@link OpenIdProviderRequestValidator#validateCreate(ScimOpenIdProvider, ValidationContext, Context)} and
   * {@link OpenIdProviderRequestValidator#validateUpdate(Supplier, ScimOpenIdProvider, ValidationContext, Context)}
   * will call the {@link OpenIdProviderRequestValidator#validateFields(ScimOpenIdProvider, ValidationContext)}
   * method
   */
  @Test
  public void testOpenIdProviderRequestValidatorTest()
  {
    final String name = "keycloak";
    final String discoveryUrl = "https://localhost:8080/auth/realms/master/.well-known/openid-configuration";
    final Set<String> resourceEndpoints = Set.of("http://localhost:8080/scim/v2/Users",
                                                 "http://localhost:8080/scim/v2/Groups");
    final KeyPair keyPair = TestUtils.generateKey(new KeyGenerationParameters(new SecureRandom(), 512));
    final String b64PublicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

    ScimOpenIdProvider scimOpenIdProvider = ScimOpenIdProvider.builder()
                                                              .name(name)
                                                              .discoveryEndpoint(discoveryUrl)
                                                              .resourceEndpointsSet(resourceEndpoints)
                                                              .signatureVerificationKey(b64PublicKey)
                                                              .build();

    ValidationContext validationContext = new ValidationContext(Mockito.mock(ResourceType.class));
    OpenIdProviderRequestValidator validator = Mockito.spy(new OpenIdProviderRequestValidator(openIdProviderDao));
    validator.validateCreate(scimOpenIdProvider, validationContext, null);
    Mockito.verify(validator).validateFields(Mockito.eq(scimOpenIdProvider), Mockito.eq(validationContext));

    Mockito.clearInvocations(validator);

    validator.validateUpdate(() -> null, scimOpenIdProvider, validationContext, null);
    Mockito.verify(validator).validateFields(Mockito.eq(scimOpenIdProvider), Mockito.eq(validationContext));
  }
}
