package com.project.contactsdemo.core.report;


import com.project.contactsdemo.contact.dto.ContactResponseDTO;
import com.project.contactsdemo.contact.mapper.ContactMapper;
import com.project.contactsdemo.contact.repository.ContactRepository;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {
    private static final String TEMPLATE = "reports/sample-report.jrxml";

    //Note: The most important part for the db connection
    private final ContactRepository contactRepository;
    private final ContactMapper contactMapper;
    // Compiling a .jrxml is slow, so it is done once at startup instead of on every request.
    // A broken template also fails the startup instead of the first report request.
    private final JasperReport contactReport;

    public ReportService(ContactRepository contactRepository, ContactMapper contactMapper) {
        this.contactRepository = contactRepository;
        this.contactMapper = contactMapper;
        this.contactReport = compile(TEMPLATE);
    }

    /**
     * Reads the template as a stream: inside a packaged jar a classpath resource is an entry in a
     * zip file, not a file on disk, so File-based access (ResourceUtils.getFile) would fail there.
     */
    static JasperReport compile(String classpathLocation) {
        try (InputStream template = new ClassPathResource(classpathLocation).getInputStream()) {
            return JasperCompileManager.compileReport(template);
        } catch (IOException e) {
            throw new UncheckedIOException("Report template not found: " + classpathLocation, e);
        } catch (JRException e) {
            throw new IllegalStateException("Report template could not be compiled: " + classpathLocation, e);
        }
    }

    /**
     * @param format pdf, xml or xlsx
     * @return the report file content
     */
    public byte[] getItemReport(String format) {
        List<ContactResponseDTO> contactResponseDTOList = contactRepository.findAll().stream()
                .map(contactMapper::fromContactEntityToContactResponseDTO)
                .toList();
        return render(contactReport, contactResponseDTOList, format);
    }

    static byte[] render(JasperReport report, List<ContactResponseDTO> rows, String format) {
        //Set report data
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("title", "All Contacts in DB Report");

        try {
            //Fill report
            JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, dataSource);
            return switch (format) {
                case "pdf" -> JasperExportManager.exportReportToPdf(jasperPrint);
                case "xml" -> JasperExportManager.exportReportToXml(jasperPrint).getBytes(StandardCharsets.UTF_8);
                case "xlsx" -> exportToXlsx(jasperPrint);
                default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Unsupported report format '" + format + "'. Use pdf, xml or xlsx.");
            };
        } catch (JRException e) {
            // Previously swallowed, which returned null and failed later with a confusing error.
            throw new IllegalStateException("Report generation failed for format " + format, e);
        }
    }

    private static byte[] exportToXlsx(JasperPrint jasperPrint) throws JRException {
        ByteArrayOutputStream xlsxOutput = new ByteArrayOutputStream();
        JRXlsxExporter exporter = new JRXlsxExporter();

        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(xlsxOutput));

        SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
        configuration.setDetectCellType(true);
        configuration.setCollapseRowSpan(false); // Optional
        configuration.setOnePagePerSheet(false); // Optional
        configuration.setWhitePageBackground(false); // Optional
        configuration.setRemoveEmptySpaceBetweenRows(true); // Optional

        exporter.setConfiguration(configuration);
        exporter.exportReport();

        return xlsxOutput.toByteArray();
    }
}
