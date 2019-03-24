package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbBrand;
import entity.PageResult;

import java.util.List;
import java.util.Map;


public interface BrandService {

    public List<TbBrand> findAll();

    PageResult findPage(Integer pageNum, Integer pageSize, TbBrand tbBrand);

    void addBrand(TbBrand brand);

    void updateBrand(TbBrand brand);

    TbBrand findById(Integer id);

    void delete(Integer[] ids);

    List<Map> selectBrandList();
}
