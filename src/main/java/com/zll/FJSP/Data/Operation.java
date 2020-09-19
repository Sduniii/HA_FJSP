package com.zll.FJSP.Data;

import java.io.Serializable;

/**
 * Description:
 *
 * @author zll-hust E-mail:zh20010728@126.com
 * @date 创建时间：2020年5月30日 下午6:22:38
 */
public class Operation implements Serializable {
    private static final long serialVersionUID = -4634848697079561765L;
    public int jobNo;
    public int task;
    public int startTime;
    public int endTime;
    public int aStartTime;
    public int machineNo;
    public int span;
    public int id;
    public String sId;

    public Operation() {
        jobNo = -1;
        task = -1;
        startTime = -1;
        endTime = -1;
        aStartTime = -1;
        machineNo = -1;
    }

    public Operation(int id, int span, int machine, int job) {
        this.id = id;
        this.span = span;
        this.machineNo = machine;
        this.jobNo = job;
    }

    public Operation(int id, int machine, int job, int start, int end, int task) {
        this.id = id;
        this.sId = "J" + job + "T" + task;
        this.span = end - start;
        this.machineNo = machine;
        this.jobNo = job;
        this.task = task;
        this.startTime = start;
        this.endTime = end;
    }

    public Operation(Operation o) {
        this.sId = o.sId;
        this.id = o.id;
        this.span = o.span;
        this.jobNo = o.jobNo;
        this.task = o.task;
        this.startTime = o.startTime;
        this.endTime = o.endTime;
        this.aStartTime = o.aStartTime;
        this.machineNo = o.machineNo;
    }

    public void initOperation() {
        jobNo = -1;
        task = -1;
        startTime = -1;
        endTime = -1;
        aStartTime = -1;
        machineNo = -1;
    }

    @Override
    public String toString() {
        return "Operation{" +
                "jobNo=" + jobNo +
                ", task=" + task +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", aStartTime=" + aStartTime +
                ", machineNo=" + machineNo +
                ", span=" + span +
                ", id=" + id +
                ", sId='" + sId + "'" +
                '}';
    }

    public boolean equals(Operation op) {
        return this.jobNo == op.jobNo && this.task == op.task;
    }
}
