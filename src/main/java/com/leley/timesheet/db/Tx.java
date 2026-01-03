package com.leley.timesheet.db;

import java.sql.Connection;

public final class Tx {
    private Tx() {}

    public static <T> T inTx(Database db, TxWork<T> work) {
        try (Connection c = db.open()) {
            boolean old = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                T out = work.run(c);
                c.commit();
                c.setAutoCommit(old);
                return out;
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    public interface TxWork<T> {
        T run(Connection c) throws Exception;
    }
}
