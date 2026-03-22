package org.example.inspect.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.inspect.entity.Inspection;

@Mapper
public interface InspectionMapper extends BaseMapper<Inspection> {

}
