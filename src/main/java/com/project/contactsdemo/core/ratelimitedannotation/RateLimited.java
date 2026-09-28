package com.project.contactsdemo.core.ratelimitedannotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {
    String service() default "DEFAULT"; // default ""
    //service name almasak bile default olarak ekleme yapacaklar
}
