package com.offcn.sms;

import com.aliyuncs.CommonResponse;
import com.aliyuncs.exceptions.ClientException;
import com.offcn.util.SmsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.Map;

@Component("smsListener")
public class SmsListener implements MessageListener {
    @Autowired
    private SmsUtil smsUtil;

    @Override
    public void onMessage(Message message) {
        if (message instanceof MapMessage) {
            MapMessage map = (MapMessage) message;
            try {
                System.out.println("收到短信发送请求---》 mobile:" + map.getString("mobile") +
                        " templateId:" + map.getString("templateId") +
                        " singName:" + map.getString("singName") +
                        " param:" + map.getString("templateParam"));
                CommonResponse response = smsUtil.sendSms(map.getString("mobile"), map.getString("templateId"), map.getString("singName"), map.getString("templateParam"));
                System.out.println("data:" + response.getData());
            } catch (ClientException e) {
                e.printStackTrace();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
