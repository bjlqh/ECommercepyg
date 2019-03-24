package com.pinyougou.search.service;

import java.util.Map;

public interface SearchService {

    /**
     * @param map 根据参数搜索索引库
     * @return 分页包装类对象
     */
    Map<String,Object> search(Map map);
}
