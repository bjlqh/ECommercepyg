package com.pinyougou.content.service.impl;

import java.util.List;

import com.pinyougou.content.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbContentMapper;
import com.pinyougou.pojo.TbContent;
import com.pinyougou.pojo.TbContentExample;
import com.pinyougou.pojo.TbContentExample.Criteria;


import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class ContentServiceImpl implements ContentService {

    @Autowired
    private TbContentMapper contentMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查询全部
     */
    @Override
    public List<TbContent> findAll() {
        return contentMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbContent> page = (Page<TbContent>) contentMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbContent content) {
        contentMapper.insert(content);
        //清除缓存
        redisTemplate.boundHashOps("content").delete(content.getCategoryId());
        System.out.println("when add content, I would clear cache");
    }


    /**
     * 修改
     */
    @Override
    public void update(TbContent content) {
        //1.根据id查询修改前广告分类,并清除缓存
        TbContent tbContent = contentMapper.selectByPrimaryKey(content.getId());
        Long oldCategoryId = tbContent.getCategoryId();
        redisTemplate.boundHashOps("content").delete(oldCategoryId);

        //2.判断广告分类是否修改，如果广告分类发生变化清空原广告分类的缓存
        Long newCategoryId = content.getCategoryId();
        if (oldCategoryId.longValue() != newCategoryId.longValue()) {
            //2.1 广告分类发生了变化,清空该分类的缓存
            redisTemplate.boundHashOps("content").delete(newCategoryId);
        }
        System.out.println("content already update,I have cleared cache");
        contentMapper.updateByPrimaryKey(content);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbContent findOne(Long id) {
        return contentMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        if (ids != null) {
            for (Long id : ids) {
                //1.根据id查询广告分类
                TbContent tbContent = contentMapper.selectByPrimaryKey(id);
                Long categoryId = tbContent.getCategoryId();
                redisTemplate.boundHashOps("content").delete(categoryId);
                System.out.println("when delete content,I would clear cache");
                contentMapper.deleteByPrimaryKey(id);
            }
        }
    }


    @Override
    public PageResult findPage(TbContent content, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbContentExample example = new TbContentExample();
        Criteria criteria = example.createCriteria();

        if (content != null) {
            if (content.getTitle() != null && content.getTitle().length() > 0) {
                criteria.andTitleLike("%" + content.getTitle() + "%");
            }
            if (content.getUrl() != null && content.getUrl().length() > 0) {
                criteria.andUrlLike("%" + content.getUrl() + "%");
            }
            if (content.getPic() != null && content.getPic().length() > 0) {
                criteria.andPicLike("%" + content.getPic() + "%");
            }
            if (content.getStatus() != null && content.getStatus().length() > 0) {
                criteria.andStatusLike("%" + content.getStatus() + "%");
            }

        }

        Page<TbContent> page = (Page<TbContent>) contentMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public List<TbContent> findByCategoryId(Long categoryId) {
        TbContentExample example = new TbContentExample();
        Criteria criteria = example.createCriteria();
        //从缓存中去查询,如果缓存中没有,去数据库查询并放到缓存中
        List<TbContent> contentList = (List<TbContent>) redisTemplate.boundHashOps("content").get(categoryId);
        if (contentList == null) {
            System.out.println("I'll go to the database query and put it in the cache");
            // 1.分类id查询
            criteria.andCategoryIdEqualTo(categoryId);
            // 2.status为1
            criteria.andStatusEqualTo("1");
            //3.要排序
            example.setOrderByClause("sort_order");
            contentList = contentMapper.selectByExample(example);
            //4.将数据放入缓存中
            redisTemplate.boundHashOps("content").put(categoryId, contentList);
        } else {
            System.out.println("I have read the data from the cache");
        }
        return contentList;
    }
}
