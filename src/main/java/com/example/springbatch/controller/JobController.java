package com.example.springbatch.controller;

import com.example.springbatch.dto.JobInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.SimpleJob;
import org.springframework.batch.core.launch.*;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Iterator;
import java.util.Set;

@RestController
@RequiredArgsConstructor
public class JobController {

    private final JobRegistry jobRegistry;
    private final JobExplorer jobExplorer;
    private final JobOperator jobOperator;

    @PostMapping(value = "/batch/start")
    public String start(@RequestBody JobInfo jobInfo) throws NoSuchJobException, JobInstanceAlreadyExistsException, JobParametersInvalidException {

        for (Iterator<String> iterator = jobRegistry.getJobNames().iterator();
             iterator.hasNext();) {
            Job job = jobRegistry.getJob(iterator.next());
            System.out.println("job.getName() = " + job.getName());
            if(job.getName().equals("operationJob")) {
                jobOperator.start(job.getName(), "id=" + jobInfo.getId());
                break;
            }
        }

        return "batch has started";
    }

    @PostMapping(value = "/batch/stop")
    public String stop() throws NoSuchJobException, JobInstanceAlreadyExistsException, JobParametersInvalidException, NoSuchJobExecutionException, JobExecutionNotRunningException {

        for (Iterator<String> iterator = jobRegistry.getJobNames().iterator();
             iterator.hasNext();) {
            Job job = jobRegistry.getJob(iterator.next());
            System.out.println("job.getName() = " + job.getName());

            if(job.getName().equals("operationJob")) {
                Set<JobExecution> runningJobExecutions = jobExplorer.findRunningJobExecutions(job.getName());
                JobExecution jobExecution = runningJobExecutions.iterator().next();
                jobOperator.stop(jobExecution.getId());
                break;
            }
        }

        return "batch has stopped";
    }

    @PostMapping(value = "/batch/restart")
    public String restart() throws NoSuchJobException, JobParametersInvalidException, NoSuchJobExecutionException, JobInstanceAlreadyCompleteException, JobRestartException {

        for (Iterator<String> iterator = jobRegistry.getJobNames().iterator();
             iterator.hasNext();) {
            Job job = jobRegistry.getJob(iterator.next());
            System.out.println("job.getName() = " + job.getName());

            if(job.getName().equals("operationJob")) {
                JobInstance lastJobInstance = jobExplorer.getLastJobInstance(job.getName());
                JobExecution lastJobExecution = jobExplorer.getLastJobExecution(lastJobInstance);
                jobOperator.restart(lastJobExecution.getId());
                break;
            }

        }

        return "batch has restarted";
    }
}
