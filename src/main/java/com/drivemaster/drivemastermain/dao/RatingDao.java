package com.drivemaster.drivemastermain.dao;

import com.drivemaster.drivemastermain.dao.pool.ConnectionPool;
import com.drivemaster.drivemastermain.domain.Rating;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class RatingDao extends AbstractDao<Rating, Long> {

    public RatingDao(ConnectionPool connectionPool) {
        super(connectionPool);
    }

    @Override
    protected String tableName() {
        return "ratings";
    }

    @Override
    protected RowMapper<Rating> rowMapper() {
        return rs -> Rating.builder()
                .id(rs.getLong("id"))
                .orderId(rs.getLong("order_id"))
                .userId(rs.getLong("user_id"))
                .dealerId(rs.getLong("dealer_id"))
                .stars(rs.getInt("stars"))
                .comment(rs.getString("comment"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .build();
    }

    @Override
    protected String insertSql() {
        return "INSERT INTO ratings (order_id, user_id, dealer_id, stars, comment, created_at) VALUES (?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected String updateSql() {
        return "UPDATE ratings SET stars = ?, comment = ? WHERE id = ?";
    }

    @Override
    protected void bindForInsert(PreparedStatement ps, Rating entity) throws SQLException {
        ps.setLong(1, entity.getOrderId());
        ps.setLong(2, entity.getUserId());
        ps.setLong(3, entity.getDealerId());
        ps.setInt(4, entity.getStars());
        ps.setString(5, entity.getComment());
        ps.setTimestamp(6, Timestamp.valueOf(entity.getCreatedAt()));
    }

    @Override
    protected void bindForUpdate(PreparedStatement ps, Rating entity) throws SQLException {
        ps.setInt(1, entity.getStars());
        ps.setString(2, entity.getComment());
        ps.setLong(3, entity.getId());
    }

    @Override
    protected Long extractGeneratedId(ResultSet keys) throws SQLException {
        return keys.getLong(1);
    }

    @Override
    protected Rating withId(Rating entity, Long id) {
        return entity.toBuilder().id(id).build();
    }

    public Optional<Rating> findByOrderId(Long orderId) {
        return queryOne("SELECT * FROM ratings WHERE order_id = ?", orderId);
    }

    public List<Rating> findByDealerId(Long dealerId) {
        return query("SELECT * FROM ratings WHERE dealer_id = ? ORDER BY created_at DESC", dealerId);
    }

    public double averageForDealer(Long dealerId) {
        return queryDouble("SELECT COALESCE(AVG(stars), 0) FROM ratings WHERE dealer_id = ?", dealerId);
    }
}
