package com.ruoyi.business.k3.service.impl;

import com.ruoyi.business.entity.DictionaryTable;
import com.ruoyi.business.k3.service.DictionaryTableService;
import com.ruoyi.business.mapper.DictionaryTableMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DictionaryTableServiceimpl implements DictionaryTableService {

    @Autowired
    private DictionaryTableMapper dictionaryTableMapper;
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
}
