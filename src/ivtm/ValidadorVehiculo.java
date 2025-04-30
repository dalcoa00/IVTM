package ivtm;

import java.util.HashSet;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ValidadorVehiculo {
    //ExcelManager manager = new ExcelManager();
    EditorXML editor = new EditorXML();
    private final String erroresVehiculosXML = "resources\\ErroresVehiculos.xml";

    public void comprobarVehiculo (XSSFWorkbook wb, Row row, HashSet<String> matriculaSet, HashSet<String> dniSet, Cell matriculaCell, Cell tipoVehiculoCell) {
        //En principio en estas celdas hay letras siempre, tiene que ser String
        String tipo = tipoVehiculoCell.getStringCellValue();
        String matricula = matriculaCell.getStringCellValue();

        //Comprueba que la matrícula sea correcta en función del tipo de vehículo
        switch (tipo) {
            case "TURISMO":
            case "AUTOBUS":
            case "CAMION":
            case "MOTOCICLETA":

                break;

            case "CICLOMOTOR":

                break;

            case "TRACTOR":

                break;

            case "REMOLQUE":

                break;

            case "HISTORICO":

                break;

            default:
                System.out.println("Tipo de vehiculo no valido");
        }


        //Comprueba que el vehículo tiene propietario y si lo tiene comprueba que el NIF sea correcto
    }

}
