package com.drivemaster.drivemastermain.dao;

import com.drivemaster.drivemastermain.dao.pool.ConnectionPool;
import com.drivemaster.drivemastermain.domain.Role;
import com.drivemaster.drivemastermain.domain.User;
import com.drivemaster.drivemastermain.domain.UserStatus;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

@Repository
public class UserDao extends AbstractDao<User, Long> {

    public UserDao(ConnectionPool connectionPool) {
        super(connectionPool);
    }

    @Override
    protected String tableName() {
        return "users";
    }

    @Override
    protected RowMapper<User> rowMapper() {
        return rs -> User.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .email(rs.getString("email"))
                .passwordHash(rs.getString("password_hash"))
                .role(Role.valueOf(rs.getString("role")))
                .status(UserStatus.valueOf(rs.getString("status")))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .build();
    }

    @Override
    protected String insertSql() {
        return "INSERT INTO users (name, email, password_hash, role, status, created_at) VALUES (?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected String updateSql() {
        return "UPDATE users SET name = ?, email = ?, password_hash = ?, role = ?, status = ? WHERE id = ?";
    }

    @Override
    protected void bindForInsert(PreparedStatement ps, User entity) throws SQLException {
        ps.setString(1, entity.getName());
        ps.setString(2, entity.getEmail());
        ps.setString(3, entity.getPasswordHash());
        ps.setString(4, entity.getRole().name());
        ps.setString(5, entity.getStatus().name());
        ps.setTimestamp(6, Timestamp.valueOf(entity.getCreatedAt()));
    }

    @Override
    protected void bindForUpdate(PreparedStatement ps, User entity) throws SQLException {
        ps.setString(1, entity.getName());
        ps.setString(2, entity.getEmail());
        ps.setString(3, entity.getPasswordHash());
        ps.setString(4, entity.getRole().name());
        ps.setString(5, entity.getStatus().name());
        ps.setLong(6, entity.getId());
    }

    @Override
    protected Long extractGeneratedId(ResultSet keys) throws SQLException {
        return keys.getLong(1);
    }

    @Override
    protected User withId(User entity, Long id) {
        return entity.toBuilder().id(id).build();
    }

    public Optional<User> findByEmail(String email) {
        return queryOne("SELECT * FROM users WHERE email = ?", email);
    }

    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    public long countByRole(Role role) {
        return queryCount("SELECT COUNT(*) FROM users WHERE role = ?", role);
    }

    public java.util.List<User> findAllPaged(int page, int size) {
        return query("SELECT * FROM users ORDER BY created_at DESC LIMIT ? OFFSET ?", size, page * size);
    }

    public long countAllUsers() {
        return queryCount("SELECT COUNT(*) FROM users");
    }
}
