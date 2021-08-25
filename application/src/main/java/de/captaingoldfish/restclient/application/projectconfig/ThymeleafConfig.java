package de.captaingoldfish.restclient.application.projectconfig;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ITemplateResolver;

import lombok.extern.slf4j.Slf4j;


/**
 * project: oauth2-test-client<br>
 * author Pascal Knueppel <br>
 * created at: 06.06.2017 - 13:00 <br>
 * <br>
 */
@Slf4j
@Configuration
public class ThymeleafConfig implements ApplicationContextAware, WebMvcConfigurer
{

  /**
   * spring application context that is needed for thymeleaf-templateresolver
   */
  protected ApplicationContext applicationContext;

  /**
   * reads the thymeleaf prefix property that should be set to 'classpath:/views/'
   */
  @Value("${spring.thymeleaf.prefix:classpath:/de/captaingoldfish/restclient/application/}")
  private String thymeleafPrefix;

  /**
   * reads the thymeleaf resources locations that will be configured for spring
   */
  @Value("${spring.resources.static-locations:classpath:/de/captaingoldfish/restclient/application/}")
  private String[] staticLocations;

  /**
   * reads the thymeleaf property that tells us if caching should be enabled or not
   */
  @Value("${spring.thymeleaf.cache:true}")
  private boolean cachingActivated;

  /**
   * {@inheritDoc}
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext)
  {
    this.applicationContext = applicationContext;
    log.info("set application context: {}", applicationContext);
  }

  /**
   * **************************************************************** <br>
   * RESOURCE FOLDERS CONFIGURATION <br>
   * Dispatcher configuration for serving static resources <br>
   * **************************************************************** <br>
   * {@inheritDoc}
   */
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry)
  {
    String imagesPattern = "/images/**";
    List<String> imagesLocations = new ArrayList<>(Arrays.stream(staticLocations)
                                                         .map(location -> location + "static/media/")
                                                         .collect(Collectors.toList()));
    imagesLocations.add(staticLocations[0]);

    registry.addResourceHandler(imagesPattern).addResourceLocations(imagesLocations.toArray(String[]::new));
    log.info("added resourceHandler (pathPattern: '{}'), (resourceLocation: '{}')",
             imagesPattern,
             Arrays.toString(imagesLocations.toArray()));
    registry.addResourceHandler("/**").addResourceLocations(imagesLocations.toArray(String[]::new));
    log.info("added resourceHandler (pathPattern: '{}'), (resourceLocation: '{}')",
             imagesPattern,
             Arrays.toString(imagesLocations.toArray()));

    String cssPattern = "/static/css/**";
    String[] cssLocations = Arrays.stream(staticLocations)
                                  .map(location -> location + "static/css/")
                                  .toArray(String[]::new);
    registry.addResourceHandler(cssPattern).addResourceLocations(cssLocations);
    log.info("added resourceHandler (pathPattern: '{}'), (resourceLocation: '{}')",
             cssPattern,
             Arrays.toString(cssLocations));

    String jsPattern = "/static/js/**";
    String[] jsLocations = Arrays.stream(staticLocations)
                                 .map(location -> location + "static/js/")
                                 .toArray(String[]::new);
    registry.addResourceHandler(jsPattern).addResourceLocations(jsLocations);
    log.info("added resourceHandler (pathPattern: '{}'), (resourceLocation: '{}')",
             jsPattern,
             Arrays.toString(jsLocations));
  }

  /**
   * **************************************************************** <br>
   * THYMELEAF-SPECIFIC ARTIFACTS <br>
   * TemplateResolver <- TemplateEngine <- ViewResolve <br>
   * **************************************************************** <br>
   *
   * @return templateengine, which configures the spring application
   */
  @Bean
  @Description("Thymeleaf Template Engine")
  public SpringTemplateEngine templateEngine()
  {
    log.info("setting up Thymeleaf template engine.");
    SpringTemplateEngine templateEngine = new SpringTemplateEngine();
    templateEngine.setTemplateResolvers(getThymeleafTemplateResolvers());
    templateEngine.setEnableSpringELCompiler(true);
    return templateEngine;
  }

  /**
   * this method will return all configured template resolvers for thymeleaf that should be used in this
   * application.
   *
   * @return a set of template resolvers
   */
  private Set<ITemplateResolver> getThymeleafTemplateResolvers()
  {
    Set<ITemplateResolver> templateResolvers = new HashSet<>();
    templateResolvers.add(templateResolver());
    return templateResolvers;
  }

  /**
   * Thymeleaf configuration expects view files in "views" with ending ".html"<br>
   * **************************************************************** <br>
   * THYMELEAF-SPECIFIC ARTIFACTS <br>
   * TemplateResolver <- TemplateEngine <- ViewResolve <br>
   * **************************************************************** <br>
   */
  private ITemplateResolver templateResolver()
  {
    log.info("setting up Thymeleaf template resolver");
    SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
    resolver.setApplicationContext(applicationContext);
    resolver.setPrefix(thymeleafPrefix);
    resolver.setSuffix(".html");
    resolver.setTemplateMode(TemplateMode.HTML);
    resolver.setCacheable(cachingActivated);
    resolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
    return resolver;
  }

  /**
   * **************************************************************** <br>
   * THYMELEAF-SPECIFIC ARTIFACTS <br>
   * TemplateResolver <- TemplateEngine <- ViewResolve <br>
   * **************************************************************** <br>
   *
   * @return ViewResolver for thymeleaf
   */
  @Bean
  @Description("Thymeleaf View Resolver")
  public ThymeleafViewResolver viewResolver()
  {
    log.info("setting up Thymeleaf view resolver");
    ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
    viewResolver.setTemplateEngine(templateEngine());
    viewResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
    viewResolver.setCache(cachingActivated);
    return viewResolver;
  }

}
