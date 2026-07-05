package com.drivemaster.drivemastermain.dao;

import com.drivemaster.drivemastermain.dao.pool.ConnectionPool;
import com.drivemaster.drivemastermain.dao.support.PageResult;
import com.drivemaster.drivemastermain.dao.support.VehicleSearchCriteria;
import com.drivemaster.drivemastermain.domain.ListingType;
import com.drivemaster.drivemastermain.domain.Vehicle;
import com.drivemaster.drivemastermain.domain.VehicleStatus;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Repository
public class VehicleDao extends AbstractDao<Vehicle, Long> {

    public VehicleDao(ConnectionPool connectionPool) {
        super(connectionPool);
    }

    @Override
    protected String tableName() {
        return "vehicles";
    }

    @Override
    protected RowMapper<Vehicle> rowMapper() {
        return rs -> Vehicle.builder()
                .id(rs.getLong("id"))
                .dealerId(rs.getLong("dealer_id"))
                .name(rs.getString("name"))
                .make(rs.getString("make"))
                .model(rs.getString("model"))
                .year(rs.getInt("year"))
                .mileage(rs.getInt("mileage"))
                .fuelType(rs.getString("fuel_type"))
                .price(rs.getBigDecimal("price"))
                .listingType(ListingType.valueOf(rs.getString("listing_type")))
                .status(VehicleStatus.valueOf(rs.getString("status")))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .build();
    }

    @Override
    protected String insertSql() {
        return "INSERT INTO vehicles (dealer_id, name, make, model, year, mileage, fuel_type, price, listing_type, status, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected String updateSql() {
        return "UPDATE vehicles SET name = ?, make = ?, model = ?, year = ?, mileage = ?, fuel_type = ?, price = ?, listing_type = ?, status = ? WHERE id = ?";
    }

    @Override
    protected void bindForInsert(PreparedStatement ps, Vehicle entity) throws SQLException {
        ps.setLong(1, entity.getDealerId());
        ps.setString(2, entity.getName());
        ps.setString(3, entity.getMake());
        ps.setString(4, entity.getModel());
        ps.setInt(5, entity.getYear());
        ps.setInt(6, entity.getMileage());
        ps.setString(7, entity.getFuelType());
        ps.setBigDecimal(8, entity.getPrice());
        ps.setString(9, entity.getListingType().name());
        ps.setString(10, entity.getStatus().name());
        ps.setTimestamp(11, Timestamp.valueOf(entity.getCreatedAt()));
    }

    @Override
    protected void bindForUpdate(PreparedStatement ps, Vehicle entity) throws SQLException {
        ps.setString(1, entity.getName());
        ps.setString(2, entity.getMake());
        ps.setString(3, entity.getModel());
        ps.setInt(4, entity.getYear());
        ps.setInt(5, entity.getMileage());
        ps.setString(6, entity.getFuelType());
        ps.setBigDecimal(7, entity.getPrice());
        ps.setString(8, entity.getListingType().name());
        ps.setString(9, entity.getStatus().name());
        ps.setLong(10, entity.getId());
    }

    @Override
    protected Long extractGeneratedId(ResultSet keys) throws SQLException {
        return keys.getLong(1);
    }

    @Override
    protected Vehicle withId(Vehicle entity, Long id) {
        return entity.toBuilder().id(id).build();
    }

    public List<Vehicle> findByDealerId(Long dealerId) {
        return query("SELECT * FROM vehicles WHERE dealer_id = ? ORDER BY created_at DESC", dealerId);
    }

    public long countActive() {
        return queryCount("SELECT COUNT(*) FROM vehicles WHERE status = 'ACTIVE'");
    }

    /** Admin-only listing: includes REMOVED vehicles, unlike {@link #search}. */
    public PageResult<Vehicle> findAllPaged(int page, int size) {
        long total = queryCount("SELECT COUNT(*) FROM vehicles");
        List<Vehicle> content = query(
                "SELECT * FROM vehicles ORDER BY created_at DESC LIMIT ? OFFSET ?", size, page * size);
        return new PageResult<>(content, total);
    }

    public int updateStatus(Connection conn, Long vehicleId, VehicleStatus status) {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE vehicles SET status = ? WHERE id = ?")) {
            ps.setString(1, status.name());
            ps.setLong(2, vehicleId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update vehicle status", e);
        }
    }

    public PageResult<Vehicle> search(VehicleSearchCriteria criteria) {
        StringBuilder where = new StringBuilder(" WHERE status = 'ACTIVE'");
        List<Object> params = new ArrayList<>();

        if (criteria.getDealerId() != null) {
            where.append(" AND dealer_id = ?");
            params.add(criteria.getDealerId());
        }
        if (criteria.getKeyword() != null && !criteria.getKeyword().isBlank()) {
            where.append(" AND (name LIKE ? OR make LIKE ? OR model LIKE ?)");
            String like = "%" + criteria.getKeyword() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (criteria.getMake() != null && !criteria.getMake().isBlank()) {
            where.append(" AND make = ?");
            params.add(criteria.getMake());
        }
        if (criteria.getModel() != null && !criteria.getModel().isBlank()) {
            where.append(" AND model = ?");
            params.add(criteria.getModel());
        }
        if (criteria.getMinPrice() != null) {
            where.append(" AND price >= ?");
            params.add(criteria.getMinPrice());
        }
        if (criteria.getMaxPrice() != null) {
            where.append(" AND price <= ?");
            params.add(criteria.getMaxPrice());
        }
        if (criteria.getListingType() != null) {
            where.append(" AND (listing_type = ? OR listing_type = 'BOTH')");
            params.add(criteria.getListingType().name());
        }

        long total = queryCount("SELECT COUNT(*) FROM vehicles" + where, params.toArray());

        List<Object> pagedParams = new ArrayList<>(params);
        int page = Math.max(criteria.getPage(), 0);
        int size = Math.max(1, Math.min(criteria.getSize(), 100));
        pagedParams.add(size);
        pagedParams.add(page * size);

        List<Vehicle> content = query(
                "SELECT * FROM vehicles" + where + " ORDER BY created_at DESC LIMIT ? OFFSET ?",
                pagedParams.toArray());

        return new PageResult<>(content, total);
    }
}
