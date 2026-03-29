package com.gongxifacai.gongxifacai.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gongxifacai.gongxifacai.common.Result;
import com.gongxifacai.gongxifacai.dto.DemoCreateRequest;
import com.gongxifacai.gongxifacai.service.DemoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/demo")
@RequiredArgsConstructor
public class DemoController {
    private final DemoService demoService;
    @PostMapping("/create")
    public Result<String> createDemo(@RequestBody DemoCreateRequest request) {

        //直接假设成功
        return Result.success(demoService.createDemo(request));
    }
}
