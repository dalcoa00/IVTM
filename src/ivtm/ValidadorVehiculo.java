package ivtm;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ValidadorVehiculo {
    //ExcelManager manager = new ExcelManager();
    EditorXML editor = new EditorXML();
    private final String erroresVehiculosXML = "resources\\ErroresVehiculos.xml";

    public void comprobarVehiculo (XSSFWorkbook wb, Row row, HashSet<String> matriculaSet, HashSet<String> dniSet, Cell matriculaCell, Cell tipoVehiculoCell, Cell fechaMatriculacion, Cell fechaAlta, Cell fechaBaja, Cell fechaBajaTemp, Cell nifPropietario) {
        //Comprueba que la correlación de fechas es correcta


        //Comprueba que la matrícula sea correcta y la añade al set de serlo
        if (!compruebaMatricula(matriculaSet, matriculaCell, tipoVehiculoCell)) return;

        //Comprueba que el vehículo tiene propietario y si lo tiene comprueba que el NIF sea correcto
    }

    private boolean compruebaMatricula (HashSet<String> matriculaSet, Cell matriculaCell, Cell tipoVehiculoCell) {
        //En principio en estas celdas hay letras siempre, tiene que ser String
        String tipo = tipoVehiculoCell.getStringCellValue().trim().toUpperCase();
        String matricula = matriculaCell.getStringCellValue().trim().toUpperCase();
        Set<String> ciudades = new HashSet<>(Arrays.asList("VI", "AB", "A", "AL", "AV", "BA", "IB", "B", "BU", "CC", "CA", "CS", "CE", "CR", "CO",
                "C", "CU", "GI", "GR", "GU", "SS", "H", "HU", "J", "LE", "L", "LO", "LU", "M", "MA", "ML", "MU", "NA", "OR", "O", "P", "GC", "PO", "SA", "TF",
                "S", "SG", "SE", "SO", "T", "TE", "TO", "V", "VA", "BI", "ZA", "Z"));

        switch (tipo) {
            case "TURISMO":
            case "AUTOBUS":
            case "CAMION":
            case "MOTOCICLETA":
                if (esMatriculaNormal(matricula, ciudades)) {
                    matriculaSet.add(matricula);
                    System.out.println(matricula + " - Matrícula correcta.");

                    return true;
                } else {
                    System.out.println(matricula + " - Matrícula incorrecta.");

                    return false;
                }

            case "CICLOMOTOR":
                if (esMatriculaCiclomotor(matricula)) {
                    matriculaSet.add(matricula);
                    System.out.println(matricula + " - Matrícula correcta.");

                    return true;
                } else {
                    System.out.println(matricula + " - Matrícula incorrecta.");

                    return false;
                }

            case "TRACTOR":
                if (esMatriculaTractor(matricula, ciudades)) {
                    matriculaSet.add(matricula);
                    System.out.println(matricula + " - Matrícula correcta.");

                    return true;
                } else {
                    System.out.println(matricula + " - Matrícula incorrecta.");

                    return false;
                }

            case "REMOLQUE":
                if (esMatriculaRemolque(matricula, ciudades)) {
                    matriculaSet.add(matricula);
                    System.out.println(matricula + " - Matrícula correcta.");

                    return true;
                } else {
                    System.out.println(matricula + " - Matrícula incorrecta.");

                    return false;
                }

            case "HISTORICO":
                if (esMatriculaHistorico(matricula)) {
                    matriculaSet.add(matricula);
                    System.out.println(matricula + " - Matrícula correcta.");

                    return true;
                } else {
                    System.out.println(matricula + " - Matrícula incorrecta.");

                    return false;
                }

            default:
                System.out.println("Tipo de vehiculo no valido");

                return false;
        }
    }

    private boolean esMatriculaNormal (String matricula, Set<String> ciudades) {

        //Formato 1: 4 dígitos + 3 letras
        if (matricula.matches("\\d{4}[A-Z]{3}")) {
            return true;
        }

        //Formato 2: ciudad + 4 dígitos + 1 o 2 letras
        if (matricula.matches("[A-Z]{1,2}\\d{4}[A-A]{1,2}")) {
            String ciudad = obtenerPrefijoCiudad(matricula);

            return ciudades.contains(ciudad);
        }

        //Formato 3: ciudad + 6 dígitos
        if (matricula.matches("[A-Z]{1,2}\\d{6}")) {
            String ciudad = obtenerPrefijoCiudad(matricula);

            return ciudades.contains(ciudad);
        }

        return false;
    }

    private boolean esMatriculaCiclomotor (String matricula) {
        return matricula.matches("C\\d{4}[A-Z]{3}");
    }

    private boolean esMatriculaTractor (String matricula, Set<String> ciudades) {
        //E + 4 dígitos + 3 letras
        if (matricula.matches("E\\d{4}[A-Z]{3}")) {
            return true;
        }

        //Ciudad + 5 dígitos + VE
        if (matricula.matches("[A-Z]{1,2}\\d{5}VE")) {
            String ciudad = obtenerPrefijoCiudad(matricula);

            return ciudades.contains(ciudad);
        }
        //Ciudad + 6 dígitos
        if (matricula.matches("[A-Z]{1,2}\\d{6}")) {
            String ciudad = obtenerPrefijoCiudad(matricula);

            return ciudades.contains(ciudad);
        }

        return false;
    }

    private boolean esMatriculaRemolque (String matricula, Set<String> ciudades) {
        //R + 4 dígitos + 3 letras
        if (matricula.matches("R\\d{4}[A-Z]{3}")) {
            return true;
        }

        //Ciudad + 5 dígitos + VE
        if (matricula.matches("[A-Z]{1,2}\\d{5}VE")) {
            String ciudad = obtenerPrefijoCiudad(matricula);

            return ciudades.contains(ciudad);
        }

        //Ciudad + 6 dígitos
        if (matricula.matches("[A-Z]{1,2}\\d{6}")) {
            String ciudad = obtenerPrefijoCiudad(matricula);

            return ciudades.contains(ciudad);
        }

        return false;
    }

    private boolean esMatriculaHistorico (String matricula) {
        return matricula.matches("H\\d{4}[A-Z]{3}");
    }

    private String obtenerPrefijoCiudad (String matricula) {
        //Extrae 1 o 2 letras iniciales de la matricula
        if (matricula.length() >= 2 && Character.isLetter(matricula.charAt(1))) {
            return matricula.substring(0, 2);
        }
        else {
            return matricula.substring(0, 1);
        }
    }

}
