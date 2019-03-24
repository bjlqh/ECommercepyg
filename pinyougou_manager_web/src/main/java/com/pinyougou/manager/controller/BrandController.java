package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import entity.Result;
import com.pinyougou.pojo.TbBrand;
import entity.PageResult;
import com.pinyougou.sellergoods.service.BrandService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brand")
public class BrandController {
    @Reference
    private BrandService brandService;

    @RequestMapping("/findAll")
    public List<TbBrand> findAll() {
        return brandService.findAll();
    }

    @RequestMapping("/findPage")
    public PageResult findPage(Integer pageNum, Integer pageSize, @RequestBody TbBrand tbBrand) {
        return brandService.findPage(pageNum, pageSize, tbBrand);
    }

    /**
     * @param tbBrand
     * @return
     * @RequestBody是前端提交参数与后端接收参数实体类映射注解
     */
    @RequestMapping("/addBrand")
    public Result addBrand(@RequestBody TbBrand tbBrand) {
        try {
            brandService.addBrand(tbBrand);
            return new Result(true, "保存成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "保存失败");
        }
    }

    @RequestMapping("/updateBrand")
    public Result updateBrand(@RequestBody TbBrand tbBrand) {
        try {
            brandService.updateBrand(tbBrand);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }
    }

    @RequestMapping("/findById")
    public TbBrand findById(Integer id) {
        return brandService.findById(id);
    }

    @RequestMapping("/delete")
    public Result delete(Integer[] ids) {
        try {
            brandService.delete(ids);
            return new Result(true, "删成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }
    }

    @RequestMapping("/brandList")
    public List<Map> selectBrandList() {
        return brandService.selectBrandList();
    }
}
