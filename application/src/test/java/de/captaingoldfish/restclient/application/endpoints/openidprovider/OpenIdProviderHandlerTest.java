package de.captaingoldfish.restclient.application.endpoints.openidprovider;

import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bouncycastle.crypto.KeyGenerationParameters;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;

import de.captaingoldfish.restclient.application.setup.AbstractScimClientConfig;
import de.captaingoldfish.restclient.application.setup.OAuthRestClientTest;
import de.captaingoldfish.restclient.application.utils.TestUtils;
import de.captaingoldfish.restclient.application.utils.Utils;
import de.captaingoldfish.restclient.commons.keyhelper.KeyStoreSupporter;
import de.captaingoldfish.restclient.database.entities.OpenIdProvider;
import de.captaingoldfish.restclient.scim.resources.ScimOpenIdProvider;
import de.captaingoldfish.scim.sdk.client.response.ServerResponse;
import de.captaingoldfish.scim.sdk.common.constants.HttpStatus;
import de.captaingoldfish.scim.sdk.common.response.ErrorResponse;
import de.captaingoldfish.scim.sdk.common.response.ListResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


/**
 * @author Pascal Knueppel
 * @since 21.05.2021
 */
@Slf4j
@DirtiesContext
@OAuthRestClientTest
public class OpenIdProviderHandlerTest extends AbstractScimClientConfig
{

  /**
   * the scim endpoint for the application keystore
   */
  private static final String OPENID_PROVIDER_ENDPOINT = "/OpenIdProvider";

  /**
   * checks that an OpenID Provider can be created with valid values
   */
  @Test
  public void testCreateOpenIdProviderWithDiscoverUrlAndPublicKey()
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
    Assertions.assertEquals(0, openIdProviderDao.count());
    ServerResponse<ScimOpenIdProvider> response = scimRequestBuilder.create(ScimOpenIdProvider.class,
                                                                            OPENID_PROVIDER_ENDPOINT)
                                                                    .setResource(scimOpenIdProvider)
                                                                    .sendRequest();
    Assertions.assertEquals(HttpStatus.CREATED, response.getHttpStatus());
    ScimOpenIdProvider createdProvider = response.getResource();
    long id = Utils.parseId(createdProvider.getId().get());
    Assertions.assertEquals(1, openIdProviderDao.count());
    OpenIdProvider openIdProvider = openIdProviderDao.findById(id).orElseThrow();

    Assertions.assertEquals(name, openIdProvider.getName());
    Assertions.assertEquals(name, createdProvider.getName());
    Assertions.assertEquals(discoveryUrl, openIdProvider.getDiscoveryEndpoint());
    Assertions.assertEquals(discoveryUrl, createdProvider.getDiscoveryEndpoint().get());
    MatcherAssert.assertThat(openIdProvider.getResourceEndpoints(),
                             Matchers.containsInAnyOrder(resourceEndpoints.stream()
                                                                          .map(Matchers::equalTo)
                                                                          .collect(Collectors.toList())));
    MatcherAssert.assertThat(createdProvider.getResourceEndpoints(),
                             Matchers.containsInAnyOrder(resourceEndpoints.stream()
                                                                          .map(Matchers::equalTo)
                                                                          .collect(Collectors.toList())));
    Assertions.assertEquals(b64PublicKey,
                            Base64.getEncoder().encodeToString(openIdProvider.getSignatureVerificationKey()));
    Assertions.assertEquals(b64PublicKey, createdProvider.getSignatureVerificationKey().get());

