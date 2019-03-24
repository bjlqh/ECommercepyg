package com.pinyougou.page.listener;

import com.pinyougou.page.service.ItemPageService;
import entitygroup.Good;
import com.pinyougou.pojo.TbItem;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class addItemPageListener implements MessageListener {
    @Autowired
    private ItemPageService itemPageService;
    @Autowired
    private FreeMarkerConfig freeMarkerConfig;

    @Override
    public void onMessage(Message message) {
        try {
            TextMessage goodsId = (TextMessage) message;
            // 第一步：创建一个 Configuration 对象，直接 new 一个对象。构造方法的参数就是 freemarker 的版本号。
            Configuration configuration = freeMarkerConfig.getConfiguration();
            // 第四步：加载一个模板，创建一个模板对象。
            Template template = configuration.getTemplate("item.ftl");
            // 第五步：创建一个模板使用的数据集，可以是 pojo 也可以是 map。一般是 Map。
            Good good = itemPageService.findOne(Long.parseLong(goodsId.getText()));
            List<TbItem> itemsList = good.getItemsList();
            for (TbItem item : itemsList) {
                // 第六步：创建一个 Writer 对象，一般创建一 FileWriter 对象，指定生成的文件名。
                Writer writer = new FileWriter("D:/freemarker/pinyougou/" + item.getId() + ".html");
                // 第七步：调用模板对象的 process 方法输出文件。
                Map<String, Object> map = new HashMap<>();
                map.put("item", item);
                map.put("good", good);
                template.process(map, writer);
                // 第八步：关闭流
                writer.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
