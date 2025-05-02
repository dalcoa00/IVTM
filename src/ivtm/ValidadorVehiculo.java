package ivtm;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Date;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ValidadorVehiculo {
    EditorXML editor = new EditorXML();
    private final String erroresVehiculosXML = "resources\\ErroresVehiculos.xml";

    /*Comprueba la validez de los datos del vehículo*/
    public void comprobarVehiculo (XSSFWorkbook wb, Row row, HashSet<String> matriculaSet, HashSet<String> dniSet, Cell matriculaCell, Cell tipoVehiculoCell, Cell fechaMatriculacionCell, Cell fechaAltaCell, Cell fechaBajaCell, Cell fechaBajaTempCell, Cell nifPropietarioCell) {
        String matricula = matriculaCell.getStringCellValue().trim().toUpperCase();

        //Comprueba que la correlación de fechas es correcta
        if (!compruebaFechas(fechaMatriculacionCell, fechaAltaCell, fechaBajaCell, fechaBajaTempCell)) {
            System.out.println("La correlación de fechas no es correcta.");
            return;
        }

        //Comprueba que la matrícula sea correcta y la añade al set de serlo
        if (!compruebaMatricula(matricula, tipoVehiculoCell)) {
            return;
        }

        //Comprueba que el vehículo tiene propietario y si lo tiene comprueba que el NIF sea correcto
        if(!compruebaPropietario(dniSet, nifPropietarioCell)) {
            System.out.println("El vehículo no tiene propietario o su NIF no es válido.");
            return;
        }

        System.out.println("\nTodos los datos del vehículos son correctos.");
        if (!matriculaSet.add(matricula)) {
            System.out.println("\nMATRICULA DUPLICADA!!!!\n");
        }
    }

    /*Comprueba que la correlación de fechas sea correcta*/
    private boolean compruebaFechas (Cell fechaMatriculacionCell, Cell fechaAltaCell, Cell fechaBajaCell, Cell fechaBajaTempCell) {
        Date fechaMatriculacion = getFechaCelda(fechaMatriculacionCell);
        Date fechaAlta = getFechaCelda(fechaAltaCell);
        Date fechaBaja = getFechaCelda(fechaBajaCell);
        Date fechaBajaTemp = getFechaCelda(fechaBajaTempCell);

        if (fechaMatriculacion != null && fechaAlta != null) {
            if (fechaMatriculacion.after(fechaAlta)) {
                return false;
            }
        }

        if (fechaAlta != null && fechaBaja != null) {
            if (fechaBaja.before(fechaAlta)) {
                return false;
            }
        }

        if (fechaAlta != null && fechaBajaTemp != null) {
            if (fechaBajaTemp.before(fechaAlta)) {
                return false;
            }
        }

        if (fechaBaja != null && fechaBajaTemp != null) {
            if (fechaBajaTemp.after(fechaBaja)) {
                return false;
            }
        }

        //Si la correlación de fechas es correcta
        System.out.println("La correlación de fechas es correcta.");
        return true;
    }

    private Date getFechaCelda (Cell celda) {
        if (celda == null || celda.getCellType() == CellType.BLANK) {
            return null;
        }

        if (celda.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(celda)) {
            return celda.getDateCellValue();
        }

        return null;
    }

    /*Comprueba que el formato de la matrícula es correcto en función del tipo de vehículo*/
    private boolean compruebaMatricula (String matricula, Cell tipoVehiculoCell) {
        //En principio en estas celdas hay letras siempre, tiene que ser String
        String tipo = tipoVehiculoCell.getStringCellValue().trim().toUpperCase();
        Set<String> ciudades = new HashSet<>(Arrays.asList("VI", "AB", "A", "AL", "AV", "BA", "IB", "B", "BU", "CC", "CA", "CS", "CE", "CR", "CO",
                "C", "CU", "GI", "GR", "GU", "SS", "H", "HU", "J", "LE", "L", "LO", "LU", "M", "MA", "ML", "MU", "NA", "OR", "O", "P", "GC", "PO", "SA", "TF",
                "S", "SG", "SE", "SO", "T", "TE", "TO", "V", "VA", "BI", "ZA", "Z"));

        switch (tipo) {
            case "TURISMO":
            case "AUTOBUS":
            case "CAMION":
            case "MOTOCICLETA":
                if (esMatriculaNormal(matricula, ciudades)) {
                    //matriculaSet.add(matricula);
                    System.out.println(matricula + " - Matrícula correcta.");

                    return true;
                } else {
                    System.out.println(matricula + " - Matrícula incorrecta.");

                    return false;
                }

            case "CICLOMOTOR":
                if (esMatriculaCiclomotor(matricula)) {
                    //matriculaSet.add(matricula);
                    System.out.println(matricula + " - Matrícula correcta.");

                    return true;
                } else {
                    System.out.println(matricula + " - Matrícula incorrecta.");

                    return false;
                }

            case "TRACTOR":
                if (esMatriculaTractor(matricula, ciudades)) {
                    //matriculaSet.add(matricula);
                    System.out.println(matricula + " - Matrícula correcta.");

                    return true;
                } else {
                    System.out.println(matricula + " - Matrícula incorrecta.");

                    return false;
                }

            case "REMOLQUE":
                if (esMatriculaRemolque(matricula, ciudades)) {
                    //matriculaSet.add(matricula);
                    System.out.println(matricula + " - Matrícula correcta.");

                    return true;
                } else {
                    System.out.println(matricula + " - Matrícula incorrecta.");

                    return false;
                }

            case "HISTORICO":
                if (esMatriculaHistorico(matricula)) {
                    //matriculaSet.add(matricula);
                    System.out.println(matricula + " - Matrícula correcta.");

                    return true;
                } else {
                    System.out.println(matricula + " - Matrícula incorrecta.");

                    return false;
                }

            default:
                System.out.println("Tipo de vehículo no valido");

                return false;
        }
    }

    private boolean esMatriculaNormal (String matricula, Set<String> ciudades) {

        //Formato 1: 4 dígitos + 3 letras
        if (matricula.matches("\\d{4}[A-Z]{3}")) {
            return true;
        }

        //Formato 2: ciudad + 4 dígitos + 1 o 2 letras
        if (matricula.matches("[A-Z]{1,2}\\d{1,4}[A-Z]{1,2}")) {
            String ciudad = obtenerPrefijoCiudad(matricula);

            return ciudades.contains(ciudad);
        }

        //Formato 3: ciudad + 6 dígitos
        if (matricula.matches("[A-Z]{1,2}\\d{1,6}")) {
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
        if (matricula.matches("[A-Z]{1,2}\\d{1,5}VE")) {
            String ciudad = obtenerPrefijoCiudad(matricula);

            return ciudades.contains(ciudad);
        }
        //Ciudad + 6 dígitos
        if (matricula.matches("[A-Z]{1,2}\\d{1,6}")) {
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
        if (matricula.matches("[A-Z]{1,2}\\d{1,5}VE")) {
            String ciudad = obtenerPrefijoCiudad(matricula);

            return ciudades.contains(ciudad);
        }

        //Ciudad + 6 dígitos
        if (matricula.matches("[A-Z]{1,2}\\d{1,6}")) {
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

    /*Comprueba que el vehículo tiene un propietario válido*/
    private boolean compruebaPropietario (HashSet<String> dniSet, Cell nifPropietarioCell) {
        if (nifPropietarioCell == null || nifPropietarioCell.getCellType() == CellType.BLANK || nifPropietarioCell.getCellType() != CellType.STRING) {
            return false;
        }

        String nifPropietario = nifPropietarioCell.getStringCellValue();

        if (dniSet.contains(nifPropietario)) {
            System.out.println("El vehículo tiene un propietario válido.");

            return true;
        }

        return false;
    }



}
