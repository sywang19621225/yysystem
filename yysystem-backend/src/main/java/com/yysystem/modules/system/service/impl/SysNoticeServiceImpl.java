package com.yysystem.modules.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yysystem.modules.system.entity.SysNotice;
import com.yysystem.modules.system.mapper.SysNoticeMapper;
import com.yysystem.modules.system.service.SysNoticeService;
import org.springframework.stereotype.Service;

@Service
public class SysNoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNotice> implements SysNoticeService {
}
