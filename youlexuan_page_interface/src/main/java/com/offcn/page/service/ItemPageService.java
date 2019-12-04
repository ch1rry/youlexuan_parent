package com.offcn.page.service;

public interface ItemPageService {

    //根据传入商品编号goodsId,生成对应商品详情页的静态页面
    public boolean genItemHtml(Long goodsId);

    /**
     * 删除商品详细页
     *
     * @param goodsIds
     * @return
     */
    public boolean deleteItemHtml(Long[] goodsIds);
}
