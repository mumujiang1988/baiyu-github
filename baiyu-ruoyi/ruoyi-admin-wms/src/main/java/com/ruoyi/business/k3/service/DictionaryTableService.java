package com.ruoyi.business.k3.service;


import com.ruoyi.business.entity.DictionaryTable;

import java.util.List;

public interface DictionaryTableService {
    int insert(DictionaryTable dictionaryTable);
    boolean update(DictionaryTable dictionaryTable);
    boolean deleteById(Long id);
    int batchDelete(List<Long> ids);
    DictionaryTable selectById(Long id);
    List<DictionaryTable> selectDictionaryTree();
    
    /**
     * 根据字典类型查询字典项列表 (从 bymaterial_dictionary 表)
     * @param dictType 字典类型 (如：currency, payment_clause 等)
     * @return 字典项列表
     */
    List<DictionaryTable> selectByDictType(String dictType);
}
