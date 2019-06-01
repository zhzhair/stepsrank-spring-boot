package com.example.demo.step.service.impl;

import com.example.demo.step.dao.mapper.CommonMapper;
import com.example.demo.step.documents.*;
import com.example.demo.step.dto.response.StepsRankAllResp;
import com.example.demo.step.service.StepService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
public class StepServiceImpl implements StepService {
    @Resource
    private CommonMapper commonMapper;
    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource(name = "listStepsTopRedisTemplate")
    private RedisTemplate<String, StepsTop> redisListTemplate;
    private final String prefix = "steps_record_";
    private final String redisKey = "tail_step";
    private final String redisQueueKey = "top_step_list";

    @Override
    public void createTables(int tableCount) {
        IntStream.range(0, tableCount).parallel().forEach(this::createOneTable);
    }

    private void createOneTable(int i) {
        commonMapper.dropTableIfExists(prefix + i);
        commonMapper.createTable(prefix + i);
    }

    @Override
    public void uploadStep(int tableCount) {
        int userId = new Random().nextInt(500_0000);
        int stepCount = 1 + new Random().nextInt(5000);
        Integer count = commonMapper.getStepCount(prefix + userId % tableCount, userId);
        if (count != null) {
            commonMapper.updateSteps(prefix + userId % tableCount, userId, count + stepCount);
        } else {
            commonMapper.insertTables(prefix + userId % tableCount, userId, stepCount);
        }
        String tailSteps = redisTemplate.opsForValue().get(redisKey);
        int totalCount = count == null ? stepCount : count + stepCount;
        if (tailSteps != null && totalCount > Integer.valueOf(tailSteps)) {
            Query query = new Query(Criteria.where("userId").is(userId));
            if (!mongoTemplate.exists(query, StepsTop.class)) {
                StepsTop stepsTop = new StepsTop();
                stepsTop.setUserId(userId);
                stepsTop.setTotalCount(totalCount);
                mongoTemplate.insert(stepsTop);
            } else {
                System.err.println("update: " + tailSteps + ", userId: " + userId);
                Update update = new Update();
                update.set("totalStep", totalCount);
                mongoTemplate.upsert(query, update, StepsTop.class);
            }
        } else {
            StepsTop stepsTop = new StepsTop();
            stepsTop.setUserId(userId);
            stepsTop.setTotalCount(stepCount);
            mongoTemplate.insert(stepsTop);
        }
    }

    @Override
    public void recordTopAll(int tableCount) {
        mongoTemplate.dropCollection(StepsTop.class);
        IntStream.range(0, tableCount).parallel().forEach(this::insertOneTable);
        Query query = new Query().with(new Sort(Sort.Direction.DESC, "totalCount")).limit(200);
        List<StepsTop> list = mongoTemplate.find(query, StepsTop.class);
        if (list.isEmpty()) return;
        mongoTemplate.dropCollection(StepsTop.class);
        mongoTemplate.insertAll(list);
        int size = Math.min(200, list.size());
        if(list.size() < 200){
            redisTemplate.opsForValue().set(redisKey, "0");
        }else{
            redisTemplate.opsForValue().set(redisKey, String.valueOf(list.get(size - 1).getTotalCount()));
        }
    }

    private void insertOneTable(int i) {
        List<StepsTop> list = commonMapper.getTopStep(prefix + i);
        mongoTemplate.insertAll(list);
    }

    @Override
    public void flushRankAll() {
//        Query query = new Query().with(new Sort(Sort.Direction.DESC,"totalCount")).limit(201);
//        List<StepsTop> list = mongoTemplate.find(query,StepsTop.class);//高并发场景下容易出现内存不足异常：out of memory Exception
        TypedAggregation<StepsTop> aggregation = Aggregation.newAggregation(
                StepsTop.class,
                project("userId", "totalCount"),//查询用到的字段
                sort(Sort.Direction.DESC, "totalCount"),
                limit(210)
        ).withOptions(newAggregationOptions().allowDiskUse(true).build());//内存不足到磁盘读写，应对高并发
        AggregationResults<StepsTop> results = mongoTemplate.aggregate(aggregation, StepsTop.class, StepsTop.class);
        List<StepsTop> list = results.getMappedResults();
        if (list.size() >= 205) {
            int totalCount = list.get(199).getTotalCount();
            Query query1 = new Query(Criteria.where("totalCount").lt(totalCount));
            mongoTemplate.remove(query1, StepsTop.class);
        }
    }

