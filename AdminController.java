@RestController
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/api/v1/company-integrations")
    public void saveCompanyIntegrations(
        @AuthenticationPrincipal final AuthenticatedAccount principal,
        @RequestBody final List<CompanyIntegration> companyIntegrations
    ) {
        adminService.saveCompanyIntegrations(companyIntegrations, principal.getCompanyId());
    }

    @GetMapping("/api/v1/company-integrations")
    public List<CompanyIntegration> getCompanyIntegrations(
        @AuthenticationPrincipal final AuthenticatedAccount principal
    ) {
        return adminService.getCompanyIntegrations(principal.getCompanyId());
    }

    @GetMapping("/api/v1/integration-triggers")
    public List<IntegrationTrigger> getIntegrationTriggers() {
        return adminService.getIntegrationTriggers();
    }
}
