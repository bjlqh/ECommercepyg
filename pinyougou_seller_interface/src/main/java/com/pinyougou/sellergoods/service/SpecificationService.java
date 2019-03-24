package com.pinyougou.sellergoods.service;

import entity.PageResult;
import entitygroup.Specification;
import com.pinyougou.pojo.TbSpecification;

import java.util.List;
import java.util.Map;

public interface SpecificationService {

    PageResult search(Integer pageNum, Integer pageSize, TbSpecification tbSpecification);

    void addSpecification(Specification specification);

    void deleteSpecification(Long[] ids);

    void updateSpecification(Specification specification);

    Specification findOne(Long id);

    List<Map> selectSpecList();
}
