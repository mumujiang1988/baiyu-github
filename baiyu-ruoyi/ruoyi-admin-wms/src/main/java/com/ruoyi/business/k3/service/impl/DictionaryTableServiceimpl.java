package com.ruoyi.business.k3.service.impl;

import com.ruoyi.business.entity.BymaterialDictionary;
import com.ruoyi.business.entity.Country;
import com.ruoyi.business.entity.DictionaryTable;
import com.ruoyi.business.k3.service.DictionaryTableService;
import com.ruoyi.business.mapper.BymaterialDictionaryMapper;
import com.ruoyi.business.mapper.DictionaryTableMapper;
import com.ruoyi.business.servicel.CountryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DictionaryTableServiceimpl implements DictionaryTableService {

    @Autowired
    private DictionaryTableMapper dictionaryTableMapper;
    
    @Autowired
    private BymaterialDictionaryMapper bymaterialDictionaryMapper;
    
    @Autowired
    private CountryService countryService;
    @Override
    public int insert(DictionaryTable dictionaryTable) {
       return dictionaryTableMapper.insert(dictionaryTable);

    }

    @Override
    public boolean update(DictionaryTable dictionaryTable) {
        return dictionaryTableMapper.update(dictionaryTable) > 0;

    }

    @Override
    public boolean deleteById(Long id) {
        return dictionaryTableMapper.deleteById(id) > 0;
    }

    @Override
    public int batchDelete(List<Long> ids) {
        return dictionaryTableMapper.batchDelete(ids);

    }

    @Override
    public DictionaryTable selectById(Long id) {
        return dictionaryTableMapper.selectById(id);

    }

    @Override
    public List<DictionaryTable> selectDictionaryTree() {
        return dictionaryTableMapper.selectAll();

    }

    @Override
    public List<DictionaryTable> selectByDictType(String dictType) { 
        // 特殊处理国家字典（从 country 表查询）
        if ("nation".equals(dictType)) {
            try {
                List<Country> countries = countryService.getnAtion();
                if (countries == null || countries.isEmpty()) {
                    return new java.util.ArrayList<>();
                }
                
                // 转换为 DictionaryTable 格式返回
                return countries.stream().map(country -> {
                    DictionaryTable dict = new DictionaryTable();
                    dict.setDictCode(country.getId());             // 使用 id 作为编码
                    dict.setDictName("nation");                     // category 作为分类名
                    dict.setRemark(country.getNameZh());            // 中文名作为 label
                    dict.setSortOrder(0);                           // 排序默认 0
                    dict.setStatus(1);                              // 状态默认 1
                    return dict;
                }).collect(Collectors.toList());
            } catch (Exception e) {
                log.error("查询国家字典失败：{}", e.getMessage(), e);
                return new java.util.ArrayList<>();
            }
        }
        
        // 从 bymaterial_dictionary 表查询 - 只使用 category 参数
        List<BymaterialDictionary> dictList = bymaterialDictionaryMapper.selectByCategory(dictType);
        
        log.info("查询字典 [{}], 找到 {} 条记录", dictType, dictList == null ? 0 : dictList.size());
        
        if (dictList == null || dictList.isEmpty()) {
            log.warn("字典 [{}] 没有找到数据，请检查 bymaterial_dictionary 表中是否存在 category = '{}' 的记录", dictType, dictType);
            return new java.util.ArrayList<>();
        }
        
        // 转换为 DictionaryTable 格式返回
        return dictList.stream().map(item -> {
            DictionaryTable dict = new DictionaryTable();
            dict.setDictCode(item.getKingdee());      // kingdee 作为编码
            dict.setDictName(item.getCategory());     // category 作为分类名
            dict.setRemark(item.getName());           // name 作为备注/说明
            dict.setSortOrder(0);                     // 排序默认 0
            dict.setStatus(1);                        // 状态默认 1
            return dict;
        }).collect(Collectors.toList());
    }
}
