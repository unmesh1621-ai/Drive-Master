package com.drivemaster.drivemastermain.dao;

import com.drivemaster.drivemastermain.dao.pool.ConnectionPool;
import com.drivemaster.drivemastermain.domain.DealerProfile;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Optional;

@Repository
public class DealerProfileDao extends AbstractDao<DealerProfile, Long> {

    public DealerProfileDao(ConnectionPool connectionPool) {
        super(connectionPool);
    }

    @Override
    protected String tableName() {
        return "dealer_profiles";
    }

    @Override
    protected RowMapper<DealerProfile> rowMapper() {
        return rs -> {
            Timestamp approvedAt = rs.getTimestamp("approved_at");
            return DealerProfile.builder()
                    .id(rs.getLong("id"))
                    .userId(rs.getLong("user_id"))
                    .businessName(rs.getString("business_name"))
                    .approvedAt(approvedAt == null ? null : approvedAt.toLocalDateTime())
                    .build();
        };
    }

    @Override
    protected String insertSql() {
        return "INSERT INTO dealer_profiles (user_id, business_name, approved_at) VALUES (?, ?, ?)";
    }

    @Override
    protected String updateSql() {
        return "UPDATE dealer_profiles SET business_name = ?, approved_at = ? WHERE id = ?";
    }

    @Override
    protected void bindForInsert(PreparedStatement ps, DealerProfile entity) throws SQLException {
        ps.setLong(1, entity.getUserId());
        ps.setString(2, entity.getBusinessName());
        bindApprovedAt(ps, 3, entity);
    }

    @Override
    protected void bindForUpdate(PreparedStatement ps, DealerProfile entity) throws SQLException {
        ps.setString(1, entity.getBusinessName());
        bindApprovedAt(ps, 2, entity);
        ps.setLong(3, entity.getId());
    }

    private void bindApprovedAt(PreparedStatement ps, int index, DealerProfile entity) throws SQLException {
        if (entity.getApprovedAt() != null) {
            ps.setTimestamp(index, Timestamp.valueOf(entity.getApprovedAt()));
        } else {
            ps.setNull(index, Types.TIMESTAMP);
        }
    }

    @Override
    protected Long extractGeneratedId(ResultSet keys) throws SQLException {
        return keys.getLong(1);
    }

    @Override
    protected DealerProfile withId(DealerProfile entity, Long id) {
        return entity.toBuilder().id(id).build();
    }

    public Optional<DealerProfile> findByUserId(Long userId) {
        return queryOne("SELECT * FROM dealer_profiles WHERE user_id = ?", userId);
    }

    public List<DealerProfile> findPending() {
        return query("SELECT dp.* FROM dealer_profiles dp JOIN users u ON u.id = dp.user_id " +
                "WHERE u.role = 'DEALER' AND u.status = 'PENDING'");
    }

    public long countApproved() {
        return queryCount("SELECT COUNT(*) FROM dealer_profiles WHERE approved_at IS NOT NULL");
    }
}
