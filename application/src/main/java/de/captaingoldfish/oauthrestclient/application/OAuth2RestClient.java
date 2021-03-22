package de.captaingoldfish.oauthrestclient.application;

import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;

import lombok.extern.slf4j.Slf4j;


/**
 * author Pascal Knueppel <br>
 * created at: 29.04.2018 - 21:38 <br>
 * <br>
 */
@Slf4j
@SpringBootApplication(exclude = {ThymeleafAutoConfiguration.class, MultipartAutoConfiguration.class})
public class OAuth2RestClient implements CommandLineRunner
{

  /**
   * start the spring-boot-application (tomcat-server)
   */
  public static void main(String[] args)
  {
    SpringApplication.run(OAuth2RestClient.class, args);
  }

  @Override
  public void run(String... args) throws Exception
  {
    log.info("\n{}", IOUtils.toString(getClass().getResourceAsStream("/running-banner.txt"), StandardCharsets.UTF_8));
  }
}
