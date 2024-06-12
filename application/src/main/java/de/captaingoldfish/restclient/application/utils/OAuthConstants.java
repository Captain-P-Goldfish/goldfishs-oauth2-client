package de.captaingoldfish.restclient.application.utils;

/**
 * @author Pascal Knueppel
 * @since 22.08.2021
 */
public final class OAuthConstants
{

  public static final String GRANT_TYPE = "grant_type";

  public static final String AUTH_CODE_GRANT_TYPE = "authorization_code";

  public static final String CLIENT_CREDENTIALS_GRANT_TYPE = "client_credentials";

  public static final String RESOURCE_PASSWORD_GRANT_TYPE = "password";

  public static final String CLIENT_ID = "client_id";

  public static final String REDIRECT_URI = "redirect_uri";

  public static final String STATE = "state";

  public static final String CODE = "code";

  public static final String SCOPE = "scope";

  public static final String JTI = "jti";

  public static final String USERNAME = "username";

  public static final String PASSWORD = "password";

  /**
   * the HTTP header name under which the DPoP value must be sent to the token-endpoint
   */
  public static final String DPOP_HEADER = "DPoP";

  /**
   * the name of the token-type for DPoP's
   */
  public static final String DPOP_TOKEN_TYPE = "DPoP";

  /**
   * the token-type of the DPoP
   */
  public static final String DPOP_JWT_TYPE = "dpop+jwt";

  /**
   * JWK SHA-256 Thumbprint confirmation method. The value of the jkt member MUST be the base64url encoding (as
   * defined in [RFC7515]) of the JWK SHA-256 Thumbprint (according to [RFC7638]) of the DPoP public key (in JWK
   * format) to which the access token is bound.
   */
  public static final String DPOP_JKT_CLAIM = "jkt";

  /**
   * The value of the HTTP method (Section 9.1 of [RFC9110]) of the request to which the JWT is attached.
   */
  public static final String HTM_CLAIIM = "htm";

  /**
   * The HTTP target URI (Section 7.1 of [RFC9110]) of the request to which the JWT is attached, without query
   * and fragment parts.
   */
  public static final String HTU_CLAIIM = "htu";

  /**
   * Hash of the access token. The value MUST be the result of a base64url encoding (as defined in Section 2 of
   * [RFC7515]) the SHA-256 [SHS] hash of the ASCII encoding of the associated access token's value.
   */
  public static final String ATH_CLAIM = "ath";

  /**
   * the JWT nonce parameter
   */
  public static final String NONCE = "nonce";

  /**
   * the AccessToken constant to determine the type of returned token
   */
  public static final String TOKEN_TYPE = "token_type";

  /**
   * the AccessToken constant to get the AccessToken-value
   */
  public static final String ACCESS_TOKEN = "access_token";

  /**
   * the code-verifier parameter of the Proof Key for Code Exchange specification: RFC7636
   */
  public static final String CODE_VERIFIER = "code_verifier";

  /**
   * the code-challenge parameter of the Proof Key for Code Exchange specification: RFC7636
   */
  public static final String CODE_CHALLENGE = "code_challenge";

  /**
   * the code-challenge-method parameter of the Proof Key for Code Exchange specification: RFC7636
   */
  public static final String CODE_CHALLENGE_METHOD = "code_challenge_method";

  /**
   * used in Pushed Authorization Request responses
   */
  public static final String REQUEST_URI = "request_uri";
}
