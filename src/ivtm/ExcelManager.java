package ivtm;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

/*Clase para la lectura y modificación de los ficheros Excel*/
public class ExcelManager {
    
    /*Lee el archivo excel indicado en la ruta que recibe y el número de hoja especificado*/
    public void readExcel(String filepath, int sheet) {
        try {
            FileInputStream file = new FileInputStream(filepath);
            XSSFWorkbook wb = new XSSFWorkbook(file);
            file.close(); //No es necesario mantenerlo abierto

            ValidadorNieDni validador = new ValidadorNieDni();

            //Verifica que el índice de la hoja sea correcto
            if (sheet < 0 || sheet >= wb.getNumberOfSheets()) {
                System.out.println("El número de hoja indicado no es válido. El archivo tiene " + wb.getNumberOfSheets() + " hojas.");
                wb.close();
                file.close();

                return;
            }

            //Si índice correcto -> Se lee la hoja
            XSSFSheet ws = wb.getSheetAt(sheet);
            System.out.println("\nLeyendo hoja \"" + ws.getSheetName() + "\"");

            //Iteración por las filas de la hoja
            Iterator<Row> rowIter = ws.iterator();

            //Para empezar en la fila 1
            if(rowIter.hasNext()){
                rowIter.next();
            }
            
            //El contador no es necesario, lee bien todas las filas
            while (rowIter.hasNext()) {
                Row row = rowIter.next();

                //Si la  fila esta vacia se las salta
                if(isEmpty(row)){
                    System.out.println("Fila sin datos\n");

                    continue;
                }

                Iterator<Cell> cellIter = row.cellIterator();
                //Celda del dni que se comprobará
                Cell dniCell = null;

                while (cellIter.hasNext()) {
                    Cell cell = cellIter.next();

                    //Imprime los valores de cada celda independientemente del documento y la hora
                    //DEPURACIÓN, se puede comentar más tarde.
                    if (cell.getCellType() == CellType.NUMERIC) {
                        System.out.print((int) cell.getNumericCellValue() + "\t");
                    }
                    else if (cell.getCellType() == CellType.STRING) {
                        System.out.print(cell.getStringCellValue() + "\t");
                    }

                    if (filepath.equals("resources\\SistemasVehiculos.xlsx") && sheet == 0 && cell.getColumnIndex() == 0) {
                        dniCell = cell;
                    }
                }

                System.out.println();

                //Si es SistemasVehiculos.xlsx, hoja 0 y columna 0 -> Comprueba DNIs/NIEs
                if (dniCell != null) {
                    validador.validaDNI(dniCell, filepath, row, row.getRowNum(), wb);
                }
            }

            wb.close();

            System.out.println("\nSe ha completado la lectura del archivo\n");

        }
        catch (IOException e) {
            System.out.println("Error al leer el archivo " + e.getMessage());
            e.printStackTrace();
        }
    }

    /*Metodo sobrecargado que lee la primera hoja si solo se indica la ruta del archivo*/
    public void readExcel(String filepath) {
        readExcel(filepath, 0);
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
            /*
            * Comprobar si celda tipo String y el valor es número y
            * si la celda es numeric y el valor String
            *
            * HAY QUE HACER LA CONVERSIÓN -> El número sin décimales como en ValidadorNieDni
            * dniNie = String.format("%.0f", celdaComprobar.getNumericCellValue());
            *
            * */
            if (cellType == CellType.STRING && newVal instanceof String) {
                cell.setCellValue((String) newVal);
                System.out.println("El nuevo valor de la celda tras modificarse es: " + newVal + " [String]");
            }
            else if (cellType == CellType.NUMERIC && newVal instanceof Number) {
                cell.setCellValue(((Number) newVal).doubleValue()); // Los números en Apache POI se almacenan como double
                System.out.println("El nuevo valor de la celda tras modificarse es: " + newVal + " [Number]");
            }
            else {
                //Si la celda es de tipo BLANK (no tiene un tipo de dato establecidon)
                if (cellType == CellType.BLANK && newVal instanceof String) {
                    cell.setCellValue((String) newVal);
                    System.out.println("El valor de la nueva celda creada es: " + newVal + " [String]");
                }
                else if (cellType == CellType.BLANK && newVal instanceof Number) {
                    cell.setCellValue(((Number) newVal).doubleValue());
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
