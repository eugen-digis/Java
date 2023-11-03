@ContextConfiguration
class ProxyRestControllerTest extends AbstractControllerIntegrationTest {

    private static final String TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJkYXRhIjp7ImlkIjoxMjk3LCJlbWFpbCI6ImEudmxhc292QGRpZ2lzY29ycC5jb20iLCJjb21wYW55SWQiOlsxLDY2MCwxODcsNjU5XSwibGFzdFVzZWRBY2NvdW50SWQiOjk5MTUsInVzZXJJc0FjdGl2ZSI6MSwibGFzdFVzZWRQZXJtaXNzaW9ucyI6WyJbSUNCXSBQZXJtaXNzaW9uIl0sImFwcCI6ImVwIn0sImV4cCI6MjU4MTY2NTAzNiwiaW90IjoxNTgxNTc4NjM2fQ.WkFRv3cm_-TkRBkBDDzyBdwA0GlF6FYZNRRPL-3mcbA";

    @Autowired
    private MockMvc mvc;

    @Test
    @DataSet(value = "datasets/proxy/lookForPublishedSupplierPrices_init.xml", disableConstraints = true)
    @ExpectedDataSet(value = "datasets/proxy/lookForPublishedSupplierPrices_expected.xml", orderBy = "target_company_id")
    void lookForPublishedSupplierPrices() throws Exception {

        mvc
            .perform(post("/api/v1/cpq/quote-configurations/1/prices/api/proxy")
                .accept(APPLICATION_JSON)
                .header(AUTHORIZATION, TOKEN))
            .andExpect(status().isOk());
    }

    @Test
    @DataSet(value = "datasets/proxy/lookForProceduredPrices_init.xml", disableConstraints = true)
    @ExpectedDataSet(value = "datasets/proxy/lookForProceduredPrices_expected.xml", orderBy = "provider_mrc")
    void lookForProceduredPrices() throws Exception {

        mvc
            .perform(post("/api/v1/cpq/quote-configurations/1/prices/api/proxy")
                .accept(APPLICATION_JSON)
                .header(AUTHORIZATION, TOKEN))
            .andExpect(status().isOk());
    }

    @Test
    @DataSet(value = "datasets/proxy/lookForGeographicPrices_init.xml", disableConstraints = true, replacers = IncludeBinaryReplacer.class)
    @ExpectedDataSet(value = "datasets/proxy/lookForGeographicPrices_expected.xml")
    void lookForGeographicPrices() throws Exception {

        mvc
            .perform(post("/api/v1/cpq/quote-configurations/1/prices/api/proxy")
                .accept(APPLICATION_JSON)
                .header(AUTHORIZATION, TOKEN))
            .andExpect(status().isOk());
    }
}


@AutoConfigureMockMvc
public abstract class AbstractControllerIntegrationTest extends AbstractDataJpaTest{

}


@SpringBootTest
@ComponentScan(lazyInit = true)
@ContextConfiguration(classes = TestApplication.class)
@TestPropertySource(locations = "classpath:secret.test.properties", properties = {
    "spring.main.allow-bean-definition-overriding=true", "spring.jpa.show-sql=true"
})
@Testcontainers(disabledWithoutDocker = true)
public abstract class AbstractDataJpaTest implements DatabaseTest {

}
