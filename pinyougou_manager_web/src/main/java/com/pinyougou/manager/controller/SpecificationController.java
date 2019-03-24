package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import entitygroup.Specification;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.sellergoods.service.SpecificationService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/spec")
public class SpecificationController {
    @Reference
    private SpecificationService specificationService;

    @RequestMapping("/search")
    public PageResult search(Integer pageNum, Integer pageSize, @RequestBody TbSpecification tbSpecification) {
        return specificationService.search(pageNum, pageSize, tbSpecification);
    }

    @RequestMapping("/addSpec")
    public Result addSpecification(@RequestBody Specification specification) {

        try {
            specificationService.addSpecification(specification);
            return new Result(true, "添加成功");
        } catch (Exception e) {
            return new Result(false, "添加失败");
        }
    }

    @RequestMapping("/delete")
    public Result deleteSpecification(Long[] ids) {
        try {
            specificationService.deleteSpecification(ids);
            return new Result(true, "删除成功");
        } catch (Exception e) {
            return new Result(false, "删除失败");
        }
    }

    @RequestMapping("/updateSpec")
    public Result updateSpecification(@RequestBody Specification specification) {
        try {
            specificationService.updateSpecification(specification);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            return new Result(false, "修改失败");
        }
    }

    @RequestMapping("/findOne")
    public Specification findOne(Long id) {
        Specification one = specificationService.findOne(id);
        return one;
    }

    @RequestMapping("/specList")
    public List<Map> selectSpecList() {
        return specificationService.selectSpecList();
    }
}
