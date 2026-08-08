package com.alibaba.otter.canal.handler;

import com.alibaba.otter.canal.annotation.CanalEventHandler;
import com.alibaba.otter.canal.annotation.CanalEventHolder;
import com.alibaba.otter.canal.annotation.OnCanalEvent;
import com.alibaba.otter.canal.context.CanalContext;
import com.alibaba.otter.canal.model.CanalModel;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.alibaba.otter.canal.util.GenericUtil;
import com.alibaba.otter.canal.util.HandlerUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Abstract base implementation of {@link MessageHandler} for protobuf-based
 * {@link Message} objects. Iterates over Canal entries, resolves the
 * appropriate handler (annotation-driven or interface-driven), and
 * dispatches row data for processing.
 *
 * <p>Also implements {@link ApplicationContextAware} to discover and
 * register {@link CanalEventHandler}-annotated beans on startup.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see SyncMessageHandlerImpl
 * @see AsyncMessageHandlerImpl
 * @see AbstractFlatMessageHandler
 */
@Slf4j
public abstract class AbstractMessageHandler implements MessageHandler<Message>, ApplicationContextAware {

    /**
     * The subscribed entry types that this handler will process.
     * Defaults to {@link CanalEntry.EntryType#ROWDATA}.
     */
    private List<CanalEntry.EntryType> subscribeTypes = Arrays.asList(CanalEntry.EntryType.ROWDATA);
    /**
     * Annotation-based event holders keyed by a composite destination+schema+table+eventType key.
     */
    private Map<String, List<CanalEventHolder>> tableEventHolderMap;
    /**
     * Interface-based entry handlers keyed by table name combination.
     */
    private Map<String, EntryHandler> tableHandlerMap;
    /**
     * The row data handler used to dispatch individual row changes.
     */
    private RowDataHandler<CanalEntry.RowData> rowDataHandler;

    /**
     * Constructs a new handler with the given entry handlers and row data handler.
     *
     * @param subscribeTypes the entry types to subscribe to, or {@code null} for defaults
     * @param entryHandlers  the list of entry handlers
     * @param rowDataHandler the row data handler for dispatching changes
     */
    public AbstractMessageHandler(List<CanalEntry.EntryType> subscribeTypes,
                                  List<? extends EntryHandler> entryHandlers,
                                  RowDataHandler<CanalEntry.RowData> rowDataHandler) {
        if(Objects.nonNull(subscribeTypes)){
            this.subscribeTypes = subscribeTypes;
        }
        this.tableHandlerMap = HandlerUtil.getTableHandlerMap(entryHandlers);
        this.rowDataHandler = rowDataHandler;
    }

    /**
     * Checks whether the given entry type is subscribed.
     *
     * @param entryType the entry type to check
     * @return {@code true} if subscribed
     */
    protected boolean isSubscribed(CanalEntry.EntryType entryType) {
        return subscribeTypes.contains(entryType);
    }

