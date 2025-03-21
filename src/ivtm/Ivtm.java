package ivtm;

import POJOS.Contribuyente;
import org.hibernate.Session;
import java.util.Scanner;

public class Ivtm {

    public static void main(String[] args) {

        /* Práctica 1 */
        //new Ivtm().p1();

        /* Práctica 2 */
        new Ivtm().p2();

    }

    //Metodo de la p1 para no llenar el main de cosas que no se usan siempre
    public void p1() {
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
                System.out.println("Media: " +avg);
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

    /* Metodo para realiza la práctica 2 */
    public void p2() {
        ExcelManager manager = new ExcelManager();
        String ruta = "resources\\SistemasVehiculos.xlsx";

        //Metodo que no recibe la hoja -> Lee la primera hoja del archivo
        manager.readExcel(ruta);

        //Metodo que recibe el número de hoja que se desea leer
        manager.readExcel(ruta, 1); //Hoja 2

        //Metodo que modifica la celda indicada con el valor especificado
        int row = 111; //Es del final, está vacía

        //Crea nuevas celdas (Tipo BLANK)
        manager.updateExcel(row, 0, "71475583R");
        manager.updateExcel(row, 1, "Diego");
        manager.updateExcel(row, 5, "14");

        //Modifica celdas
        manager.updateExcel(row, 0, "Aquí va el DNI");
        manager.updateExcel(row, 5, 16); //Prueba a cambiar el tipo de dato de la celda -> ERROR
    }
    
}
