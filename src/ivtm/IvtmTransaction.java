package ivtm;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class IvtmTransaction {
    private Transaction transaction;

    /* Comienza una transacción */
    public void beginTrans(Session s) {
        transaction = s.beginTransaction();
    }

    /* Confirma cambios en la DB */
    public void commitTrans() {
        if (transaction != null) {
            transaction.commit();
        }
    }

    /* Deshacer cambios */
    public void rollbackTrans() {
        if (transaction != null) {
            transaction.rollback();
        }
    }
}
