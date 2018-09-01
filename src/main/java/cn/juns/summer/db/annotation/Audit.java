package cn.juns.summer.db.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Audit {
    boolean saveOldValue() default true;
    boolean saveNewValue() default true;
}
