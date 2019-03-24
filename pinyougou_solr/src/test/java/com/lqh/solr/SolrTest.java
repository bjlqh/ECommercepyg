package com.lqh.solr;

import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.solr.SolrUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:spring/applicationContext-*.xml")
public class SolrTest {
    @Autowired
    private SolrTemplate solrTemplate;


    @Autowired
    private SolrUtil solrUtil;

    @Test
    public void dataImport() {
        solrUtil.dataImport();
    }


    //新增和修改solr索引数据
    @Test
    public void save() {
        TbItem item = new TbItem();
        item.setId(1L);
        item.setBrand("联想");
        item.setTitle("联想超级本,移动4G,16G");
        item.setSeller("联想官方旗舰店");

        solrTemplate.saveBean(item);
        //必须提交
        solrTemplate.commit();
    }

    //基于id查询商品数据
    @Test
    public void queryById() {
        HashMap map = solrTemplate.getById(1, HashMap.class);
        Set<String> set = map.keySet();
        for (String key : set) {
            System.out.println(key+"  "+map.get(key));
        }
        TbItem tbItem = solrTemplate.getById(1L, TbItem.class);
        System.out.println(tbItem.getId() + " " + tbItem.getBrand() + " " + tbItem.getTitle() + "  " + tbItem.getSeller());
    }

    //基于id 删除商品
    @Test
    public void deleteById() {
        solrTemplate.deleteById("2");
        solrTemplate.commit();
    }

    //删除所有商品
    @Test
    public void deleteAll() {
        //先查询出所有商品
        SimpleQuery query = new SimpleQuery("*:*");
        //再删除
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    //批量添加100条数据
    @Test
    public void saveBatch() {
        List<TbItem> list = new ArrayList<>();
        for (long i = 1; i <= 100; i++) {
            TbItem item = new TbItem();
            item.setId(i);
            item.setBrand("联想");
            item.setTitle(i + "联想超级本,移动4G,16G");
            item.setSeller("联想官方旗舰店" + i + "号店");
            list.add(item);
        }
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    //分页查询商品数据
    @Test
    public void queryPage() {
        Query query = new SimpleQuery("*:*");
        ScoredPage<TbItem> items = null;
        //1.设置分页条件
        for (int i = 1; i <= 10; i++) {
            query.setOffset((i - 1) * 10);
            query.setRows(10);
            items = solrTemplate.queryForPage(query, TbItem.class);
            for (TbItem item : items) {
                System.out.println(item.getId() + " " + item.getBrand() + " " + item.getTitle() + "  " + item.getSeller());

            }
            System.out.println("-----------------------第" + i + "页--------------------------");
        }
        System.out.println("总页数:" + items.getTotalPages() + "总记录数:" + items.getTotalElements());
    }

    //条件查询商品数据
    @Test
    public void multiQuery() {
        SimpleQuery query = new SimpleQuery("*:*");
        //1.设置查询条件------title有5  seller有8
        Criteria criteria = new Criteria("item_title").contains("5").and("item_seller").contains("8");
        query.addCriteria(criteria);
        ScoredPage<TbItem> items = solrTemplate.queryForPage(query, TbItem.class);
        for (TbItem item : items) {
            System.out.println(item.getId() + " " + item.getBrand() + " " + item.getTitle() + "  " + item.getSeller());
        }
        System.out.println("总页数:" + items.getTotalPages() + "总记录数:" + items.getTotalElements());
    }

    //高亮数据显示
    @Test
    public void highLightQuery() {
        //1.创建高亮查询对象
        SimpleHighlightQuery query = new SimpleHighlightQuery();
        //2.构建查询条件
        Criteria criteria = new Criteria("item_title").contains("联想");
        //3.添加查询条件
        query.addCriteria(criteria);
        //4.高亮对象
        HighlightOptions highlightOptions = new HighlightOptions();
        //5.指定高亮字段是神马,设置前缀和后缀
        highlightOptions.addField("item_title");
        //highlightOptions.addField("item_seller");
        highlightOptions.setSimplePrefix("<font color='red'>");
        highlightOptions.setSimplePostfix("</font>");
        //6.高亮设置
        query.setHighlightOptions(highlightOptions);
        //7.分页查询
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        System.out.println("总页数:"+page.getTotalPages());
        System.out.println("总记录数:"+page.getTotalElements());
        //8.获取当前记录结果-----为什么要getContent，直接遍历page不行吗？
        //List<TbItem> content = page.getContent();
        for (TbItem item : page) {
            //9.对高亮结果替换操作
            List<HighlightEntry.Highlight> highlights = page.getHighlights(item);
            if (highlights.size() > 0) {
                //9.1说明有高亮内容
                List<String> snipplets = highlights.get(0).getSnipplets();
                if (snipplets.size() > 0) {
                    //9.2获取到高亮内容进行替换
                    String highLight = snipplets.get(0);
                    item.setSeller(highLight);
                    item.setTitle(highLight);
                }
            }
            System.out.println(item.getId() + " " + item.getBrand() + " " + item.getTitle() + "  " + item.getSeller());
        }
    }

}
