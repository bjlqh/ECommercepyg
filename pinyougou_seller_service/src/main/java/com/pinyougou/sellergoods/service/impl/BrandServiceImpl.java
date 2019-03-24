package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.BrandDao;
import com.pinyougou.pojo.TbBrand;
import entity.PageResult;
import com.pinyougou.sellergoods.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Service
@Transactional
public class BrandServiceImpl implements BrandService {
    @Autowired
    private BrandDao brandDao;

    @Override
    public List<TbBrand> findAll() {
        return brandDao.findAll();
    }
    @Override
    public PageResult findPage(Integer pageNum, Integer pageSize, TbBrand tbBrand) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbBrand> page = (Page<TbBrand>)brandDao.findAllBrand(tbBrand);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void addBrand(TbBrand tbBrand) {
        brandDao.addBrand(tbBrand);
    }

    @Override
    public void updateBrand(TbBrand brand) {
        brandDao.updateBrand(brand);
    }

    @Override
    public TbBrand findById(Integer id) {
        return brandDao.findById(id);
    }

    @Override
    public void delete(Integer[] ids) {
        if (ids != null) {
            for (Integer id : ids) {
                brandDao.delete(id);
            }
        }
    }

    @Override
    public List<Map> selectBrandList() {
        return brandDao.selectBrandList();
    }
}
