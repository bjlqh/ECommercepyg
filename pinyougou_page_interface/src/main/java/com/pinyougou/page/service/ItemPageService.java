package com.pinyougou.page.service;

import entitygroup.Good;

public interface ItemPageService {

    /**
     * 根据goodsId查询商品静态页面所需要的数据
     * @param goodsId
     * @return
     */
    public Good findOne(Long goodsId);
}
