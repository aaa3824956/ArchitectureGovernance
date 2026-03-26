package org.example.inspect.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.inspect.entity.InspectionIssue;

import java.util.List;

@Mapper
public interface InspectionIssueMapper extends BaseMapper<InspectionIssue> {

    @Delete("DELETE FROM inspection_issue WHERE job_id = #{jobId} AND status IN ('OPEN', 'NEW')")
    int deleteActiveByJobId(Long jobId);

    @Select("SELECT fingerprint FROM inspection_issue WHERE job_id = #{jobId} AND status IN ('OPEN', 'NEW')")
    List<String> findActiveFingerprintsByJobId(Long jobId);

    @Select("SELECT fingerprint FROM inspection_issue WHERE job_id = #{jobId} AND status IN ('RESOLVED', 'WONT_FIX')")
    List<String> findClosedFingerprintsByJobId(Long jobId);

}
