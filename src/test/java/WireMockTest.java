import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.junit.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.core.Is.is;

public class WireMockTest {

    @Rule
    public WireMockRule wireMock = new WireMockRule(8092);

    private StubMapping existingWorkerStub;

    @Before
    public void setUp() {
        existingWorkerStub = stubFor(get(urlEqualTo(  "/companies/1/workers?name=Arman&surname=Shamenov"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[\n" +
                                "  {\n" +
                                "    \"id\": 1,\n" +
                                "    \"name\": \"Arman\",\n" +
                                "    \"surname\": \"Shamenov\",\n" +
                                "    \"age\": 23,\n" +
                                "    \"profession\": \"Java-developer\",\n" +
                                "    \"companyId\": 1\n" +
                                "  }\n" +
                                "]")));
          stubFor(get(urlEqualTo("/companies/777/workers?name=Vasily&surname=Terkin"))
               .willReturn(aResponse()
                       .withStatus(404)
                       .withBody("{}")
               ));
    }

    @Test
    public void existingWorkerTest() {
        final ResponseEntity <String> responseEntity
                = new RestTemplate().getForEntity("http://localhost:8092/companies/1/workers?name=Arman&surname=Shamenov", String.class);
        Assert.assertNotNull(responseEntity);
        Assert.assertThat(responseEntity.getStatusCode().value(), is(200));
        Assert.assertEquals(existingWorkerStub.getResponse().getBody(), responseEntity.getBody());
    }

    /*
     * Exception thrown when an HTTP 4xx is received (see javadoc for HttpClientErrorException.class).
     */
    @Test(expected = HttpClientErrorException.class)
    public void notExistingCompanyTest() {
        new RestTemplate().getForEntity("http://localhost:8092/companies/777/workers?name=Vasily&surname=Terkin", String.class);
    }

    @After
    public void after() {
        wireMock.stop();
    }
}