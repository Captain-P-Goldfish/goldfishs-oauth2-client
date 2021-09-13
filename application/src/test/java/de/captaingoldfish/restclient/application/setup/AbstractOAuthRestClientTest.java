package de.captaingoldfish.restclient.application.setup;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.LocalServerPort;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.captaingoldfish.restclient.database.repositories.CurrentWorkflowSettingsDao;
import de.captaingoldfish.restclient.database.repositories.HttpClientSettingsDao;
import de.captaingoldfish.restclient.database.repositories.KeystoreDao;
import de.captaingoldfish.restclient.database.repositories.OpenIdClientDao;
import de.captaingoldfish.restclient.database.repositories.OpenIdProviderDao;
import de.captaingoldfish.restclient.database.repositories.ProxyDao;
import de.captaingoldfish.restclient.database.repositories.TokenCategoryDao;
import de.captaingoldfish.restclient.database.repositories.TokenStoreDao;
import de.captaingoldfish.restclient.database.repositories.TruststoreDao;
import kong.unirest.json.JSONArray;
import lombok.SneakyThrows;


/**
 * @author Pascal Knueppel
 * @since 31.03.2021
 */
public abstract class AbstractOAuthRestClientTest implements FileReferences
{

  @Autowired
  protected ObjectMapper objectMapper;

  @Autowired
  protected KeystoreDao keystoreDao;

  @Autowired
  protected TruststoreDao truststoreDao;

  @Autowired
  protected ProxyDao proxyDao;

  @Autowired
  protected OpenIdClientDao openIdClientDao;

  @Autowired
  protected OpenIdProviderDao openIdProviderDao;

  @Autowired
  protected HttpClientSettingsDao httpClientSettingsDao;

  @Autowired
  protected CurrentWorkflowSettingsDao currentWorkflowSettingsDao;

  @Autowired
  protected TokenCategoryDao tokenCategoryDao;

  @Autowired
  protected TokenStoreDao tokenStoreDao;

  @LocalServerPort
  protected int port;

  @Value("${server.servlet.context-path:}")
  private String contextPath;

  private String applicationUrl;

  @BeforeEach
  public void initializeApplicationUrl()
  {
    this.applicationUrl = String.format("http://localhost:%s%s",
                                        port,
                                        StringUtils.isBlank(contextPath) ? "" : "/" + contextPath);
  }

  @AfterEach
  public void clearTables()
  {
    tokenStoreDao.deleteAll();
    tokenCategoryDao.deleteAll();
    currentWorkflowSettingsDao.deleteAll();
    httpClientSettingsDao.deleteAll();
    openIdClientDao.deleteAll();
    openIdProviderDao.deleteAll();
    proxyDao.deleteAll();
    truststoreDao.deleteAll();
    keystoreDao.deleteAll();
  }

  public String getApplicationUrl()
  {
    return getApplicationUrl(null);
  }

  public String getApplicationUrl(String path)
  {
    return this.applicationUrl + StringUtils.stripToEmpty(path);
  }

  public List<String> jsonArrayToList(JSONArray jsonArray)
  {
    List<String> values = new ArrayList<>();
    jsonArray.forEach(val -> values.add((String)val));
    return values;
  }

  @SneakyThrows
  public <T> T getForm(String jsonResponse, Class<T> type)
  {
    return objectMapper.readValue(jsonResponse, type);
  }

  @SneakyThrows
  public <T> T getForm(String jsonResponse, TypeReference<T> type)
  {
    return objectMapper.readValue(jsonResponse, type);
  }

  @SneakyThrows
  public String toJson(Object object)
  {
    return objectMapper.writeValueAsString(object);
  }
}
