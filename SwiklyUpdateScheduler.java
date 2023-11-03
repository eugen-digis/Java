@Service
@Slf4j
@RequiredArgsConstructor
public class SwiklyUpdateScheduler {

    private final SwiklyService swiklyService;
    private final DriverRepositoryV2 driverRepository;
    private final FountainService fountainService;
    private final DriverServiceV2 driverService;

    @Scheduled(cron = "${scheduling.job.swiklyDriverDepositsUpdate}", zone = "UTC")
    public void scheduleDriverDepositsUpdate() {
        swiklyService.updateDriverDeposits();
        checkUpdatedSwiklies();
    }

    @Scheduled(cron = "${scheduling.job.swiklyDriverExpirationCheck}", zone = "UTC")
    public void scheduleDriverSwiklyExpirationCheck() {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime in15Days = now.plusDays(EXPIRATION_DAYS);
        Set<Driver> swiklyExpiresIn15DaysDrivers = driverRepository.findBySwiklyExpiryDateBefore(in15Days);

        swiklyExpiresIn15DaysDrivers.stream()
            .map(driver -> runAsync(() -> fountainService.updateApplicantSwiklyExpired(driver, true)))
            .forEach(CompletableFuture::join);

        ZonedDateTime in7Days = now.plusDays(YELLOW_CARD_DAYS);
        Set<Driver> swiklyExpiresIn7DaysDrivers = driverRepository.findBySwiklyExpiryDateBefore(in7Days);

        swiklyExpiresIn7DaysDrivers.stream()
            .map(driver -> runAsync(() -> {
                driver.setYellowCardBecauseOldDeposit(true);
                driverService.updateDriverStatus(driver, DriverStatusEnum.YELLOW_CARD);
                fountainService.findOrCreateApplicantInCreationStage(driver);
            }))
            .forEach(CompletableFuture::join);
    }

    private void checkUpdatedSwiklies() {
        ZonedDateTime afterMonthFromNow = ZonedDateTime.now().plusDays(RESTORATION_DAYS);
        Set<Driver> driversWithUpdatedSwiklies = driverRepository
            .findBySwiklyExpiryDateAfterAndYellowCardBecauseOldDepositTrue(afterMonthFromNow);

        driversWithUpdatedSwiklies.stream()
            .map(driver -> runAsync(() -> {
                driver.setYellowCardBecauseOldDeposit(false);
                driverService.updateDriverStatus(driver, DriverStatusEnum.ACTIVE);
                fountainService.updateDriver(driver);
            }))
            .forEach(CompletableFuture::join);
    }
}
