package com.chainpass.task;

import com.chainpass.vc.mapper.VCRecordMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * VC定时任务
 *
 * 定期检查并更新过期凭证状态
 */
@Component
@RequiredArgsConstructor
public class VCScheduledTask {

    private static final Logger log = LoggerFactory.getLogger(VCScheduledTask.class);

    private final VCRecordMapper vcRecordMapper;

    /**
     * 每小时执行一次过期VC状态更新
     * 将过期的有效VC状态更新为过期(1)
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void updateExpiredVCStatus() {
        log.info("Starting VC expiration check task...");

        try {
            int updated = vcRecordMapper.updateExpiredStatus();
            if (updated > 0) {
                log.info("Updated {} expired VC records", updated);
            } else {
                log.debug("No expired VC records found");
            }
        } catch (Exception e) {
            log.error("Failed to update expired VC status", e);
        }
    }
}