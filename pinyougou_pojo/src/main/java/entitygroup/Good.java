package entitygroup;

import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Good implements Serializable {

    private TbGoods goods;
    private TbGoodsDesc goodsDesc;
    private List<TbItem> itemsList;
    private Map<String,String> categoryName;

    public Map<String, String> getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(Map<String, String> categoryName) {
        this.categoryName = categoryName;
    }

    public TbGoods getGoods() {
        return goods;
    }

    public void setGoods(TbGoods goods) {
        this.goods = goods;
    }

    public TbGoodsDesc getGoodsDesc() {
        return goodsDesc;
    }

    public void setGoodsDesc(TbGoodsDesc goodsDesc) {
        this.goodsDesc = goodsDesc;
    }

    public List<TbItem> getItemsList() {
        return itemsList;
    }

    public void setItemsList(List<TbItem> itemsList) {
        this.itemsList = itemsList;
    }
}
