package ivtm;

import modelosExcel.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/*Clase para la lectura y modificación de los ficheros Excel*/
public class ExcelManager {
    HashSet<String> dniSet = new HashSet<>();
    HashSet<String> cccSet = new HashSet<>();
    HashSet<String> correoSet = new HashSet<>();
    HashSet<String> matriculaSet = new HashSet<>();

    //Mapas para relacionar contribuyentes y sus vehículos para generar los recibos
    Map<String, ContribuyenteExcel> contribuyentesMap = new HashMap<>(); // <- Hoja Contribuyentes
    Map<String, List<VehiculoExcel>> vehiculosContribuyentesMap = new HashMap<>(); // <- Hoja vehiculos

    /*Lee el archivo excel indicado en la ruta que recibe y el número de hoja especificado*/
    public void readExcel(String filepath, int sheet) {
        //Hoja "Contribuyentes" -> Comprobación de DNIs, CCCs y generación de e-mails e IBANs
        if (filepath.equals("resources\\SistemasVehiculos.xlsx") && sheet == 0) {
            try {
                FileInputStream file = new FileInputStream(filepath);
                XSSFWorkbook wb = new XSSFWorkbook(file);
                file.close(); //No es necesario mantenerlo abierto

                ValidadorNieDni validadorNieDni = new ValidadorNieDni();
                ValidadorCCC validadorCCC= new ValidadorCCC();

                //Se lee la hoja
                XSSFSheet ws = wb.getSheetAt(sheet);
                System.out.println("\nLeyendo hoja \"" + ws.getSheetName() + "\"");

                //Iteración por las filas de la hoja
                Iterator<Row> rowIter = ws.iterator();

                //Para empezar en la fila 1
                if(rowIter.hasNext()){
                    rowIter.next();
                }

                int dniLeidos = 0;

                //Va fila por fila
                while (rowIter.hasNext()) {
                    Row row = rowIter.next();

                    //Si la  fila esta vacia se las salta
                    if(isEmpty(row)){
                        System.out.println("Fila sin datos\n");

                        continue;
                    }

                    Iterator<Cell> cellIter = row.cellIterator();

                    //Celdas que se comprobarán
                    Cell dniCell = null;
                    Cell cccCell = null;

                    //Lee columna por columna (celdas de la fila)
                    while (cellIter.hasNext()) {
                        Cell cell = cellIter.next();

                        if (cell.getCellType() == CellType.NUMERIC) {
                            System.out.print((int) cell.getNumericCellValue() + "\t");
                        }
                        else if (cell.getCellType() == CellType.STRING) {
                            System.out.print(cell.getStringCellValue() + "\t");
                        }

                        if (cell.getColumnIndex() == 0) {
                            dniCell = cell;
                        }

                        if (cell.getColumnIndex() == 9) {
                            cccCell = cell;
                        }
                    }

                    System.out.println();

                    //Valida el DNI/NIE si la celda 0 es no nula
                    if (dniCell != null) {
                        dniLeidos++;
                        validadorNieDni.validaDNI(dniCell, filepath, row, row.getRowNum() + 1, wb, dniSet);
                    }

                    //Valida el CCC si la celda 9 es no nula
                    //Genera IBAN y correo si NIF/NIE y CCC correctos o subsanados
                    if (cccCell != null) {
                        validadorCCC.comprobarCCC(row, wb, filepath, sheet, cccSet, dniSet, correoSet, dniCell, cccCell, contribuyentesMap);
                    }

                }

                wb.close();

                System.out.println("\nSe ha completado la lectura del archivo\n");

                //DEPURACION
                System.out.println("Número de NIFs leídos: " + dniLeidos);
                System.out.println("Número de NIFs correctos o subsanados: " + dniSet.size());
                System.out.println("Número de CCCs correctos o subsanados: " + cccSet.size());
                System.out.println("Número de e-mails generados: " + correoSet.size());
            }
            catch (IOException e) {
                System.out.println("Error al leer el archivo " + e.getMessage());
                e.printStackTrace();
            }
        }
        //Hoja "Vehiculos" -> Comprobación de vehiculos
        else if (filepath.equals("resources\\SistemasVehiculos.xlsx") && sheet == 1) {
            try {
                FileInputStream file = new FileInputStream(filepath);
                XSSFWorkbook wb = new XSSFWorkbook(file);
                file.close(); //No es necesario mantenerlo abierto

                //Se lee la hoja
                XSSFSheet ws = wb.getSheetAt(sheet);
                System.out.println("\nLeyendo hoja \"" + ws.getSheetName() + "\"");

                //Iteración por las filas de la hoja
                Iterator<Row> rowIter = ws.iterator();

                ValidadorVehiculo validaVehiculo = new ValidadorVehiculo();

                //Para empezar en la fila 1
                if(rowIter.hasNext()){
                    rowIter.next();
                }

                //Lee columna por columna de cada fila
                while (rowIter.hasNext()) {
                    Row row = rowIter.next();

                    //Si la  fila esta vacia se las salta
                    if(isEmpty(row)){
                        System.out.println("Fila sin datos\n");

                        continue;
                    }

                    Iterator<Cell> cellIter = row.cellIterator();

                    Cell tipoVehiculoCell = null;
                    Cell matriculaCell = null;
                    Cell fechaMatriculacionCell = null;
                    Cell fechaAltaCell = null;
                    Cell fechaBajaCell = null;
                    Cell fechaBajaTempCell = null;
                    Cell nifPropietarioCell = null;

                    System.out.println();

                    while (cellIter.hasNext()) {
                        Cell cell = cellIter.next();

                        if ((cell.getColumnIndex() == 0 || cell.getColumnIndex() == 3) && cell.getCellType() == CellType.NUMERIC) {
                            //Si es celda formato fecha
                            if (DateUtil.isCellDateFormatted(cell)) {
                                System.out.print(cell.getDateCellValue() + "\t");
                            } else {
                                System.out.print((int) cell.getNumericCellValue() + "\t");
                            }
                        }
                        else if ((cell.getColumnIndex() == 0 || cell.getColumnIndex() == 3) && cell.getCellType() == CellType.STRING) {
                            System.out.print(cell.getStringCellValue() + "\t");
                        }

                        if (cell.getColumnIndex() == 0) {
                            tipoVehiculoCell = cell;
                        }

                        if (cell.getColumnIndex() == 3) {
                            matriculaCell = cell;
                        }

                        if(cell.getColumnIndex() == 10) {
                            fechaMatriculacionCell = cell;
                        }

                        if(cell.getColumnIndex() == 11) {
                            fechaAltaCell = cell;
                        }

                        if(cell.getColumnIndex() == 12) {
                            fechaBajaCell = cell;
                        }

                        if(cell.getColumnIndex() == 13) {
                            fechaBajaTempCell = cell;
                        }

                        if(cell.getColumnIndex() == 14) {
                            nifPropietarioCell = cell;
                        }
                    }

                    //Comprueba que los datos del vehiculo son correctos
                    if (matriculaCell != null && tipoVehiculoCell != null) {
                        validaVehiculo.comprobarVehiculo(wb, row, matriculaSet, dniSet, matriculaCell, tipoVehiculoCell, fechaMatriculacionCell, fechaAltaCell, fechaBajaCell, fechaBajaTempCell, nifPropietarioCell, vehiculosContribuyentesMap);
                    }
                    else {
                        System.out.println("No es posible comprobar los datos del vehículo.");
                    }

                }

                wb.close();

                System.out.println("\nSe ha completado la lectura del archivo\n");

                //DEPURACION
                System.out.println("Número de matrículas correctas: " + matriculaSet.size());
            }
            catch (IOException e) {
                System.out.println("Error al leer el archivo " + e.getMessage());
                e.printStackTrace();
            }
        }
        /*Lee la hoja Ordenanza para determinar el importe a pagar por cada vehiculo*/
        else if (filepath.equals("resources\\SistemasOrdenanzas.xlsx") && sheet == 0) {
            try {
                FileInputStream file = new FileInputStream(filepath);
                XSSFWorkbook wb = new XSSFWorkbook(file);
                file.close(); //No es necesario mantenerlo abierto

                //Se lee la hoja
                XSSFSheet ws = wb.getSheetAt(sheet);
                System.out.println("\nSe van a calcular los importes correspondientes a cada vehículo \"" + ws.getSheetName() + "\"");

                //Pido por pantalla el año del que se quieren generar los recibos
                System.out.println("\n\n\nIntroduce el año del que se quieren generar los recibos con formato \"aaaa\" (ej. 2024): ");
                int anio = solicitaAnyo();

                ImporteRecibo importe = new ImporteRecibo();
                importe.calculaImporte(ws, vehiculosContribuyentesMap, contribuyentesMap, anio);

                wb.close();

                /*Imprime por pantalla los recibos de los vehículos del año solicitado*/
                printContribuyentesVehiculos(contribuyentesMap, vehiculosContribuyentesMap, anio);
            }
            catch (IOException e) {
                System.out.println("Error al leer el archivo " + e.getMessage());
                e.printStackTrace();
            }
        }
        else {
            System.out.println("Ruta u hoja errónea.");
        }
    }

