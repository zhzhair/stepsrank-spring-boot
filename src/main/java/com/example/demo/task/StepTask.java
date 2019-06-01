package com.example.demo.task;

import com.example.demo.common.config.aspect.log.annotation.LogForTask;
import com.example.demo.step.service.StepService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.stream.IntStream;

@Component
public class StepTask {
    @Resource
    private StepService stepService;

    @LogForTask
    @Scheduled(cron = "0/1 * * * * ?")
    public void uploadStep() {
        IntStream.range(0, 300).parallel().forEach(i -> stepService.uploadStep(32));
    }

    @LogForTask
    @Scheduled(cron = "0/1 * * * * ?")
    public void recordRankAll() {
        stepService.recordRankAll();
    }

    @LogForTask
    @Scheduled(cron = "0/10 * * * * ?")
    public void recordRank() {
        stepService.flushRankAll();
    }
}
