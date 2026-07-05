package com.drivemaster.drivemastermain.dao;

import com.drivemaster.drivemastermain.dao.pool.ConnectionPool;
import com.drivemaster.drivemastermain.domain.VehicleFeatures;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;

/**
 * Standalone DAO (deliberately not {@link AbstractDao}): vehicle_features is a
 * 1:1 extension table keyed by vehicle_id itself, not a surrogate auto-increment id,
 * so the generic insert/generated-key machinery doesn't fit.
 */
@Repository
public class VehicleFeatureDao {

    private final ConnectionPool connectionPool;

    public VehicleFeatureDao(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public void upsert(Connection conn, VehicleFeatures features) {
        String sql = "INSERT INTO vehicle_features " +
                "(vehicle_id, color, engine_capacity, transmission, horsepower, seating_capacity, safety_rating, has_gps, has_bluetooth, has_sunroof) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE color = VALUES(color), engine_capacity = VALUES(engine_capacity), " +
                "transmission = VALUES(transmission), horsepower = VALUES(horsepower), " +
                "seating_capacity = VALUES(seating_capacity), safety_rating = VALUES(safety_rating), " +
                "has_gps = VALUES(has_gps), has_bluetooth = VALUES(has_bluetooth), has_sunroof = VALUES(has_sunroof)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, features.getVehicleId());
            ps.setString(2, features.getColor());
            ps.setString(3, features.getEngineCapacity());
            ps.setString(4, features.getTransmission());
            ps.setString(5, features.getHorsepower());
            if (features.getSeatingCapacity() != null) {
                ps.setInt(6, features.getSeatingCapacity());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.setString(7, features.getSafetyRating());
            ps.setBoolean(8, features.isHasGps());
            ps.setBoolean(9, features.isHasBluetooth());
            ps.setBoolean(10, features.isHasSunroof());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to upsert vehicle_features for vehicle " + features.getVehicleId(), e);
        }
    }

    public void upsert(VehicleFeatures features) {
        Connection conn = connectionPool.borrowConnection();
        try {
            upsert(conn, features);
        } finally {
            closeQuietly(conn);
        }
    }

    public Optional<VehicleFeatures> findByVehicleId(Long vehicleId) {
        Connection conn = connectionPool.borrowConnection();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM vehicle_features WHERE vehicle_id = ?")) {
            ps.setLong(1, vehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load vehicle_features for vehicle " + vehicleId, e);
        } finally {
            closeQuietly(conn);
        }
    }

    private VehicleFeatures mapRow(ResultSet rs) throws SQLException {
        Integer seatingCapacity = rs.getObject("seating_capacity", Integer.class);
        return VehicleFeatures.builder()
                .vehicleId(rs.getLong("vehicle_id"))
                .color(rs.getString("color"))
                .engineCapacity(rs.getString("engine_capacity"))
                .transmission(rs.getString("transmission"))
                .horsepower(rs.getString("horsepower"))
                .seatingCapacity(seatingCapacity)
                .safetyRating(rs.getString("safety_rating"))
                .hasGps(rs.getBoolean("has_gps"))
                .hasBluetooth(rs.getBoolean("has_bluetooth"))
                .hasSunroof(rs.getBoolean("has_sunroof"))
                .build();
    }

    private void closeQuietly(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.close();
        } catch (SQLException ignored) {
        }
    }
}
