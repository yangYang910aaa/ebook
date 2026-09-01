package com.example.demo.util;

/**
 * 雪花算法 ID 生成器（单例，用于生成登录 token 与日志流水号）
 */
public class SnowflakeIdWorker {

    private static final long EPOCH = 1735689600000L; // 2025-01-01 00:00:00 UTC
    private static final long WORKER_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    private final long workerId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    private static final SnowflakeIdWorker INSTANCE = new SnowflakeIdWorker(1);

    private SnowflakeIdWorker(long workerId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException("workerId 超出范围");
        }
        this.workerId = workerId;
    }

    public static SnowflakeIdWorker getInstance() {
        return INSTANCE;
    }

    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();
        if (timestamp < lastTimestamp) {
            throw new IllegalStateException("时钟回拨");
        }
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = timestamp;
        return ((timestamp - EPOCH) << TIMESTAMP_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}
