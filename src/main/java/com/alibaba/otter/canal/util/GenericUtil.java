package com.alibaba.otter.canal.util;


import com.alibaba.otter.canal.handler.EntryHandler;
import com.alibaba.otter.canal.model.CanalModel;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for resolving generic type parameters and building
 * method invocation argument arrays for Canal event handler methods.
 *
 * <p>Provides helper methods to extract the entity class from an
 * {@link EntryHandler}'s generic type parameter, resolve the
 * corresponding MyBatis-Plus table name, and construct argument
 * arrays for reflective method invocation.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see EntryHandler
 * @see HandlerUtil
 */
public class GenericUtil {

    private static Map<Class<? extends EntryHandler>, Class> cache = new ConcurrentHashMap<>();

    /**
     * Builds an argument array for invoking a handler method that accepts
     * {@link CanalModel}, {@link CanalEntry.RowChange}, and/or
     * {@link CanalEntry.EventType} parameters.
     *
     * @param method    the handler method to invoke
     * @param model     the canal model context
     * @param rowChange the parsed row change data
     * @param eventType the event type
     * @return an argument array matching the method's parameter types
     */
    public static Object[] getInvokeArgs(Method method, CanalModel model, CanalEntry.RowChange rowChange, CanalEntry.EventType eventType) {
        return Arrays.stream(method.getParameterTypes()).map(pClass -> {
                    if(CanalModel.class.isAssignableFrom(pClass)){
                        return model;
                    }
                    if(CanalEntry.RowChange.class.isAssignableFrom(pClass)) {
                        return rowChange;
                    }
                    if(CanalEntry.EventType.class.isAssignableFrom(pClass)) {
                        return eventType;
                    }
                    return null;
                })
                .toArray();
    }

    /**
     * Builds an argument array for invoking a handler method that accepts
     * {@link CanalModel}, {@link List} (of maps), and/or
     * {@link CanalEntry.EventType} parameters.
     *
     * @param method    the handler method to invoke
     * @param model     the canal model context
     * @param rowData   the row data as a list of maps
     * @param eventType the event type
     * @return an argument array matching the method's parameter types
     */
    public static Object[] getInvokeArgs(Method method, CanalModel model, List<Map<String, String>> rowData, CanalEntry.EventType eventType) {
        return Arrays.stream(method.getParameterTypes()).map(pClass -> {
                if(CanalModel.class.isAssignableFrom(pClass)){
                    return model;
                }
                if(List.class.isAssignableFrom(pClass)) {
                    return rowData;
                }
                if(CanalEntry.EventType.class.isAssignableFrom(pClass)) {
                    return eventType;
                }
                return null;
            }).toArray();
    }

    /**
     * Returns the MyBatis-Plus table name for the entity type bound to
     * the given entry handler.
     *
     * @param entryHandler the entry handler
     * @return the table name, or {@code null} if the type cannot be resolved
     */
    public static String getTableGenericProperties(EntryHandler entryHandler) {
        Class<?> tableClass = getTableClass(entryHandler);
        if (tableClass != null) {
            TableInfo tableInfo = TableInfoHelper.getTableInfo(tableClass);
            if (Objects.nonNull(tableInfo)) {
                return tableInfo.getTableName();
            }
        }
        return null;
    }


    /**
     * Resolves the entity class from the generic type parameter of the
     * given {@link EntryHandler} implementation. Results are cached for
     * performance.
     *
     * @param <T>    the entity type
     * @param object the entry handler instance
     * @return the resolved entity class, or {@code null} if not found
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getTableClass(EntryHandler object) {
        Class<? extends EntryHandler> handlerClass = object.getClass();
        Class tableClass = cache.get(handlerClass);
        if (tableClass == null) {
            Type[] interfacesTypes = handlerClass.getGenericInterfaces();
            for (Type t : interfacesTypes) {
                Class c = (Class) ((ParameterizedType) t).getRawType();
                if (c.equals(EntryHandler.class)) {
                    tableClass = (Class<T>) ((ParameterizedType) t).getActualTypeArguments()[0];
                    cache.putIfAbsent(handlerClass, tableClass);
                    return tableClass;
                }
            }
        }
        return tableClass;
    }


}
