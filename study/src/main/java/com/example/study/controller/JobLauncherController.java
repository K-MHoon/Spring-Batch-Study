package com.example.study.controller;

import com.example.study.dto.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequiredArgsConstructor
public class JobLauncherController {

    // TODO JobLauncher 충돌로 임시 컨트롤러 중단
//    private final Job job;
//    private final JobLauncher jobLauncher;
//    private final BasicBatchConfigurer basicBatchConfigurer;

    @PostMapping("/batch")
    public String launch(@RequestBody Member member) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {

        JobParameters jobParameters = new JobParametersBuilder().addString("id", member.getId())
                .addDate("date", new Date())
                .toJobParameters();

        // JobLauncher 비동기 실행
        // SimpleJobLauncher(proxy) 는 직접 DI로 받을 수 없음. BasicBatchConfigurer가 실제 객체를 가지고 있음.
        /*

        SimpleJobLauncher jobLauncher = (SimpleJobLauncher) basicBatchConfigurer.getJobLauncher();
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());

        jobLauncher.run(job, jobParameters);

         */

        return "batch completed";
    }
}
