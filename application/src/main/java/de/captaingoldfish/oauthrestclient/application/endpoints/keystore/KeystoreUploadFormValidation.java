package de.captaingoldfish.oauthrestclient.application.endpoints.keystore;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;


/**
 * @author Pascal Knueppel
 * @since 27.03.2021
 */
@Constraint(validatedBy = KeystoreUploadFormValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface KeystoreUploadFormValidation
{

  String message() default "";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
