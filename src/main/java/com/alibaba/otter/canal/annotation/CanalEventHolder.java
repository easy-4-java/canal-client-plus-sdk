package com.alibaba.otter.canal.annotation;


import com.alibaba.otter.canal.protocol.CanalEntry;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Holds a reference to a Canal event listener method along with its
 * {@link OnCanalEvent} annotation metadata. Used internally to match
 * incoming Canal events to the appropriate handler methods at runtime.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see OnCanalEvent
 * @see CanalEventHandler
 */
public class CanalEventHolder {

    /**
     * The target bean instance that owns the handler method.
     */
    private Object target;
    /**
     * The handler method to invoke when a matching event arrives.
     */
    private Method method;
    /**
     * The {@link OnCanalEvent} annotation present on the handler method.
     */
    private OnCanalEvent event;

    /**
     * Constructs a new holder with the given target bean, method, and event annotation.
     *
     * @param target the bean instance containing the handler method
     * @param method the handler method annotated with {@link OnCanalEvent}
     * @param event  the merged {@link OnCanalEvent} annotation metadata
     */
    public CanalEventHolder(Object target, Method method, OnCanalEvent event) {
        this.target = target;
        this.method = method;
        this.event = event;
    }

    /**
     * Returns the target bean instance.
     *
     * @return the bean that owns the handler method
     */
    public Object getTarget() {
        return target;
    }

    /**
     * Returns the handler method.
     *
     * @return the reflective method reference
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Returns the {@link OnCanalEvent} annotation metadata.
     *
     * @return the event annotation on the handler method
     */
    public OnCanalEvent getEvent() {
        return event;
    }

    /**
     * Checks whether this holder matches the given Canal event type.
     * A match occurs when the holder's declared event types array is empty
     * (meaning it accepts all types), when the given type is {@code null},
     * or when the declared event types contain the given type.
     *
     * @param eventType the Canal event type to check
     * @return {@code true} if this holder matches the event type
     */
    public boolean isMatch(CanalEntry.EventType eventType) {
        return this.getEvent().eventType().length == 0 || Arrays.stream(this.getEvent().eventType()).anyMatch(ev -> ev == eventType) || eventType == null;
    }

}
