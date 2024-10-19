package com.zlatko.packageselfservicebackend.model.dtos.annotations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import com.zlatko.packageselfservicebackend.model.dtos.Package;

public class SenderIsNotRecipientValidator implements ConstraintValidator<SenderIsNotRecipient, Package> {

    @Override
    public boolean isValid(Package pkg, ConstraintValidatorContext context) {
        if (pkg.senderId() == null || pkg.recipientId() == null) {
            return true;  // Skip validation if sender or receiver is null
        }
        return !pkg.senderId().equals(pkg.recipientId());
    }
}