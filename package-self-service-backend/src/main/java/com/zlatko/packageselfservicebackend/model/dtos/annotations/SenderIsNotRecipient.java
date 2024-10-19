package com.zlatko.packageselfservicebackend.model.dtos.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = SenderIsNotRecipientValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface SenderIsNotRecipient {
    String message() default "The sender and the recipient cannot be the same.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}