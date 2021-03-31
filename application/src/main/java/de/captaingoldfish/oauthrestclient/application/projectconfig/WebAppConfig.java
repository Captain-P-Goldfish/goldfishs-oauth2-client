package de.captaingoldfish.oauthrestclient.application.projectconfig;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.Getter;


/**
 * @author Pascal Knueppel
 * @since 03.02.2021
 */
@ComponentScan("de.captaingoldfish.oauthrestclient")
@Configuration
public class WebAppConfig implements WebMvcConfigurer, ApplicationContextAware
{

  /**
   * the spring application context to get access from classes that act outside the spring context
   */
  @Getter
  private static ApplicationContext applicationContext;

  /**
   * this method holds all resource-bundle paths that will be used
   *
   * @return all resource-bundle paths
   */
  public String[] resourceBundles()
  {
    return new String[]{"classpath:/de/captaingoldfish/oauthrestclient/resources/bundles/validation"};
  }

  /**
   * this method will load the resource-bundles for localization
   *
   * @return the message source that contains the messages of the resource-bundles.
   */
  @Bean("messageSource")
  public ReloadableResourceBundleMessageSource messageSource()
  {
    final ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
    messageSource.setBasenames(resourceBundles());
    messageSource.setUseCodeAsDefaultMessage(false);
    messageSource.setDefaultEncoding("UTF-8");
    messageSource.setCacheSeconds(5);
    return messageSource;
  }

  /**
   * creates a new validation-message-resolver. Normally the spring-application looks inside the root-classpath
   * for the file "ValidationMessages.properties", but in our case we want localized message handling why I
   * created the resource-bundle "/resource_bundles/validationMessages". In order for spring to use the custom
   * validationMessages-file that I created we need to create a Validator -bean that uses our
   * {@link #messageSource()} and overrides the spring validation-message-resolver. <br>
   * <br>
   * <b>NOTE:</b><br>
   * since we are giving a reference to our configured {@link #messageSource()} Bean here the
   * validation-message-resolver will have access to ALL messages in the application
   *
   * @return the new spring validation-message-resolver
   */
  @Override
  public Validator getValidator()
  {
    LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
    bean.setValidationMessageSource(messageSource());
    return bean;
  }

  /**
   * this is the multipart resolver which is needed to upload files. It MUST be named "multipartResolver"
   * because it is not found by the framework otherwise
   *
   * @return the multipart resolver
   */
  @Bean(name = "multipartResolver")
  public CommonsMultipartResolver multiPartResolver()
  {
    return new CommonsMultipartResolver();
  }

  /**
   * make the application context available from everywhere within the application
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
  {
    WebAppConfig.applicationContext = applicationContext;
  }
}
