package com.yysystem.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yysystem.modules.system.entity.SysMessage;
import com.yysystem.modules.system.mapper.SysMessageMapper;
import com.yysystem.modules.system.service.SysMessageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SysMessageServiceImpl extends ServiceImpl<SysMessageMapper, SysMessage> implements SysMessageService {

    @Override
    public List<SysMessage> getUnreadMessages(Long userId, Integer limit) {
        LambdaQueryWrapper<SysMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMessage::getUserId, userId);
        wrapper.eq(SysMessage::getIsRead, 0);
        wrapper.orderByDesc(SysMessage::getCreateTime);
        if (limit != null && limit > 0) {
            wrapper.last("LIMIT " + limit);
        }
        return list(wrapper);
    }

    @Override
    public boolean markAsRead(Long messageId) {
        LambdaUpdateWrapper<SysMessage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysMessage::getId, messageId);
        wrapper.set(SysMessage::getIsRead, 1);
        wrapper.set(SysMessage::getReadTime, LocalDateTime.now());
        return update(wrapper);
    }

    @Override
    @Transactional
    public boolean markAllAsRead(Long userId) {
        LambdaUpdateWrapper<SysMessage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysMessage::getUserId, userId);
        wrapper.eq(SysMessage::getIsRead, 0);
        wrapper.set(SysMessage::getIsRead, 1);
        wrapper.set(SysMessage::getReadTime, LocalDateTime.now());
        return update(wrapper);
    }

    @Override
    public boolean sendMessage(Long userId, String title, String content, String type) {
        SysMessage message = new SysMessage();
        message.setUserId(userId);
        message.setTitle(title);
        message.setContent(content);
        message.setType(type);
        message.setIsRead(0);
        message.setCreateTime(LocalDateTime.now());
        return save(message);
    }

    @Override
    @Transactional
    public boolean sendMessageToUsers(List<Long> userIds, String title, String content, String type) {
        if (userIds == null || userIds.isEmpty()) {
            return false;
        }
        List<SysMessage> messages = new ArrayList<>();
        for (Long userId : userIds) {
            SysMessage message = new SysMessage();
            message.setUserId(userId);
            message.setTitle(title);
            message.setContent(content);
            message.setType(type);
            message.setIsRead(0);
            message.setCreateTime(LocalDateTime.now());
            messages.add(message);
        }
        return saveBatch(messages);
    }

    @Override
    public Long getUnreadCount(Long userId) {
        LambdaQueryWrapper<SysMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMessage::getUserId, userId);
        wrapper.eq(SysMessage::getIsRead, 0);
        return count(wrapper);
    }
}
