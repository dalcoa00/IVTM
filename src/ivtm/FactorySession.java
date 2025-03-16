package ivtm;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class FactorySession {
    private static SessionFactory sessionFactory;

    /* Una única instancia de SessionFactory */
    public static SessionFactory getSessionFactory() {
        //Si no hay una sesión, se crea
        if (sessionFactory == null) {
            try {
                //Carga config desde hibernate.cfg.xml
                sessionFactory = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
            }
            catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Error al crear SessionFactory");
            }
        }

        return sessionFactory;
    }

    /* Cierra sessionFactory al finalizar el programa */
    public static void closeSessionFactory() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}
