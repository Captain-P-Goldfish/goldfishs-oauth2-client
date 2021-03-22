package de.captaingoldfish.oauthrestclient.database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.captaingoldfish.oauthrestclient.database.config.DatabaseConfig;
import de.captaingoldfish.oauthrestclient.database.springboot.SpringBootInitializer;



/**
 * @author Pascal Knueppel
 * @since 03.02.2021
 */
@TestPropertySource(properties = "database.url=jdbc:hsqldb:mem:oauth2restclient")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {SpringBootInitializer.class,
                                                                                       DatabaseConfig.class})
@ExtendWith(SpringExtension.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface DatabaseTest
{

}
