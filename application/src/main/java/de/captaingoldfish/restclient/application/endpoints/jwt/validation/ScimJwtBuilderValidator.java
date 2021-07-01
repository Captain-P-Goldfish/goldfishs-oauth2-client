package de.captaingoldfish.restclient.application.endpoints.jwt.validation;

import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.node.ObjectNode;

import de.captaingoldfish.restclient.application.crypto.JwtHandler;
import de.captaingoldfish.restclient.database.repositories.KeystoreDao;
import de.captaingoldfish.restclient.scim.resources.ScimJwtBuilder;
import de.captaingoldfish.scim.sdk.common.utils.JsonHelper;
import de.captaingoldfish.scim.sdk.server.endpoints.validation.RequestValidator;
import de.captaingoldfish.scim.sdk.server.endpoints.validation.ValidationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * @author Pascal Knueppel
 * @since 27.06.2021
 */
@Slf4j
@RequiredArgsConstructor
public class ScimJwtBuilderValidator implements RequestValidator<ScimJwtBuilder>
{

  /**
   * used to build a {@link JwtHandler} that will test the operation that should be executed
   */
  private final KeystoreDao keystoreDao;

  /**
   * checks that the operation can be performed with the given data and extends the validation context by
   * appropriate error messages it was not possible
   */
  @Override
  public void validateCreate(ScimJwtBuilder resource, ValidationContext validationContext)
  {
    if (validationContext.hasErrors())
    {
      return;
    }

    String header = resource.getHeader();
    String keyId = resource.getKeyId();
    validateHeaderNode(keyId, header, validationContext);
    if (validationContext.hasErrors())
    {
      return;
    }

    JwtHandler jwtHandler = new JwtHandler(keystoreDao);
    try
    {
      jwtHandler.createJwt(keyId, header, "test");
    }
    catch (Exception ex)
    {
      log.debug(ex.getMessage(), ex);
      validationContext.addError(ScimJwtBuilder.FieldNames.HEADER, ex.getMessage());
    }
  }

  /**
   * checks if the given header is a valid JWS or JWE header
   */
  private void validateHeaderNode(String keyId, String header, ValidationContext validationContext)
  {
    final ObjectNode headerNode;
    try
    {
      headerNode = (ObjectNode)JsonHelper.readJsonDocument(header);
    }
    catch (Exception ex)
    {
      String errorMessage = String.format("Header is not a valid JSON structure '%s'", ex.getMessage());
      validationContext.addError(ScimJwtBuilder.FieldNames.HEADER, errorMessage);
      return;
    }

    final boolean hasKeyIdKey = StringUtils.isNotBlank(keyId) || headerNode.has("kid");
    if (!hasKeyIdKey)
    {
      String errorMessage = "keyId is required and must match an alias of the application keystore";
      validationContext.addError(ScimJwtBuilder.FieldNames.HEADER, errorMessage);
      return;
    }

    final boolean hasEncKey = headerNode.has("enc");
    final boolean hasAlgKey = headerNode.has("alg");
    if (!hasEncKey && !hasAlgKey)
    {
      String errorMessage = "Header is missing required values. JWS requires [kid, alg]. JWE requires [kid, alg, enc]";
      validationContext.addError(ScimJwtBuilder.FieldNames.HEADER, errorMessage);
    }
  }

  /**
   * update is disabled
   */
  @Override
  public void validateUpdate(Supplier<ScimJwtBuilder> oldResourceSupplier,
                             ScimJwtBuilder newResource,
                             ValidationContext validationContext)
  {
    // no updates possible
  }
}
