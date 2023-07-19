package com.yoursummoner.api.model.match;

public class MatchRequest {

    private Long startTime;

    private Long endTime;

    private Integer queue;

    private String type;

    private Integer start;

    private Integer count;

    public MatchRequest(Long startTime, Long endTime, Integer queue, String type, Integer start, Integer count) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.queue = queue;
        this.type = type;
        this.start = start;
        this.count = count;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Integer getQueue() {
        return queue;
    }

    public void setQueue(Integer queue) {
        this.queue = queue;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
