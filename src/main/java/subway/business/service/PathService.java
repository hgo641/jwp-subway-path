package subway.business.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import subway.business.domain.*;
import subway.business.domain.fare.DistanceFareCalculator;
import subway.business.service.dto.ShortestPathResponse;

import java.util.List;
import java.util.stream.Collectors;

@Transactional
@Service
public class PathService {
    private final DistanceFareCalculator distanceFareCalculator;
    private final LineRepository lineRepository;
    private final StationRepository stationRepository;

    public PathService(DistanceFareCalculator distanceFareCalculator, LineRepository lineRepository, StationRepository stationRepository) {
        this.distanceFareCalculator = distanceFareCalculator;
        this.lineRepository = lineRepository;
        this.stationRepository = stationRepository;
    }

    public ShortestPathResponse getShortestPath(Long sourceStationId, Long destStationId) {
        Station sourceStation = stationRepository.findById(sourceStationId);
        Station destStation = stationRepository.findById(destStationId);
        Subway subway = new Subway(lineRepository.findAll());
        SubwayGraph subwayGraph = SubwayGraph.from(subway);
        List<String> stationNamesOfShortestPath = subwayGraph.getShortestPath(sourceStation, destStation).stream()
                .map(Station::getName)
                .collect(Collectors.toList());
        int totalDistance = subwayGraph.getTotalDistance(sourceStation, destStation);
        int totalFare = distanceFareCalculator.calculateByDistance(totalDistance);
        return new ShortestPathResponse(stationNamesOfShortestPath, totalDistance, totalFare);
    }
}