    /**
     * Processes a Canal message by iterating its entries and dispatching
     * row data changes to the appropriate handlers.
     *
     * @param destination the Canal destination name
     * @param message     the Canal message to process
     */
    @Override
    public void handleMessage(String destination, Message message) {
        // Iterate entries and process each one
        for (CanalEntry.Entry entry : message.getEntries()) {
            // Get entry type
            CanalEntry.EntryType entryType = entry.getEntryType();
            // Check if this entry type is subscribed
            if (this.isSubscribed(entryType)) {
                // Get schema and table names
                String schemaName = entry.getHeader().getSchemaName();
                String tableName = entry.getHeader().getTableName();
                try {
                    // Parse the serialized row change data
                    CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
                    // Get the event type
                    CanalEntry.EventType eventType = rowChange.getEventType();
                    // Try annotation-based event holders first
                    List<CanalEventHolder> eventHolders = HandlerUtil.getEventHolders(tableEventHolderMap, destination, schemaName, tableName, eventType);
                    if(!CollectionUtils.isEmpty(eventHolders)){
                        CanalModel model = CanalModel.builder()
                                .id(message.getId())
                                .schema(schemaName)
                                .table(tableName)
                                .eventType(eventType)
                                .executeTime(entry.getHeader().getExecuteTime())
                                .build();
                        for (CanalEventHolder eventHolder : eventHolders) {
                            this.handlerRowData(model, rowChange, eventHolder, eventType);
                        }
                        continue;
                    }
                    // Fall back to interface-based entry handler
                    EntryHandler<?> entryHandler = HandlerUtil.getEntryHandler(tableHandlerMap, schemaName, tableName);
                    if(Objects.nonNull(entryHandler)){
                        CanalModel model = CanalModel.builder()
                                .id(message.getId())
                                .schema(schemaName)
                                .table(tableName)
                                .eventType(eventType)
                                .executeTime(entry.getHeader().getExecuteTime())
                                .build();
                        // Dispatch each row to the handler
                        for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
                            this.handlerRowData(model, rowData, entryHandler, eventType);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException("parse event has an error , data:" + entry.toString(), e);
                }
            } else {
                log.info("current entry type: {}", entryType);
            }
        }
    }

    /**
     * Dispatches a row change to an annotation-based event holder via reflection.
     *
     * @param model      the canal model context
     * @param rowChange  the parsed row change
     * @param eventHolder the annotation event holder
     * @param eventType  the event type
     * @throws Exception if method invocation fails
     */
    public void handlerRowData(CanalModel model, CanalEntry.RowChange rowChange, CanalEventHolder eventHolder, CanalEntry.EventType eventType) throws Exception {
        try {
            CanalContext.setModel(model);
            Method method = eventHolder.getMethod();
            ReflectionUtils.makeAccessible(method);
            Object[] args = GenericUtil.getInvokeArgs(method, model, rowChange, eventType);
            method.invoke(eventHolder.getTarget(), args);
        } finally {
            // Remove context
            CanalContext.removeModel();
        }
    }

    /**
     * Dispatches a row to an interface-based entry handler.
     *
     * @param model        the canal model context
     * @param rowData      the row data
     * @param entryHandler the entry handler
     * @param eventType    the event type
     * @throws Exception if handler invocation fails
     */
    public void handlerRowData(CanalModel model, CanalEntry.RowData rowData, EntryHandler entryHandler, CanalEntry.EventType eventType) throws Exception {
        try {
            // Set context
            CanalContext.setModel(model);
            // Dispatch to the row data handler
            rowDataHandler.handlerRowData(rowData, entryHandler, eventType);
        } finally {
            // Remove context
            CanalContext.removeModel();
        }
    }

    /**
     * Discovers and registers all {@link CanalEventHandler}-annotated beans
     * from the Spring application context.
     *
     * @param applicationContext the Spring application context
     * @throws BeansException if context access fails
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        log.info("{}: annotation event handler is initializing....", Thread.currentThread().getName());
        // Discover all handler beans
        Map<String, Object> eventHandlerMap = applicationContext.getBeansWithAnnotation(CanalEventHandler.class);
        if(CollectionUtils.isEmpty(eventHandlerMap)){
            log.info("{}: not found annotation event handler.", Thread.currentThread().getName());
            return;
        }
        // Collect event holders from annotated methods
        List<CanalEventHolder> eventHolders = new ArrayList<>();
        for (Object target : eventHandlerMap.values()) {
            Method[] methods = ReflectionUtils.getDeclaredMethods(target.getClass());
            for (Method method : methods) {
                OnCanalEvent canalEvent = AnnotatedElementUtils.findMergedAnnotation(method, OnCanalEvent.class);
                if (Objects.nonNull(canalEvent)) {
                    eventHolders.add(new CanalEventHolder(target, method, canalEvent));
                }
            }
        }
        this.tableEventHolderMap = HandlerUtil.getEventHolderMap(eventHolders);
        log.info("{}: annotation event handler initialized finish.", Thread.currentThread().getName());
    }

}
