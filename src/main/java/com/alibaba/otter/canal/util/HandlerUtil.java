package com.alibaba.otter.canal.util;


import com.alibaba.otter.canal.annotation.CanalEventHolder;
import com.alibaba.otter.canal.annotation.CanalTable;
import com.alibaba.otter.canal.annotation.OnCanalEvent;
import com.alibaba.otter.canal.enums.TableNameEnum;
import com.alibaba.otter.canal.handler.EntryHandler;
import com.alibaba.otter.canal.protocol.CanalEntry;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utility class for resolving Canal table handler mappings, building
 * composite keys from destination/schema/table/eventType combinations,
 * and filtering annotation-based event holders.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see CanalEventHolder
 * @see CanalTable
 * @see OnCanalEvent
 */
public class HandlerUtil {

    protected static Map<String, Predicate<CanalEventHolder>> eventPredicateMap = new ConcurrentHashMap<>();

    /**
     * Finds the entry handler that matches the given schema and table names.
     * Returns the specific handler if found, or the global wildcard handler
     * as a fallback.
     *
     * @param entryHandlers the list of entry handlers to search
     * @param schemaName    the database schema name
     * @param tableName     the table name
     * @return the matching entry handler, or {@code null}
     */
    public static EntryHandler getEntryHandler(List<? extends EntryHandler> entryHandlers, String schemaName, String tableName) {
        StringJoiner joiner = new StringJoiner(".").add(schemaName).add(tableName);
        EntryHandler globalHandler = null;
        for (EntryHandler handler : entryHandlers) {
            String canalTableNameCombination = getCanalTableNameCombination(handler);
            if (StringUtils.isBlank(canalTableNameCombination)) {
                continue;
            }
            if (TableNameEnum.ALL.name().toLowerCase().equals(canalTableNameCombination)) {
                globalHandler = handler;
                continue;
            }
            if (canalTableNameCombination.equals(joiner.toString().toLowerCase())) {
                return handler;
            }
            String name = GenericUtil.getTableGenericProperties(handler);
            if (name != null) {
                if (name.equals(tableName)) {
                    return handler;
                }
            }
        }
        return globalHandler;
    }


    /**
     * Builds a map from table name combinations to entry handlers.
     *
     * @param entryHandlers the list of entry handlers
     * @return a map keyed by lowercase table name combination
     */
    public static Map<String, EntryHandler> getTableHandlerMap(List<? extends EntryHandler> entryHandlers) {
        Map<String, EntryHandler> map = new ConcurrentHashMap<>();
        if (CollectionUtils.isEmpty(entryHandlers)) {
            return map;
        }
        for (EntryHandler handler : entryHandlers) {
            String canalTableNameCombination = getCanalTableNameCombination(handler);
            if (StringUtils.isNotBlank(canalTableNameCombination)) {
                map.putIfAbsent(canalTableNameCombination.toLowerCase(), handler);
            } else {
                String name = GenericUtil.getTableGenericProperties(handler);
                if (name != null) {
                    map.putIfAbsent(name.toLowerCase(), handler);
                }
            }
        }
        return map;
    }

    /**
     * Builds a map from composite keys to lists of annotation event holders.
     * Each holder may appear under multiple keys (one per event type).
     *
     * @param eventHolders the list of event holders
     * @return a map keyed by destination+schema+table+eventType
     */
    public static Map<String, List<CanalEventHolder>> getEventHolderMap(List<CanalEventHolder> eventHolders) {
        Map<String, List<CanalEventHolder>> map = new ConcurrentHashMap<>();
        if (CollectionUtils.isEmpty(eventHolders)) {
            return map;
        }
        for (CanalEventHolder holder : eventHolders) {
            List<String> canalTableNameCombinations = getCanalTableNameCombinations(holder);
            if (CollectionUtils.isEmpty(canalTableNameCombinations)) {
                continue;
            }
            for (String canalTableNameCombination : canalTableNameCombinations) {
                map.computeIfAbsent(canalTableNameCombination, k -> new ArrayList<>()).add(holder);
            }
        }
        return map;
    }

    /**
     * Returns the event holders that match the given destination, schema,
     * table, and event type by applying a composed predicate filter.
     *
     * @param map         the event holder map
     * @param destination the Canal destination
     * @param schemaName  the schema name
     * @param tableName   the table name
     * @param eventType   the event type
     * @return the filtered list of matching event holders
     */
    public static List<CanalEventHolder> getEventHolders(Map<String, List<CanalEventHolder>> map,
                                                        String destination,
                                                        String schemaName,
                                                        String tableName,
                                                        CanalEntry.EventType eventType) {
        String key = getCombinationValue(destination, schemaName, tableName, eventType);
        Predicate<CanalEventHolder> predicate =  eventPredicateMap.computeIfAbsent(key, k -> getAnnotationFilter(destination, schemaName, tableName, eventType));
        return map.getOrDefault(key, Collections.emptyList()).stream().filter(predicate).collect(Collectors.toList());
    }

