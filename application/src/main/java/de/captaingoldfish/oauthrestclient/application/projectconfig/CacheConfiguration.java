package de.captaingoldfish.oauthrestclient.application.projectconfig;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;


/**
 * @author Pascal Knueppel
 * @since 26.03.2021
 */
@EnableCaching
@Configuration
public class CacheConfiguration
{

  /**
   * the name for the keystore file cache
   */
  public static final String KEYSTORE_CACHE = "keystore-cache";

  /**
   * creates a cache with a short lifetime
   */
  @Bean
  public Caffeine caffeineConfig()
  {
    return Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES);
  }

  /**
   * creates a {@link CacheManager} for this application
   */
  @Bean
  public CacheManager cacheManager(Caffeine caffeine)
  {
    CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
    caffeineCacheManager.setCaffeine(caffeine);
    return caffeineCacheManager;
  }
}
