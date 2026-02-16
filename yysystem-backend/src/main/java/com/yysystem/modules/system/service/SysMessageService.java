package com.yysystem.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yysystem.modules.system.entity.SysMessage;
import java.util.List;

public interface SysMessageService extends IService<SysMessage> {

    /**
     * 获取用户未读消息列表
     */
    List<SysMessage> getUnreadMessages(Long userId, Integer limit);

    /**
     * 标记消息为已读
     */
    boolean markAsRead(Long messageId);

    /**
     * 标记用户所有消息为已读
     */
    boolean markAllAsRead(Long userId);

    /**
     * 发送消息给指定用户
     */
    boolean sendMessage(Long userId, String title, String content, String type);

    /**
     * 发送消息给多个用户
     */
    boolean sendMessageToUsers(List<Long> userIds, String title, String content, String type);

    /**
     * 获取用户未读消息数量
     */
    Long getUnreadCount(Long userId);
}
