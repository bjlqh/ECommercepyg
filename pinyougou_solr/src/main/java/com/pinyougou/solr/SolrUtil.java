package com.pinyougou.solr;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class SolrUtil {
    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private SolrTemplate solrTemplate;

    public void dataImport() {
        //查询item表中的is_marketable=1，goods表中的status=1
        List<TbItem> items = itemMapper.selectAllGrounding();
        //查询动态变化的属性名和属性值
        for (TbItem item : items) {
            String spec = item.getSpec();
            Map specMap = JSON.parseObject(spec, Map.class);
            item.setSpecMap(specMap);
        }
        solrTemplate.saveBeans(items);
        solrTemplate.commit();
    }
}
