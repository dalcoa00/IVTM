package ivtm;

import POJOS.Contribuyente;
import org.hibernate.Session;
import java.util.Scanner;

public class Ivtm {
    public static void main(String[] args) {
        // Se obtiene el NIF del contribuyente
        Scanner sc = new Scanner(System.in);
        System.out.println("Ingrese el NIF del contribuyente: ");

        String nif = sc.nextLine();
        sc.close();

        //Se abre una sesión de hibernate y se crea una transacción
        Session session = IvtmSession.open();
        IvtmTransaction transaction = new IvtmTransaction();

        try {
            transaction.beginTrans(session);

            //Buscar contribuyente por el NIF
            Contribuyente contribuyente = (Contribuyente) session.createQuery("FROM Contribuyente WHERE nifnie = :nif").setParameter("nif", nif).uniqueResult(); //Sentencia HQL

            if (contribuyente == null) {
                System.out.println("El NIF indicado no se encuentra en la base de datos");
            }
            else {
                //Mostrar los datos del contribuyente
                System.out.println("Nombre: " + contribuyente.getNombre());
                System.out.println("Apellidos: " + contribuyente.getApellido1() + " " + contribuyente.getApellido2());
                System.out.println("Dirección: " + contribuyente.getDireccion());

                //Total de los recibos = 115€
                session.createQuery("UPDATE Recibos SET totalRecibo = 115 WHERE contribuyente.idContribuyente = :id").setParameter("id", contribuyente.getIdContribuyente()).executeUpdate();

                //Calcular la media de los de los recibos
                Double avg = (Double) session.createQuery("SELECT AVG(totalRecibo) FROM Recibos").uniqueResult();

                //Elimina recibos menores a la media
                session.createQuery("DELETE FROM Recibos WHERE totalRecibo < :media").setParameter("media", avg).executeUpdate();
            }

            //Confirma los cambios en la base de datos
            transaction.commitTrans();
        } catch (Exception e) {
            transaction.rollbackTrans(); //Se revierten los cambios en caso de erros
            e.printStackTrace();
        } finally {
            session.close(); //Se cierra la sesión
            FactorySession.closeSessionFactory(); //Se cierra SessionFactory
        }

    }
    
}
