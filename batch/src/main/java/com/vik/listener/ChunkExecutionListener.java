package com.vik.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.listener.ChunkListenerSupport;
import org.springframework.batch.core.scope.context.ChunkContext;

public class ChunkExecutionListener extends ChunkListenerSupport {
    private static final Logger logger = LoggerFactory.getLogger(ChunkExecutionListener.class);

    @Override
    public void beforeChunk(ChunkContext chunkContext) {
        chunkContext.attributeNames();
        logger.info("Before chunk");
        super.beforeChunk(chunkContext);
    }

    @Override
    public void afterChunk(ChunkContext chunkContext) {
        logger.info("After chunk");
        super.afterChunk(chunkContext);
    }

}
