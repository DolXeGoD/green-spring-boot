package com.green_computer.green_board.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Service
@Slf4j
public class DiscordNotificationService {
    private final String webhookUrl;

    public DiscordNotificationService(@Value("${DISCORD_WEBHOOK_URL:}") String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public void sendReportAlert() {
        if (webhookUrl.isBlank()) {
            log.warn("DISCORD_WEBHOOK_URL이 설정되지 않아 신고 알림을 보내지 못했습니다.");
            return;
        }

        try {
            RestClient.create().post()
                    .uri(webhookUrl)
                    .body(Map.of("content", "새 신고가 접수되었습니다. 관리자 화면에서 확인해주세요."))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            log.error("신고 알림을 보내지 못했습니다.", exception);
        }
    }
}
