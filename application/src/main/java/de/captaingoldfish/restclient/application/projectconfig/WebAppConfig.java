package de.captaingoldfish.restclient.application.projectconfig;

import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;

import lombok.Getter;


/**
 * @author Pascal Knueppel
 * @since 03.02.2021
 */
@ComponentScan("de.captaingoldfish.restclient")
@Configuration
public class WebAppConfig implements WebMvcConfigurer, ApplicationContextAware
{

  /**
   * the spring application context to get access from classes that act outside the spring context
   */
  @Getter
  private static ApplicationContext applicationContext;

  /**
   * make the application context available from everywhere within the application
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
  {
    WebAppConfig.applicationContext = applicationContext;
  }

  @Bean
  public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer()
  {
    return builder -> builder.applicationContext(applicationContext)
                             .failOnUnknownProperties(false)
                             .featuresToEnable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                             .serializationInclusion(JsonInclude.Include.NON_NULL);
  }
}
