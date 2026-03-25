package org.example.inspect.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.inspect.entity.InspectionJob;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface InspectionJobMapper extends BaseMapper<InspectionJob> {
    @Update("""
        UPDATE inspection_job
        SET locked = 1,
            lock_node = #{nodeId},
            lock_time = #{claimTime}
        WHERE status = 'RUNNING'
        AND next_run_time <= #{claimTime}
        AND locked = 0
        ORDER BY next_run_time ASC
        LIMIT #{limit}
        """)
    int claimDueJobs(
            @Param("nodeId") String nodeId,
            @Param("claimTime") LocalDateTime claimTime,
            @Param("limit") int limit
    );

    @Select("""
        SELECT *
        FROM inspection_job
        WHERE locked = 1
        AND lock_node = #{nodeId}
        AND lock_time = #{claimTime}
        ORDER BY next_run_time ASC
        LIMIT #{limit}
        """)
    List<InspectionJob> findClaimedJobs(
            @Param("nodeId") String nodeId,
            @Param("claimTime") LocalDateTime claimTime,
            @Param("limit") int limit
    );

    @Update("""
        UPDATE inspection_job
        SET locked = 1,
            lock_node = #{nodeId},
            lock_time = NOW()
        WHERE job_id = #{jobId}
        AND locked = 0
    """)
    int tryLock(Long jobId, String nodeId);

    @Update("""
        UPDATE inspection_job
        SET locked = 0
        WHERE job_id = #{jobId}
        AND lock_node = #{nodeId}
    """)
    int unlock(Long jobId, String nodeId);

    @Update("""
        UPDATE inspection_job
        SET next_run_time = #{nextRunTime},
            last_run_time = #{lastRunTime}
        WHERE job_id = #{jobId}
    """)
    void updateNextRunTime(
            Long jobId,
            LocalDateTime nextRunTime,
            LocalDateTime lastRunTime
    );

    @Update("""
        UPDATE inspection_job
        SET retry_count = #{retryCount},
            next_run_time = #{nextRunTime}
        WHERE job_id = #{jobId}
        """)
    void scheduleRetry(
            @Param("jobId") Long jobId,
            @Param("retryCount") int retryCount,
            @Param("nextRunTime") LocalDateTime nextRunTime
    );

    @Update("""
        UPDATE inspection_job
        SET retry_count = 0
        WHERE job_id = #{jobId}
        """)
    void resetRetryCount(Long jobId);

    @Update("""
        UPDATE inspection_job
        SET locked = 0
        WHERE locked = 1
        AND lock_time < NOW() - INTERVAL 5 MINUTE
    """)
    void releaseTimeoutLocks();
}
