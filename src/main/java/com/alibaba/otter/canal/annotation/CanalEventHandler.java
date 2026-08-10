package com.alibaba.otter.canal.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Marks a class as a Canal event handler component. Classes annotated with
 * {@code @CanalEventHandler} are automatically detected by the Spring
 * application context and registered as event-driven Canal message processors.
 *
 * <p>This annotation is a specialization of {@link Component}, so annotated
 * classes are eligible for Spring's component scanning and autowiring.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see OnCanalEvent
 * @see CanalTable
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface CanalEventHandler {

    /**
     * The value may optionally specify the component name of the handler.
     * Defaults to an empty string, letting Spring generate the bean name.
     *
     * @return the component name
     */
    @AliasFor(annotation = Component.class)
    String value() default "";

}
