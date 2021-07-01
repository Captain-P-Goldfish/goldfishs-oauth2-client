package de.captaingoldfish.restclient.application.crypto;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;

import de.captaingoldfish.restclient.application.setup.FileReferences;
import de.captaingoldfish.restclient.commons.keyhelper.KeyStoreSupporter;
import de.captaingoldfish.restclient.database.entities.Keystore;
import de.captaingoldfish.restclient.database.repositories.KeystoreDao;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


/**
 * @author Pascal Knueppel
 * @since 25.06.2021
 */
@Slf4j
public class JwtHandlerTest implements FileReferences
{

  private Keystore applicationKeystore;

  private KeystoreDao keystoreDao;

  private static Stream<Arguments> signatureAlgorithmParameters()
  {
    List<Arguments> arguments = JWSAlgorithm.Family.RSA.stream()
                                                       .map(algorithm -> Arguments.arguments("goldfish-rsa", algorithm))
                                                       .collect(Collectors.toList());
    arguments.addAll(JWSAlgorithm.Family.RSA.stream()
                                            .map(algorithm -> Arguments.arguments("localhost-rsa", algorithm))
                                            .collect(Collectors.toList()));
    arguments.addAll(JWSAlgorithm.Family.RSA.stream()
                                            .map(algorithm -> Arguments.arguments("unit-test-rsa", algorithm))
                                            .collect(Collectors.toList()));

    arguments.add(Arguments.arguments("goldfish-ec", JWSAlgorithm.ES256));
    arguments.add(Arguments.arguments("localhost-ec", JWSAlgorithm.ES384));
    arguments.add(Arguments.arguments("unit-test-ec", JWSAlgorithm.ES512));
    return arguments.stream();
  }

  private static Stream<Arguments> encryptionAlgorithmParameters()
  {
    List<Arguments> arguments = JWEAlgorithm.Family.RSA.stream()
                                                       .map(algorithm -> Arguments.arguments("goldfish-rsa",
                                                                                             algorithm,
                                                                                             EncryptionMethod.A192GCM))
                                                       .collect(Collectors.toList());
    arguments.addAll(JWEAlgorithm.Family.RSA.stream()
                                            .map(algorithm -> Arguments.arguments("localhost-rsa",
                                                                                  algorithm,
                                                                                  EncryptionMethod.A192GCM))
                                            .collect(Collectors.toList()));
    arguments.addAll(JWEAlgorithm.Family.RSA.stream()
                                            .map(algorithm -> Arguments.arguments("unit-test-rsa",
                                                                                  algorithm,
                                                                                  EncryptionMethod.A192GCM))
                                            .collect(Collectors.toList()));

    arguments.add(Arguments.arguments("goldfish-ec", JWEAlgorithm.ECDH_ES_A256KW, EncryptionMethod.A192GCM));
    arguments.add(Arguments.arguments("localhost-ec", JWEAlgorithm.ECDH_ES_A192KW, EncryptionMethod.A192GCM));
    arguments.add(Arguments.arguments("unit-test-ec", JWEAlgorithm.ECDH_ES_A128KW, EncryptionMethod.A192GCM));
    return arguments.stream();
  }


  @SneakyThrows
  @BeforeEach
  public void initialize()
  {
    try (InputStream inputStream = readAsInputStream(UNIT_TEST_KEYSTORE_JKS_EXTENDED))
    {
      applicationKeystore = new Keystore(inputStream, KeyStoreSupporter.KeyStoreType.JKS, UNIT_TEST_KEYSTORE_PASSWORD);
      applicationKeystore.setKeystoreType(KeyStoreSupporter.KeyStoreType.JKS);
      applicationKeystore.setKeystorePassword(UNIT_TEST_KEYSTORE_PASSWORD);
      getExtendedUnitTestKeystoreEntryAccess().forEach(applicationKeystore::addKeyEntry);
    }
    keystoreDao = Mockito.mock(KeystoreDao.class);
    Mockito.doReturn(applicationKeystore).when(keystoreDao).getKeystore();
  }

  @SneakyThrows
  @ParameterizedTest
  @MethodSource("signatureAlgorithmParameters")
  public void testJwtSignerTest(String keyId, JWSAlgorithm algorithm)
  {
    JwtHandler jwtHandler = new JwtHandler(keystoreDao);

    String header = String.format("{\"kid\": \"%s\", \"alg\": \"%s\"}", keyId, algorithm);
    String body = "{\"iss\": \"goldfish-ec\"}";
    String jws = jwtHandler.createJwt(keyId, header, body);
    String verifiedBody = jwtHandler.handleJwt(null, jws);
    Assertions.assertEquals(body, verifiedBody);
  }

  @SneakyThrows
  @ParameterizedTest
  @MethodSource("signatureAlgorithmParameters")
  public void testJwtSignerTestWithDirectKeyId(String keyId, JWSAlgorithm algorithm)
  {
    JwtHandler jwtHandler = new JwtHandler(keystoreDao);

    String header = String.format("{ \"alg\": \"%s\"}", algorithm);
    String body = "{\"iss\": \"goldfish-ec\"}";
    String jws = jwtHandler.createJwt(keyId, header, body);
    String verifiedBody = jwtHandler.handleJwt(keyId, jws);
    Assertions.assertEquals(body, verifiedBody);
  }

  @SneakyThrows
  @ParameterizedTest
  @MethodSource("encryptionAlgorithmParameters")
  public void testJwtEncryptionTest(String keyId, JWEAlgorithm algorithm, EncryptionMethod contentAlgorithm)
  {
    JwtHandler jwtHandler = new JwtHandler(keystoreDao);

    String header = String.format("{\"kid\": \"%s\", \"alg\": \"%s\", \"enc\": \"%s\"}",
                                  keyId,
                                  algorithm,
                                  contentAlgorithm);
    String body = "{\"iss\": \"goldfish-ec\"}";
    String jwe = jwtHandler.createJwt(keyId, header, body);
    String decrypted = jwtHandler.handleJwt(null, jwe);
    Assertions.assertEquals(decrypted, body);
  }

  @SneakyThrows
  @ParameterizedTest
  @MethodSource("encryptionAlgorithmParameters")
  public void testJwtEncryptionTestWithDirectKeyId(String keyId,
                                                   JWEAlgorithm algorithm,
                                                   EncryptionMethod contentAlgorithm)
  {
    JwtHandler jwtHandler = new JwtHandler(keystoreDao);

    String header = String.format("{ \"alg\": \"%s\", \"enc\": \"%s\"}", algorithm, contentAlgorithm);
    String body = "{\"iss\": \"goldfish-ec\"}";
    String jwe = jwtHandler.createJwt(keyId, header, body);
    String decrypted = jwtHandler.handleJwt(keyId, jwe);
    Assertions.assertEquals(decrypted, body);
  }
}
