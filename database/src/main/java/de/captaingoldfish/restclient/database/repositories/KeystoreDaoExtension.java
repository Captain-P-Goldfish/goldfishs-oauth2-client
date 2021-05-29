package de.captaingoldfish.restclient.database.repositories;

import de.captaingoldfish.restclient.database.entities.Keystore;


/**
 * @author Pascal Knueppel
 * @since 30.03.2021
 */
public interface KeystoreDaoExtension
{

  /**
   * @return gets or creates the application keystore
   */
  public Keystore getKeystore();

  /**
   * removes entries from other resources that are linked with entries within the application keystore
   * 
   * @param alias the linked alias that was removed from the application keystore
   */
  public void deleteKeystoreAlias(String alias);

}
