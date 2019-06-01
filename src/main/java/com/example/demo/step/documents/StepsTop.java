package com.example.demo.step.documents;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "steps_top")
public class StepsTop {
    private Integer userId;
    private Integer totalCount;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }
}
