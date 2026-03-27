package com.ruoyi.business.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.business.entity.AiLlmModer;
import com.ruoyi.business.entity.Bymaterial;
import com.ruoyi.business.entity.BymaterialDictionary;
import com.ruoyi.business.k3.domain.vo.SupplierVo;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface BymaterialDictionaryMapper extends BaseMapper<BymaterialDictionary> {

    /**
     * 根据分类和名称查询字典项是否存在
     * @param category 分类
     * @param name 名称
     * @return 记录数
     */
    @Select("SELECT COUNT(*) FROM bymaterial_dictionary WHERE category = #{category} AND name = #{name}")
    int countByCategoryAndName(@Param("category") String category, @Param("name") String name);

    @Select({
        "<script>",
        "SELECT kingdee, name FROM bymaterial_dictionary",
        "WHERE 1=0",
        "<if test='keys != null and keys.size() > 0'>",
        "   OR kingdee IN",
        "   <foreach collection='keys' item='item' open='(' separator=',' close=')'>",
        "       #{item}",
        "   </foreach>",
        "</if>",
        "</script>"
    })
    List<Map<String, Object>> selectByKeys(@Param("keys") List<String> keys);

    List<BymaterialDictionary> selectByKingdees(@Param("codes") List<String> codes);


    /**
     * 根据分类和名称查询字典项
     * @param kingdee 名称
     */
    @Select("SELECT * FROM bymaterial_dictionary WHERE  kingdee = #{kingdee}")
    BymaterialDictionary selectByCategoryAndName( @Param("kingdee") String kingdee);

    /**
     * 根据分类和名称查询字典项
     * @param bymaterialDictionary 名称
     */
    BymaterialDictionary selectByCategoryAndNames(@Param("bymaterialDictionary") BymaterialDictionary bymaterialDictionary);

    BymaterialDictionary selectByName(@Param("bymaterialDictionary") BymaterialDictionary bymaterialDictionary);

    /**
     * 根据分类和名称查询字典项
     * @param erpName 名称
     */
    @Select("SELECT * FROM bymaterial_dictionary WHERE  name = #{erpName}")
    BymaterialDictionary selectByCategoryerpName( @Param("erpName") String erpName);

    /**
     * 根据分类和名称查询字典项
     * @param productName 名称
     */
    @Select("SELECT * FROM bymaterial_dictionary WHERE  name = #{productName}")
    BymaterialDictionary selectByCategoryproductName( @Param("productName") String productName);

    /**
     * 根据分类和名称查询字典项
     * @param fxlcpName 名称
     */
    @Select("SELECT * FROM bymaterial_dictionary WHERE  name = #{fxlcpName}")
    BymaterialDictionary selectByCategoryfxlcpName( @Param("fxlcpName") String fxlcpName);

    /**
     * 根据编码查询和分类名称查询
     * */
    @Select("SELECT * FROM bymaterial_dictionary WHERE category = #{category} AND code = #{code}")
    BymaterialDictionary selectByCategoryAndCode(@Param("category") String category, @Param("code") String code);


    @Select("select * from tb_ai_llm_moder where title = #{title}")
    AiLlmModer selectByTitle(@Param("title") String title);


    String selectBymaterialDictionary(String kingdee);

    @Select(" SELECT id,code,kingdee,name,category_name FROM bymaterial_dictionary WHERE category_name = #{categoryName} and category = #{category}")
    List<BymaterialDictionary> selectCategoryName(@Param("categoryName") String categoryName, @Param("category") String category);

    /**
     * 通code获取来源名称
     * */
    @Select(" SELECT id,code,kingdee,name,category_name FROM bymaterial_dictionary WHERE code = #{code}")
    BymaterialDictionary selectCategoryCode(@Param("code") String code);

    @Select(" SELECT id,code,kingdee,name,category_name FROM bymaterial_dictionary WHERE id = #{id}")
    BymaterialDictionary selectCategoryId(@Param("id") Long id);

    @Select(" SELECT id,code,kingdee,name,category_name FROM bymaterial_dictionary WHERE kingdee = #{kingdee} and category = #{category} ")
    BymaterialDictionary selectCategoryIds(@Param("kingdee") String kingdee,@Param("category") String category);

    @Select(" SELECT id,code,kingdee,name,category_name FROM bymaterial_dictionary WHERE kingdee = #{kingdee}")
    BymaterialDictionary selectByKingdee(@Param("kingdee") String kingdee);

    @Select(" SELECT id,code,kingdee,name,category_name FROM bymaterial_dictionary WHERE kingdee = #{kingdee}")
    BymaterialDictionary selectByKingds(@Param("kingdee") String kingdee);

    @Select(" SELECT id,code,kingdee,name,category_name FROM bymaterial_dictionary WHERE kingdee = #{kingdee}")
    BymaterialDictionary selectByKingdes(@Param("kingdee") String kingdee);

    /** 结算币别 */
    @Select(" SELECT id,code,kingdee,name,category_name FROM bymaterial_dictionary WHERE kingdee = #{kingdee} and category = #{category}")
    BymaterialDictionary selectByKingde(@Param("kingdee") String kingdee,@Param("category") String category);

    @Select(" SELECT id,code,kingdee,name,category_name FROM bymaterial_dictionary WHERE kingdee = #{kingdee} and category = #{category}")
    BymaterialDictionary selectByKingd(@Param("kingdee") String kingdee,@Param("category") String category);

    @Select(" SELECT id,code,kingdee,name,category_name FROM bymaterial_dictionary WHERE name = #{name} and category = #{category} ")
    BymaterialDictionary selectByKingdeeCategory(@Param("name") String name, @Param("category") String category);

    /**
     * 供应商
     * */
    @Select(" SELECT id as FSupplierID, name FROM supplier")
    List<SupplierVo> listSuppliers();

    /**
     * 物料
     * */
    @Select(" SELECT number as FMaterialId,name as FMaterialName FROM by_material")
    List<BymaterialDictionary> selectmaterial();
}
