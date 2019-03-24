package com.pinyougou.page.listener;

import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.io.File;
import java.util.List;

public class DeleItemPageListener implements MessageListener {
    @Autowired
    private TbItemMapper itemMapper;

    @Override
    public void onMessage(Message message) {
        try {
            TextMessage goodsId = (TextMessage) message;
            //查询出goodsId所对应的所有item
            TbItemExample example = new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            criteria.andGoodsIdEqualTo(Long.parseLong(goodsId.getText()));
            List<TbItem> items = itemMapper.selectByExample(example);
            //根据item的id 删除静态页面
            for (TbItem item : items) {
                new File("D:/freemarker/pinyougou/" + item.getId() + ".html").delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
