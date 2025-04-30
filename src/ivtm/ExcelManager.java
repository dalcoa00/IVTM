package ivtm;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.HashSet;

/*Clase para la lectura y modificación de los ficheros Excel*/
public class ExcelManager {
    HashSet<String> dniSet = new HashSet<>();
    HashSet<String> cccSet = new HashSet<>();
    HashSet<String> correoSet = new HashSet<>();
    HashSet<String> matriculaSet = new HashSet<>();

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
                System.out.println("Número de DNIs leídos: " + dniLeidos);
                System.out.println("Número de elementos almacenados en dniSet: " + dniSet.size());
                System.out.println("Número de elementos almacenados en cccSet: " + cccSet.size());
                System.out.println("Número de elementos almacenados en el correoSet: " +correoSet.size());
                //Despues de leer el documento, vació el HashSet para no consumir memoria
                dniSet.clear();
                cccSet.clear();
                correoSet.clear();

                //Si no se va a utilizar, se puede eliminar la referencia
                /*dniSet = null;
               cccSet = null;
               correoSet= null;*/

            }
            catch (IOException e) {
                System.out.println("Error al leer el archivo " + e.getMessage());
                e.printStackTrace();
            }
        }
        //Hoja "Vehiculos" -> Comprobación de matrículas
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

                    while (cellIter.hasNext()) {
                        Cell cell = cellIter.next();

                        if (cell.getCellType() == CellType.NUMERIC) {
                            System.out.print((int) cell.getNumericCellValue() + "\t");
                        }
                        else if (cell.getCellType() == CellType.STRING) {
                            System.out.print(cell.getStringCellValue() + "\t");
                        }

                        if (cell.getColumnIndex() == 0) {
                            tipoVehiculoCell = cell;
                        }

                        if (cell.getColumnIndex() == 3) {
                            matriculaCell = cell;
                        }

                    }

                    //Valido los datos del vehículo cuando me aseguro de que tengo un tipo y matrícula
                    if (matriculaCell != null && tipoVehiculoCell != null) {
                        validaVehiculo.comprobarVehiculo(wb, row, matriculaSet, dniSet, matriculaCell, tipoVehiculoCell);
                    }
                    else {
                        System.out.println("No es posible comprobar los datos del vehículo.");
                    }

                }

                wb.close();

                matriculaSet.clear();

                System.out.println("\nSe ha completado la lectura del archivo\n");
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

    //Metodo que comprueba si una fila esta vacia o no
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
}
