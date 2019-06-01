package com.example.demo.step.dto.response;

public class StepsRankAllResp {
    private Integer userId;
    private Integer rank;
    private Integer totalCount;

    @Override
    public String toString() {
        return "StepsRankAllResp{" +
                "userId=" + userId +
                ", rank=" + rank +
                ", totalCount=" + totalCount +
                '}';
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }
}
