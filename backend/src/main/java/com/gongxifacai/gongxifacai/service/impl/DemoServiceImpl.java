package com.gongxifacai.gongxifacai.service.impl;

import com.gongxifacai.gongxifacai.dto.DemoCreateRequest;
import com.gongxifacai.gongxifacai.service.DemoService;

import org.springframework.stereotype.Service;

@Service
public class DemoServiceImpl implements DemoService {
    @Override
    public String createDemo(DemoCreateRequest request) {
        if("创建失败，比如非法".equals(request.getName())){
            throw new IllegalArgumentException("直接丢出异常，不要catch");
        }
        return "创建成功";
       }
}
