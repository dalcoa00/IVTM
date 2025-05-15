package ivtm;

import java.util.*;
import POJOS.*;
import modelosExcel.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ValidadorVehiculo {
    EditorXML editor = new EditorXML();
    private final String erroresVehiculosXML = "resources\\ErroresVehiculos.xml";

    /*Comprueba la validez de los datos del vehículo*/
    public void comprobarVehiculo (XSSFWorkbook wb, Row row, HashSet<String> matriculaSet, HashSet<String> dniSet, Cell matriculaCell, Cell tipoVehiculoCell, Cell fechaMatriculacionCell, Cell fechaAltaCell, Cell fechaBajaCell, Cell fechaBajaTempCell, Cell nifPropietarioCell, Map<String, List<VehiculoExcel>> vehiculosContribuyentesMap) {
        String matricula = matriculaCell.getStringCellValue().trim().toUpperCase();
        List<String> errores = new ArrayList<>();
        //Comprueba que la correlación de fechas es correcta
        if (!compruebaFechas(fechaMatriculacionCell, fechaAltaCell, fechaBajaCell, fechaBajaTempCell)) {
            System.out.println("La correlación de fechas no es correcta.");
            errores.add("Fechas incoherentes");
            //return;
        }

        //Comprueba que la matrícula sea correcta y la añade al set de serlo
        if (!compruebaMatricula(matricula, tipoVehiculoCell)) {
            errores.add("Matricula Erronea");
            //return;
        }

        //Comprueba que el vehículo tiene propietario y si lo tiene comprueba que el NIF sea correcto
        if(!compruebaPropietario(dniSet, nifPropietarioCell)) {
            System.out.println("El vehículo no tiene propietario o su NIF no es válido.");
            errores.add("Vehiculo sin propietario");
            //return;
        }

        System.out.println("\nTodos los datos del vehículo son correctos.");
        if (!matriculaSet.add(matricula)) {
            System.out.println("\nMATRICULA DUPLICADA!\n");
            errores.add("Matricula duplicada");
            //return;
        }
        if(errores.isEmpty()){
            agregarVehiculo(vehiculosContribuyentesMap, row);
        }else{
            String aux="";
            String aux2="";
             if (row.getCell(1).getCellType() == CellType.NUMERIC) {
                aux = String.format("%.0f", row.getCell(1).getNumericCellValue());
            }else{
                aux=row.getCell(1).getStringCellValue();
            }
            if(row.getCell(2).getCellType()==CellType.NUMERIC){
                aux2= String.format("%.0f", row.getCell(2).getNumericCellValue());
            }else{
                aux2=row.getCell(2).getStringCellValue();
            }
            
            editor.xmlVehiculos(erroresVehiculosXML, row.getRowNum(),aux, aux2, errores);
        }
        //Agrego el vehiculo al Map de vehiculos asociado al nif del propietario si todos los datos son correctos

        //Vacía la lista de errores para que no ocupe memoria por si acaso
        if (!errores.isEmpty()) {
            errores.clear();
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
        if (matricula.matches("[A-Z]{1,2}\\d{4}[A-Z]{1,2}")) {
            String ciudad = obtenerPrefijoCiudad(matricula);

            return ciudades.contains(ciudad);
        }

        //Formato 3: ciudad + 6 dígitos
        if (matricula.matches("[A-Z]{1,2}\\d{5,6}")) {
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

    /*
    * Añade el vehiculo al map asociado al nif, si el nif no estaba ya crea una lista, si ya existía (ya tenía
    * un vehículo registrado a su nombre) recupera la lista e incluye el nuevo vehículo
    *
    * La clave del mapa es el NIFNIE, puesto que existe la posibilidad de que una persona tenga varios vehiculos (valor)
    * Cada vehiculo se asocia a un nif, por eso un nif puede asociarse a una lista de vehiculos
    */
    public void agregarVehiculo (Map<String, List<VehiculoExcel>> vehiculosContribuyentesMap, Row row) {
        int idFila = row.getRowNum();
        String nifPropietario = row.getCell(14).getStringCellValue();
        String tipo = row.getCell(0).getStringCellValue();
        String marca = row.getCell(1).getStringCellValue();
        String modelo = row.getCell(2).getStringCellValue();
        String matricula = row.getCell(3).getStringCellValue();
        String bastidor = row.getCell(4).getStringCellValue();
        Date fechaAlta = null;
        Date fechaBaja = null;
        Date fechaBajaTemp = null;
        int unidadCobro = 0;
        double valorUnidad = 0.0;

        //Fechas de alta, baja y baja temporal
        for (int i = 11; i < 14; i++) {
            if (row.getCell(i) != null && row.getCell(i).getCellType() != CellType.BLANK) {
                if (i == 11) {
                    fechaAlta = row.getCell(i).getDateCellValue();
                }
                else if (i == 12) {
                    fechaBaja = row.getCell(i).getDateCellValue();
                }
                else if (i == 13) {
                    fechaBajaTemp = row.getCell(i).getDateCellValue();
                }
                else {
                    System.out.println("La celda no contiene una fecha válida");
                }
            }
        }

        System.out.println("aa");

        String exenc = row.getCell(9).getStringCellValue().trim();
        Character exencion = exenc.charAt(0);

        //Unidad de cobro -> CVs(1), plazas(2), kgs(3), CCs(4)
        if (row.getCell(5) != null) {
            unidadCobro = 1;

            if (row.getCell(5).getCellType() == CellType.NUMERIC) {
                valorUnidad = row.getCell(5).getNumericCellValue();
            }
            else if (row.getCell(5).getCellType() == CellType.STRING) {
                try {
                    valorUnidad = Double.parseDouble(row.getCell(5).getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    System.out.println("Error al convertir String a double: " + row.getCell(5).getStringCellValue());
                }
            }
        }
        else if (row.getCell(6) != null) {
            unidadCobro = 2;

            if (row.getCell(6).getCellType() == CellType.NUMERIC) {
                valorUnidad = row.getCell(6).getNumericCellValue();
            }
            else if (row.getCell(6).getCellType() == CellType.STRING) {
                try {
                    valorUnidad = Double.parseDouble(row.getCell(6).getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    System.out.println("Error al convertir String a double: " + row.getCell(6).getStringCellValue());
                }
            }
        }
        else if (row.getCell(7) != null) {
            unidadCobro = 3;

            if (row.getCell(7).getCellType() == CellType.NUMERIC) {
                valorUnidad = row.getCell(7).getNumericCellValue();
            }
            else if (row.getCell(7).getCellType() == CellType.STRING) {
                try {
                    valorUnidad = Double.parseDouble(row.getCell(7).getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    System.out.println("Error al convertir String a double: " + row.getCell(7).getStringCellValue());
                }
            }
        }
        else if (row.getCell(8) != null) {
            unidadCobro = 4;

            if (row.getCell(8).getCellType() == CellType.NUMERIC) {
                valorUnidad = row.getCell(8).getNumericCellValue();
            }
            else if (row.getCell(8).getCellType() == CellType.STRING) {
                try {
                    valorUnidad = Double.parseDouble(row.getCell(8).getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    System.out.println("Error al convertir String a double: " + row.getCell(8).getStringCellValue());
                }
            }
        }

        VehiculoExcel v = new VehiculoExcel();
        v.setIdFila(idFila);
        v.setNifPropietario(nifPropietario);
        v.setTipoVehiculo(tipo);
        v.setMarca(marca);
        v.setModelo(modelo);
        v.setMatricula(matricula);
        v.setBastidor(bastidor);
        v.setFechaAlta(fechaAlta);
        v.setFechaBaja(fechaBaja);
        v.setFechaBajaTemporal(fechaBajaTemp);
        v.setUnidadCobro(unidadCobro);
        v.setValorUnidad(valorUnidad);
        v.setExenc_bonif(exencion);

        vehiculosContribuyentesMap.computeIfAbsent(nifPropietario, k -> new ArrayList<>()).add(v);

        System.out.println("Vehiculo mapeado correctamente: " + tipo + marca + modelo);
    }

}
