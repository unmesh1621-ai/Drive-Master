package com.drivemaster.drivemastermain.dao;

import com.drivemaster.drivemastermain.dao.pool.ConnectionPool;
import com.drivemaster.drivemastermain.domain.Order;
import com.drivemaster.drivemastermain.domain.OrderStatus;
import com.drivemaster.drivemastermain.domain.OrderType;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.List;

@Repository
public class OrderDao extends AbstractDao<Order, Long> {

    public OrderDao(ConnectionPool connectionPool) {
        super(connectionPool);
    }

    @Override
    protected String tableName() {
        return "orders";
    }

    @Override
    protected RowMapper<Order> rowMapper() {
        return rs -> {
            Date rentalStart = rs.getDate("rental_start");
            Date rentalEnd = rs.getDate("rental_end");
            return Order.builder()
                    .id(rs.getLong("id"))
                    .userId(rs.getLong("user_id"))
                    .vehicleId(rs.getLong("vehicle_id"))
                    .type(OrderType.valueOf(rs.getString("type")))
                    .rentalStart(rentalStart == null ? null : rentalStart.toLocalDate())
                    .rentalEnd(rentalEnd == null ? null : rentalEnd.toLocalDate())
                    .totalPrice(rs.getBigDecimal("total_price"))
                    .status(OrderStatus.valueOf(rs.getString("status")))
                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                    .build();
        };
    }

    @Override
    protected String insertSql() {
        return "INSERT INTO orders (user_id, vehicle_id, type, rental_start, rental_end, total_price, status, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected String updateSql() {
        return "UPDATE orders SET status = ? WHERE id = ?";
    }

    @Override
    protected void bindForInsert(PreparedStatement ps, Order entity) throws SQLException {
        ps.setLong(1, entity.getUserId());
        ps.setLong(2, entity.getVehicleId());
        ps.setString(3, entity.getType().name());
        if (entity.getRentalStart() != null) {
            ps.setDate(4, Date.valueOf(entity.getRentalStart()));
            ps.setDate(5, Date.valueOf(entity.getRentalEnd()));
        } else {
            ps.setNull(4, Types.DATE);
            ps.setNull(5, Types.DATE);
        }
        ps.setBigDecimal(6, entity.getTotalPrice());
        ps.setString(7, entity.getStatus().name());
        ps.setTimestamp(8, Timestamp.valueOf(entity.getCreatedAt()));
    }

    @Override
    protected void bindForUpdate(PreparedStatement ps, Order entity) throws SQLException {
        ps.setString(1, entity.getStatus().name());
        ps.setLong(2, entity.getId());
    }

    @Override
    protected Long extractGeneratedId(ResultSet keys) throws SQLException {
        return keys.getLong(1);
    }

    @Override
    protected Order withId(Order entity, Long id) {
        return entity.toBuilder().id(id).build();
    }

    public List<Order> findByUserId(Long userId) {
        return query("SELECT * FROM orders WHERE user_id = ? ORDER BY created_at DESC", userId);
    }

    public List<Order> findByDealerId(Long dealerId) {
        return query("SELECT o.* FROM orders o JOIN vehicles v ON v.id = o.vehicle_id " +
                "WHERE v.dealer_id = ? ORDER BY o.created_at DESC", dealerId);
    }

    public int updateStatus(Long orderId, OrderStatus status) {
        return executeUpdate("UPDATE orders SET status = ? WHERE id = ?", status, orderId);
    }

    public long countAll() {
        return queryCount("SELECT COUNT(*) FROM orders");
    }

    public long countByStatus(OrderStatus status) {
        return queryCount("SELECT COUNT(*) FROM orders WHERE status = ?", status);
    }

    /** Overlap check: an existing PENDING/CONFIRMED rental blocks a new one covering any shared day. */
    public boolean existsOverlappingRental(Long vehicleId, LocalDate start, LocalDate end) {
        long count = queryCount(
                "SELECT COUNT(*) FROM orders WHERE vehicle_id = ? AND type = 'RENTAL' " +
                        "AND status IN ('PENDING','CONFIRMED') AND rental_start < ? AND rental_end > ?",
                vehicleId, Date.valueOf(end), Date.valueOf(start));
        return count > 0;
    }
}
