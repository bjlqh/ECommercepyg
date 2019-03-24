package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public Map<String, Object> search(Map searchMap) {

        //创建高亮查询对象
        HighlightQuery query = new SimpleHighlightQuery();
        //初始化查询条件
        Criteria criteria = null;
        //1.获取keywords的值,判断是否为空
        String keywords = (String) searchMap.get("keywords");
        if (keywords.trim().length() > 0) {
            //1.1不为空获取值,创建查询条件
            criteria = new Criteria("item_keywords").is(keywords);
        } else {
            //1.2查询所有
            criteria = new Criteria().expression("*:*");
        }

        //2.基于品牌进行条件分页查询
        String brand = (String) searchMap.get("brand");
        if (brand != null && !"".equals(brand)) {
            Criteria brandCriteria = new Criteria("item_brand").is(brand);
            //设置过滤查询对象,传递给高亮查询对象
            SimpleFilterQuery simpleFilterQuery = new SimpleFilterQuery(brandCriteria);
            query.addFilterQuery(simpleFilterQuery);
        }

        //3.基于分类进行条件分页查询
        String category = (String) searchMap.get("category");
        if (category != null && !"".equals(category)) {
            Criteria categoryCriteria = new Criteria("item_category").is(category);
            //设置过滤查询对象,传递给高亮查询对象
            SimpleFilterQuery simpleFilterQuery = new SimpleFilterQuery(categoryCriteria);
            query.addFilterQuery(simpleFilterQuery);
        }

        //4.基于规格条件进行分页查询
        Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
        if (specMap != null) {

            for (String key : specMap.keySet()) {
                //设置过滤查询条件
                Criteria specCriteria = new Criteria("item_spec_" + key).is(specMap.get(key));
                //设置过滤查询对象
                SimpleFilterQuery filterQuery = new SimpleFilterQuery(specCriteria);
                query.addFilterQuery(filterQuery);
            }
        }

        //5.基于价格条件查询
        String price = (String) searchMap.get("price");
        if (price != null && !"".equals(price)) {
            String[] prices = price.split("-");

            //prices[0]-----低价,prices[1]------高价
            if (!"0".equals(prices[0])) {
                //设置价格查询条件
                Criteria priceCriteria = new Criteria("item_price").greaterThanEqual(prices[0]);
                //设置过滤条件对象
                SimpleFilterQuery simpleFilterQuery = new SimpleFilterQuery(priceCriteria);
                query.addFilterQuery(simpleFilterQuery);
            }
            if (!"*".equals(prices[1])) {
                //设置价格查询条件
                Criteria priceCriteria = new Criteria("item_price").lessThanEqual(prices[1]);
                //设置过滤条件对象
                SimpleFilterQuery simpleFilterQuery = new SimpleFilterQuery(priceCriteria);
                query.addFilterQuery(simpleFilterQuery);
            }
        }

        //6.获取排序字段和排序条件,进行排序
        String sortField = (String) searchMap.get("sortField");
        if (sortField.trim().length() > 0) {
            String sort = (String) searchMap.get("sort");
            if ("ASC".equals(sort)) {
                //升序
                query.addSort(new Sort(Sort.Direction.ASC, "item_" + sortField));
            } else {
                //降序
                query.addSort(new Sort(Sort.Direction.DESC, "item_" + sortField));
            }
        }

        //7.分页(需要起始值和每页展示数据)
        Integer pageNo = (Integer) searchMap.get("pageNo");
        Integer pageSize = (Integer) searchMap.get("pageSize");
        query.setOffset((pageNo - 1) * pageSize);
        query.setRows(pageSize);


        //1.3 将查询对象赋给总查询对象
        query.addCriteria(criteria);
        /*
         * 高亮显示处理
         */
        HighlightOptions highlightOptions = new HighlightOptions();
        //2.1设置高亮字段
        highlightOptions.addField("item_title");
        //2.2设置高亮的前后缀
        highlightOptions.setSimplePrefix("<font color='red'>");
        highlightOptions.setSimplePostfix("</font>");
        query.setHighlightOptions(highlightOptions);
        //满足查询条件的商品数据
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        List<TbItem> items = page.getContent();
        //2.3高亮结果处理
        for (TbItem item : items) {
            //2.3.1获取高亮的结果值
            List<HighlightEntry.Highlight> highlights = page.getHighlights(item);
            if (highlights.size() > 0) {
                HighlightEntry.Highlight highlightTitle = highlights.get(0);
                //HighlightEntry.Highlight highlightBrand = highlights.get(1);
                List<String> snippletsTitle = highlightTitle.getSnipplets();
                //List<String> snippletsBrand = highlightBrand.getSnipplets();
                if (snippletsTitle.size() > 0) {
                    item.setTitle(snippletsTitle.get(0));
                }
                /*if (snippletsBrand.size() > 0) {
                    item.setBrand(snippletsBrand.get(0));
                }*/
            }
        }
        System.out.println("查询总记录数:" + page.getTotalElements());

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("rows", items);
        resultMap.put("totalPages", page.getTotalPages());
        resultMap.put("pageNo", pageNo);
        return resultMap;
    }
}
