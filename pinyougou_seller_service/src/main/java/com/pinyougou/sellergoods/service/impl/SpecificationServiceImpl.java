package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import com.pinyougou.mapper.TbSpecificationMapper;
import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.*;
import com.pinyougou.sellergoods.service.SpecificationService;
import entitygroup.Specification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SpecificationServiceImpl implements SpecificationService {
    @Autowired
    private TbSpecificationMapper tbSpecificationMapper;
    @Autowired
    private TbSpecificationOptionMapper tbSpecificationOptionMapper;

    @Override
    public PageResult search(Integer pageNum, Integer pageSize, TbSpecification tbSpecification) {
        PageHelper.startPage(pageNum, pageSize);
        TbSpecificationExample example = new TbSpecificationExample();
        //判断tbSpecification是否为空
        if (tbSpecification != null) {
            //不为空获取specification里面的specName,进行条件分页查询
            String specName = tbSpecification.getSpecName();
            if (specName != null && specName.trim() != "") {
                //设置查询条件为模糊查询
                TbSpecificationExample.Criteria criteria = example.createCriteria();
                criteria.andSpecNameLike("%" + specName + "%");
            }
        }
        //Specification为空则为分页查询
        Page<TbSpecification> page = (Page<TbSpecification>) tbSpecificationMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void addSpecification(Specification specification) {
        //保存规格
        TbSpecification tbSpecification = specification.getTbSpecification();
        tbSpecificationMapper.insert(tbSpecification);

        //保存规格选项
         for (TbSpecificationOption option : specification.getOptions()) {
             //获取插入后的规格的id,
             Long id = tbSpecification.getId();
             System.out.println(id);
             option.setSpecId(id);
             tbSpecificationOptionMapper.insert(option);
         }
    }

    @Override
    public void deleteSpecification(Long[] ids) {
        if (ids != null) {
            for (Long id : ids) {
                //1.删除规格
                tbSpecificationMapper.deleteByPrimaryKey(id);
                //2.删除规格选项
                TbSpecificationOptionExample optionExample = new TbSpecificationOptionExample();
                TbSpecificationOptionExample.Criteria criteria = optionExample.createCriteria();
                criteria.andIdEqualTo(id);
                tbSpecificationOptionMapper.deleteByExample(optionExample);
            }
        }
    }

    @Override
    public void updateSpecification(Specification specification) {
        //1.更新规格
        TbSpecification tbSpecification = specification.getTbSpecification();
        tbSpecificationMapper.updateByPrimaryKey(tbSpecification);
        //2.更新规格选项
        //2.1删除原来的规格选项
        TbSpecificationOptionExample example = new TbSpecificationOptionExample();
        TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
        criteria.andSpecIdEqualTo(tbSpecification.getId());
        tbSpecificationOptionMapper.deleteByExample(example);

        //2.2新增页面传递过来的规格选项(选项对象)
        for (TbSpecificationOption option : specification.getOptions()) {
            //加入规格的id
            option.setSpecId(tbSpecification.getId());
            tbSpecificationOptionMapper.insert(option);
        }

    }

    @Override
    public Specification findOne(Long id) {
        TbSpecification tbSpecification = tbSpecificationMapper.selectByPrimaryKey(id);
        Specification specification = new Specification();
        //1.封装规格
        specification.setTbSpecification(tbSpecification);
        //2.封装规格选项
        TbSpecificationOptionExample optionExample = new TbSpecificationOptionExample();
        TbSpecificationOptionExample.Criteria criteria = optionExample.createCriteria();
        criteria.andSpecIdEqualTo(id);
        List<TbSpecificationOption> options = tbSpecificationOptionMapper.selectByExample(optionExample);
        specification.setOptions(options);
        return specification;
    }

    @Override
    public List<Map> selectSpecList() {
        return tbSpecificationMapper.selectSpecList();
    }
}
