@Component
@Order(700)
public class AdminSecurityConfiguration extends CommonSecurityConfiguration {

    @Value("${roles.admin.name}")
    private String adminRole;
    @Value("${permissions.integration.name}")
    private String integrationPermission;

    @Override
    public void configure(final HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers(GET, "/api/v1/company-integrations").access(hasAdminAndIntegration())
                .antMatchers(GET, "/api/v1/integration-triggers").access(hasAdminAndIntegration())
                .antMatchers(POST, "/api/v1/company-integrations").access(hasAdminAndIntegration())
        ;

        log("Admin");
    }

    private String hasAdminAndIntegration(){
        return hasAuthority(adminRole) + " and " + hasAuthority(integrationPermission);
    }

    private String hasAuthority(final String authority){
        return "hasAuthority('" + authority + "')";
    }

}
