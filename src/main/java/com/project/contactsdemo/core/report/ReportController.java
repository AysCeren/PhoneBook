package com.project.contactsdemo.core.report;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@Controller
@RequestMapping({"/api"})
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Validated
public class ReportController {

    private final ReportService reportService;


    @RequestMapping(method = {RequestMethod.GET}, path = {"/contactReport/{format}"})
    public ResponseEntity<Resource> getItemReport(@PathVariable("format") String format) {

        byte[] reportContent = reportService.getItemReport(format);
        ByteArrayResource resource = new ByteArrayResource(reportContent);

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(resource.contentLength())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename("item-report." + format)
                                .build().toString())
                .body(resource);
    }
}
