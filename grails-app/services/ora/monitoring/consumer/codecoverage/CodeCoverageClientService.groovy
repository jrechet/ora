package ora.monitoring.consumer.codecoverage


import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Slf4j
@Service
class CodeCoverageClientService {

    @Autowired
    RestTemplate restTemplate

    CodeCoverageResponse getCodeCoverage(String url) {
        try {
            ResponseEntity<CodeCoverageResponse> response = restTemplate.getForEntity(
                    url,
                    CodeCoverageResponse
            )
            return response.body
        } catch (Exception e) {
            log.error("Error fetching code coverage from ${url}", e)
            return new CodeCoverageResponse(
                    coveredLines: 0,
                    fileDetails: [],
                    totalCoverage: 0,
                    totalLines: 0
            )
        }
    }
}