package com.alibaba.otter.canal.context;

import com.alibaba.otter.canal.model.CanalModel;
import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * Thread-local context for the current Canal event being processed.
 * Stores a {@link CanalModel} that describes the database change event
 * currently being handled on the calling thread.
 *
 * <p>Uses {@link TransmittableThreadLocal} so that the context propagates
 * correctly across thread pools (e.g., when async handlers are used).</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see CanalModel
 */
public class CanalContext {

    private static TransmittableThreadLocal<CanalModel> threadLocal = new TransmittableThreadLocal<>();

    /**
     * Returns the {@link CanalModel} bound to the current thread.
     *
     * @return the current canal model, or {@code null} if none is set
     */
    public static CanalModel getModel(){
        return threadLocal.get();
    }


    /**
     * Binds the given {@link CanalModel} to the current thread.
     *
     * @param canalModel the canal model to store
     */
    public static void setModel(CanalModel canalModel){
        threadLocal.set(canalModel);
    }


    /**
     * Removes the {@link CanalModel} from the current thread.
     * Should be called in a {@code finally} block after event processing.
     */
    public  static void removeModel(){
        threadLocal.remove();
    }
}
