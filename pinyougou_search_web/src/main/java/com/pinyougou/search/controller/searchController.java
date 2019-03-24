package com.pinyougou.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.search.service.SearchService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/search")
public class searchController {
    @Reference
    private SearchService searchService;

    @RequestMapping("/itemsearch")
    public Map<String, Object> search(@RequestBody Map<String,Object>searchMap) {
        return searchService.search(searchMap);
    }
}
