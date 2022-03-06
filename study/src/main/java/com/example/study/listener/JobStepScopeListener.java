package com.example.study.listener;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

public class JobStepScopeListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        jobExecution.getExecutionContext().putString("name", "user1");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {

    }
}
