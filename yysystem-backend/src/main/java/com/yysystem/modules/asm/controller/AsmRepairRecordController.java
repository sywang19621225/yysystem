package com.yysystem.modules.asm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yysystem.common.result.Result;
import com.yysystem.modules.asm.entity.AsmRepairRecord;
import com.yysystem.modules.asm.service.AsmRepairRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/asm/repair-record")
public class AsmRepairRecordController {

    @Autowired
    private AsmRepairRecordService asmRepairRecordService;

    @GetMapping("/list")
    public Result<List<AsmRepairRecord>> list(@RequestParam Long workOrderId) {
        return Result.success(asmRepairRecordService.list(
                new LambdaQueryWrapper<AsmRepairRecord>().eq(AsmRepairRecord::getWorkOrderId, workOrderId)
        ));
    }

    @PostMapping
    public Result<String> create(@RequestBody AsmRepairRecord record) {
        asmRepairRecordService.addRepairRecord(record);
        return Result.success("添加成功");
    }

    @PutMapping
    public Result<String> update(@RequestBody AsmRepairRecord record) {
        asmRepairRecordService.updateById(record);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        asmRepairRecordService.removeById(id);
        return Result.success("删除成功");
    }
}
