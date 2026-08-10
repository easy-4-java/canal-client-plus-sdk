package com.alibaba.otter.canal.factory;

import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.BeanUtils;

import java.util.Map;

/**
 * Factory that converts a {@code Map<String, String>} of column name-value
 * pairs into typed entity instances using MyBatis-Plus table metadata for
 * property mapping.
 *
 * <p>This factory is used with the FlatMessage pipeline where row data
 * arrives as maps rather than as protobuf column objects.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see AbstractModelFactory
 * @see EntryColumnModelFactory
 */
public class MapColumnModelFactory extends AbstractModelFactory<Map<String, String>> {

    /**
     * Creates a new instance of the given class and populates it from the
     * value map by matching MyBatis-Plus table column names to entity properties.
     *
     * @param tableClass the target entity class
     * @param valueMap   the column name-value pairs
     * @param <R>        the target model type
     * @return the populated model instance
     * @throws Exception if instantiation or property mapping fails
     */
    @Override
    <R> R newInstance(Class<R> tableClass, Map<String, String> valueMap) throws Exception {
        R object = BeanUtils.instantiateClass(tableClass);
        // Retrieve MyBatis-Plus table metadata
        TableInfo tableInfo = TableInfoHelper.getTableInfo(tableClass);
        // Iterate table fields and map values from the value map
        for (TableFieldInfo tableFieldInfo:  tableInfo.getFieldList()) {
            // Get the value corresponding to the entity property's column name
            Object value = MapUtils.getObject(valueMap, tableFieldInfo.getColumn());
            PropertyUtils.setProperty(object, tableFieldInfo.getProperty(), value);
        }
        return object;
    }

}
