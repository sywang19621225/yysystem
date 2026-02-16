package com.yysystem.modules.purchase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yysystem.modules.purchase.entity.ScmPurchaseContract;
import com.yysystem.modules.purchase.entity.ScmPurchaseContractDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ScmPurchaseContractMapper extends BaseMapper<ScmPurchaseContract> {
    
    @Select("SELECT * FROM scm_purchase_contract_detail WHERE contract_id = #{contractId}")
    List<ScmPurchaseContractDetail> selectDetailList(Long contractId);
}

