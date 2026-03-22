package org.example.inspect.common;

import org.springframework.scheduling.support.CronExpression;

import java.time.LocalDateTime;

public class CronUtils {

    private static final long MAX_RETRY_DELAY_SECONDS = 3600L;

    /**
     * 计算下一次执行时间
     */
    public static LocalDateTime calcNextRunTime(String cron) {
        CronExpression cronExpression = CronExpression.parse(cron);
        return cronExpression.next(LocalDateTime.now());
    }

    /**
     * 失败重试：指数退避（基准间隔 × 2^(failureOrdinal-1)），上限 1 小时。
     *
     * @param from             基准时间（通常为当前时间）
     * @param baseIntervalSecs 基准间隔（秒）
     * @param failureOrdinal   第几次失败，从 1 开始
     */
    public static LocalDateTime calcRetryAfter(
            LocalDateTime from,
            int baseIntervalSecs,
            int failureOrdinal
    ) {
        int exp = Math.max(0, failureOrdinal - 1);
        long mult = 1L << Math.min(exp, 12);
        long seconds = Math.min((long) baseIntervalSecs * mult, MAX_RETRY_DELAY_SECONDS);
        return from.plusSeconds(seconds);
    }
}
