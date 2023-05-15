package subway.persistence.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import subway.business.domain.Line;
import subway.business.domain.Section;
import subway.persistence.dao.LineDao;
import subway.persistence.dao.SectionDao;
import subway.persistence.entity.LineEntity;
import subway.persistence.entity.SectionEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static subway.fixtures.station.StationFixture.강남역;
import static subway.fixtures.station.StationFixture.잠실역;

@ExtendWith(MockitoExtension.class)
public class DbLineRepositoryTest {
    @InjectMocks
    private DbLineRepository dbLineRepository;

    @Mock
    private LineDao lineDao;

    @Mock
    private SectionDao sectionDao;

    @DisplayName("새 라인을 추가한다.")
    @Test
    void shouldCreateLineWhenRequest() {
        when(lineDao.insert(any())).thenReturn(1L);
        Line line = Line.of("2호선", "강남역", "잠실역", 10);
        assertThat(dbLineRepository.create(line)).isEqualTo(1L);
    }

    @DisplayName("노선의 ID를 통해 노선을 가져온다.")
    @Test
    void shouldReturnLineWhenInputLineId() {
        LineEntity lineEntity = new LineEntity(
                1L,
                "2호선",
                "강남역",
                "잠실역"
        );
        when(lineDao.findById(1L)).thenReturn(lineEntity);

        SectionEntity upwardSection = new SectionEntity(
                1L,
                1L,
                "강남역",
                "역삼역",
                10
        );
        SectionEntity downwardSection = new SectionEntity(
                2L,
                1L,
                "역삼역",
                "잠실역",
                10
        );
        when(sectionDao.findAllByLineId(1L)).thenReturn(List.of(downwardSection, upwardSection));

        Line line = dbLineRepository.findById(1L);

        assertAll(
                () -> assertThat(line.getId()).isEqualTo(1L),
                () -> assertThat(line.getName()).isEqualTo("2호선"),
                () -> assertThat(line.getSections().get(0).getUpwardStation().getName()).isEqualTo("강남역"),
                () -> assertThat(line.getSections().get(1).getUpwardStation().getName()).isEqualTo("역삼역")
        );
    }

    @DisplayName("모든 노선을 가져온다.")
    @Test
    void ShouldReturnAllLinesWhenRequest() {
        LineEntity lineEntity1 = new LineEntity(
                1L,
                "2호선",
                "강남역",
                "잠실역"
        );
        SectionEntity sectionEntity1 = new SectionEntity(
                1L,
                "강남역",
                "잠실역",
                10
        );

        LineEntity lineEntity2 = new LineEntity(
                2L,
                "1호선",
                "인천역",
                "부평역"
        );
        SectionEntity sectionEntity2 = new SectionEntity(
                1L,
                "인천역",
                "부평역",
                10
        );

        when(lineDao.findAll()).thenReturn(List.of(lineEntity1, lineEntity2));
        when(lineDao.findById(1L)).thenReturn(lineEntity1);
        when(lineDao.findById(2L)).thenReturn(lineEntity2);
        when(sectionDao.findAllByLineId(1L)).thenReturn(List.of(sectionEntity1));
        when(sectionDao.findAllByLineId(2L)).thenReturn(List.of(sectionEntity2));
        List<Line> lines = dbLineRepository.findAll();

        assertAll(
                () -> assertThat(lines.get(0).getName()).isEqualTo("2호선"),
                () -> assertThat(lines.get(1).getName()).isEqualTo("1호선")
        );
    }

    @DisplayName("노선에 대한 정보를 업데이트한다.")
    @Test
    void shouldUpdateLineWhenInputLine() {
        Section section = new Section(강남역, 잠실역, 10);
        Line line = new Line(1L, "2호선", List.of(section));
        doNothing().when(lineDao).update(any());
        doNothing().when(sectionDao).deleteAllByLineId(any());

        assertDoesNotThrow(() -> dbLineRepository.update(line));
    }
}
