package com.example.study.listener;

import org.springframework.batch.core.*;
import org.springframework.stereotype.Component;

@Component
public class CustomStepExecutionListener implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        String stepName = stepExecution.getStepName();
        System.out.println("stepName = " + stepName);
        stepExecution.getExecutionContext().put("name", "user1");
        System.out.println();
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {

        ExitStatus exitStatus = stepExecution.getExitStatus();
        System.out.println("exitStatus = " + exitStatus);

        BatchStatus status = stepExecution.getStatus();
        System.out.println("status = " + status);

        String name = (String)stepExecution.getExecutionContext().get("name");
        System.out.println("name = " + name);
        System.out.println();

        return ExitStatus.COMPLETED;
    }
}
