package com.alibaba.otter.canal.factory;


import com.alibaba.otter.canal.handler.EntryHandler;

import java.util.Set;

/**
 * Strategy interface for converting raw Canal data (columns or maps) into
 * typed domain model objects. Implementations are used by row data handlers
 * to materialize entity instances from change event payloads.
 *
 * @param <T> the raw data type consumed by the factory (e.g., column list or map)
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see EntryColumnModelFactory
 * @see MapColumnModelFactory
 */
public interface IModelFactory<T> {

    /**
     * Creates a new model instance from the given raw data.
     *
     * @param entryHandler the entry handler that provides type information
     * @param t            the raw data to convert
     * @param <R>          the target model type
     * @return the populated model instance
     * @throws Exception if instantiation or property mapping fails
     */
    <R> R newInstance(EntryHandler entryHandler, T t) throws Exception;

    /**
     * Creates a new model instance from the given raw data, populating only
     * the columns whose names appear in {@code updateColumn}.
     *
     * <p>The default implementation returns {@code null}; override to provide
     * selective column mapping.</p>
     *
     * @param entryHandler the entry handler that provides type information
     * @param t            the raw data to convert
     * @param updateColumn the set of column names that were updated
     * @param <R>          the target model type
     * @return the populated model instance, or {@code null}
     * @throws Exception if instantiation or property mapping fails
     */
    default <R> R newInstance(EntryHandler entryHandler, T t, Set<String> updateColumn) throws Exception {
        return null;
    }
}
