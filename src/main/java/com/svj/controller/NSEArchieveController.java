package com.svj.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NSEArchieveController {
    @Autowired
    private Job job;
    @Autowired
    private JobLauncher jobLauncher;

    @PostMapping("/save-NSE-data")
    public ResponseEntity<?> saveData(){
        JobParameters jobParameters= new JobParametersBuilder()
                .addLong("startAt", System.currentTimeMillis()).toJobParameters();
        try {
            jobLauncher.run(job, jobParameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok("Data saved to DB successfully");
    }
}
