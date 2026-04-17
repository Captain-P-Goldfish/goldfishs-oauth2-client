package de.captaingoldfish.restclient.application.endpoints.authcodegrant;

/**
 * @author Pascal Knueppel
 * @since 17.04.2026
 */
public record AuthcodeRequestDetails(String authCodeUrl,String query,String requestBody,String state,String redirectUri){}
