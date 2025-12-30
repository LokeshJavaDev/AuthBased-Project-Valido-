package io.service.valido.dao.auth;

import io.service.valido.commons.util.ServiceError;
import io.service.valido.dao.ObjectRepository;
import io.service.valido.dao.rowMappers.RowMappers;
import io.service.valido.model.User;
import io.vavr.control.Either;
import jakarta.ws.rs.core.Response.Status;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class UserDao implements ObjectRepository<UUID, User> {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDao.class);

    private final Jdbi jdbi;

    public UserDao(final Jdbi jdbi) {
        this.jdbi = jdbi;
        jdbi.registerRowMapper(new RowMappers.UserMapper());
    }

    @Override
    public Either<ServiceError, User> create(User user) {
        final var id = UUID.randomUUID();
        final var sql = """
            INSERT INTO users (
                id, first_name, last_name, email, password, phone_number,
                is_active, is_verified, creator, created_at, modifier, updated_at
            )
            VALUES (
                :id, :first_name, :last_name, :email, :password, :phone_number,
                :is_active, :is_verified, :creator, :created_at, :modifier, :updated_at
            )
        """;
        try {
            jdbi.useHandle(handle ->
                   handle.createUpdate(sql)
                           .bind("id", user.getId() == null ? id : user.getId())
                           .bind("first_name", user.getFirstName())
                           .bind("last_name", user.getLastName())
                           .bind("email", user.getEmail())
                           .bind("password", user.getPassword())
                           .bind("phone_number", user.getPhoneNumber())
                           .bind("is_active", user.isActive())
                           .bind("is_verified", user.isVerified())
                           .bind("creator", user.getCreator())
                           .bind("created_at", user.getCreatedAt())
                           .bind("modifier", user.getModifier())
                           .bind("updated_at", user.getUpdatedAt())
                           .execute()
                    );
            return Either.right(user.toBuilder().id(id).build());
        }catch (Exception e) {
            LOGGER.error("Error in creating user:{}",e.getMessage(),e);
            return Either.left(ServiceError.builder()
                    .code(Status.INTERNAL_SERVER_ERROR.getStatusCode())
                    .message("Internal Server Error")
                    .build());
        }
    }

    @Override
    public Either<ServiceError, List<User>> fetch() {
        final var sql = "SELECT * FROM users";
        try{
            List<User> users = jdbi.withHandle(handle ->
                    handle.createQuery(sql)
                            .mapTo(User.class)
                            .list()
            );
            return Either.right(users);
        }catch (Exception e) {
            LOGGER.error("Error in fetching users.",e);
            return Either.left(ServiceError.builder()
                    .code(Status.NOT_FOUND.getStatusCode())
                    .message("Failed to fetch users")
                    .build());
        }
    }

    @Override
    public Either<ServiceError, Optional<User>> retrieve(UUID id) {
        final var sql = "SELECT * FROM users WHERE id = :id";
        try {
            Optional<User> user = jdbi.withHandle(handle ->
                    handle.createQuery(sql)
                            .bind("id",id)
                            .mapTo(User.class)
                            .findFirst()
            );
            return Either.right(user);
        }catch (Exception e) {
            LOGGER.error("Error retrieving user with id {}",id,e);
            return Either.left(ServiceError.builder()
                    .code(Status.NOT_FOUND.getStatusCode())
                    .message("user with id not found.")
                    .build()
            );
        }
    }

    @Override
    public Either<ServiceError, List<User>> retrieve(Set<UUID> id) {
        return Either.left(new ServiceError(Status.BAD_REQUEST.getStatusCode(),
                "Not implemented"));    }

    @Override
    public Either<ServiceError, User> update(User user) {
        LocalDateTime now = LocalDateTime.now();

        final var sql = """
                UPDATE users
                        SET first_name = :firstName,
                            last_name = :lastName,
                            email = :email,
                            password = :password,
                            phone_number = :phoneNumber,
                            is_active = :isActive,
                            is_verified = :isVerified,
                            modifier = :modifier,
                            updated_at = :updatedAt
                        WHERE id = :id
                """;

        try {
            int rows = jdbi.withHandle(handle ->
                    handle.createUpdate(sql)
                            .bind("firstName", user.getFirstName())
                            .bind("lastName", user.getLastName())
                            .bind("email", user.getEmail())
                            .bind("password", user.getPassword())
                            .bind("phoneNumber", user.getPhoneNumber())
                            .bind("isActive", user.isActive())
                            .bind("isVerified", user.isVerified())
                            .bind("modifier", user.getModifier())
                            .bind("updatedAt", now)
                            .bind("id", user.getId())
                            .execute()
            );
            if(rows == 0){
                return Either.left(
                        ServiceError.builder()
                                .code(Status.NOT_FOUND.getStatusCode())
                                .message("User not found.")
                                .build()
                );
            }
            return Either.right(user.toBuilder().updatedAt(now).build());
        }catch (Exception e) {
            LOGGER.error("Error updating user: {}", e.getMessage(), e);
            return Either.left(ServiceError.builder()
                    .code(Status.NOT_FOUND.getStatusCode())
                    .message("Faild to Update user")
                    .build()
            );
        }
    }

    /** TODO:--This approach is wrong don't  delete user directly
     * TODO: User may be disabled
     **/

    @Override
    public Either<ServiceError, Boolean> delete(UUID id) {
        final var sql = "DELETE FROM user WHERE id = :id";
        try{
            int rows = jdbi.withHandle(handle ->
                    handle.createUpdate(sql)
                            .bind("id", id)
                            .execute()
            );
            if(rows == 0) {
                return Either.left(ServiceError.builder()
                        .code(Status.NOT_FOUND.getStatusCode())
                        .message("User not found")
                        .build()
                );
            }
            return Either.right(true);
        }catch (Exception e){
            LOGGER.error("Error in deleting user: {}", e.getMessage());
            return Either.left(ServiceError.builder()
                    .code(Status.INTERNAL_SERVER_ERROR.getStatusCode())
                    .message("Internal Server Error")
                    .build()
            );
        }

    }

    public User getUserByCredentials(String email, String password) {
        final var sql = "SELECT * FROM users WHERE email = :email";
        return jdbi.withHandle(handle ->
                handle.createQuery(sql)
                        .bind("email", email)
                        .map((rs, ctx) -> User.builder()
                                .id(rs.getObject("id", UUID.class))
                                .email(rs.getString("email"))
                                .firstName(rs.getString("first_name"))
                                .lastName(rs.getString("last_name"))
                                .password(rs.getString("password"))
                                .phoneNumber(rs.getString("phone_number"))
                                .isActive(rs.getBoolean("is_active"))
                                .isVerified(rs.getBoolean("is_verified"))
                                .creator(rs.getObject("creator", UUID.class))
                                .modifier(rs.getObject("modifier", UUID.class))
                                .updatedAt(rs.getTime("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null)
                                .createdAt(rs.getTime("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null)
                                .build()
                        )
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"))
        );
    }

    public Either<ServiceError, Optional<User>> getUserByEmail(String email) {
        final var sql = "SELECT * FROM users WHERE email = :email";

        try {
            Optional<User> user = jdbi.withHandle(handle ->
                    handle.createQuery(sql)
                            .bind("email", email)
                            .mapTo(User.class)
                            .findFirst()
            );
            return Either.right(user);
        }catch (Exception e){
            return Either.left(ServiceError.builder()
                    .code(Status.INTERNAL_SERVER_ERROR.getStatusCode())
                    .message("Failed to retrieve user.")
                    .build()
            );
        }
    }

    public void markUserVerified(UUID userId) {
        final var sql = """
            UPDATE users
            SET is_verified = true,
                is_active = true,
                updated_at = :updated_at
            WHERE id = :id
        """;
        jdbi.useHandle(h -> h.createUpdate(sql)
                .bind("id", userId)
                .bind("updated_at", LocalDateTime.now())
                .execute());
    }

}
