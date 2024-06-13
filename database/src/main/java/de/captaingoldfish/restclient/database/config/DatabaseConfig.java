package de.captaingoldfish.restclient.database.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;
import liquibase.integration.spring.SpringLiquibase;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


/**
 * this class will setup the database configuration for hibernate provider with spring in a container managed
 * context.
 *
 * @author Pascal Knüppel
 */
@Order(2)
@Slf4j
@Configuration
@EnableJpaRepositories(basePackages = {DatabaseConfig.REPOSITORY_PACKAGE})
@EnableTransactionManagement
public class DatabaseConfig
{

  protected static final String REPOSITORY_PACKAGE = "de.captaingoldfish.restclient.database.repositories";

  /**
   * this variable declares the package that should be scanned by this bean for repositories and entities
   */
  protected static final String[] PACKAGE_TO_SCAN = new String[]{"de.captaingoldfish.restclient.database.entities",
                                                                 REPOSITORY_PACKAGE};

  /**
   * the database URL
   */
  private final String databaseUrl;

  /**
   * the database user
   */
  private final String databaseUsername;

  /**
   * the database password
   */
  private final String databasePassword;

  public DatabaseConfig(@Value("${database.url:jdbc:hsqldb:file:./hsql-db/application-db}") String databaseUrl,
                        @Value("${database.user:sa}") String databaseUsername,
                        @Value("${database.password:123456}") String databasePassword)
  {
    this.databaseUrl = databaseUrl;
    this.databaseUsername = databaseUsername;
    this.databasePassword = databasePassword;
  }

  /**
   * @return tries to determine the type of database that should be used
   */
  @Bean
  protected SupportedDatabases getUsedDatabase()
  {
    for ( SupportedDatabases supportedDatabase : SupportedDatabases.values() )
    {
      if (StringUtils.startsWithIgnoreCase(databaseUrl, supportedDatabase.getExpectedUrlPrefix()))
      {
        return supportedDatabase;
      }
    }
    String errorMessage = String.format("Unsupported database or incorrect JDBC URL. JDBC URL must start with "
                                        + "one of [%s] but was %s",
                                        Arrays.stream(SupportedDatabases.values())
                                              .map(SupportedDatabases::getExpectedUrlPrefix)
                                              .collect(Collectors.joining(", ")),
                                        databaseUrl);
    throw new IllegalStateException(errorMessage);
  }

  /**
   * tries to resolve the datasource from the URI string in {@link #databaseUrl}
   *
   * @return the datasource
   */
  @Bean(name = "datasource")
  protected DataSource datasourceProduction(SupportedDatabases usedDatabase)
  {
    log.info("connecting to {} server: {}", usedDatabase.name(), databaseUrl);
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName(usedDatabase.getDriverClass());
    dataSource.setUrl(databaseUrl);
    dataSource.setUsername(databaseUsername);
    dataSource.setPassword(databasePassword);
    usedDatabase.checkConnection(dataSource);
    return dataSource;
  }

  /**
   * sets the singleton bean of liquibase to create database tables and befill it with default values.
   *
   * @return liquibase bean
   */
  @Bean(name = "liquibase")
  protected SpringLiquibase liquibase(DataSource dataSource)
  {
    SpringLiquibase liquibase = new SpringLiquibase();
    liquibase.setDataSource(dataSource);
    liquibase.setChangeLog("classpath:/de/captaingoldfish/restclient/database/liquibase/changelog.xml");
    return liquibase;
  }

  /**
   * sets up a spring container managed {@link EntityManagerFactory} for JTA transaction.
   *
   * @return the {@link EntityManagerFactory} that creates container managed
   *         {@link jakarta.persistence.EntityManager}
   */
  @Bean
  protected EntityManagerFactory entityManagerFactory(DataSource dataSource, SupportedDatabases usedDatabase)
  {
    LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
    em.setDataSource(dataSource);
    em.setPackagesToScan(PACKAGE_TO_SCAN);
    JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
    em.setJpaVendorAdapter(vendorAdapter);
    em.setJpaProperties(usedDatabase.getAdditionalProperties());

    em.afterPropertiesSet();
    return em.getObject();
  }

  /**
   * gives back the TransactionManager for the {@link EntityManagerFactory} that will be managed by Bean
   *
   * @return the Bean that manages the {@link EntityManagerFactory}
   */
  @Bean
  protected PlatformTransactionManager transactionManager(DataSource dataSource, SupportedDatabases usedDatabase)
  {
    return new JpaTransactionManager(entityManagerFactory(dataSource, usedDatabase));
  }

  /**
   * this post processor is detected automatically by the spring context and will catch all exceptions on beans
   * that are marked with the {@link org.springframework.stereotype.Repository}-annotation
   */
  @Bean
  public PersistenceExceptionTranslationPostProcessor exceptionTranslation()
  {
    return new PersistenceExceptionTranslationPostProcessor();
  }

  /**
   * this enum holds the supported database types and the values that are associated with the supported database
   * types
   */
  public enum SupportedDatabases
  {

    HSQLDB("jdbc:hsqldb", "org.hsqldb.jdbcDriver", "org.hibernate.dialect.HSQLDialect",
    // @formatter:off
        Map.of("hibernate.format_sql", "false",
              "hibernate.show_sql", "false")
      // @formatter:on
    ),

    SQLSERVER("jdbc:sqlserver",
              "com.microsoft.sqlserver.jdbc.SQLServerDriver",
              "org.hibernate.dialect.SQLServerDialect",
              null),

    MYSQL("jdbc:mysql", "com.mysql.cj.jdbc.Driver", "org.hibernate.dialect.MySQL8Dialect", null),

    MARIADB("jdbc:mariadb", "org.mariadb.jdbc.Driver", "org.hibernate.dialect.MariaDBDialect", null),

    POSTGRES("jdbc:postgres", "org.postgresql.Driver", "org.hibernate.dialect.PostgreSQLDialect", null),

    ORACLE("jdbc:oracle", "oracle.jdbc.driver.OracleDriver", "org.hibernate.dialect.Oracle12cDialect", null);

    /**
     * this prefix is used to determine if a JDBC url maps to this supported database type
     */
    @Getter
    private String expectedUrlPrefix;

    /**
     * the driver class that will handle connections to the SQL database
     */
    @Getter
    private String driverClass;

    /**
     * the database dialect implementation that should be used by hibernate
     */
    @Getter
    private String hibernateDialect;

    /**
     * optional additional properties that can be set for each supported database individually
     */
    @Getter
    private Properties additionalProperties;


    SupportedDatabases(String expectedUrlPrefix,
                       String driverClass,
                       String hibernateDialect,
                       Map<String, String> additionalProperties)
    {
      this.expectedUrlPrefix = expectedUrlPrefix;
      this.driverClass = driverClass;
      this.hibernateDialect = hibernateDialect;
      this.additionalProperties = Optional.ofNullable(additionalProperties).map(propertyMap -> {
        Properties properties = new Properties();
        propertyMap.forEach(properties::setProperty);
        return properties;
      }).orElse(null);
    }

    /**
     * checks the connection to the database and writes a success message into the log if the connection was
     * successfully established
     *
     * @param dataSource the datasource to create a valid connection
     */
    @SneakyThrows
    public void checkConnection(DataSource dataSource)
    {
      try (Connection connection = dataSource.getConnection())
      {
        DatabaseMetaData metaData = connection.getMetaData();
        String databaseProductname = metaData.getDatabaseProductName();
        String databaseVersion = metaData.getDatabaseProductVersion();
        log.info("Successfully connected to database: {} : {}", databaseProductname, databaseVersion);
      }
    }
  }
}
