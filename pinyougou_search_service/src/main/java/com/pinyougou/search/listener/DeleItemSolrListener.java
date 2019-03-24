package com.pinyougou.search.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SolrDataQuery;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public class DeleItemSolrListener implements MessageListener {
    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public void onMessage(Message message) {
        try {
            TextMessage goodsId = (TextMessage) message;
            //从索引库中查出删除
            SolrDataQuery query = new SimpleQuery("item_goodsid:" + goodsId.getText());
            solrTemplate.delete(query);
            solrTemplate.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
