package com.alibaba.otter.canal.factory;


import com.alibaba.otter.canal.enums.TableNameEnum;
import com.alibaba.otter.canal.handler.EntryHandler;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.util.GenericUtil;
import com.alibaba.otter.canal.util.HandlerUtil;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Factory that converts a list of Canal {@link CanalEntry.Column} objects
 * into typed entity instances using MyBatis-Plus table metadata for
 * column-to-property mapping.
 *
 * <p>When the handler is bound to the wildcard table, columns are returned
 * as a plain {@code Map<String, String>}. Otherwise, the factory resolves
 * the entity class from the handler's generic type and populates it
 * using reflection.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see AbstractModelFactory
 * @see MapColumnModelFactory
 */
public class EntryColumnModelFactory extends AbstractModelFactory<List<CanalEntry.Column>> {

    /**
     * Creates a model instance from the full list of columns.
     *
     * @param entryHandler the entry handler providing type information
     * @param columns      the list of Canal columns
     * @param <R>          the target model type
     * @return the populated model, or a column map for wildcard handlers
     * @throws Exception if instantiation or property mapping fails
     */
    @Override
    public <R> R newInstance(EntryHandler entryHandler, List<CanalEntry.Column> columns) throws Exception {
        String canalTableName = HandlerUtil.getCanalTableNameCombination(entryHandler);
        if (TableNameEnum.ALL.name().toLowerCase().equals(canalTableName)) {
            Map<String, String> map = columns.stream().collect(Collectors.toMap(CanalEntry.Column::getName, CanalEntry.Column::getValue));
            return (R) map;
        }
        Class<R> entityClass = GenericUtil.getTableClass(entryHandler);
        if (entityClass != null) {
            return newInstance(entityClass, columns);
        }
        return null;
    }

    /**
     * Creates a model instance from columns, populating only the columns
     * whose names appear in the {@code updateColumn} set.
     *
     * @param entryHandler the entry handler providing type information
     * @param columns      the list of Canal columns
     * @param updateColumn the set of column names that were updated
     * @param <R>          the target model type
     * @return the partially populated model
     * @throws Exception if instantiation or property mapping fails
     */
    @Override
    public <R> R newInstance(EntryHandler entryHandler, List<CanalEntry.Column> columns, Set<String> updateColumn) throws Exception {
        String canalTableName = HandlerUtil.getCanalTableNameCombination(entryHandler);
        if (TableNameEnum.ALL.name().toLowerCase().equals(canalTableName)) {
            Map<String, String> map = columns.stream().filter(column -> updateColumn.contains(column.getName()))
                    .collect(Collectors.toMap(CanalEntry.Column::getName, CanalEntry.Column::getValue));
            return (R) map;
        }
        Class<R> tableClass = GenericUtil.getTableClass(entryHandler);
        if (tableClass != null) {
            // Retrieve MyBatis-Plus table metadata
            TableInfo tableInfo = TableInfoHelper.getTableInfo(tableClass);
            // Create entity instance
            R object = BeanUtils.instantiateClass(tableClass);
            for (CanalEntry.Column column : columns) {
                if (updateColumn.contains(column.getName())) {
                    // Iterate table fields to find matching column
                    for (TableFieldInfo tableFieldInfo:  tableInfo.getFieldList()) {
                        String fieldName = tableFieldInfo.getProperty();
                        // Map column value to entity property
                        if (StringUtils.equals(tableFieldInfo.getColumn(), column.getName())) {
                            PropertyUtils.setProperty(object, fieldName, column.getValue());
                            break;
                        }
                    }
                }
            }
            return object;
        }
        return null;
    }


    /**
     * Creates a model instance of the given class and populates it by
     * matching column names to MyBatis-Plus table field metadata.
     *
     * @param rtClass the target entity class
     * @param columns the list of Canal columns
     * @param <R>     the target model type
     * @return the populated model, or {@code null} if columns are empty
     * @throws Exception if instantiation or property mapping fails
     */
    @Override
    <R> R newInstance(Class<R> rtClass, List<CanalEntry.Column> columns) throws Exception {
        // Return null if columns are empty
        if(CollectionUtils.isEmpty(columns)){
            return null;
        }
        // Create entity instance
        R object = BeanUtils.instantiateClass(rtClass);
        // Retrieve MyBatis-Plus table metadata
        TableInfo tableInfo = TableInfoHelper.getTableInfo(rtClass);
        // Iterate table fields and map columns
        for (TableFieldInfo tableFieldInfo:  tableInfo.getFieldList()) {
            String fieldName = tableFieldInfo.getProperty();
            for (CanalEntry.Column column : columns) {
                // Map column value to entity property
                if (StringUtils.equals(tableFieldInfo.getColumn(), column.getName())) {
                    PropertyUtils.setProperty(object, fieldName, column.getValue());
                    break;
                }
            }
        }
        return object;
    }

}
