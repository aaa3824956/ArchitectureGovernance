package org.example.inspect.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.inspect.dto.ApiFanInDTO;
import org.example.inspect.entity.ApiDependency;

import java.util.List;

@Mapper
public interface ApiDependencyMapper extends BaseMapper<ApiDependency> {

    /**
     * 扫描指定微服务下所有 API，找出被其他微服务调用次数超过阈值的接口。
     *
     * @param releaseUnitId 要扫描的微服务 ID
     * @param threshold     扇入阈值（调用方微服务数 > threshold 才算违规）
     */
    @Select("""
        SELECT d.to_api_id                                AS apiId,
               a.api_name                                  AS apiName,
               COUNT(DISTINCT d.from_api_id)               AS depCount,
               GROUP_CONCAT(DISTINCT d.from_release_unit_id
                            ORDER BY d.from_release_unit_id) AS callerUnitIds
        FROM api_dependency d
        LEFT JOIN api a ON a.api_id = d.to_api_id
        WHERE d.to_release_unit_id = #{releaseUnitId}
        GROUP BY d.to_api_id, a.api_name
        HAVING COUNT(DISTINCT d.from_api_id) > #{threshold}
        ORDER BY depCount DESC
    """)
    List<ApiFanInDTO> findFanIn(@Param("releaseUnitId") Integer releaseUnitId,
                                @Param("threshold") Integer threshold);

}
