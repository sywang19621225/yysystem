package com.yysystem.modules.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yysystem.common.result.Result;
import com.yysystem.modules.system.entity.SysMessage;
import com.yysystem.modules.system.entity.SysNotice;
import com.yysystem.modules.system.service.SysMessageService;
import com.yysystem.modules.system.service.SysNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/system")
public class SysInfoController {

    @Autowired
    private SysNoticeService sysNoticeService;

    @Autowired
    private SysMessageService sysMessageService;

    @GetMapping("/message/unread")
    public Result<List<Map<String, Object>>> unreadMessages(@RequestParam(required = false, defaultValue = "10") Integer size) {
        // TODO: 从当前登录用户获取userId
        Long userId = getCurrentUserId();
        List<SysMessage> list = sysMessageService.getUnreadMessages(userId, size);
        return Result.success(convertMessageToMapList(list));
    }

    @PostMapping("/message/read/{id}")
    public Result<Boolean> readMessage(@PathVariable Long id) {
        boolean success = sysMessageService.markAsRead(id);
        return success ? Result.success(true) : Result.error("标记已读失败");
    }

    @PostMapping("/message/read-all")
    public Result<Boolean> readAllMessages() {
        // TODO: 从当前登录用户获取userId
        Long userId = getCurrentUserId();
        boolean success = sysMessageService.markAllAsRead(userId);
        return Result.success(success);
    }

    @GetMapping("/message/unread-count")
    public Result<Long> unreadMessageCount() {
        // TODO: 从当前登录用户获取userId
        Long userId = getCurrentUserId();
        Long count = sysMessageService.getUnreadCount(userId);
        return Result.success(count);
    }

    /**
     * 获取当前登录用户ID（临时实现，应从JWT或Session获取）
     */
    private Long getCurrentUserId() {
        // TODO: 实现从当前登录上下文获取用户ID
        // 临时返回1L，实际应从JWT token或SecurityContext获取
        return 1L;
    }

    @GetMapping("/notice/list")
    public Result<List<Map<String, Object>>> notices(@RequestParam(required = false, defaultValue = "1") Integer current,
                                                     @RequestParam(required = false, defaultValue = "10") Integer size) {
        // 查询已发布的通告
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNotice::getStatus, "PUBLISHED");
        wrapper.orderByDesc(SysNotice::getPublishTime);
        List<SysNotice> list = sysNoticeService.list(wrapper);
        return Result.success(convertToMapList(list));
    }

    @GetMapping("/notice/page")
    public Result<Map<String, Object>> noticePage(@RequestParam(required = false, defaultValue = "1") Integer current,
                                                  @RequestParam(required = false, defaultValue = "10") Integer size,
                                                  @RequestParam(required = false) String title,
                                                  @RequestParam(required = false) String status) {
        Page<SysNotice> page = new Page<>(current, size);
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        
        if (title != null && !title.isEmpty()) {
            wrapper.like(SysNotice::getTitle, title);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(SysNotice::getStatus, status);
        }
        wrapper.orderByDesc(SysNotice::getCreateTime);
        
        IPage<SysNotice> result = sysNoticeService.page(page, wrapper);
        
        Map<String, Object> map = new HashMap<>();
        map.put("records", convertToMapList(result.getRecords()));
        map.put("total", result.getTotal());
        map.put("current", result.getCurrent());
        map.put("size", result.getSize());
        return Result.success(map);
    }

    @PostMapping("/notice/create")
    public Result<Boolean> createNotice(@RequestBody SysNotice notice) {
        notice.setCreateTime(LocalDateTime.now());
        if ("PUBLISHED".equals(notice.getStatus())) {
            notice.setPublishTime(LocalDateTime.now());
        }
        // TODO: 设置发布人ID和名称（从当前登录用户获取）
        notice.setPublisherId(1L);
        notice.setPublisherName("管理员");
        boolean success = sysNoticeService.save(notice);
        return success ? Result.success(true) : Result.error("创建失败");
    }

    @PutMapping("/notice/update")
    public Result<Boolean> updateNotice(@RequestBody SysNotice notice) {
        if (notice.getId() == null) {
            return Result.error("通告ID不能为空");
        }
        if ("PUBLISHED".equals(notice.getStatus()) && notice.getPublishTime() == null) {
            notice.setPublishTime(LocalDateTime.now());
        }
        boolean success = sysNoticeService.updateById(notice);
        return success ? Result.success(true) : Result.error("更新失败");
    }

    @DeleteMapping("/notice/delete/{id}")
    public Result<Boolean> deleteNotice(@PathVariable Long id) {
        boolean success = sysNoticeService.removeById(id);
        return success ? Result.success(true) : Result.error("删除失败");
    }

    private List<Map<String, Object>> convertToMapList(List<SysNotice> list) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (SysNotice notice : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", notice.getId());
            map.put("title", notice.getTitle());
            map.put("content", notice.getContent());
            map.put("publisherId", notice.getPublisherId());
            map.put("publisherName", notice.getPublisherName());
            map.put("publishTime", notice.getPublishTime());
            map.put("status", notice.getStatus());
            map.put("createTime", notice.getCreateTime());
            result.add(map);
        }
        return result;
    }

    private List<Map<String, Object>> convertMessageToMapList(List<SysMessage> list) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (SysMessage message : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", message.getId());
            map.put("title", message.getTitle());
            map.put("content", message.getContent());
            map.put("type", message.getType());
            map.put("isRead", message.getIsRead());
            map.put("createTime", message.getCreateTime());
            map.put("readTime", message.getReadTime());
            result.add(map);
        }
        return result;
    }
}
