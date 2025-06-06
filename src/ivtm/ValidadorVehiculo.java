package ivtm;

import java.util.*;

import POJOS.Contribuyente;
import POJOS.Vehiculos;
import modelosExcel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ValidadorVehiculo {
    EditorXML editor = new EditorXML();
    private final String erroresVehiculosXML = "resources\\ErroresVehiculos.xml";

    /*Comprueba la validez de los datos del vehículo*/
    public void comprobarVehiculo (XSSFWorkbook wb, Row row, HashSet<String> matriculaSet, HashSet<String> dniSet, Cell matriculaCell, Cell tipoVehiculoCell, Cell fechaMatriculacionCell, Cell fechaAltaCell, Cell fechaBajaCell, Cell fechaBajaTempCell, Cell nifPropietarioCell, Map<String, List<VehiculoExcel>> vehiculosContribuyentesMap, Map<String, List<Vehiculos>> vehiculosPojosContribuyentesMap, Map<String, Contribuyente> contribuyentesPojosMap) {
        String matricula = matriculaCell.getStringCellValue().trim().toUpperCase();
        List<String> errores = new ArrayList<>();

        //Comprueba que la correlación de fechas es correcta
        if (!compruebaFechas(fechaMatriculacionCell, fechaAltaCell, fechaBajaCell, fechaBajaTempCell)) {
            System.out.println("La correlación de fechas no es correcta.");
            errores.add("Fechas incoherentes");
        }

        //Comprueba que la matrícula sea correcta y la añade al set de serlo
        if (!compruebaMatricula(matricula, tipoVehiculoCell)) {
            errores.add("Matricula Erronea");
        }

        //Comprueba que el vehículo tiene propietario y si lo tiene comprueba que el NIF sea correcto
        if(!compruebaPropietario(dniSet, nifPropietarioCell, errores)) {
            System.out.println("El vehículo no tiene propietario o su NIF no es válido.");
        }

        if (!matriculaSet.add(matricula)) {
            System.out.println("\nMATRICULA DUPLICADA!\n");
            errores.add("Matricula duplicada");
        }

        if (errores.isEmpty()){
            agregarVehiculo(vehiculosContribuyentesMap, vehiculosPojosContribuyentesMap, contribuyentesPojosMap ,row);
        }
        else {
            String aux="";
            String aux2="";
             if (row.getCell(1).getCellType() == CellType.NUMERIC) {
                aux = String.format("%.0f", row.getCell(1).getNumericCellValue());
            }
             else {
                aux=row.getCell(1).getStringCellValue();
            }

             if (row.getCell(2).getCellType() == CellType.NUMERIC) {
                aux2 = String.format("%.0f", row.getCell(2).getNumericCellValue());
            }
             else {
                aux2 = row.getCell(2).getStringCellValue();
             }
            
             editor.xmlVehiculos(erroresVehiculosXML, row.getRowNum()+1,aux, aux2, errores);
        }

        //Vacía la lista de errores para el siguiente vehículo
        errores.clear();
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
                if (esMatriculaNormal(matricula, ciudades) || esMatriculaHistorico(matricula)) {
                    System.out.println(matricula + " - Matrícula correcta.");

                    return true;
                } else {
                    System.out.println(matricula + " - Matrícula incorrecta.");

                    return false;
                }

            case "CICLOMOTOR":
                if (esMatriculaCiclomotor(matricula) || esMatriculaHistorico(matricula)) {
                    System.out.println(matricula + " - Matrícula correcta.");

                    return true;
                } else {
                    System.out.println(matricula + " - Matrícula incorrecta.");

                    return false;
                }

            case "TRACTOR":
                if (esMatriculaTractor(matricula, ciudades) || esMatriculaHistorico(matricula)) {
                    System.out.println(matricula + " - Matrícula correcta.");

                    return true;
                } else {
                    System.out.println(matricula + " - Matrícula incorrecta.");

                    return false;
                }

            case "REMOLQUE":
                if (esMatriculaRemolque(matricula, ciudades) || esMatriculaHistorico(matricula)) {
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
        if (matricula.matches("^\\d{4}[A-Z]{3}")) {
            return true;
        }

        //Formato 2: ciudad + 4 dígitos + 1 o 2 letras
        if (matricula.matches("^[A-Z]{1,2}(?!0+$)\\d{1,4}[A-Z]{1,2}")) {
            String ciudad = obtenerPrefijoCiudad(matricula);

            return ciudades.contains(ciudad);
        }

        //Formato 3: ciudad + 6 dígitos
        if (matricula.matches("^[A-Z]{1,2}(?!0+$)\\d{1,6}")) {
            String ciudad = obtenerPrefijoCiudad(matricula);

            return ciudades.contains(ciudad);
        }

        return false;
    }

    private boolean esMatriculaCiclomotor (String matricula) {

        return matricula.matches("^C\\d{4}[A-Z]{3}");
    }

    private boolean esMatriculaTractor (String matricula, Set<String> ciudades) {
        //E + 4 dígitos + 3 letras
        if (matricula.matches("^E\\d{4}[A-Z]{3}")) {
            return true;
        }

        //Ciudad + 5 dígitos + VE
        if (matricula.matches("^[A-Z]{1,2}(?!0+$)\\d{1,5}VE")) {
            String ciudad = obtenerPrefijoCiudad(matricula);

            return ciudades.contains(ciudad);
        }
        //Ciudad + 6 dígitos
        if (matricula.matches("^[A-Z]{1,2}(?!0+$)\\d{1,6}")) {
            String ciudad = obtenerPrefijoCiudad(matricula);

            return ciudades.contains(ciudad);
        }

        return false;
    }

    private boolean esMatriculaRemolque (String matricula, Set<String> ciudades) {
        //R + 4 dígitos + 3 letras
        if (matricula.matches("^R\\d{4}[A-Z]{3}")) {
            return true;
        }

        //Ciudad + 5 dígitos + VE
        if (matricula.matches("^[A-Z]{1,2}(?!0+$)\\d{1,5}VE")) {
            String ciudad = obtenerPrefijoCiudad(matricula);

            return ciudades.contains(ciudad);
        }

        //Ciudad + 6 dígitos
        if (matricula.matches("^[A-Z]{1,2}(?!0+$)\\d{1,6}")) {
            String ciudad = obtenerPrefijoCiudad(matricula);

            return ciudades.contains(ciudad);
        }

        return false;
    }

    private boolean esMatriculaHistorico (String matricula) {

        return matricula.matches("^H\\d{4}[A-Z]{3}");
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
    private boolean compruebaPropietario (HashSet<String> dniSet, Cell nifPropietarioCell, List<String> errores) {
        if (nifPropietarioCell == null || nifPropietarioCell.getCellType() == CellType.BLANK || nifPropietarioCell.getCellType() != CellType.STRING) {
            errores.add("Vehiculo sin propietario.");

            return false;
        }

        String nifPropietario = nifPropietarioCell.getStringCellValue();

        if (dniSet.contains(nifPropietario)) {
            System.out.println("El vehículo tiene un propietario válido.");

            return true;
        }

        errores.add("Vehiculo con propietario erróneo.");
        return false;
    }

    /*
    * Añade el vehiculo al map asociado al nif, si el nif no estaba ya crea una lista, si ya existía (ya tenía
    * un vehículo registrado a su nombre) recupera la lista e incluye el nuevo vehículo
    *
    * La clave del mapa es el NIFNIE, puesto que existe la posibilidad de que una persona tenga varios vehiculos (valor)
    * Cada vehiculo se asocia a un nif, por eso un nif puede asociarse a una lista de vehiculos
    */
    public void agregarVehiculo (Map<String, List<VehiculoExcel>> vehiculosContribuyentesMap, Map<String, List<Vehiculos>> vehiculosPojosContribuyentesMap, Map<String, Contribuyente> contribuyentesPojosMap, Row row) {
        Integer idFila = row.getRowNum();
        String nifPropietario = row.getCell(14).getStringCellValue().trim();
        String tipo = row.getCell(0).getStringCellValue();
        String marca = row.getCell(1).getStringCellValue();

        String modelo;
        if (row.getCell(2).getCellType() == CellType.NUMERIC) {
            modelo = String.format("%.0f", row.getCell(2).getNumericCellValue());
        } else {
            modelo = row.getCell(2).getStringCellValue();
        }

        String matricula = row.getCell(3).getStringCellValue();
        String bastidor = row.getCell(4).getStringCellValue();
        Date fechaMatriculacion = null;
        Date fechaAlta = null;
        Date fechaBaja = null;
        Date fechaBajaTemp = null;
        int unidadCobro = 0;
        double valorUnidad = 0.0;

        //Fechas de alta, baja y baja temporal
        for (int i = 10; i < 14; i++) {
            if (row.getCell(i) != null && row.getCell(i).getCellType() != CellType.BLANK) {
                if (i == 10) {
                    fechaMatriculacion = row.getCell(i).getDateCellValue();
                }
                else if (i == 11) {
                    fechaAlta = row.getCell(i).getDateCellValue();
                }
                else if (i == 12) {
                    fechaBaja = row.getCell(i).getDateCellValue();
                }
                else {
                    fechaBajaTemp = row.getCell(i).getDateCellValue();
                }
            }
        }

        String exenc = row.getCell(9).getStringCellValue().trim().toUpperCase();
        Character exencion = exenc.charAt(0);

        //Unidad de cobro -> CVs(1), plazas(2), kgs(3), CCs(4)
        if (row.getCell(5) != null && row.getCell(5).getCellType() != CellType.BLANK && row.getCell(5).getNumericCellValue() != 0.0) {
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
        else if (row.getCell(6) != null && row.getCell(6).getCellType() != CellType.BLANK && row.getCell(6).getNumericCellValue() != 0.0) {
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
        else if (row.getCell(7) != null && row.getCell(7).getCellType() != CellType.BLANK && row.getCell(7).getNumericCellValue() != 0.0) {
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
        else if (row.getCell(8) != null && row.getCell(8).getCellType() != CellType.BLANK && row.getCell(8).getNumericCellValue() != 0.0) {
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
        v.setNifnifPropietario(nifPropietario);
        v.setTipoVehiculo(tipo);
        v.setMarca(marca);
        v.setModelo(modelo);
        v.setMatricula(matricula);
        v.setBastidor(bastidor);
        v.setFechaMatriculacion(fechaMatriculacion);
        v.setFechaAlta(fechaAlta);
        v.setFechaBaja(fechaBaja);
        v.setFechaBajaTemporal(fechaBajaTemp);
        v.setUnidadCobro(unidadCobro);
        v.setValorUnidad(valorUnidad);
        v.setExenc_bonif(exencion);

        vehiculosContribuyentesMap.computeIfAbsent(nifPropietario, k -> new ArrayList<>()).add(v);

        Vehiculos ve = new Vehiculos();
        Contribuyente c = contribuyentesPojosMap.get(encontrarIdContribuyente(nifPropietario, contribuyentesPojosMap));

        //ve.setIdVehiculo(idFila);
        ve.setContribuyente(c);
        ve.setTipo(tipo);
        ve.setMarca(marca);
        ve.setModelo(modelo);
        ve.setMatricula(matricula);
        ve.setNumeroBastidor(bastidor);

        switch (tipo) {
            case "TURISMO":
            case "TRACTOR":
                ve.setCaballosFiscales(valorUnidad);
                break;
            case "CAMION":
            case "REMOLQUE":
                ve.setKgcarga(valorUnidad);
                break;
            case "MOTOCICLETA":
            case "CICLOMOTOR":
                ve.setCentimetroscubicos(valorUnidad);
            case "AUTOBUS":
                ve.setPlazas(valorUnidad);
        }

        ve.setExencion(exencion);
        ve.setFechaMatriculacion(fechaMatriculacion);
        ve.setFechaAlta(fechaAlta);
        ve.setFechaBaja(fechaBaja);
        ve.setFechaBajaTemporal(fechaBajaTemp);

        vehiculosPojosContribuyentesMap.computeIfAbsent(matricula, k -> new ArrayList<>()).add(ve);
    }

    private String encontrarIdContribuyente (String nif, Map<String, Contribuyente> contribuyentesPojosMap) {
        for (Map.Entry<String, Contribuyente> entry : contribuyentesPojosMap.entrySet()) {
            Contribuyente contribuyente = entry.getValue();
            //System.out.println("Buscando contribuyente con NIF: " + nif);
            //System.out.println("Comparando con NIF existente: " + contribuyente.getNifnie());
            if (contribuyente.getNifnie().equalsIgnoreCase(nif)) {
                //System.out.println("Contribuyente encontrado con ID: " + entry.getKey());
                return entry.getKey();
            }
        }
        //System.out.println("Contribuyente no encontrado para NIF: " + nif);
        return null;


    }

}
