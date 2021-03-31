package de.captaingoldfish.oauthrestclient.application.setup;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.test.context.SpringBootTest;

import de.captaingoldfish.oauthrestclient.application.projectconfig.WebAppConfig;


/**
 * @author Pascal Knueppel
 * @since 31.03.2021
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {SpringBootInitializer.class,
                                                                                       WebAppConfig.class})
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface OAuthRestClientTest
{

}
