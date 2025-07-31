package com.barbershop.features.auth.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, Object> {
    
    private String password;
    private String confirmPassword;
    
    @Override
    public void initialize(PasswordMatch constraintAnnotation) {
        this.password = constraintAnnotation.password();
        this.confirmPassword = constraintAnnotation.confirmPassword();
    }
    
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        
        BeanWrapperImpl beanWrapper = new BeanWrapperImpl(value);
        Object passwordValue = beanWrapper.getPropertyValue(password);
        Object confirmPasswordValue = beanWrapper.getPropertyValue(confirmPassword);
        
        if (passwordValue == null && confirmPasswordValue == null) {
            return true;
        }
        
        if (passwordValue == null || confirmPasswordValue == null) {
            return false;
        }
        
        boolean isValid = passwordValue.equals(confirmPasswordValue);
        
        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                   .addPropertyNode(confirmPassword)
                   .addConstraintViolation();
        }
        
        return isValid;
    }
}