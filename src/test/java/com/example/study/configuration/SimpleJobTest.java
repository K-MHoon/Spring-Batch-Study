package com.example.study.configuration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBatchTest
@SpringBootTest(classes = {SimpleTestJobConfiguration.class, TestBatchConfig.class})
class SimpleJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private JobParameters jobParameters;

    @BeforeEach
    public void beforeEach() {
        jobParameters = new JobParametersBuilder()
                .addString("name", "user1")
                .addLong("date", new Date().getTime())
                .toJobParameters();
    }


    @Test
    @DisplayName("Job 상태, 종료상태 여부 테스트")
    public void simpleJobTest() throws Exception {
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
    }

    @Test
    @DisplayName("Step 커밋 개수, Read, Write 개수 확인")
    void simpleStepTest() {
        List<StepExecution> stepExecutions = (List<StepExecution>) jobLauncherTestUtils.launchStep("simpleTestJobStep").getStepExecutions();
        StepExecution stepExecution = stepExecutions.get(0);

        assertThat(stepExecution.getCommitCount()).isEqualTo(21);
        assertThat(stepExecution.getReadCount()).isEqualTo(100);
        assertThat(stepExecution.getWriteCount()).isEqualTo(100);
    }


    @AfterEach
    public void clear() {
        jobParameters = null;
        jdbcTemplate.execute("delete from customer2");
    }
}