    /*
    * Metodo que permite modificar el libro que se está leyendo con la ruta pasada como parámetro
    * Recibe la fila y la columna de la hoja que se desea modificar
    * También recibe el nuevo valor que modificará la celda
    */
    public void updateExcel (XSSFWorkbook wb, String filepath, int sheet, int row_, int col_, Object newVal) {
        try {
            Sheet hojaModif = wb.getSheetAt(sheet); //Index 0 -> Hoja 1 -> Contribuyentes

            //Obtiene la fila, o la crea si no existe
            Row row = hojaModif.getRow(row_);
            if (row == null) {
                row = hojaModif.createRow(row_);
            }

            //Obtiene la celda, o la crea si no existe
            Cell cell = row.getCell(col_);
            if (cell == null) {
                cell = row.createCell(col_);
            }

            //Obtiene el tipo de dato original de la celda -> No se pueden cambiar los tipos de datos
            Object cellType = cell.getCellType();
            System.out.println("La celda que se quiere modificar es de tipo " + cellType);

            //Modifica el valor de la celda según el tipo de dato de la misma
            if (cellType == CellType.STRING && newVal instanceof String) {
                cell.setCellValue((String) newVal);
                System.out.println("El nuevo valor de la celda tras modificarse es: " + newVal + " [String]");
            }
            else if (cellType == CellType.NUMERIC && newVal instanceof Number) {
                cell.setCellValue(((Number) newVal).longValue()); // Los números en Apache POI se almacenan como double
                System.out.println("El nuevo valor de la celda tras modificarse es: " + newVal + " [Number]");
            }
            else if (cellType == CellType.STRING && newVal instanceof Number) {
                cell.setCellValue(String.format("%.0f", ((Number) newVal).doubleValue())); // Los números en Apache POI se almacenan como double
                System.out.println("El nuevo valor de la celda tras modificarse es: " + newVal + " [Number]");
            }
            else if (cellType == CellType.NUMERIC && newVal instanceof String) {
                cell.setCellValue(Double.parseDouble((String) newVal)); // Los números en Apache POI se almacenan como double
                System.out.println("El nuevo valor de la celda tras modificarse es: " + newVal + " [Number]");
            }
            else {
                //Si la celda es de tipo BLANK
                if (cellType == CellType.BLANK && newVal instanceof String) {
                    cell.setCellValue((String) newVal);
                    System.out.println("El valor de la nueva celda creada es: " + newVal + " [String]");
                }
                else if (cellType == CellType.BLANK && newVal instanceof Number) {
                    cell.setCellValue(((Number) newVal).longValue());
                    System.out.println("El valor de la nueva celda creada es: " + newVal + " [Number]");
                }
                else {
                    throw new IllegalArgumentException("Tipo de dato incompatible con el tipo de celda existente.");
                }
            }

            //Guarda los cambios en el archivo
            FileOutputStream out = new FileOutputStream(filepath);
            wb.write(out);
            out.close();

            System.out.println("El valor de la celda " + row_ + "-" + col_ +" ha sido modificado correctamente\n");

        } catch (IOException e) {
            System.out.println("Error al modificar el archivo " + e.getMessage());
            e.printStackTrace();
        }

    }

