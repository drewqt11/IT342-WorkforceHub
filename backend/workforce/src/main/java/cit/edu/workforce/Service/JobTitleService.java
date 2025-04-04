package cit.edu.workforce.Service;

import cit.edu.workforce.Entity.JobTitleEntity;
import cit.edu.workforce.Repository.JobTitleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class JobTitleService {

    private final JobTitleRepository jobTitleRepository;

    @Autowired
    public JobTitleService(JobTitleRepository jobTitleRepository) {
        this.jobTitleRepository = jobTitleRepository;
    }

    @Transactional(readOnly = true)
    public List<JobTitleEntity> getAllJobTitles() {
        return jobTitleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<JobTitleEntity> getJobTitleById(UUID jobId) {
        return jobTitleRepository.findById(jobId);
    }

    @Transactional(readOnly = true)
    public Optional<JobTitleEntity> getJobTitleByName(String jobName) {
        return jobTitleRepository.findByJobName(jobName);
    }

    @Transactional
    public JobTitleEntity createJobTitle(String jobName, String jobDescription, String payGrade) {
        JobTitleEntity jobTitle = new JobTitleEntity();
        jobTitle.setJobName(jobName);
        jobTitle.setJobDescription(jobDescription);
        jobTitle.setPayGrade(payGrade);
        return jobTitleRepository.save(jobTitle);
    }

    @Transactional
    public JobTitleEntity updateJobTitle(UUID jobId, String jobName, String jobDescription, String payGrade) {
        JobTitleEntity jobTitle = jobTitleRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job title not found"));

        jobTitle.setJobName(jobName);
        jobTitle.setJobDescription(jobDescription);
        jobTitle.setPayGrade(payGrade);
        return jobTitleRepository.save(jobTitle);
    }

    @Transactional
    public void deleteJobTitle(UUID jobId) {
        jobTitleRepository.deleteById(jobId);
    }
} 