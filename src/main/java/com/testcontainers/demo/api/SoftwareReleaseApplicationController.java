package com.testcontainers.demo.api;

import com.testcontainers.demo.dto.ReleaseDTO;
import com.testcontainers.demo.entity.Application;
import com.testcontainers.demo.entity.SoftwareRelease;
import com.testcontainers.demo.entity.Ticket;
import com.testcontainers.demo.service.IApplicationService;
import com.testcontainers.demo.service.ISoftwareReleaseService;
import com.testcontainers.demo.service.ITicketService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api")
public class SoftwareReleaseApplicationController {

    @Autowired
    private IApplicationService applicationService;

    @Autowired
    private ISoftwareReleaseService releaseService;

    @PostMapping("/application")
    public ResponseEntity<Application> addApplication(@RequestBody Application application, UriComponentsBuilder builder) {
        application = applicationService.addApplication(application);
        if (application.getId() == null) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(builder.path("/application/{id}").buildAndExpand(application.getId()).toUri());
        return new ResponseEntity<>(application, headers, HttpStatus.CREATED);
    }

    @GetMapping("/application/{id}")
    public ResponseEntity<Application> getApplicationById(@PathVariable("id") Integer id) {
        Application app = applicationService.getApplicationById(id);
        if (app == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(app, HttpStatus.OK);
    }

    @PutMapping("/application")
    public ResponseEntity<Application> updateApplication(@RequestBody Application application) {
        applicationService.updateApplication(application);
        return new ResponseEntity<>(application, HttpStatus.OK);
    }

    @GetMapping("/applications")
    public ResponseEntity<List<Application>> getApplications() {
        List<Application> app = applicationService.getAllApplications();
        if (app == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(app, HttpStatus.OK);
    }

    @DeleteMapping("/application/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable("id") Integer id) {
        applicationService.deleteApplication(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/softwareRelease")
    public ResponseEntity<Void> addRelease(@RequestBody SoftwareRelease softwareRelease, UriComponentsBuilder builder) {
        releaseService.addRelease(softwareRelease);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(builder.path("/softwareRelease/{id}").buildAndExpand(softwareRelease.getId()).toUri());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @PutMapping("/softwareRelease/{appid}/{releaseid}")
    public ResponseEntity<Void> addAppToRelease(
        @PathVariable("appid") Integer appid,
        @PathVariable("releaseid") Integer releaseid
    ) {
        releaseService.addApplication(appid, releaseid);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/softwareRelease/{releaseid}")
    public ResponseEntity<ReleaseDTO> getReleaseById(@PathVariable("releaseid") Integer releaseid) {
        ReleaseDTO release = releaseService.getReleaseById(releaseid);
        if (release == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(release, HttpStatus.OK);
    }
}
