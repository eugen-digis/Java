@Service
@Slf4j
@RequiredArgsConstructor
public class SwiklyService {

    private static final String LIST_SWIK_URL = "/v1_0/listSwik";
    private static final String GET_SWIK_URL = "/v1_0/getSwik?id=";

    // swikly returns expiration date with 20 days less than the actual day
    // don't know why they do that, but we need to add these 20 days to keep the date consistent
    private static final Integer CALIBRATION_DAYS = 20;

    public static final Integer RESTORATION_DAYS = 30;
    public static final Integer EXPIRATION_DAYS = 15;
    public static final Integer YELLOW_CARD_DAYS = 7;


    @Value("${swikly.rootUrl}")
    private String rootUrl;
    @Value("${swikly.apiKey}")
    private String apiKey;
    @Value("${swikly.apiSecret}")
    private String apiSecret;
    @Value("${swikly.userId}")
    private String userId;
    @Value("${swikly.linkSecret}")
    private String linkSecret;


    private final RestTemplate restTemplate = new RestTemplate();
    private final DriverServiceV2 driverService;
    private final FountainService fountainService;

    public void processCallback(SwiklyCallbackDto callbackDto) {
        log.info("Callback from Swikly on approved transaction for {}", callbackDto.getUserEmail());
        authenticateRequest(callbackDto);
        Driver driver = driverService.findByLogin(callbackDto.getUserEmail());
        if (nonNull(driver)) {
            String swiklyToken = Stream.of(
                callbackDto.getSwiklyPayinToken(),
                callbackDto.getSwiklyDepositToken(),
                callbackDto.getSwiklyReservationToken()).filter(token -> !isEmpty(token)).findFirst().get();
            boolean isAccepted = callbackDto.getStatus().equals("OK");

            updateDriverData(driver, swiklyToken, callbackDto.getEndDate(), callbackDto.getStartDate(), isAccepted);
        }
    }

    public void updateDriverDeposits() {
        log.info("[updateDriverDeposits]: update driver swikly deposits started");
        HttpHeaders headers = new HttpHeaders();
        headers.add("api_key", apiKey);
        headers.add("api_secret", apiSecret);
        HttpEntity<String> entity = new HttpEntity<>("body", headers);

        GetAllSwiksResponse getAllSwiksResponse = restTemplate.exchange(
            rootUrl + LIST_SWIK_URL, HttpMethod.GET, entity, GetAllSwiksResponse.class).getBody();

        Map<String, GetSwikResponse> driversPerDeposit = getAllSwiksResponse.getSwiks().parallelStream()
            .map(swik -> restTemplate
                .exchange(rootUrl + GET_SWIK_URL + swik.getSwiklyId(), HttpMethod.GET, entity, GetSwikResponse.class)
                .getBody())
            .filter(Objects::nonNull)
            .peek(swik -> {
                if (nonNull(swik.getSwik()) && nonNull(swik.getSwik().getExpiryDate())) {
                    swik.getSwik().setExpiryDate(swik.getSwik().getExpiryDate().plusDays(CALIBRATION_DAYS));
                }
            })
            .collect(Collectors.toMap(swik -> swik.getSwik().getRecipient().getEmail().toLowerCase(), Function.identity(),
                (swik1, swik2) ->
                    swik1.getSwik().getExpiryDate().isAfter(swik2.getSwik().getExpiryDate()) ? swik1 : swik2));

        Map<String, Driver> drivers = driverService.findDriversByLoginInIgnoreCase(driversPerDeposit.keySet())
            .stream().collect(Collectors.toMap(Driver::getLogin, Function.identity()));

        Set<Driver> driversToUpdate = new HashSet<>();
        for (Entry<String, GetSwikResponse> stringSwikEntry : driversPerDeposit.entrySet()) {
            GetSwikResponse swikResponse = stringSwikEntry.getValue();
            Swik swik = stringSwikEntry.getValue().getSwik();
            Driver driver = drivers.get(swik.getRecipient().getEmail().toLowerCase());

            if (nonNull(driver)) {
                try {
                    driver.setSwiklyId(swikResponse.getSwik().getSwiklyId());
                    driver.setSwiklyAcceptedDate(Optional.ofNullable(swikResponse.getSwik().getAcceptedAt())
                        .map(acceptedDate -> acceptedDate.atZone(ZoneId.ofOffset("", ZoneOffset.UTC)))
                        .orElse(null));
                    driver.setSwiklyExpiryDate(Optional.ofNullable(swikResponse.getSwik().getExpiryDate())
                        .map(expiryDate -> expiryDate.atTime(LocalTime.MIN).atZone(ZoneId.ofOffset("", ZoneOffset.UTC)))
                        .orElse(ZonedDateTime.now().minusSeconds(1)));
                    Boolean accepted = swikResponse.getSwik().getAccepted();
                    driver.setSwiklyAccepted(accepted);
                    driversToUpdate.add(driver);
                    log.info("[updateDriverDeposits]: driver {} updated. Swikly accepted: {}", driver.getLogin(), accepted);
                    if (nonNull(driver.getFountainApplicantId())) {
                        fountainService.updateDriver(driver);
                        log.info("[updateDriverDeposits]: driver {} fountain caution_ok updated", driver.getLogin());
                    }
                } catch (Exception e) {
                    log.error("[updateDriverDeposits]: failed to update driver {} deposit", driver.getLogin(), e);
                }
            }
        }
        driverService.saveAll(driversToUpdate);
        log.info("[updateDriverDeposits]: update driver swikly deposits finished");
    }

