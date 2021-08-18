package de.captaingoldfish.restclient.scim.resources;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import de.captaingoldfish.scim.sdk.common.resources.ResourceNode;
import de.captaingoldfish.scim.sdk.common.resources.base.ScimObjectNode;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import lombok.Builder;


/**
 * Contains some application information that might be useful for the UI
 *
 * @author Pascal Knueppel
 * @since 29.06.2021
 */

public class ScimApplicationInfo extends ResourceNode
{

  private static final Meta META = Meta.builder().created(Instant.now()).build();

  public ScimApplicationInfo()
  {}

  @Builder
  public ScimApplicationInfo(String authCodeRedirectUri, JwtInfo jwtInfo)
  {
    setSchemas(Collections.singletonList(FieldNames.SCHEMA_ID));
    setId("1");
    setAuthCodeRedirectUri(authCodeRedirectUri);
    setJwt(jwtInfo);
    setMeta(META);
  }

  /** The redirect uri for authorization codes to this application. */
  public List<String> getAuthCodeRedirectUri()
  {
    return getSimpleArrayAttribute(FieldNames.AUTH_CODE_REDIRECT_URI);
  }

  /** The redirect uri for authorization codes to this application. */
  public void setAuthCodeRedirectUri(String authCodeRedirectUri)
  {
    setAttribute(FieldNames.AUTH_CODE_REDIRECT_URI, authCodeRedirectUri);
  }

  /** Contains the algorithms supported for JWTs */
  public Optional<JwtInfo> getJwt()
  {
    return getObjectAttribute(FieldNames.JWT_INFO, JwtInfo.class);
  }

  /** Contains the algorithms supported for JWTs */
  public void setJwt(JwtInfo jwt)
  {
    setAttribute(FieldNames.JWT_INFO, jwt);
  }

  /** Contains the algorithms supported for JWTs */
  public static class JwtInfo extends ScimObjectNode
  {

    public JwtInfo()
    {}

    @Builder
    public JwtInfo(List<String> signatureAlgorithmsList,
                   List<String> keyWrapAlgorithmsList,
                   List<String> encryptionAlgorithmsList)
    {
      setSignatureAlgorithms(signatureAlgorithmsList);
      setKeyWrapAlgorithms(keyWrapAlgorithmsList);
      setEncryptionAlgorithms(encryptionAlgorithmsList);
    }

    /** The supported signature algorithms for JWTs */
    public List<String> getSignatureAlgorithms()
    {
      return getSimpleArrayAttribute(FieldNames.SIGNATURE_ALGORITHMS);
    }

    /** The supported signature algorithms for JWTs */
    public void setSignatureAlgorithms(List<String> signatureAlgorithmsList)
    {
      setAttributeList(FieldNames.SIGNATURE_ALGORITHMS, signatureAlgorithmsList);
    }

    /** The key wrap encryption algorithms that are supported */
    public List<String> getKeyWrapAlgorithms()
    {
      return getSimpleArrayAttribute(FieldNames.KEY_WRAP_ALGORITHMS);
    }

    /** The key wrap encryption algorithms that are supported */
    public void setKeyWrapAlgorithms(List<String> keyWrapAlgorithmsList)
    {
      setAttributeList(FieldNames.KEY_WRAP_ALGORITHMS, keyWrapAlgorithmsList);
    }

    /** The supported content encryption algorithms */
    public List<String> getEncryptionAlgorithms()
    {
      return getSimpleArrayAttribute(FieldNames.ENCRYPTION_ALGORITHMS);
    }

    /** The supported content encryption algorithms */
    public void setEncryptionAlgorithms(List<String> encryptionAlgorithmsList)
    {
      setAttributeList(FieldNames.ENCRYPTION_ALGORITHMS, encryptionAlgorithmsList);
    }


  }

  public static class FieldNames
  {

    public static final String SCHEMA_ID = "urn:ietf:params:scim:schemas:captaingoldfish:2.0:AppInfo";

    public static final String AUTH_CODE_REDIRECT_URI = "authCodeRedirectUri";

    public static final String KEY_WRAP_ALGORITHMS = "keyWrapAlgorithms";

    public static final String JWT_INFO = "jwtInfo";

    public static final String ENCRYPTION_ALGORITHMS = "encryptionAlgorithms";

    public static final String ID = "id";

    public static final String SIGNATURE_ALGORITHMS = "signatureAlgorithms";
  }
}
