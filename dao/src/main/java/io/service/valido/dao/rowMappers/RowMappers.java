package io.service.valido.dao.rowMappers;

import io.service.valido.model.User;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public final class RowMappers {

    private RowMappers() {}

    public static class UserMapper implements RowMapper<User> {
        @Override
        public User map(ResultSet rs, StatementContext ctx) throws SQLException {
            return User.builder()
                    .id(rs.getObject("id", UUID.class))
                    .firstName(rs.getString("first_name"))
                    .lastName(rs.getString("last_name"))
                    .email(rs.getString("email"))
                    .password(rs.getString("password"))
                    .phoneNumber(rs.getString("phone_number"))
                    .isActive(rs.getBoolean("is_active"))
                    .isVerified(rs.getBoolean("is_verified"))
                    .creator(rs.getObject("creator", UUID.class))
                    .modifier(rs.getObject("modifier", UUID.class))
                    .updatedAt(rs.getTimestamp("updated_at") != null ?
                            rs.getTimestamp("updated_at").toLocalDateTime() : null)
                    .createdAt(rs.getTimestamp("created_at") != null ?
                            rs.getTimestamp("created_at").toLocalDateTime() : null)
                    .build();
        }
    }
}