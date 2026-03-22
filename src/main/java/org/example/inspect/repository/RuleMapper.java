package org.example.inspect.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.inspect.entity.InspectionRule;

@Mapper
public interface RuleMapper extends BaseMapper<InspectionRule> {

}
