package com.example.study.listener;

import org.springframework.batch.core.annotation.AfterChunk;
import org.springframework.batch.core.annotation.AfterChunkError;
import org.springframework.batch.core.annotation.BeforeChunk;
import org.springframework.batch.core.scope.context.ChunkContext;

public class CustomChunkListener {

    @BeforeChunk
    public void bChunk(ChunkContext chunkContext) {
        System.out.println(">> before Chunk");
    }

    @AfterChunk
    public void aChunk(ChunkContext chunkContext) {
        System.out.println(">> after Chunk");

    }

    @AfterChunkError
    public void aChunkError(ChunkContext chunkContext) {
        System.out.println(">> after Chunk Error");

    }
}