    private void updateDriverData(Driver driver,
                                  String swiklyId,
                                  String expiryDate,
                                  String acceptedDate,
                                  Boolean isAccepted) {
        driver.setSwiklyId(swiklyId);
        driver.setSwiklyExpiryDate(Optional.ofNullable(expiryDate)
            .map(expDate -> LocalDate.parse(expDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                .atTime(LocalTime.MIN).atZone(ZoneId.ofOffset("", ZoneOffset.UTC)))
            .orElse(null));
        driver.setSwiklyAcceptedDate(Optional.ofNullable(acceptedDate)
            .map(accDate -> LocalDate.parse(accDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                .atTime(LocalTime.MIN).atZone(ZoneId.ofOffset("", ZoneOffset.UTC)))
            .orElse(null));
        driver.setSwiklyAccepted(isAccepted);
        driverService.save(driver);
        if (nonNull(driver.getFountainApplicantId())) {
            fountainService.updateDriver(driver);
        }
    }

    private void authenticateRequest(SwiklyCallbackDto callbackDto) {
        List<String> strings = Arrays.asList(
            userId,
            nonNull(callbackDto.getId()) ? callbackDto.getId() : "",
            nonNull(callbackDto.getStartDate()) ? callbackDto.getStartDate() : "",
            nonNull(callbackDto.getEndDate()) ? callbackDto.getEndDate() : "",
            nonNull(callbackDto.getTotalPaymentAmount()) ? callbackDto.getTotalPaymentAmount() : "0",
            nonNull(callbackDto.getTotalDepositAmount()) ? callbackDto.getTotalDepositAmount() : "0",
            nonNull(callbackDto.getTotalReservationAmount()) ? callbackDto.getTotalReservationAmount() : "0",
            nonNull(callbackDto.getSwiklyPayinToken()) ? callbackDto.getSwiklyPayinToken() : "",
            nonNull(callbackDto.getSwiklyDepositToken()) ? callbackDto.getSwiklyDepositToken() : "",
            nonNull(callbackDto.getSwiklyReservationToken()) ? callbackDto.getSwiklyReservationToken() : "",
            nonNull(callbackDto.getFreeText()) ? callbackDto.getFreeText() : "",
            linkSecret);
        String encodedString = DigestUtils.sha256Hex(String.join("", strings));
        if (!encodedString.equals(callbackDto.getCtrlKey())) {
            log.error("Swikly authentication failed. callbackDto: {}", callbackDto);
            throw new ServiceException(ServiceExceptionEnum.SWIKLY_AUTH_FAIL);
        }
    }
}
