package com.example.study.tasklet;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.util.List;

public class ExploringTasklet implements Tasklet {

    private JobExplorer jobExplorer;

    public ExploringTasklet(JobExplorer jobExplorer) {
        this.jobExplorer = jobExplorer;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        String jobName = chunkContext.getStepContext().getJobName();

        List<JobInstance> jobInstances = jobExplorer.getJobInstances(jobName, 0, Integer.MAX_VALUE);
        System.out.println(String.format("There are %d job instances for the job %s", jobInstances.size(), jobName));

        for (JobInstance jobInstance : jobInstances) {
            List<JobExecution> jobExecutions = jobExplorer.getJobExecutions(jobInstance);
            System.out.println(String.format("Instance %d had %d executions",
                    jobInstance.getInstanceId(), jobExecutions.size()));

            for (JobExecution jobExecution : jobExecutions) {
                System.out.println(
                        String.format("Execution %d resulted in ExitStatus %s"
                        , jobExecution.getId()
                        , jobExecution.getExitStatus()));
            }
        }

        return RepeatStatus.FINISHED;
    }
}
