package com.project.contactsdemo.core.report;

import com.project.contactsdemo.contact.dto.ContactResponseDTO;
import net.sf.jasperreports.engine.JasperReport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Compiles the real template and renders it, without starting the application or a database. */
class ReportServiceTest {

    private static JasperReport report;
    private static final List<ContactResponseDTO> ROWS = List.of(contact(1L, "Mehmet Kaya", "+905321112233", 7L));

    @BeforeAll
    static void compileTemplate() {
        report = ReportService.compile("reports/sample-report.jrxml");
    }

    @Test
    void rendersXmlWithTheContactData() {
        String xml = new String(ReportService.render(report, ROWS, "xml"), StandardCharsets.UTF_8);
        assertThat(xml).contains("Mehmet Kaya").contains("+905321112233").contains("All Contacts in DB Report");
    }

    @Test
    void rendersXlsx() {
        byte[] xlsx = ReportService.render(report, ROWS, "xlsx");
        assertThat(xlsx).startsWith('P', 'K'); //an .xlsx file is a zip archive
    }

    @Test
    void rendersPdf() {
        byte[] pdf = ReportService.render(report, ROWS, "pdf");
        assertThat(new String(pdf, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }

    @Test
    void rejectsUnknownFormatWithBadRequest() {
        assertThatThrownBy(() -> ReportService.render(report, ROWS, "docx"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Unsupported report format");
    }

    private static ContactResponseDTO contact(Long id, String name, String phoneNumber, Long personId) {
        ContactResponseDTO dto = new ContactResponseDTO();
        dto.setId(id);
        dto.setName(name);
        dto.setPhoneNumber(phoneNumber);
        dto.setPersonId(personId);
        return dto;
    }
}