    @Override
    public void recordRankAll() {
//        Query query = new Query().with(new Sort(Sort.Direction.DESC,"totalCount")).limit(200);
//        List<StepsTop> list = mongoTemplate.find(query,StepsTop.class);
        TypedAggregation<StepsTop> aggregation = Aggregation.newAggregation(
                StepsTop.class,
                project("userId", "totalCount"),//查询用到的字段
                sort(Sort.Direction.DESC, "totalCount"),
                limit(200)
        ).withOptions(newAggregationOptions().allowDiskUse(true).build());//内存不足到磁盘读写，应对高并发
        AggregationResults<StepsTop> results = mongoTemplate.aggregate(aggregation, StepsTop.class, StepsTop.class);
        List<StepsTop> list = results.getMappedResults();
        if (list.size() == 200) {
            Integer stepCount = list.get(199).getTotalCount();
            redisTemplate.opsForValue().set(redisKey, String.valueOf(stepCount));
        }
        if (!list.isEmpty()) {
            redisListTemplate.delete(redisQueueKey);
            //noinspection unchecked
            redisListTemplate.opsForList().rightPushAll(redisQueueKey, list);
        }
    }

    @Override//todo 从mongodb查询
    public List<StepsRankAllResp> getRankAll(int begin, int pageSize) {
        List<StepsTop> list = this.getStepsTop(begin, pageSize);
        //轮询10次
        for (int i = 0; i < 10; i++) {
            if (!list.isEmpty()) {
                break;
            } else {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                list = this.getStepsTop(begin, pageSize);
            }
        }
        List<StepsRankAllResp> stepsRankAllResps = new ArrayList<>(pageSize);
        for (int i = 0; i < list.size(); i++) {
            StepsTop step = list.get(i);
            StepsRankAllResp stepsRankAllResp = new StepsRankAllResp();
            BeanUtils.copyProperties(step, stepsRankAllResp);
            stepsRankAllResp.setRank(begin + i + 1);
            stepsRankAllResps.add(stepsRankAllResp);
        }
        return stepsRankAllResps;
    }

    private List<StepsTop> getStepsTop(int begin, int pageSize) {
        TypedAggregation<StepsTop> aggregation = Aggregation.newAggregation(
                StepsTop.class,
                project("userId", "totalCount"),//查询用到的字段
                sort(Sort.Direction.DESC, "totalCount"),
                skip((long) begin),
                limit(pageSize)
        ).withOptions(newAggregationOptions().allowDiskUse(true).build());//内存不足到磁盘读写，应对高并发
        AggregationResults<StepsTop> results = mongoTemplate.aggregate(aggregation, StepsTop.class, StepsTop.class);
        return results.getMappedResults();
    }

    @Override//todo 从redis读取
    public List<StepsRankAllResp> getRankAllFromRedis(int begin, int pageSize) {
        List<StepsTop> stepsList = redisListTemplate.opsForList().range(redisQueueKey, begin, pageSize);
        List<StepsRankAllResp> list = new ArrayList<>(stepsList.size());
        for (int i = 0; i < stepsList.size(); i++) {
            StepsRankAllResp stepsRankAllResp = new StepsRankAllResp();
            StepsTop stepsTop = stepsList.get(i);
            BeanUtils.copyProperties(stepsTop, stepsRankAllResp);
            stepsRankAllResp.setRank(begin + i + 1);
            list.add(stepsRankAllResp);
        }
        return list;
    }

}
