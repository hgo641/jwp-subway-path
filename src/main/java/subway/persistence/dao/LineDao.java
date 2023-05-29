package subway.persistence.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import subway.persistence.entity.LineEntity;

import java.util.List;
import java.util.Optional;

@Repository
public class LineDao {
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final RowMapper<LineEntity> rowMapper = (resultSet, rowNumber) -> new LineEntity(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            resultSet.getLong("upward_terminus_id"),
            resultSet.getLong("downward_terminus_id"),
            resultSet.getInt("fare"));

    public LineDao(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(namedParameterJdbcTemplate.getJdbcTemplate())
                .withTableName("line")
                .usingColumns("name", "upward_terminus_id", "downward_terminus_id", "fare")
                .usingGeneratedKeyColumns("id");
    }

    public long insert(LineEntity lineEntity) {
        SqlParameterSource sqlParameterSource = new BeanPropertySqlParameterSource(lineEntity);
        return simpleJdbcInsert.executeAndReturnKey(sqlParameterSource).longValue();
    }

    public Optional<LineEntity> findById(Long id) {
        String sql = "SELECT id, name, upward_terminus_id, downward_terminus_id, fare FROM line WHERE id=:id";
        SqlParameterSource sqlParameterSource = new MapSqlParameterSource("id", id);
        try {
            return Optional.ofNullable(namedParameterJdbcTemplate.queryForObject(sql, sqlParameterSource, rowMapper));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public List<LineEntity> findAll() {
        String sql = "SELECT id, name, upward_terminus_id, downward_terminus_id, fare FROM line";
        return namedParameterJdbcTemplate.query(sql, rowMapper);
    }

    public void update(LineEntity lineEntity) {
        String sql = "UPDATE line SET name=:name, upward_terminus_id=:upwardTerminusId, downward_terminus_id=:downwardTerminusId, fare=:fare WHERE id=:id";
        SqlParameterSource sqlParameterSource = new MapSqlParameterSource()
                .addValue("name", lineEntity.getName())
                .addValue("upwardTerminusId", lineEntity.getUpwardTerminusId())
                .addValue("downwardTerminusId", lineEntity.getDownwardTerminusId())
                .addValue("id", lineEntity.getId())
                .addValue("fare", lineEntity.getFare());
        namedParameterJdbcTemplate.update(sql, sqlParameterSource);
    }
}
