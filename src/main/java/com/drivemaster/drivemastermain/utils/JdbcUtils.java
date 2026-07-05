package com.drivemaster.drivemastermain.utils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class JdbcUtils {

    private JdbcUtils() {
    }

    public static void setParameters(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            bind(ps, i + 1, params[i]);
        }
    }

    private static void bind(PreparedStatement ps, int index, Object value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.NULL);
        } else if (value instanceof LocalDate localDate) {
            ps.setObject(index, localDate);
        } else if (value instanceof LocalDateTime localDateTime) {
            ps.setObject(index, localDateTime);
        } else if (value instanceof Enum<?> enumValue) {
            ps.setString(index, enumValue.name());
        } else {
            ps.setObject(index, value);
        }
    }
}
