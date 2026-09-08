package com.example.demo.util;

/**
 * 雪花算法 ID 生成器（单例，用于生成登录 token 与日志流水号）
 */
public class SnowflakeIdWorker {

    // 起始时间戳（2025-01-01 00:00:00 UTC），ID 中时间戳部分 = 当前时间 - EPOCH，可使用到约 2089 年
    private static final long EPOCH = 1735689600000L;
    // 工作节点 ID 占 5 位，支持 0~31 共 32 个节点
    private static final long WORKER_ID_BITS = 5L;
    // 序列号占 12 位，同一毫秒内每节点可生成 0~4095 共 4096 个 ID
    private static final long SEQUENCE_BITS = 12L;
    // 工作节点 ID 最大值 = 31
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    // 序列号掩码 = 4095，用于 sequence 自增后取模
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);
    // 工作节点 ID 左移位数 = 12（序列号位数）
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    // 时间戳左移位数 = 12 + 5 = 17（序列号 + 工作节点位数）
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    private final long workerId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    // 单例实例，workerId 固定为 1（单机部署；多机部署需按机器分配不同 workerId）
    private static final SnowflakeIdWorker INSTANCE = new SnowflakeIdWorker(1);

    private SnowflakeIdWorker(long workerId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException("workerId 超出范围");
        }
        this.workerId = workerId;
    }

    /**
     * 获取单例实例
     */
    public static SnowflakeIdWorker getInstance() {
        return INSTANCE;
    }

    /**
     * 生成下一个雪花 ID（线程安全，synchronized 保证并发正确性）
     * ID 结构（64位）：0(符号位) | 41位时间戳差 | 5位工作节点 | 12位序列号
     *
     * @return 全局唯一长整型 ID
     */
    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();
        // 时钟回拨检测：当前时间小于上次生成时间，拒绝生成防止 ID 重复
        if (timestamp < lastTimestamp) {
            throw new IllegalStateException("时钟回拨");
        }
        // 同一毫秒内：序列号自增，达到上限（4096）则自旋等待下一毫秒
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = timestamp;
        // 拼接 ID：(时间戳差 << 17) | (workerId << 12) | 序列号
        return ((timestamp - EPOCH) << TIMESTAMP_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    /**
     * 自旋等待直到下一毫秒（当前毫秒序列号用尽时调用）
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}