    /**
     * Returns the entry handler from the map that matches the given
     * schema and table names.
     *
     * @param map        the handler map
     * @param schemaName the schema name
     * @param tableName  the table name
     * @return the matching handler, or the global wildcard handler
     */
    public static EntryHandler getEntryHandler(Map<String, EntryHandler> map, String schemaName, String tableName) {
        StringJoiner joiner = new StringJoiner(".").add(schemaName).add(tableName);
        EntryHandler entryHandler = map.get(joiner.toString().toLowerCase());
        if (entryHandler == null) {
            return map.get(TableNameEnum.ALL.name().toLowerCase());
        }
        return entryHandler;
    }

    /**
     * Builds a composite predicate that filters event holders by
     * destination, schema, table, and event type.
     *
     * @param destination the Canal destination
     * @param schemaName  the schema name
     * @param tableName   the table name
     * @param eventType   the event type
     * @return the composite predicate
     */
    protected static Predicate<CanalEventHolder> getAnnotationFilter(String destination,
                                                                     String schemaName,
                                                                     String tableName,
                                                                     CanalEntry.EventType eventType) {

        Predicate<CanalEventHolder> df = holder -> StringUtils.isEmpty(holder.getEvent().destination())
                || holder.getEvent().destination().equals(destination) || destination == null;

        Predicate<CanalEventHolder> sf = holder -> StringUtils.isNotBlank(holder.getEvent().schema())
                && holder.getEvent().schema().equalsIgnoreCase(schemaName);

        Predicate<CanalEventHolder> tf = holder -> StringUtils.isNotBlank(holder.getEvent().table())
                && ( holder.getEvent().table().equalsIgnoreCase(tableName) || holder.getEvent().table().equals(TableNameEnum.ALL.getTable()) );

        Predicate<CanalEventHolder> ef = holder -> holder.getEvent().eventType().length > 0 && Arrays.stream(holder.getEvent().eventType()).anyMatch(ev -> ev == eventType) ;

        return df.and(sf).and(tf).and(ef);
    }

    /**
     * Returns the composite table name combination for the given entry handler
     * based on its {@link CanalTable} annotation.
     *
     * @param entryHandler the entry handler
     * @return the composite key, or {@code null} if no annotation is present
     */
    public static String getCanalTableNameCombination(EntryHandler entryHandler) {
        CanalTable canalTable = entryHandler.getClass().getAnnotation(CanalTable.class);
        if (Objects.nonNull(canalTable)) {
            return getCombinationValue(canalTable.destination(), canalTable.schema(), canalTable.table());
        }
        return null;
    }

    /**
     * Returns the composite table name combinations for the given event holder,
     * one per declared event type.
     *
     * @param eventHolder the event holder
     * @return the list of composite keys
     */
    public static List<String> getCanalTableNameCombinations(CanalEventHolder eventHolder) {
        OnCanalEvent canalEvent = eventHolder.getEvent();
        if (Objects.nonNull(canalEvent) && Objects.nonNull(canalEvent.eventType()) && canalEvent.eventType().length > 0) {
            return Arrays.stream(canalEvent.eventType())
                    .map(eventType -> getCombinationValue(canalEvent.destination(), canalEvent.schema(), canalEvent.table(), eventType))
                    .distinct().collect(Collectors.toList());
        }
        return null;
    }

    /**
     * Builds a dot-separated composite key from destination, schema, and table.
     * Blank values are replaced with the wildcard default from {@link TableNameEnum#ALL}.
     *
     * @param destination the Canal destination
     * @param schema      the schema name
     * @param table       the table name
     * @return the lowercase composite key
     */
    public static String getCombinationValue(String destination, String schema, String table) {
        destination = StringUtils.defaultIfBlank(destination, TableNameEnum.ALL.getDestination());
        schema = StringUtils.defaultIfBlank(schema, TableNameEnum.ALL.getSchema());
        table = StringUtils.defaultIfBlank(table, TableNameEnum.ALL.getTable());
        StringJoiner joiner = new StringJoiner(TableNameEnum.DELIMITER).add(destination).add(schema).add(table);
        return joiner.toString().toLowerCase();
    }

    /**
     * Builds a dot-separated composite key from destination, schema, table,
     * and event type. Blank values are replaced with wildcard defaults.
     *
     * @param destination the Canal destination
     * @param schema      the schema name
     * @param table       the table name
     * @param eventType   the event type
     * @return the lowercase composite key including the event type
     */
    public static String getCombinationValue(String destination, String schema, String table, CanalEntry.EventType eventType) {
        destination = StringUtils.defaultIfBlank(destination, TableNameEnum.ALL.getDestination());
        schema = StringUtils.defaultIfBlank(schema, TableNameEnum.ALL.getSchema());
        table = StringUtils.defaultIfBlank(table, TableNameEnum.ALL.getTable());
        StringJoiner joiner = new StringJoiner(TableNameEnum.DELIMITER).add(destination).add(schema).add(table).add(eventType.name().toLowerCase());
        return joiner.toString().toLowerCase();
    }

}
