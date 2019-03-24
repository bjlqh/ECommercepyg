package com.pinyougou.sellergoods.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import entitygroup.Good;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.Destination;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private TbBrandMapper brandMapper;
    @Autowired
    private TbSellerMapper sellerMapper;


    /**
     * 查询全部
     */
    @Override
    public List<TbGoods> findAll() {
        return goodsMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(Good good) {
        //1.添加商品信息
        TbGoods tbGoods = good.getGoods();
        tbGoods.setAuditStatus("0");
        goodsMapper.insert(tbGoods);
        //2.添加商品详细描述信息
        //2.1获取商品id
        Long goodsId = tbGoods.getId();
        TbGoodsDesc tbGoodsDesc = good.getGoodsDesc();
        tbGoodsDesc.setGoodsId(goodsId);
        goodsDescMapper.insert(tbGoodsDesc);
        //启用规格
        if ("1".equals(tbGoods.getIsEnableSpec())) {
            //3.添加SUK到商品明细tb_item
            List<TbItem> itemsList = good.getItemsList();
            //`title`商品标题',
            for (TbItem tbItem : itemsList) {
                //spu名
                String title = tbGoods.getGoodsName();
                //{"机身内存":"16G","网络":"联通3G"} JSON_String->Map
                String spec = tbItem.getSpec();
                Map<String, String> map = JSON.parseObject(spec, Map.class);
                for (String key : map.keySet()) {
                    title += "," + map.get(key);
                }
                tbItem.setTitle(title);
                //`image`'商品图片'
                String itemImages = tbGoodsDesc.getItemImages();
                tbItem.setImage(itemImages);
                //`categoryId` bigint(10) NOT NULL COMMENT '所属类目，叶子类目',   //三级分类id
                Long category3Id = tbGoods.getCategory3Id();
                tbItem.setCategoryid(category3Id);
                //`create_time` datetime NOT NULL COMMENT '创建时间',
                tbItem.setCreateTime(new Date());
                //`update_time` datetime NOT NULL COMMENT '更新时间',
                tbItem.setUpdateTime(new Date());
                //`goods_id` bigint(20) DEFAULT NULL,
                Long id = tbGoods.getId();
                tbItem.setGoodsId(id);
                //`seller_id` varchar(30) DEFAULT NULL,
                String sellerId = tbGoods.getSellerId();
                tbItem.setSellerId(sellerId);
                // 以下三个字段，方便商品搜索的
                //`category` varchar(200) DEFAULT NULL, //分类名称
                TbItemCat tbItemCat = itemCatMapper.selectByPrimaryKey(category3Id);
                tbItem.setCategory(tbItemCat.getName());
                //`brand` varchar(100) DEFAULT NULL, //品牌名称
                Long brandId = tbGoods.getBrandId();
                TbBrand tbBrand = brandMapper.selectByPrimaryKey(brandId);
                tbItem.setBrand(tbBrand.getName());
                //`seller` varchar(200) DEFAULT NULL,商家店铺名称nick_name
                TbSeller tbSeller = sellerMapper.selectByPrimaryKey(sellerId);
                tbItem.setSeller(tbSeller.getNickName());

                //保存
                itemMapper.insert(tbItem);
            }
        } else {
            TbItem item = new TbItem();
            //未启用规格SPU+规格描述串作为SKU
            item.setTitle(tbGoods.getGoodsName());
            item.setPrice(tbGoods.getPrice());
            item.setStatus("1");
            item.setIsDefault("1");
            item.setNum(99999);
            item.setSpec("{}");
            item.setCategoryid(tbGoods.getCategory3Id());
            itemMapper.insert(item);
        }
    }


    /**
     * 修改
     */
    @Override
    public void update(TbGoods goods) {
        goodsMapper.updateByPrimaryKey(goods);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbGoods findOne(Long id) {
        return goodsMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            goodsMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbGoodsExample example = new TbGoodsExample();
        Criteria criteria = example.createCriteria();

        if (goods != null) {
            if (goods.getSellerId() != null && goods.getSellerId().length() > 0) {
                criteria.andSellerIdEqualTo(goods.getSellerId());
            }
            if (goods.getGoodsName() != null && goods.getGoodsName().length() > 0) {
                criteria.andGoodsNameLike("%" + goods.getGoodsName() + "%");
            }
            if (goods.getAuditStatus() != null && goods.getAuditStatus().length() > 0) {
                criteria.andAuditStatusEqualTo(goods.getAuditStatus());
            }
            if (goods.getIsMarketable() != null && goods.getIsMarketable().length() > 0) {
                criteria.andIsMarketableEqualTo(goods.getIsMarketable());
            }
            if (goods.getCaption() != null && goods.getCaption().length() > 0) {
                criteria.andCaptionLike("%" + goods.getCaption() + "%");
            }
            if (goods.getSmallPic() != null && goods.getSmallPic().length() > 0) {
                criteria.andSmallPicLike("%" + goods.getSmallPic() + "%");
            }
            if (goods.getIsEnableSpec() != null && goods.getIsEnableSpec().length() > 0) {
                criteria.andIsEnableSpecLike("%" + goods.getIsEnableSpec() + "%");
            }
            if (goods.getIsDelete() != null && goods.getIsDelete().length() > 0) {
                criteria.andIsDeleteLike("%" + goods.getIsDelete() + "%");
            }

        }

        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void updateStatus(Long[] ids, String status) {
        for (Long id : ids) {
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
            tbGoods.setAuditStatus(status);
            goodsMapper.updateByPrimaryKey(tbGoods);
        }
    }

    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private Destination addItemSolrDestination;
    @Autowired
    private Destination deleItemSolrDestination;
    @Autowired
    private Destination addItemPageDestination;
    @Autowired
    private Destination deleItemPageDestination;

    @Override
    public void updateIsMarketable(Long[] ids, String isMarketable) {
        for (Long id : ids) {
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
            //只有审核通过的商品才能上下架
            if ("1".equals(tbGoods.getAuditStatus())) {
                if ("1".equals(isMarketable)) {
                    //上架
                    jmsTemplate.send(addItemSolrDestination,session->{return session.createTextMessage(id+"");});
                    jmsTemplate.send(addItemPageDestination,session->{return session.createTextMessage(id+"");});
                }
                if("0".equals(tbGoods.getIsMarketable())) {
                    //下架
                    jmsTemplate.send(deleItemSolrDestination,session->{return session.createTextMessage(id+"");});
                    jmsTemplate.send(deleItemPageDestination,session->{return session.createTextMessage(id+"");});

                }
                tbGoods.setIsMarketable(isMarketable);
                goodsMapper.updateByPrimaryKey(tbGoods);
            } else {
                throw new RuntimeException("只有审核通过的商品才能上下架");
            }
        }
    }

}
