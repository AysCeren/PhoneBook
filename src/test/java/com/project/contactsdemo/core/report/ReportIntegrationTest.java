package com.project.contactsdemo.core.report;

import com.project.contactsdemo.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class ReportIntegrationTest extends IntegrationTest {

    @Test
    void downloadsTheContactReportAsAnAttachment() {
        ResponseEntity<byte[]> response = restTemplate.getForEntity("/api/contactReport/pdf", byte[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)).contains("item-report.pdf");
        assertThat(new String(response.getBody(), 0, 5)).isEqualTo("%PDF-");
    }

    @Test
    void rejectsUnsupportedFormats() {
        assertThat(get("/api/contactReport/docx").getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
