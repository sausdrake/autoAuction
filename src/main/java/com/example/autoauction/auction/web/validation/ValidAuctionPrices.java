package com.example.autoauction.auction.web.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AuctionPricesValidator.class)
@Documented
public @interface ValidAuctionPrices {
    String message() default "Неверные цены аукциона";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}