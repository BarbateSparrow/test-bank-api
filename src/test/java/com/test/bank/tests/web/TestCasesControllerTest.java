package com.test.bank.tests.web;

import com.test.bank.enums.TestCaseStatus;
import com.test.bank.model.Project;
import com.test.bank.model.Suite;
import com.test.bank.model.TestCase;
import com.test.bank.service.ProjectsService;
import com.test.bank.service.SuitesService;
import com.test.bank.service.TestCasesService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;


import java.util.*;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TestCasesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectsService projectsService;

    @MockBean
    private SuitesService suitesService;

    @MockBean
    private TestCasesService testCasesService;

    private Project project;
    private Suite suite;

    @Before
    public void setUp() {
        project = new Project();
        project.setId(1L);
        project.setName("Demo");
        when(projectsService.findProjectById(1L)).thenReturn(Optional.of(project));

        suite = new Suite();
        suite.setId(1L);
        suite.setProjectId(1L);
        suite.setName("Test suite");
        when(suitesService.findSuiteById(1L)).thenReturn(Optional.of(suite));
    }

    @Test
    public void testCanCreateTestCase() throws Exception {
        TestCase testCase = new TestCase();
        testCase.setId(0L);
        testCase.setName("Test case");
        testCase.setStatus(TestCaseStatus.NOT_TESTED.name());
        when(testCasesService.add(1L, testCase)).thenReturn(0L);

        this.mockMvc.perform(post("/suites/{suiteId}/cases",  suite.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(IntegrationTestUtils.toJson(testCase)))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().json("{\"id\":0}"));
    }

    @Test
    public void testCanNotCreateTestCaseWithWrongStatus() throws Exception {
        TestCase testCase = new TestCase();
        testCase.setId(0L);
        testCase.setName("Test case");
        testCase.setStatus("Status");
        when(testCasesService.add(1L, testCase)).thenReturn(0L);

        this.mockMvc.perform(post("/suites/{suiteId}/cases", suite.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(IntegrationTestUtils.toJson(testCase)))
                .andDo(print()).andExpect(status().isBadRequest())
                .andExpect(content().json(" {\"status\":\"No such status Status found for test cases, use on of NOT_TESTED, ON_REVIEW, FAILED, PASSED status.\"}"));
    }

    @Test
    public void testCanGetTestCaseById() throws Exception {
        TestCase testCase = new TestCase();
        testCase.setId(1L);
        testCase.setName("Test case");
        testCase.setStatus(TestCaseStatus.NOT_TESTED.name());
        when(testCasesService.findTestCaseById(1L)).thenReturn(Optional.of(testCase));

        this.mockMvc.perform(get("/suites/cases/{caseId}", suite.getId(), 1)
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().json("{\"id\":1,\"suiteId\":null,\"name\":\"Test case\",\"description\":null,\"steps\":[],\"labels\":[],\"status\":\"NOT_TESTED\",\"deleted\":false}"));
    }

    @Test
    public void testCanGetAllTestCaseBySuitId() throws Exception {
        TestCase testCase = new TestCase();
        testCase.setId(1L);
        testCase.setName("Test case");
        testCase.setStatus(TestCaseStatus.NOT_TESTED.name());
        when(testCasesService.findActiveTestCasesBySuiteId(1L, false))
                .thenReturn(Collections.singletonList(testCase));

        this.mockMvc.perform(get("/suites/{suiteId}/cases", 1)
                .param("deleted", "false")
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().json(
                        "[{\"id\":1,\"suiteId\":null,\"name\":\"Test case\",\"description\":null,\"steps\":[],\"labels\":[],\"status\":\"NOT_TESTED\",\"deleted\":false}]"));
    }

    @Test
    public void testCanDeleteTestCaseById() throws Exception {
        TestCase testCase = new TestCase();
        testCase.setId(1L);
        testCase.setName("Test case");
        testCase.setStatus(TestCaseStatus.NOT_TESTED.name());
        when(testCasesService.findTestCaseById(1L)).thenReturn(Optional.of(testCase));
        when(testCasesService.deleteTestCase(1L)).thenReturn(true);


        this.mockMvc.perform(delete("/suites/cases/{id}", suite.getId(), 1)
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"status\":\"Deleted\"}"));
    }

    @Test
    public void testCanNotFoundSuiteId() throws Exception {
        TestCase testCase = new TestCase();
        testCase.setId(1L);
        testCase.setName("Test case");
        testCase.setStatus(TestCaseStatus.NOT_TESTED.name());
        when(testCasesService.findActiveTestCasesBySuiteId(1L, false))
                .thenReturn(Collections.singletonList(testCase));


        this.mockMvc.perform(get("/suites/{suiteId}/cases",  2)
                .param("deleted", "false")
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print()).andExpect(status().isNotFound())
                .andExpect(content().json("{\"status\":\"No such suite with id 2\"}"));
    }

    @Test
    public void testCanNotFoundTestCaseId() throws Exception {
        TestCase testCase = new TestCase();
        testCase.setId(1L);
        testCase.setName("Test case");
        testCase.setStatus(TestCaseStatus.NOT_TESTED.name());
        when(testCasesService.findTestCaseById(1L)).thenReturn(Optional.of(testCase));

        this.mockMvc.perform(get("/suites/cases/{id}",2)
                .param("deleted", "false")
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print()).andExpect(status().isNotFound())
                .andExpect(content().json("{\"status\":\"No such test case with id 2\"}"));
    }

    @Test
    public void testCanGetAllTestCaseByLabels() throws Exception {
        List<String> labels = Arrays.asList("smoke", "api");
        TestCase testCase = new TestCase();
        testCase.setId(1L);
        testCase.setName("Test case");
        testCase.setStatus(TestCaseStatus.NOT_TESTED.name());
        testCase.setLabels(labels);
        when(testCasesService.findByLabel(labels)).thenReturn(Collections.singletonList(testCase));

        this.mockMvc.perform(get("/projects/{projectId}/cases/labels", 1)
                .param("label", "smoke", "api")
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().json(
                        "[{\"id\":1,\"suiteId\":null,\"name\":\"Test case\",\"description\":null,\"steps\":[],\"labels\":[\"smoke\",\"api\"],\"status\":\"NOT_TESTED\",\"deleted\":false}]"));
    }

    @Test
    public void testCanNotCreateTestCaseWithWrongProjectId() throws Exception {
        List<String> labels = Arrays.asList("smoke", "api");
        TestCase testCase = new TestCase();
        testCase.setId(1L);
        testCase.setName("Test case");
        testCase.setStatus(TestCaseStatus.NOT_TESTED.name());
        testCase.setLabels(labels);

        this.mockMvc.perform(get("/projects/{projectId}/cases/labels", 2)
                .param("label", "smoke", "api")
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print()).andExpect(status().isNotFound())
                .andExpect(content().json(
                        "{\"status\":\"No such project with id 2\"}"));
    }
}
