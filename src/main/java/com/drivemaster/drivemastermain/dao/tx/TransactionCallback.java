package com.drivemaster.drivemastermain.dao.tx;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface TransactionCallback<T> {
    T doInTransaction(Connection conn) throws SQLException;
}
