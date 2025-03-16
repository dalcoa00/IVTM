package ivtm;

import org.hibernate.Session;

public class IvtmSession {
    /* Abre una nueva sesión de Hibernate */
    public static Session open() {
        return FactorySession.getSessionFactory().openSession();
    }
}
