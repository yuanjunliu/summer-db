package cn.juns.summer.db.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
@Inherited
public @interface Title {
    int priority() default 0;
}
