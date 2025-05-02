package ivtm;

import modelosExcel.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/*Clase para la lectura y modificación de los ficheros Excel*/
public class ExcelManager {
    HashSet<String> dniSet = new HashSet<>();
    HashSet<String> cccSet = new HashSet<>();
    HashSet<String> correoSet = new HashSet<>();
    HashSet<String> matriculaSet = new HashSet<>();

    //Mapas para relacionar contribuyentes y sus vehículos para generar los recibos
    Map<String, ContribuyenteExcel> contribuyentes = new HashMap<>();
    Map<String, List<VehiculoExcel>> vehiculosContribuyentes = new HashMap<>();

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
                        validadorCCC.comprobarCCC(row, wb, filepath, sheet, cccSet, dniSet, correoSet, dniCell, cccCell);
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

                /******************************************************************************************************/
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
                        validaVehiculo.comprobarVehiculo(wb, row, matriculaSet, dniSet, matriculaCell, tipoVehiculoCell, fechaMatriculacionCell, fechaAltaCell, fechaBajaCell, fechaBajaTempCell, nifPropietarioCell);
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
        /*
        *Otro else-if para leer SistemaOrdenanzas
        * Y almacenar en algún sitio los datos que se necesitan para generar el recibo
        * y calcular el precio de lo que debería de pagar
        * */
        else {
            System.out.println("Ruta u hoja errónea.");
        }

        /*********************************************************************************************************/
        /*
        * Una vez comprobados todos los contribuyentes y vehiculos correctos
        * Los HashSets se mantienen porque sirven para comprobar otros datos
        *
        * Con (ya declarados al principio):
        * Map <String, Contribuyente> contribuyentes = new HashMap<>()
        * Map <String, List<Vehiculo> vehiculosContribuyente = new HashMap<>()>
        *
        * Cuando un contribuyente es correcto, tras validar CCC, IBAN, etc. se añade a contribuyentes con todos
        * los datos necesarios para generar el recibo (Tras lo último que haga ValidadorCCC creo recordar)
        *
        * Cuando el vehiculo es correcto, se busca el dni del propietario en HashMap contribuyentes, si está se añade
        * el vehiculo a vehiculosContribuyentes con todos los datos necesarios para generar el recibo (En ValidadorVehiculo)
        *
        * De este modo, los datos del vehiculo y su contribuyente estarán relacionados y se podrán generar los recibos
        * sin necesidad de volver a abri los documentos excel*/
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

    /*Metodo que limpia los sets al finalizar la ejecución*/
    public void cleanSets() {
        //Despues de leer el documento, vació el HashSet para no consumir memoria
        dniSet.clear();
        cccSet.clear();
        correoSet.clear();
        matriculaSet.clear();

        //Si no se va a utilizar, se puede eliminar la referencia
        /*dniSet = null;
        cccSet = null;
        correoSet= null;*/
    }
}
