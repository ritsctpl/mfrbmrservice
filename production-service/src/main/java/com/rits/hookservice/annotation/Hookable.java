package com.rits.hookservice.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Hookable {
    // Marker annotation to indicate the method supports hooks/extensions.
}
