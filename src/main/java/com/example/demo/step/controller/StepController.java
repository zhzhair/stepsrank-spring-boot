package com.example.demo.step.controller;

import com.example.demo.common.controller.BaseController;
import com.example.demo.common.dto.BaseResponse;
import com.example.demo.step.dto.response.StepsRankAllResp;
import com.example.demo.step.service.StepService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("step")
@Api(description = "步步健康")
public class StepController extends BaseController {

    @Resource
    private StepService stepService;

    @ApiOperation(value = "查询当日总步数排名", notes = "查询当日总步数排名")
    @RequestMapping(value = "/getRankAll", method = {RequestMethod.GET}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public BaseResponse<List<StepsRankAllResp>> getRankAll(int begin, int pageSize) {
        BaseResponse<List<StepsRankAllResp>> baseResponse = new BaseResponse<>();
        List<StepsRankAllResp> list = stepService.getRankAllFromRedis(begin, pageSize);
        if (list.isEmpty()) list = stepService.getRankAll(begin, pageSize);
        baseResponse.setCode(0);
        baseResponse.setMsg("返回数据成功");
        baseResponse.setData(list);
        return baseResponse;
    }

    @ApiOperation(value = "新建表", notes = "新建表")
    @RequestMapping(value = "/createTables", method = {RequestMethod.POST}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public BaseResponse<Object> createTables() {
        BaseResponse<Object> baseResponse = new BaseResponse<>();
        stepService.createTables(32);
        baseResponse.setCode(0);
        baseResponse.setMsg("新建表成功");
        return baseResponse;
    }
}
