package de.captaingoldfish.oauthrestclient.application.endpoints;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartException;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.captaingoldfish.oauthrestclient.application.exceptions.RequestException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


/**
 * to assure proper error handling between java backend and react frontend I decided on an easy data structure
 * for error responses. In case of field-validation errors the frontend will receive a message like this:
 * 
 * <pre>
 *   {
 *     "inputFieldErrors": {
 *       "keystorePassword": "Not accepting empty passwords"
 *     }
 *   }
 * </pre>
 * 
 * the key in the "inputFieldErrors" parameter represents the input-field name that also matches the
 * pojo-form-object name. Like this it is easy to map errors directly to fields in the view.<br>
 * <br>
 * in case of unspecific errors like {@link NullPointerException}s or something similiar the response will
 * contain something like this:
 * 
 * <pre>
 *   {
 *     "errorMessages": ["Media Type not supported", "Unexpected type found for conversion"]
 *   }
 * </pre>
 * 
 * @author Pascal Knueppel
 * @since 27.03.2021
 */
@Slf4j
@ControllerAdvice
public class ExceptionRequestHandler
{

  /**
   * parses an unhandled exception for the react frontend
   */
  @SneakyThrows
  @ExceptionHandler(Exception.class)
  public void handleException(Exception ex, HttpServletResponse response)
  {
    log.error(ex.getMessage(), ex);
    response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    ObjectNode errorNode = getErrorNode(ex);
    response.getOutputStream().write(errorNode.toString().getBytes(StandardCharsets.UTF_8));
  }

  /**
   * parses an erroneous http media type exception for the react frontend
   */
  @SneakyThrows
  @ExceptionHandler({HttpMediaTypeNotSupportedException.class, MultipartException.class})
  public void handleOtherExceptions(Exception ex, HttpServletResponse response)
  {
    log.debug(ex.getMessage(), ex);
    response.setStatus(HttpStatus.BAD_REQUEST.value());
    ObjectNode errorNode = getErrorNode(ex);
    response.getOutputStream().write(errorNode.toString().getBytes(StandardCharsets.UTF_8));
  }

  /**
   * {@link RequestException}s are handled exceptions that will be thrown if the bean validation has failed. The
   * mapped {@link BindingResult} that should be present within the exception instance will then be used to
   * create the field-errors-structure for the react-frontend
   */
  @SneakyThrows
  @ExceptionHandler(RequestException.class)
  public void handleException(RequestException ex, HttpServletResponse response)
  {
    log.debug(ex.getMessage(), ex);
    response.setStatus(ex.getStatus());
    ObjectNode responseNode = getErrorNodeFromBindingResult(ex.getBindingResult()).orElseGet(() -> getErrorNode(ex));
    response.getOutputStream().write(responseNode.toString().getBytes(StandardCharsets.UTF_8));
  }

  /**
   * parses an unexpected exception into the "errorMessages" array structure
   */
  private ObjectNode getErrorNode(Exception ex)
  {
    ObjectNode errorNode = new ObjectNode(JsonNodeFactory.instance);
    ArrayNode arrayNode = new ArrayNode(JsonNodeFactory.instance);
    Throwable current = ex;
    while (current != null)
    {
      arrayNode.add(current.getMessage());
      current = current.getCause();
    }
    if (arrayNode.isEmpty())
    {
      arrayNode.add("An internal error has occurred");
    }
    errorNode.set("errorMessages", arrayNode);
    return errorNode;
  }

  /**
   * parses a {@link BindingResult} into the field-errors-structure that is parsable by the react-frontend
   */
  private Optional<ObjectNode> getErrorNodeFromBindingResult(BindingResult bindingResult)
  {
    if (bindingResult == null)
    {
      return Optional.empty();
    }
    ObjectNode responseNode = new ObjectNode(JsonNodeFactory.instance);
    ObjectNode errorNode = new ObjectNode(JsonNodeFactory.instance);
    bindingResult.getAllErrors().forEach(objectError -> {
      if (objectError instanceof FieldError)
      {
        FieldError fieldError = (FieldError)objectError;
        handleFieldError(errorNode, fieldError);
      }
      else
      {
        handleRequestError(responseNode, objectError);
      }
    });
    if (!errorNode.isEmpty())
    {
      responseNode.set("inputFieldErrors", errorNode);
    }
    return Optional.of(responseNode);
  }

  /**
   * handles a more unspecific {@link ObjectError} from a {@link BindingResult}
   */
  private void handleRequestError(ObjectNode responseNode, ObjectError objectError)
  {
    ArrayNode errorMessages = (ArrayNode)responseNode.get("errorMessages");
    if (errorMessages == null)
    {
      errorMessages = new ArrayNode(JsonNodeFactory.instance);
    }
    if (StringUtils.isNotBlank(objectError.getDefaultMessage()))
    {
      errorMessages.add(objectError.getDefaultMessage());
    }
    if (!errorMessages.isEmpty())
    {
      responseNode.set("errorMessages", errorMessages);
    }
  }

  /**
   * handles a {@link FieldError} from a {@link BindingResult}
   */
  private void handleFieldError(ObjectNode errorNode, FieldError fieldError)
  {
    ArrayNode arrayNode = (ArrayNode)errorNode.get(fieldError.getField());
    if (arrayNode == null)
    {
      arrayNode = new ArrayNode(JsonNodeFactory.instance);
    }
    arrayNode.add(fieldError.getDefaultMessage());
    errorNode.set(fieldError.getField(), arrayNode);
  }
}
