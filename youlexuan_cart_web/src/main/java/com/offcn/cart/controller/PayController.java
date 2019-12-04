package com.offcn.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.entity.Result;
import com.offcn.order.service.OrderService;
import com.offcn.pay.service.AlipayService;
import com.offcn.pojo.TbPayLog;
import com.offcn.util.IdWorker;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private AlipayService alipayService;

    @Reference
    private OrderService orderService;

    @RequestMapping("/createNative")
    public Map createNative() {
        //获取用户名
        String userId= SecurityContextHolder.getContext().getAuthentication().getName();

        //到redis查询支付日志
        TbPayLog payLog = orderService.searchPayLogFromRedis(userId);
        //判断支付日志存在
        if (payLog!=null){
            return alipayService.createNative(payLog.getOutTradeNo(),payLog.getTotalFee()+"");
        }else {
            return new HashMap();
        }
//        IdWorker idWorker = new IdWorker();
//        return alipayService.createNative(idWorker.nextId() + "", "10000000");
    }

    /**
     * 查询支付状态
     *
     * @param out_trade_no
     * @return
     */
    @RequestMapping("queryPayStatus")
    public Result queryPayStatus(String out_trade_no) {
        Result result = null;

        int x=0;

        while (true) {
            //调用查询接口
            Map<String, String> map = null;

            try {
                map = alipayService.queryPayStatus(out_trade_no);
            } catch (Exception e) {
//                e.printStackTrace();
                System.out.println("调用查询服务出错");
            }

            if (map == null) {
                result = new Result(false, "支付出错");
                break;
            }

            if (map.get("trade_status") != null && map.get("trade_status").equals("TRADE_SUCCESS")) {
                //如果成功
                result = new Result(true, "支付成功");
                orderService.updateOrderStatus(out_trade_no,map.get("trade_no"));
                break;
            }

            if (map.get("trade_status") != null && map.get("trade_status").equals("TRADE_CLOSED")) {
                //如果成功
                result = new Result(true, "未付款交易超时关闭，或支付完成后全额退款");
                break;
            }

            if (map.get("trade_status") != null && map.get("trade_status").equals("TRADE_FINISHED")) {
                //如果成功
                result = new Result(true, "交易结束，不可退款");
                break;
            }

            try {
                Thread.sleep(3000);//间隔3秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //为了不让循环无休止的运行,我们定义一个循环变量,如果这个变量超过了这个值,则退出循环,设置时间为5分钟
            x++;
            if ((x>=20)){
                result=new Result(false,"二维码超时");
                break;
            }

        }
        return result;
    }

}
