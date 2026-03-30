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
}
