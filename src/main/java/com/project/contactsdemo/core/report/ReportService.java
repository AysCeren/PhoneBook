package com.project.contactsdemo.core.report;


import com.project.contactsdemo.contact.dto.ContactResponseDTO;
import com.project.contactsdemo.contact.mapper.ContactMapper;
import com.project.contactsdemo.contact.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {
    //Note: The most important part for the db connection
    private final ContactRepository contactRepository;
    private final ContactMapper contactMapper;

    /**
     *
     * @param format
     * @return
     * @throws FileNotFoundException
     * @throws JRException
     */
    // Note: Method Signatures
    public byte[] getItemReport(String format) throws FileNotFoundException, JRException {
        File file = ResourceUtils.getFile("classpath:reports/sample-report.jrxml");
        System.out.println("JRXML path: " + file.getAbsolutePath());
        System.out.println("Exists: " + file.exists() + ", Is File: " + file.isFile());
        JasperReport jasperReport = JasperCompileManager.compileReport(file.getAbsolutePath());
        //bunu ayrı bir method içerisine alalım
        List<ContactResponseDTO> contactResponseDTOList = contactRepository.findAll().stream()
                .map(contactMapper::fromContactEntityToContactResponseDTO)
                .toList();
        //Set report data
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(contactResponseDTOList);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("title", "All Contacts in DB Report");

        //Fill report
        JasperPrint jasperPrint = null;
        byte[] reportContent = null;
        try {
            jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
            switch (format) {
                case "pdf" -> reportContent = JasperExportManager.exportReportToPdf(jasperPrint);
                case "xml" -> reportContent = JasperExportManager.exportReportToXml(jasperPrint).getBytes();
                case "xlsx" -> {
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

                    reportContent = xlsxOutput.toByteArray();
                }
                default -> throw new RuntimeException("Unknown report format");
            }
        } catch (JRException e) {
            //handle exception
        }
        return reportContent;
    }
}
