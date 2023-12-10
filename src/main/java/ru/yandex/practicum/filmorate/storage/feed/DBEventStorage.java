package ru.yandex.practicum.filmorate.storage.feed;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.enumerations.EventType;
import ru.yandex.practicum.filmorate.model.enumerations.Operation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class DBEventStorage implements EventStorage {
    private final JdbcTemplate jdbcTemplate;
    private static final String SQL_CREATE = "INSERT INTO feed (user_id, entity_id, event_type_id, operation_id) "
            + "VALUES (?, ?, (SELECT event_type_id FROM event_types WHERE name = ?), "
            + "(SELECT operation_id FROM operations WHERE name = ?));";
    private static final String SQL_GET_ALL_BY_USER = "SELECT \n" +
            "f.event_id, f.user_id, f.timestamp, f.entity_id, e.name event_type_name, o.name operation_name\n" +
            "FROM feed f\n" +
            "JOIN event_types e ON e.event_type_id = f.event_type_id\n" +
            "JOIN operations o ON o.operation_id = f.operation_id\n" +
            "WHERE f.user_id = ?" +
            "ORDER BY f.event_id asc";

    @Override
    public Event create(Event event) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(SQL_CREATE, new String[]{"event_id"});
            ps.setInt(1, event.getUserId());
            ps.setInt(2, event.getEntityId());
            ps.setString(3, event.getEventType().toString());
            ps.setString(4, event.getOperation().toString());
            return ps;
        }, keyHolder);

        event.setEventId(Objects.requireNonNull(keyHolder.getKey()).intValue());

        return event;
    }

    @Override
    public List<Event> getAllByUserId(int userId) {
        return jdbcTemplate.query(SQL_GET_ALL_BY_USER, this::mapToRow, userId);
    }

    private Event mapToRow(ResultSet resultSet, int rowNum) throws SQLException {
        return Event.builder()
                .eventId(resultSet.getInt("event_id"))
                .userId(resultSet.getInt("user_id"))
                .timestamp(resultSet.getTimestamp("timestamp").getTime())
                .eventType(EventType.valueOf(resultSet.getString("event_type_name")))
                .operation(Operation.valueOf(resultSet.getString("operation_name")))
                .entityId(resultSet.getInt("entity_id"))
                .build();
    }
}