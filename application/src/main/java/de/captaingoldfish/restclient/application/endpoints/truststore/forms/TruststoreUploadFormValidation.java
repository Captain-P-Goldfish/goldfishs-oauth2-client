package de.captaingoldfish.restclient.application.endpoints.truststore.forms;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;


/**
 * @author Pascal Knueppel
 * @since 04.04.2021
 */
@Constraint(validatedBy = TruststoreUploadFormValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface TruststoreUploadFormValidation
{

  String message() default "";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
