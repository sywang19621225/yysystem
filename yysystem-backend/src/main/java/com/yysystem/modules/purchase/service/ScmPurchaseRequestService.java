package com.yysystem.modules.purchase.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yysystem.modules.purchase.entity.ScmPurchaseRequest;

public interface ScmPurchaseRequestService extends IService<ScmPurchaseRequest> {
    boolean saveWithItems(ScmPurchaseRequest req);
    boolean updateWithItems(ScmPurchaseRequest req);
}
