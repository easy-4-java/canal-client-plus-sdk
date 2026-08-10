package com.alibaba.otter.canal.handler;

import com.alibaba.otter.canal.annotation.CanalEventHandler;
import com.alibaba.otter.canal.annotation.CanalEventHolder;
import com.alibaba.otter.canal.annotation.OnCanalEvent;
import com.alibaba.otter.canal.context.CanalContext;
import com.alibaba.otter.canal.model.CanalModel;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.FlatMessage;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Abstract base implementation of {@link MessageHandler} for
 * {@link FlatMessage} objects (JSON-friendly Canal messages).
 * Iterates over flat message data rows, resolves the appropriate
 * handler (annotation-driven or interface-driven), and dispatches
 * row data for processing.
 *
 * <p>Also implements {@link ApplicationContextAware} to discover and
 * register {@link CanalEventHandler}-annotated beans on startup.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 3.0.0
 * @see SyncFlatMessageHandlerImpl
 * @see AsyncFlatMessageHandlerImpl
 * @see AbstractMessageHandler
 */
@Slf4j
public abstract class AbstractFlatMessageHandler implements MessageHandler<FlatMessage>, ApplicationContextAware {

    /**
     * The subscribed entry types that this handler will process.
     * Defaults to {@link CanalEntry.EntryType#ROWDATA}.
     */
    private List<CanalEntry.EntryType> subscribeTypes = Arrays.asList(CanalEntry.EntryType.ROWDATA);
    /**
     * Annotation-based event holders keyed by a composite key.
     */
    private Map<String, List<CanalEventHolder>> tableEventHolderMap;
    /**
     * Interface-based entry handlers keyed by table name.
     */
    private Map<String, EntryHandler> tableHandlerMap;
    /**
     * The row data handler for dispatching flat map-based row data.
     */
    private RowDataHandler<List<Map<String, String>>> rowDataHandler;

    /**
     * Constructs a new handler with the given entry handlers and row data handler.
     *
     * @param subscribeTypes the entry types to subscribe to, or {@code null} for defaults
     * @param entryHandlers  the list of entry handlers
     * @param rowDataHandler the row data handler for dispatching changes
     */
    public AbstractFlatMessageHandler(List<CanalEntry.EntryType> subscribeTypes,
                                      List<? extends EntryHandler> entryHandlers,
                                      RowDataHandler<List<Map<String, String>>> rowDataHandler) {
        if(Objects.nonNull(subscribeTypes)){
            this.subscribeTypes = subscribeTypes;
        }
        this.tableHandlerMap = HandlerUtil.getTableHandlerMap(entryHandlers);
        this.rowDataHandler = rowDataHandler;
    }

    /**
     * Processes a flat message by iterating its data rows and dispatching
     * each to the appropriate handler.
     *
     * @param destination  the Canal destination name
     * @param flatMessage  the flat message to process
     */
    @Override
    public void handleMessage(String destination, FlatMessage flatMessage) {
        // Return early if no data
        List<Map<String, String>> data = flatMessage.getData();
        if(CollectionUtils.isEmpty(data)){
            return;
        }
        // Iterate data rows
        for (int i = 0; i < data.size(); i++) {
            String schemaName = flatMessage.getDatabase();
            String tableName = flatMessage.getTable();
            CanalEntry.EventType eventType = CanalEntry.EventType.valueOf(flatMessage.getType());
            List<Map<String, String>> maps;
            if (eventType.equals(CanalEntry.EventType.UPDATE)) {
                // Merge before/after data for updates
                Map<String, String> map = data.get(i);
                Map<String, String> oldMap = flatMessage.getOld().get(i);
                maps = Stream.of(map, oldMap).collect(Collectors.toList());
            } else {
                maps = Stream.of(data.get(i)).collect(Collectors.toList());
            }
            try {
                // Try annotation-based event holders first
                List<CanalEventHolder> eventHolders = HandlerUtil.getEventHolders(tableEventHolderMap, destination, schemaName, tableName, eventType);
                if(!CollectionUtils.isEmpty(eventHolders)){
                    CanalModel model = CanalModel.builder()
                            .id(flatMessage.getId())
                            .schema(schemaName)
                            .table(tableName)
                            .eventType(eventType)
                            .executeTime(flatMessage.getEs())
                            .createTime(flatMessage.getTs()).build();
                    for (CanalEventHolder eventHolder : eventHolders) {
                        this.handlerRowData(model, maps, eventHolder, eventType);
                    }
                    continue;
                }
                // Fall back to interface-based entry handler
                EntryHandler<?> entryHandler = HandlerUtil.getEntryHandler(tableHandlerMap, schemaName, tableName);
                if(Objects.nonNull(entryHandler)){
                    CanalModel model = CanalModel.builder()
                            .id(flatMessage.getId())
                            .schema(schemaName)
                            .table(tableName)
                            .eventType(eventType)
                            .executeTime(flatMessage.getEs())
                            .createTime(flatMessage.getTs()).build();
                   this.handlerRowData(model, maps, entryHandler, eventType);
                }
            } catch (Exception e) {
                throw new RuntimeException("parse event has an error , data:" + maps.toString(), e);
            }
        }
    }

    /**
     * Dispatches flat row data to an annotation-based event holder via reflection.
     *
     * @param model       the canal model context
     * @param rowData     the row data as a list of maps
     * @param eventHolder the annotation event holder
     * @param eventType   the event type
     * @throws Exception if method invocation fails
     */
    public void handlerRowData(CanalModel model, List<Map<String, String>> rowData, CanalEventHolder eventHolder, CanalEntry.EventType eventType) throws Exception {
        Method method = eventHolder.getMethod();
        try {
            CanalContext.setModel(model);
            ReflectionUtils.makeAccessible(method);
            Object[] args = GenericUtil.getInvokeArgs(method, model, rowData, eventType);
            method.invoke(eventHolder.getTarget(), args);
        } finally {
            // Remove context
            CanalContext.removeModel();
        }
    }

    /**
     * Dispatches flat row data to an interface-based entry handler.
     *
     * @param model        the canal model context
     * @param rowData      the row data as a list of maps
     * @param entryHandler the entry handler
     * @param eventType    the event type
     * @throws Exception if handler invocation fails
     */
    public void handlerRowData(CanalModel model, List<Map<String, String>> rowData, EntryHandler entryHandler, CanalEntry.EventType eventType) throws Exception {
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
        Map<String, Object> eventHandlerMap = applicationContext.getBeansWithAnnotation(CanalEventHandler.class);
        if(CollectionUtils.isEmpty(eventHandlerMap)){
            log.info("{}: not found annotation event handler.", Thread.currentThread().getName());
            return;
        }
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
