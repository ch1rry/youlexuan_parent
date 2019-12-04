package com.offcn.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.pojo.TbItem;
import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ItemSearchListener implements MessageListener {
    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {
        System.out.println("接收到导入solr数据请求");
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            try {
                String jsonStr = textMessage.getText();
                List<TbItem> list = JSON.parseArray(jsonStr, TbItem.class);
                for (TbItem item : list) {
                    System.out.println("title" + item.getTitle());
                    Map<String, Object> specMap = JSON.parseObject(item.getSpec());
                    Map map = new HashMap<>();
                    for (String key : specMap.keySet()) {
                        map.put(Pinyin.toPinyin(key, "").toLowerCase(), specMap.get(key));
                    }
                    item.setSpecMap(map);
                }
                itemSearchService.importList(list);
                System.out.println("成功保存sku数据到solr");
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
