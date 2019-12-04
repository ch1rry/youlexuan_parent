package com.offcn.page.service.impl;

import com.offcn.mapper.TbGoodsDescMapper;
import com.offcn.mapper.TbGoodsMapper;
import com.offcn.mapper.TbItemCatMapper;
import com.offcn.mapper.TbItemMapper;
import com.offcn.page.service.ItemPageService;
import com.offcn.pojo.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemPageServiceImpl implements ItemPageService {


    @Value("${pageDir}")
    private String pageDir;

    @Autowired
    private FreeMarkerConfig freeMarkerConfig;

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbItemMapper itemMapper;


    @Override
    public boolean genItemHtml(Long goodsId) {

        Configuration configuration = freeMarkerConfig.getConfiguration();
         //获取模板文件
        try {
            Template template = configuration.getTemplate("item.ftl");

            //创建数据模型对象
            Map map=new HashMap();
            //读取商品基本信息
            TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
            map.put("goods",goods);
            //读取商品扩展信息
            TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            map.put("goodsDesc",goodsDesc);

            //获取一级分类对应分类名称
            TbItemCat itemCat1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id());
            TbItemCat itemCat2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id());
            TbItemCat itemCat3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id());

            map.put("itemCat1",itemCat1.getName());
            map.put("itemCat2",itemCat2.getName());
            map.put("itemCat3",itemCat3.getName());

            //读取sku列表
            TbItemExample example = new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            criteria.andGoodsIdEqualTo(goodsId);
            criteria.andStatusEqualTo("1");//状态必须是审核通过
            //设置排序，把默认 is_default 1 排在最前面
            example.setOrderByClause("is_default desc");

            List<TbItem> skuList = itemMapper.selectByExample(example);

            map.put("itemList",skuList);


            //创建静态页面输出对象
            FileWriter out = new FileWriter(new File(pageDir + goodsId + ".html"));

            //执行模板渲染
            template.process(map,out);

            out.close();

            return  true;


        } catch (IOException e) {
            e.printStackTrace();
        }catch (TemplateException e){
            e.printStackTrace();
        }


        return false;
    }

    @Override
    public boolean deleteItemHtml(Long[] goodsIds) {
        try {
            for(Long goodsId:goodsIds){
                new File(pageDir+goodsId+".html").delete();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
