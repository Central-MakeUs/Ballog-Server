package com.example.ballog.domain.alert.scheduler;

import com.example.ballog.domain.alert.firebase.FcmPushJob;
import com.example.ballog.domain.match.entity.Matches;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FcmSchedulerService { //JOB 예약 서비스

    private final Scheduler scheduler;

    public void scheduleAlertJob(Matches match, String alertType, LocalDateTime triggerTime, List<Long> userIds) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("matchId", match.getMatchesId());
        jobDataMap.put("alertType", alertType);
        jobDataMap.put("userIds", userIds); // 유저 ID 리스트

        JobDetail jobDetail = JobBuilder.newJob(FcmPushJob.class)
                .withIdentity(match.getMatchesId() + "_" + alertType, "fcm-jobs")
                .setJobData(jobDataMap)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(match.getMatchesId() + "_" + alertType + "_trigger", "fcm-triggers")
                .startAt(Timestamp.valueOf(triggerTime))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule())
                .build();

        try {
            if (scheduler.checkExists(jobDetail.getKey())) {
                scheduler.deleteJob(jobDetail.getKey());
            }
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public boolean isJobExists(Long matchId, String alertType) {
        try {
            return scheduler.checkExists(new JobKey(matchId + "_" + alertType, "fcm-jobs"));
        } catch (SchedulerException e) {
            e.printStackTrace();
            return false;
        }
    }


//    private final Scheduler scheduler;
//
//    public void scheduleAlertJob(User user, Matches match, String alertType, LocalDateTime triggerTime) {
//        JobDataMap jobDataMap = new JobDataMap();
//        jobDataMap.put("userId", user.getUserId());
//        jobDataMap.put("type", alertType);
//        jobDataMap.put("team", user.getBaseballTeam().name());
//
//        JobDetail jobDetail = JobBuilder.newJob(FcmPushJob.class)
//                .withIdentity(user.getUserId() + "_" + alertType + "_" + match.getMatchesId(), "fcm-jobs")
//                .setJobData(jobDataMap)
//                .build();
//
//        Trigger trigger = TriggerBuilder.newTrigger()
//                .withIdentity(user.getUserId() + "_" + alertType + "_trigger", "fcm-triggers")
//                .startAt(Timestamp.valueOf(triggerTime))
//                .withSchedule(SimpleScheduleBuilder.simpleSchedule())
//                .build();
//
//        try {
//            scheduler.scheduleJob(jobDetail, trigger);
//        } catch (SchedulerException e) {
//            e.printStackTrace();
//        }
//    }
}
