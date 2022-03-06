package com.example.study.listener;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.annotation.AfterJob;
import org.springframework.batch.core.annotation.BeforeJob;

public class CustomAnnotationJobExecutionListener {

    @BeforeJob
    public void bJob(JobExecution jobExecution) {
        System.out.println("[Annotation] Job is started");
        System.out.println("jobName : " + jobExecution.getJobInstance().getJobName());

    }

    @AfterJob
    public void aJob(JobExecution jobExecution) {
        long startTime = jobExecution.getStartTime().getTime();
        long endTime = jobExecution.getEndTime().getTime();

        System.out.println("[Annotation] 총 소요시간 : " + (endTime - startTime));
    }
}
