package ora.monitoring.consumer.codecoverage

class CodeCoverageResponse {
    Integer coveredLines
    List<FileDetail> fileDetails
    Integer totalCoverage
    Integer totalLines

    static class FileDetail {
        Integer coverage
        Integer coveredLines
        String file
        Integer totalLines
    }
}