    Assertions.assertNull(openIdProvider.getAuthorizationEndpoint());
    Assertions.assertTrue(createdProvider.getAuthorizationEndpoint().isEmpty());
    Assertions.assertNull(openIdProvider.getTokenEndpoint());
    Assertions.assertTrue(createdProvider.getTokenEndpoint().isEmpty());
    Assertions.assertEquals(1, openIdProviderDao.count());
  }

  /**
   * checks that an OpenID Provider can be created with valid values
   */
  @SneakyThrows
  @Test
  public void testCreateOpenIdProviderWithDiscoverUrlAndCertificate()
  {
    final String name = "keycloak";
    final String discoveryUrl = "https://localhost:8080/auth/realms/master/.well-known/openid-configuration";
    final Set<String> resourceEndpoints = Set.of("http://localhost:8080/scim/v2/Users",
                                                 "http://localhost:8080/scim/v2/Groups");
    X509Certificate certificate;
    try (InputStream inputStream = readAsInputStream(UNIT_TEST_KEYSTORE_JKS))
    {
      KeyStore keyStore = KeyStoreSupporter.readKeyStore(inputStream,
                                                         KeyStoreSupporter.KeyStoreType.JKS,
                                                         UNIT_TEST_KEYSTORE_PASSWORD);
      certificate = (X509Certificate)keyStore.getCertificate("goldfish");
    }
    final String b64PublicKey = Base64.getEncoder().encodeToString(certificate.getEncoded());

    ScimOpenIdProvider scimOpenIdProvider = ScimOpenIdProvider.builder()
                                                              .name(name)
                                                              .discoveryEndpoint(discoveryUrl)
                                                              .resourceEndpointsSet(resourceEndpoints)
                                                              .signatureVerificationKey(b64PublicKey)
                                                              .build();
    Assertions.assertEquals(0, openIdProviderDao.count());
    ServerResponse<ScimOpenIdProvider> response = scimRequestBuilder.create(ScimOpenIdProvider.class,
                                                                            OPENID_PROVIDER_ENDPOINT)
                                                                    .setResource(scimOpenIdProvider)
                                                                    .sendRequest();
    Assertions.assertEquals(HttpStatus.CREATED, response.getHttpStatus());
    ScimOpenIdProvider createdProvider = response.getResource();
    long id = Utils.parseId(createdProvider.getId().get());
    Assertions.assertEquals(1, openIdProviderDao.count());
    OpenIdProvider openIdProvider = openIdProviderDao.findById(id).orElseThrow();

    Assertions.assertEquals(name, openIdProvider.getName());
    Assertions.assertEquals(name, createdProvider.getName());
    Assertions.assertEquals(discoveryUrl, openIdProvider.getDiscoveryEndpoint());
    Assertions.assertEquals(discoveryUrl, createdProvider.getDiscoveryEndpoint().get());
    MatcherAssert.assertThat(openIdProvider.getResourceEndpoints(),
                             Matchers.containsInAnyOrder(resourceEndpoints.stream()
                                                                          .map(Matchers::equalTo)
                                                                          .collect(Collectors.toList())));
    MatcherAssert.assertThat(createdProvider.getResourceEndpoints(),
                             Matchers.containsInAnyOrder(resourceEndpoints.stream()
                                                                          .map(Matchers::equalTo)
                                                                          .collect(Collectors.toList())));
    Assertions.assertArrayEquals(certificate.getPublicKey().getEncoded(), openIdProvider.getSignatureVerificationKey());
    Assertions.assertArrayEquals(certificate.getPublicKey().getEncoded(),
                                 Base64.getDecoder().decode(createdProvider.getSignatureVerificationKey().get()));

    Assertions.assertNull(openIdProvider.getAuthorizationEndpoint());
    Assertions.assertTrue(createdProvider.getAuthorizationEndpoint().isEmpty());
    Assertions.assertNull(openIdProvider.getTokenEndpoint());
    Assertions.assertTrue(createdProvider.getTokenEndpoint().isEmpty());
    Assertions.assertEquals(1, openIdProviderDao.count());
  }

  /**
   * checks that an OpenID Provider can be created with valid values
   */
  @Test
  public void testCreateOpenIdProviderWithoutADiscoveryUrl()
  {
    final String name = "keycloak";
    final String authorizationEndpoint = "https://localhost:8080/auth/realms/master/openid/authorization";
    final String tokenEndpoint = "https://localhost:8080/auth/realms/master/openid/token";
    final Set<String> resourceEndpoints = Set.of("http://localhost:8080/scim/v2/Users",
                                                 "http://localhost:8080/scim/v2/Groups");
    final KeyPair keyPair = TestUtils.generateKey(new KeyGenerationParameters(new SecureRandom(), 512));
    final String b64PublicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

    ScimOpenIdProvider scimOpenIdProvider = ScimOpenIdProvider.builder()
                                                              .name(name)
                                                              .authorizationEndpoint(authorizationEndpoint)
                                                              .tokenEndpoint(tokenEndpoint)
                                                              .resourceEndpointsSet(resourceEndpoints)
                                                              .signatureVerificationKey(b64PublicKey)
                                                              .build();
    Assertions.assertEquals(0, openIdProviderDao.count());
    ServerResponse<ScimOpenIdProvider> response = scimRequestBuilder.create(ScimOpenIdProvider.class,
                                                                            OPENID_PROVIDER_ENDPOINT)
                                                                    .setResource(scimOpenIdProvider)
                                                                    .sendRequest();
    Assertions.assertEquals(HttpStatus.CREATED, response.getHttpStatus());
    ScimOpenIdProvider createdProvider = response.getResource();
    long id = Utils.parseId(createdProvider.getId().get());
    Assertions.assertEquals(1, openIdProviderDao.count());
    OpenIdProvider openIdProvider = openIdProviderDao.findById(id).orElseThrow();

    Assertions.assertEquals(name, openIdProvider.getName());
    Assertions.assertEquals(name, createdProvider.getName());
    Assertions.assertEquals(authorizationEndpoint, openIdProvider.getAuthorizationEndpoint());
    Assertions.assertEquals(authorizationEndpoint, createdProvider.getAuthorizationEndpoint().get());
    Assertions.assertEquals(tokenEndpoint, openIdProvider.getTokenEndpoint());
    Assertions.assertEquals(tokenEndpoint, createdProvider.getTokenEndpoint().get());
    MatcherAssert.assertThat(openIdProvider.getResourceEndpoints(),
                             Matchers.containsInAnyOrder(resourceEndpoints.stream()
                                                                          .map(Matchers::equalTo)
                                                                          .collect(Collectors.toList())));
    MatcherAssert.assertThat(createdProvider.getResourceEndpoints(),
                             Matchers.containsInAnyOrder(resourceEndpoints.stream()
                                                                          .map(Matchers::equalTo)
                                                                          .collect(Collectors.toList())));
    Assertions.assertEquals(b64PublicKey,
                            Base64.getEncoder().encodeToString(openIdProvider.getSignatureVerificationKey()));
    Assertions.assertEquals(b64PublicKey, createdProvider.getSignatureVerificationKey().get());

    Assertions.assertNull(openIdProvider.getDiscoveryEndpoint());
    Assertions.assertTrue(createdProvider.getDiscoveryEndpoint().isEmpty());
    Assertions.assertEquals(1, openIdProviderDao.count());
  }

  /**
   * checks that an OpenID Provider can be created with valid values
   */
  @Test
  public void testCreateOpenIdProviderNoPublicKey()
  {
    final String name = "keycloak";
    final String authorizationEndpoint = "https://localhost:8080/auth/realms/master/openid/authorization";
    final String tokenEndpoint = "https://localhost:8080/auth/realms/master/openid/token";
    final Set<String> resourceEndpoints = Set.of("http://localhost:8080/scim/v2/Users",
                                                 "http://localhost:8080/scim/v2/Groups");

    ScimOpenIdProvider scimOpenIdProvider = ScimOpenIdProvider.builder()
                                                              .name(name)
                                                              .authorizationEndpoint(authorizationEndpoint)
                                                              .tokenEndpoint(tokenEndpoint)
                                                              .resourceEndpointsSet(resourceEndpoints)
                                                              .build();
    Assertions.assertEquals(0, openIdProviderDao.count());
    ServerResponse<ScimOpenIdProvider> response = scimRequestBuilder.create(ScimOpenIdProvider.class,
                                                                            OPENID_PROVIDER_ENDPOINT)
                                                                    .setResource(scimOpenIdProvider)
                                                                    .sendRequest();
    Assertions.assertEquals(HttpStatus.CREATED, response.getHttpStatus());
    ScimOpenIdProvider createdProvider = response.getResource();
    long id = Utils.parseId(createdProvider.getId().get());
    Assertions.assertEquals(1, openIdProviderDao.count());
    OpenIdProvider openIdProvider = openIdProviderDao.findById(id).orElseThrow();

    Assertions.assertEquals(name, openIdProvider.getName());
    Assertions.assertEquals(name, createdProvider.getName());
    Assertions.assertEquals(authorizationEndpoint, openIdProvider.getAuthorizationEndpoint());
    Assertions.assertEquals(authorizationEndpoint, createdProvider.getAuthorizationEndpoint().get());
    Assertions.assertEquals(tokenEndpoint, openIdProvider.getTokenEndpoint());
    Assertions.assertEquals(tokenEndpoint, createdProvider.getTokenEndpoint().get());
    MatcherAssert.assertThat(openIdProvider.getResourceEndpoints(),
                             Matchers.containsInAnyOrder(resourceEndpoints.stream()
                                                                          .map(Matchers::equalTo)
                                                                          .collect(Collectors.toList())));
    MatcherAssert.assertThat(createdProvider.getResourceEndpoints(),
                             Matchers.containsInAnyOrder(resourceEndpoints.stream()
                                                                          .map(Matchers::equalTo)
                                                                          .collect(Collectors.toList())));
    Assertions.assertNull(openIdProvider.getSignatureVerificationKey());
    Assertions.assertTrue(createdProvider.getSignatureVerificationKey().isEmpty());

    Assertions.assertNull(openIdProvider.getDiscoveryEndpoint());
    Assertions.assertTrue(createdProvider.getDiscoveryEndpoint().isEmpty());
    Assertions.assertEquals(1, openIdProviderDao.count());
  }

  @Test
  public void testCreateOpenIdProviderPublicKeyNotBase64Encoded()
  {
    final String name = "keycloak";
    final String authorizationEndpoint = "https://localhost:8080/auth/realms/master/openid/authorization";
    final String tokenEndpoint = "https://localhost:8080/auth/realms/master/openid/token";
    final Set<String> resourceEndpoints = Set.of("http://localhost:8080/scim/v2/Users",
                                                 "http://localhost:8080/scim/v2/Groups");

    ScimOpenIdProvider scimOpenIdProvider = ScimOpenIdProvider.builder()
                                                              .name(name)
                                                              .authorizationEndpoint(authorizationEndpoint)
                                                              .tokenEndpoint(tokenEndpoint)
                                                              .resourceEndpointsSet(resourceEndpoints)
                                                              .signatureVerificationKey("%&/")
                                                              .build();
    ServerResponse<ScimOpenIdProvider> response = scimRequestBuilder.create(ScimOpenIdProvider.class,
                                                                            OPENID_PROVIDER_ENDPOINT)
                                                                    .setResource(scimOpenIdProvider)
                                                                    .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();

    List<String> errorMessageList = errorResponse.getErrorMessages();
    Assertions.assertEquals(0, errorMessageList.size());

    Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
    Assertions.assertEquals(1, fieldErrors.size());
    List<String> nameErrors = fieldErrors.get(ScimOpenIdProvider.FieldNames.SIGNATURE_VERIFICATION_KEY);
    Assertions.assertEquals(1, nameErrors.size());
    String errorMessage = "Uploaded key is not Base64 encoded: Illegal base64 character 25";
    MatcherAssert.assertThat(nameErrors, Matchers.containsInAnyOrder(errorMessage));
  }

  @Test
  public void testCreateOpenIdProviderIllegalPublicKey()
  {
    final String name = "keycloak";
    final String authorizationEndpoint = "https://localhost:8080/auth/realms/master/openid/authorization";
    final String tokenEndpoint = "https://localhost:8080/auth/realms/master/openid/token";
    final Set<String> resourceEndpoints = Set.of("http://localhost:8080/scim/v2/Users",
                                                 "http://localhost:8080/scim/v2/Groups");

    ScimOpenIdProvider scimOpenIdProvider = ScimOpenIdProvider.builder()
                                                              .name(name)
                                                              .authorizationEndpoint(authorizationEndpoint)
                                                              .tokenEndpoint(tokenEndpoint)
                                                              .resourceEndpointsSet(resourceEndpoints)
                                                              .signatureVerificationKey("JSYv")
                                                              .build();
    ServerResponse<ScimOpenIdProvider> response = scimRequestBuilder.create(ScimOpenIdProvider.class,
                                                                            OPENID_PROVIDER_ENDPOINT)
                                                                    .setResource(scimOpenIdProvider)
                                                                    .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();

    List<String> errorMessageList = errorResponse.getErrorMessages();
    Assertions.assertEquals(0, errorMessageList.size());

    Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
    Assertions.assertEquals(1, fieldErrors.size());
    List<String> nameErrors = fieldErrors.get(ScimOpenIdProvider.FieldNames.SIGNATURE_VERIFICATION_KEY);
    Assertions.assertEquals(1, nameErrors.size());
    String errorMessage = "Uploaded key is neither a public RSA key nor a X509 certificate";
    MatcherAssert.assertThat(nameErrors, Matchers.containsInAnyOrder(errorMessage));
  }

  @Test
  public void testCreateOpenIdProviderNoEndpointUrls()
  {
    final String name = "keycloak";
    final Set<String> resourceEndpoints = Set.of("http://localhost:8080/scim/v2/Users",
                                                 "http://localhost:8080/scim/v2/Groups");
    final KeyPair keyPair = TestUtils.generateKey(new KeyGenerationParameters(new SecureRandom(), 512));
    final String b64PublicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

    ScimOpenIdProvider scimOpenIdProvider = ScimOpenIdProvider.builder()
                                                              .name(name)
                                                              .resourceEndpointsSet(resourceEndpoints)
                                                              .signatureVerificationKey(b64PublicKey)
                                                              .build();
    ServerResponse<ScimOpenIdProvider> response = scimRequestBuilder.create(ScimOpenIdProvider.class,
                                                                            OPENID_PROVIDER_ENDPOINT)
                                                                    .setResource(scimOpenIdProvider)
                                                                    .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();

    List<String> errorMessageList = errorResponse.getErrorMessages();
    Assertions.assertEquals(1, errorMessageList.size());
    String errorMessage = String.format("Either the '%s' field or the '%s' and '%s' fields must be present.",
                                        ScimOpenIdProvider.FieldNames.DISCOVERY_ENDPOINT,
                                        ScimOpenIdProvider.FieldNames.AUTHORIZATION_ENDPOINT,
                                        ScimOpenIdProvider.FieldNames.TOKEN_ENDPOINT);
    MatcherAssert.assertThat(errorMessageList, Matchers.containsInAnyOrder(errorMessage));

    Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
    Assertions.assertEquals(0, fieldErrors.size());
  }

  @Test
  public void testCreateOpenIdProviderInvalidDiscoveryUrl()
  {
    final String name = "keycloak";
    final String discoveryUrl = "htt://a:JHH/;HJH:somewhere";
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
    ServerResponse<ScimOpenIdProvider> response = scimRequestBuilder.create(ScimOpenIdProvider.class,
                                                                            OPENID_PROVIDER_ENDPOINT)
                                                                    .setResource(scimOpenIdProvider)
                                                                    .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();

    List<String> errorMessageList = errorResponse.getErrorMessages();
    Assertions.assertEquals(0, errorMessageList.size());

    Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
    Assertions.assertEquals(1, fieldErrors.size());
    List<String> discoveryUrlErrors = fieldErrors.get(ScimOpenIdProvider.FieldNames.DISCOVERY_ENDPOINT);
    String errorMessage = String.format("Attribute 'urn:ietf:params:scim:schemas:captaingoldfish:2.0:OpenIdProvider:discoveryEndpoint'"
                                        + " is a referenceType and must apply to one of the following types '[URL]' "
                                        + "but value is '%s'",
                                        discoveryUrl);
    MatcherAssert.assertThat(discoveryUrlErrors, Matchers.containsInAnyOrder(errorMessage));
  }

  @Test
  public void testCreateOpenIdProviderWithInvalidAuthAndTokenUrl()
  {
    final String name = "keycloak";
    final String authorizationUrl = "hello";
    final String tokenUrl = "world";
    final Set<String> resourceEndpoints = Set.of("http://localhost:8080/scim/v2/Users",
                                                 "http://localhost:8080/scim/v2/Groups");
    final KeyPair keyPair = TestUtils.generateKey(new KeyGenerationParameters(new SecureRandom(), 512));
    final String b64PublicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

    ScimOpenIdProvider scimOpenIdProvider = ScimOpenIdProvider.builder()
                                                              .name(name)
                                                              .authorizationEndpoint(authorizationUrl)
                                                              .tokenEndpoint(tokenUrl)
                                                              .resourceEndpointsSet(resourceEndpoints)
                                                              .signatureVerificationKey(b64PublicKey)
                                                              .build();
    ServerResponse<ScimOpenIdProvider> response = scimRequestBuilder.create(ScimOpenIdProvider.class,
                                                                            OPENID_PROVIDER_ENDPOINT)
                                                                    .setResource(scimOpenIdProvider)
                                                                    .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();

    List<String> errorMessageList = errorResponse.getErrorMessages();
    Assertions.assertEquals(0, errorMessageList.size());

    Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
    Assertions.assertEquals(2, fieldErrors.size());
    List<String> authUrlErrors = fieldErrors.get(ScimOpenIdProvider.FieldNames.AUTHORIZATION_ENDPOINT);
    String errorMessage1 = String.format("Attribute 'urn:ietf:params:scim:schemas:captaingoldfish:2.0:OpenIdProvider:authorizationEndpoint'"
                                         + " is a referenceType and must apply to one of the following types '[URL]' "
                                         + "but value is '%s'",
                                         authorizationUrl);
    MatcherAssert.assertThat(authUrlErrors, Matchers.containsInAnyOrder(errorMessage1));

    List<String> tokenUrlErrors = fieldErrors.get(ScimOpenIdProvider.FieldNames.TOKEN_ENDPOINT);
    String errorMessage2 = String.format("Attribute 'urn:ietf:params:scim:schemas:captaingoldfish:2.0:OpenIdProvider:tokenEndpoint'"
                                         + " is a referenceType and must apply to one of the following types '[URL]' "
                                         + "but value is '%s'",
                                         tokenUrl);
    MatcherAssert.assertThat(tokenUrlErrors, Matchers.containsInAnyOrder(errorMessage2));
  }

  @Test
  public void testCreateOpenIdProviderDuplicateName()
  {
    final String name = "keycloak";
    openIdProviderDao.save(OpenIdProvider.builder().name(name).build());

    final Set<String> resourceEndpoints = Set.of("http://localhost:8080/scim/v2/Users",
                                                 "http://localhost:8080/scim/v2/Groups");
    final KeyPair keyPair = TestUtils.generateKey(new KeyGenerationParameters(new SecureRandom(), 512));
    final String b64PublicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

    ScimOpenIdProvider scimOpenIdProvider = ScimOpenIdProvider.builder()
                                                              .name(name)
                                                              .resourceEndpointsSet(resourceEndpoints)
                                                              .signatureVerificationKey(b64PublicKey)
                                                              .build();
    ServerResponse<ScimOpenIdProvider> response = scimRequestBuilder.create(ScimOpenIdProvider.class,
                                                                            OPENID_PROVIDER_ENDPOINT)
                                                                    .setResource(scimOpenIdProvider)
                                                                    .sendRequest();
    Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    ErrorResponse errorResponse = response.getErrorResponse();

    List<String> errorMessageList = errorResponse.getErrorMessages();
    Assertions.assertEquals(0, errorMessageList.size());

    Map<String, List<String>> fieldErrors = errorResponse.getFieldErrors();
    Assertions.assertEquals(1, fieldErrors.size());
    List<String> nameErrors = fieldErrors.get(ScimOpenIdProvider.FieldNames.NAME);
    Assertions.assertEquals(1, nameErrors.size());
    String errorMessage = String.format("A provider with name '%s' does already exist", name);
    MatcherAssert.assertThat(nameErrors, Matchers.containsInAnyOrder(errorMessage));
  }

  @Test
  public void testGetOpenIdProvider()
  {
    OpenIdProvider openIdProvider = createOpenIdProvider();

    ServerResponse<ScimOpenIdProvider> response = scimRequestBuilder.get(ScimOpenIdProvider.class,
                                                                         OPENID_PROVIDER_ENDPOINT,
                                                                         String.valueOf(openIdProvider.getId()))
                                                                    .sendRequest();
    Assertions.assertEquals(HttpStatus.OK, response.getHttpStatus());
    ScimOpenIdProvider scimOpenIdProvider = response.getResource();
    Assertions.assertEquals(openIdProvider.getName(), scimOpenIdProvider.getName());
    Assertions.assertEquals(openIdProvider.getDiscoveryEndpoint(),
                            scimOpenIdProvider.getDiscoveryEndpoint().orElse(null));
    MatcherAssert.assertThat(openIdProvider.getResourceEndpoints(),
                             Matchers.containsInAnyOrder(scimOpenIdProvider.getResourceEndpoints()
                                                                           .stream()
                                                                           .map(Matchers::equalTo)
                                                                           .collect(Collectors.toList())));
    Assertions.assertArrayEquals(openIdProvider.getSignatureVerificationKey(),
                                 Base64.getDecoder().decode(scimOpenIdProvider.getSignatureVerificationKey().get()));
    Assertions.assertTrue(scimOpenIdProvider.getAuthorizationEndpoint().isEmpty());
    Assertions.assertTrue(scimOpenIdProvider.getTokenEndpoint().isEmpty());
  }

  @Test
  public void testGetNotExistingOpenIdProvider()
  {
    ServerResponse<ScimOpenIdProvider> response = scimRequestBuilder.get(ScimOpenIdProvider.class,
                                                                         OPENID_PROVIDER_ENDPOINT,
                                                                         "1")
                                                                    .sendRequest();
    Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getHttpStatus());
  }

  @Test
  public void testListOpenIdProviders()
  {
    createOpenIdProvider();
    createOpenIdProvider("key");
    createOpenIdProvider("cloak");

    ServerResponse<ListResponse<ScimOpenIdProvider>> response = scimRequestBuilder.list(ScimOpenIdProvider.class,
                                                                                        OPENID_PROVIDER_ENDPOINT)
                                                                                  .get()
                                                                                  .sendRequest();
    Assertions.assertEquals(HttpStatus.OK, response.getHttpStatus());
    ListResponse<ScimOpenIdProvider> listResponse = response.getResource();
    Assertions.assertEquals(3, listResponse.getTotalResults());
    Assertions.assertEquals(3, listResponse.getListedResources().size());
  }

  @Test
  public void testDeleteOpenIdProvider()
  {
    OpenIdProvider openIdProvider = createOpenIdProvider();

    Assertions.assertEquals(1, openIdProviderDao.count());
    ServerResponse<ScimOpenIdProvider> response = scimRequestBuilder.delete(ScimOpenIdProvider.class,
                                                                            OPENID_PROVIDER_ENDPOINT,
                                                                            String.valueOf(openIdProvider.getId()))
                                                                    .sendRequest();
    Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getHttpStatus());
    Assertions.assertEquals(0, openIdProviderDao.count());
  }

  @Test
  public void testDeleteNotExistingOpenIdProvider()
  {
    createOpenIdProvider();

    Assertions.assertEquals(1, openIdProviderDao.count());
    ServerResponse<ScimOpenIdProvider> response = scimRequestBuilder.delete(ScimOpenIdProvider.class,
                                                                            OPENID_PROVIDER_ENDPOINT,
                                                                            "9999999999999")
                                                                    .sendRequest();
    Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getHttpStatus());
    Assertions.assertEquals(1, openIdProviderDao.count());
  }

  @Test
  public void testUpdateOpenIdProvider()
  {
    OpenIdProvider openIdProvider = createOpenIdProvider();

    final String name = "hello-world";
    final String authorizationUrl = "https://hello-world/auth/realms/master/authorization";
    final String tokenUrl = "https://hello-world/auth/realms/master/.well-known/token";
    final Set<String> resourceEndpoints = Set.of("http://hello-world/scim/v2/Users",
                                                 "http://hello-world/scim/v2/Groups");
    final KeyPair keyPair = TestUtils.generateKey(new KeyGenerationParameters(new SecureRandom(), 512));
    String b64Key = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

    ScimOpenIdProvider scimOpenIdProvider = ScimOpenIdProvider.builder()
                                                              .name(name)
                                                              .authorizationEndpoint(authorizationUrl)
                                                              .tokenEndpoint(tokenUrl)
                                                              .resourceEndpointsSet(resourceEndpoints)
                                                              .signatureVerificationKey(b64Key)
                                                              .build();

    Assertions.assertEquals(1, openIdProviderDao.count());
    ServerResponse<ScimOpenIdProvider> response = scimRequestBuilder.update(ScimOpenIdProvider.class,
                                                                            OPENID_PROVIDER_ENDPOINT,
                                                                            String.valueOf(openIdProvider.getId()))
                                                                    .setResource(scimOpenIdProvider)
                                                                    .sendRequest();
    Assertions.assertEquals(HttpStatus.OK, response.getHttpStatus());
    ScimOpenIdProvider updatedProvider = response.getResource();
    long id = Utils.parseId(updatedProvider.getId().get());
    Assertions.assertEquals(1, openIdProviderDao.count());
    OpenIdProvider updatedOpenIdProvider = openIdProviderDao.findById(id).orElseThrow();

    Assertions.assertEquals(name, updatedOpenIdProvider.getName());
    Assertions.assertEquals(name, updatedProvider.getName());
    Assertions.assertEquals(authorizationUrl, updatedOpenIdProvider.getAuthorizationEndpoint());
    Assertions.assertEquals(authorizationUrl, updatedProvider.getAuthorizationEndpoint().get());
    Assertions.assertEquals(tokenUrl, updatedOpenIdProvider.getTokenEndpoint());
    Assertions.assertEquals(tokenUrl, updatedProvider.getTokenEndpoint().get());
    MatcherAssert.assertThat(updatedOpenIdProvider.getResourceEndpoints(),
                             Matchers.containsInAnyOrder(resourceEndpoints.stream()
                                                                          .map(Matchers::equalTo)
                                                                          .collect(Collectors.toList())));
    MatcherAssert.assertThat(updatedProvider.getResourceEndpoints(),
                             Matchers.containsInAnyOrder(resourceEndpoints.stream()
                                                                          .map(Matchers::equalTo)
                                                                          .collect(Collectors.toList())));
    Assertions.assertEquals(b64Key,
                            Base64.getEncoder().encodeToString(updatedOpenIdProvider.getSignatureVerificationKey()));
    Assertions.assertEquals(b64Key, updatedProvider.getSignatureVerificationKey().get());

    Assertions.assertNull(updatedOpenIdProvider.getDiscoveryEndpoint());
    Assertions.assertTrue(updatedProvider.getDiscoveryEndpoint().isEmpty());
    Assertions.assertEquals(1, openIdProviderDao.count());
  }

  @Test
  public void testUpdateNotExistingOpenIdProvider()
  {
    OpenIdProvider openIdProvider = createOpenIdProvider();

    final String name = "hello-world";
    final String authorizationUrl = "https://hello-world/auth/realms/master/authorization";
    final String tokenUrl = "https://hello-world/auth/realms/master/.well-known/token";
    final Set<String> resourceEndpoints = Set.of("http://hello-world/scim/v2/Users",
                                                 "http://hello-world/scim/v2/Groups");
    final KeyPair keyPair = TestUtils.generateKey(new KeyGenerationParameters(new SecureRandom(), 512));
    String b64Key = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

    ScimOpenIdProvider scimOpenIdProvider = ScimOpenIdProvider.builder()
                                                              .name(name)
                                                              .authorizationEndpoint(authorizationUrl)
                                                              .tokenEndpoint(tokenUrl)
                                                              .resourceEndpointsSet(resourceEndpoints)
                                                              .signatureVerificationKey(b64Key)
                                                              .build();

    Assertions.assertEquals(1, openIdProviderDao.count());
    ServerResponse<ScimOpenIdProvider> response = scimRequestBuilder.update(ScimOpenIdProvider.class,
                                                                            OPENID_PROVIDER_ENDPOINT,
                                                                            "99999999999999999")
                                                                    .setResource(scimOpenIdProvider)
                                                                    .sendRequest();
    Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getHttpStatus());
    Assertions.assertEquals(1, openIdProviderDao.count());
    Assertions.assertEquals(openIdProvider, openIdProviderDao.findById(openIdProvider.getId()).orElseThrow());
  }

  private OpenIdProvider createOpenIdProvider()
  {
    final String name = "keycloak";
    return createOpenIdProvider(name);
  }

  private OpenIdProvider createOpenIdProvider(String name)
  {
    final String discoveryUrl = "https://localhost:8080/auth/realms/master/.well-known/openid-configuration";
    final Set<String> resourceEndpoints = Set.of("http://localhost:8080/scim/v2/Users",
                                                 "http://localhost:8080/scim/v2/Groups");
    final KeyPair keyPair = TestUtils.generateKey(new KeyGenerationParameters(new SecureRandom(), 512));

    OpenIdProvider openIdProvider = OpenIdProvider.builder()
                                                  .name(name)
                                                  .discoveryEndpoint(discoveryUrl)
                                                  .resourceEndpoints(resourceEndpoints)
                                                  .signatureVerificationKey(keyPair.getPublic().getEncoded())
                                                  .build();
    return openIdProviderDao.save(openIdProvider);
  }
}
