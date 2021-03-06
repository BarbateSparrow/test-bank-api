package com.test.bank.web;


import com.google.common.base.Enums;
import com.test.bank.ControllerKeyConstants;
import com.test.bank.enums.LaunchStatus;
import com.test.bank.model.Launch;
import com.test.bank.service.LaunchesService;
import com.test.bank.service.ProjectsService;
import com.test.bank.service.TestCasesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.test.bank.ControllerKeyConstants.STATUS_KEY;
import static java.util.Collections.singletonMap;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
public class LaunchesController {

    private final LaunchesService launchesService;
    private final ProjectsService projectsService;

    @RequestMapping(value = "/projects/{projectId}/launches", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity getLaunchesByProjectId(@PathVariable Long projectId) {
        return projectsService.findProjectById(projectId)
                .<ResponseEntity>map(project -> new ResponseEntity<>(launchesService.findAllLaunchesByProjectId(projectId), OK))
                .orElseGet(() -> new ResponseEntity<>(Collections.singletonMap(
                        STATUS_KEY, "No such project with id " + projectId), HttpStatus.NOT_FOUND));
    }

    @RequestMapping(value = "/projects/{projectId}/launches", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity createLaunch(@PathVariable Long projectId, @RequestBody Launch launch) {
        if (!isProjectPresent(projectId)) {
            return projectIdNotFoundResponse(projectId);
        }

        if (!isResultCorrect(launch.getResult())) {
            return badResultResponse(launch.getResult());
        }

        launch.setProjectId(projectId);
        return new ResponseEntity<>(singletonMap(ControllerKeyConstants.ID_KEY, launchesService.add(launch)), HttpStatus.OK);
    }

    @RequestMapping(value = "/projects/launches/{launchId}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity updateLaunchById(@PathVariable Long launchId, @RequestBody Launch launch) {
        Optional<Launch> value = launchesService.findLaunchById(launchId);
        if (!value.isPresent()) {
            return launchNotFoundResponse(launchId);
        }
        if (launch.getProjectId() == null) {
            return new ResponseEntity<>(singletonMap(ControllerKeyConstants.STATUS_KEY, "Project id required."), HttpStatus.BAD_REQUEST);
        }
        if (!isProjectPresent(launch.getProjectId())) {
            return projectIdNotFoundResponse(launch.getProjectId());
        }

        if (launchId.longValue() != launch.getId().longValue()) {
            return new ResponseEntity<>(singletonMap(ControllerKeyConstants.STATUS_KEY, "Wrong lunch id."), HttpStatus.BAD_REQUEST);
        }

        if (!isResultCorrect(launch.getResult())) {
            return badResultResponse(launch.getResult());
        }

        if (value.get().getTestCaseId().longValue() != launch.getTestCaseId().longValue()) {
            return new ResponseEntity<>(singletonMap(ControllerKeyConstants.STATUS_KEY, "Wrong test case id."), HttpStatus.BAD_REQUEST);

        }

        return new ResponseEntity<>(singletonMap(ControllerKeyConstants.ID_KEY, launchesService.add(launch)), HttpStatus.OK);
    }

    @RequestMapping(value = "/projects/launches/cases/{caseId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity getAllLaunchesByTestCaseId(@PathVariable Long caseId) {
        return new ResponseEntity<>(launchesService.findAllLaunchesByTestCaseId(caseId), HttpStatus.OK);
    }

    @RequestMapping(value = "/projects/launches/{launchId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity getLaunchesById(@PathVariable Long launchId) {
        return launchesService.findLaunchById(launchId)
                .<ResponseEntity>map(launch -> new ResponseEntity<>(launch, OK))
                .orElseGet(() -> launchNotFoundResponse(launchId));
    }

    @RequestMapping(value = "/projects/launches/{launchId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity deleteLaunchById(@PathVariable Long launchId) {
        boolean deleted = launchesService.deleteById(launchId);
        if (!deleted) {
            return launchNotFoundResponse(launchId);
        }
        return new ResponseEntity<>(singletonMap(ControllerKeyConstants.STATUS_KEY, ControllerKeyConstants.DELETED_STATUS), HttpStatus.OK);
    }


    private boolean isProjectPresent(Long projectId) {
        return projectsService.findProjectById(projectId).isPresent();
    }


    private boolean isResultCorrect(String status) {
        return Enums.getIfPresent(LaunchStatus.class, status).isPresent();
    }

    private ResponseEntity launchNotFoundResponse(Long launchId) {
        return new ResponseEntity<>((singletonMap(ControllerKeyConstants.STATUS_KEY, "No such launch with id " + launchId)), HttpStatus.NOT_FOUND);
    }

    private ResponseEntity projectIdNotFoundResponse(Long projectId) {
        return new ResponseEntity<>(Collections.singletonMap(ControllerKeyConstants.STATUS_KEY, "No such project with id " + projectId), HttpStatus.NOT_FOUND);
    }

    private ResponseEntity badResultResponse(String result) {
        return new ResponseEntity<>(Collections.singletonMap(ControllerKeyConstants.STATUS_KEY,
                String.format("No such result %s found for launch, use on of %s status.",
                        result, Stream.of(LaunchStatus.values())
                                .map(Enum::name)
                                .collect(Collectors.joining(", ")))), HttpStatus.BAD_REQUEST);
    }
}
