package org.example.inspect.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.inspect.dto.ApiFanInDTO;
import org.example.inspect.entity.ApiDependency;

import java.util.List;

@Mapper
public interface ApiDependencyMapper extends BaseMapper<ApiDependency> {

    @Select("""
        SELECT to_api_id AS apiId,
               COUNT(*) AS depCount
        FROM api_dependency
        WHERE to_release_unit_id = #{toReleaseUnitId}
        GROUP BY to_api_id
        HAVING COUNT(*) > #{threshold}
    """)
    List<ApiFanInDTO> findFanIn(Integer toReleaseUnitId, Integer threshold);

}
