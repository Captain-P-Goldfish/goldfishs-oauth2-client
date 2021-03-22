package de.captaingoldfish.oauthrestclient.application.projectconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * @author Pascal Knueppel
 * @since 03.02.2021
 */
@ComponentScan("de.captaingoldfish.oauthrestclient")
@Configuration
public class WebAppConfig implements WebMvcConfigurer
{

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
}
