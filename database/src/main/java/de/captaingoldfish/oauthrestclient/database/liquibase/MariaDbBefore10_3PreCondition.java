package de.captaingoldfish.oauthrestclient.database.liquibase;

import java.util.Locale;

import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.exception.CustomPreconditionErrorException;
import liquibase.exception.CustomPreconditionFailedException;
import liquibase.precondition.CustomPrecondition;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


/**
 * this class is used for the liquibase changesets to determine between mariadb versions before 10.3 and
 * after. This check is necessary since mariadb supports sequences since version 10.3 causing the
 * table-sequence alternative that was used until then to be broken
 * 
 * @author Pascal Knüppel
 * @since 22.02.2021
 */
@Slf4j
public class MariaDbBefore10_3PreCondition implements CustomPrecondition
{

  /**
   * check if the current mariadb version is a younger version than 10.3
   * 
   * @throws CustomPreconditionFailedException if the database version at 10.3 or older
   */
  @SneakyThrows
  @Override
  public void check(Database database) throws CustomPreconditionFailedException, CustomPreconditionErrorException
  {
    DatabaseConnection connection = database.getConnection();
    int majorVersion = connection.getDatabaseMajorVersion();
    int minorVersion = connection.getDatabaseMinorVersion();
    String productname = connection.getDatabaseProductName();
    boolean isMariaDbBefore10_3 = productname.toLowerCase(Locale.ROOT).equals("mariadb") && majorVersion == 10
                                  && minorVersion < 3;
    if (!isMariaDbBefore10_3)
    {
      throw new CustomPreconditionFailedException("Is not MariaDB before 10.3");
    }
  }
}
