package subway.business.service.dto;

import com.fasterxml.jackson.annotation.JsonCreator;

public class StationDeleteRequest {
    private final Long stationId;

    @JsonCreator
    public StationDeleteRequest(Long stationId) {
        this.stationId = stationId;
    }

    public Long getStationId() {
        return stationId;
    }
}
