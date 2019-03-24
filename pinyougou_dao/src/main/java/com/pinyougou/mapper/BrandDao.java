package com.pinyougou.mapper;

import com.pinyougou.pojo.TbBrand;

import java.util.List;
import java.util.Map;

public interface BrandDao {

    List<TbBrand> findAllBrand(TbBrand tbBrand);

    List<TbBrand> findAll();

    void addBrand(TbBrand tbBrand);

    void updateBrand(TbBrand brand);

    TbBrand findById(Integer id);

    void delete(Integer id);

    List<Map> selectBrandList();
}