    /*Metodo que comprueba si una fila esta vacia o no*/
    public static boolean isEmpty(Row comprobar){
        if (comprobar == null){

            return true;
        }

        for(Cell cell: comprobar){
            if (cell.getCellType() != CellType.BLANK && cell.getCellType() != CellType._NONE){

                return false;
            }
        }

        return true;
    }

    /*Pide al usuario el año del que se quieren generar los recibos*/
    private int solicitaAnyo () {
        Scanner scan = new Scanner(System.in);

        int anio = scan.nextInt();

        scan.close();

        return anio;
    }

    /* Metodo que imprime los recibos por pantalla */
    private void printContribuyentesVehiculos(Map<String, ContribuyenteExcel> contribuyentesMap, Map<String, List<VehiculoExcel>> vehiculosContribuyentesMap, int anio) {
        //Obtengo el día de hoy (Día que se genera el recibo)
        LocalDate hoy = LocalDate.now();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        System.out.println("\n---------------  RECIBOS GENERADOS  ---------------\n");

        int totalVehiculos = 0;
        double totalPadron = 0.0;

        for (Map.Entry<String, List<VehiculoExcel>> entry : vehiculosContribuyentesMap.entrySet()) {
            String nif = entry.getKey();
            List<VehiculoExcel> vehiculos = entry.getValue();
            ContribuyenteExcel c = contribuyentesMap.get(nif);

            if (c == null) continue; //Por si acaso, aunque no debería de haber ningún contribuyente nulo

            for (VehiculoExcel v : vehiculos) {
                //Imprime los datos del contribuyente/propietario
                System.out.println("Nombre: " + c.getNombre());
                System.out.println("Apellido 1: " + c.getApellido1());
                System.out.println("Apellido 2: " + c.getApellido2());
                System.out.println("NIFNIE: " + c.getNifnie());
                System.out.println("Direccion: " + c.getDireccion());
                System.out.println("IBAN: " + c.getIban());
                System.out.println("Bonificacion: " + c.getBonificacion());

                //Fecha del recibo y del padrón
                System.out.println("\nFecha del recibo: " + hoy.format(formato) + "\n"); //Día que ha sido generado el recibo

                String unidadCobro;
                //Paso la unidad de cobro a String para imprimir
                switch (v.getUnidadCobro()) {
                    case 1:
                        unidadCobro = "CABALLOS";
                        break;
                    case 2:
                        unidadCobro = "PLAZAS";
                        break;
                    case 3:
                        unidadCobro = "KG";
                        break;
                    case 4:
                        unidadCobro = "CC";
                        break;
                    default:
                        unidadCobro = "No definido";
                        break;
                }

                //System.out.println("Propietario: " + v.getNifPropietario());
                System.out.println("Tipo de vehículo: " + v.getTipoVehiculo());
                System.out.println("Marca: " + v.getMarca());
                System.out.println("Modelo: " + v.getModelo());
                System.out.println("Matricula: " + v.getMatricula());
                System.out.println("Número de bastidor: " + v.getBastidor());
                System.out.println("Unidad de cobro: " + unidadCobro);
                System.out.println("Valor de la unidad de cobro: " + v.getValorUnidad());
                System.out.println("Importe (sin exenciones ni bonificaciones): " + v.getImporte());
                System.out.println("Exencion/Bonificación: " + v.getExencion());
                System.out.println("\nImporte total del recibo: " + v.getTotal());
                System.out.println("\n---------------------------------------------------------------------------\n");

                totalVehiculos++;
                totalPadron = totalPadron + v.getTotal();
            }
        }

        System.out.println("Número de vehículos para los que se ha generado un recibo: " + totalVehiculos);
        System.out.println("Número de contribuyentes a los que se les ha generado un recibo: " + vehiculosContribuyentesMap.size());
        System.out.println("\nFecha del padrón: 01/01/" + anio); //Siempre el 1 de enero del año solicitado
        System.out.println("Importe total del padrón (Suma de todos los recibos generados): " + totalPadron + "€");
    }

    /*Metodo que limpia los sets al finalizar la ejecución*/
    public void cleanSets() {
        //Despues de leer el documento, vació el HashSet para no consumir memoria
        dniSet.clear();
        cccSet.clear();
        correoSet.clear();
        matriculaSet.clear();
        contribuyentesMap.clear();
        vehiculosContribuyentesMap.clear();

        //Si no se va a utilizar, se puede eliminar la referencia
        /*dniSet = null;
        cccSet = null;
        correoSet= null;*/
    }


}
