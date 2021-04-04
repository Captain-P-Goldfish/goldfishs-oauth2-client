package de.captaingoldfish.oauthrestclient.application.setup;

import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author Pascal Knueppel
 * @since 04.04.2021
 */
@Data
@NoArgsConstructor
public class ErrorResponseForm
{

  private List<String> errorMessages;

  private Map<String, List<String>> inputFieldErrors;
}
