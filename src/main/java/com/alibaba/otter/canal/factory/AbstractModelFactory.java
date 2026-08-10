package com.alibaba.otter.canal.factory;


import com.alibaba.otter.canal.enums.TableNameEnum;
import com.alibaba.otter.canal.handler.EntryHandler;
import com.alibaba.otter.canal.util.GenericUtil;
import com.alibaba.otter.canal.util.HandlerUtil;

/**
 * Abstract base implementation of {@link IModelFactory} that resolves the
 * target entity class from the entry handler's generic type parameter.
 *
 * <p>If the handler is bound to the wildcard table ({@code ALL}), the raw
 * data is returned as-is. Otherwise, the factory delegates to
 * {@link #newInstance(Class, Object)} for type-safe conversion.</p>
 *
 * @param <T> the raw data type consumed by the factory
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see IModelFactory
 * @see EntryColumnModelFactory
 * @see MapColumnModelFactory
 */
public abstract class AbstractModelFactory<T> implements IModelFactory<T> {

    /**
     * Creates a new model instance from the given raw data. If the entry handler
     * is bound to the wildcard table name, the raw data is returned directly.
     * Otherwise, the generic type argument of the handler is resolved and used
     * to instantiate the target class.
     *
     * @param entryHandler the entry handler providing type information
     * @param t            the raw data to convert
     * @param <R>          the target model type
     * @return the populated model instance, or {@code null} if the type cannot be resolved
     * @throws Exception if instantiation or property mapping fails
     */
    @Override
    public <R> R newInstance(EntryHandler entryHandler, T t) throws Exception {
        String canalTableName = HandlerUtil.getCanalTableNameCombination(entryHandler);
        if (TableNameEnum.ALL.name().toLowerCase().equals(canalTableName)) {
            return (R) t;
        }
        Class<R> tableClass = GenericUtil.getTableClass(entryHandler);
        if (tableClass != null) {
            return newInstance(tableClass, t);
        }
        return null;
    }

    /**
     * Creates a new instance of the given class and populates it with data
     * from the raw input.
     *
     * @param tableClass the target entity class
     * @param t          the raw data to convert
     * @param <R>        the target model type
     * @return the populated model instance
     * @throws Exception if instantiation or property mapping fails
     */
    abstract <R> R newInstance(Class<R> tableClass, T t) throws Exception;
}
