package ivtm;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

/*Clase para la lectura y modificación de los ficheros Excel*/
public class ExcelManager {

    /*Lee el archivo excel indicado en la ruta que recibe y el número de hoja especificado*/
    public void readExcel(String filepath, int sheet) {
        try {
            FileInputStream file = new FileInputStream(new File(filepath));
            XSSFWorkbook wb = new XSSFWorkbook(file);

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
            int contador=0;
            while (rowIter.hasNext()) {
                contador++;
                Row row = rowIter.next();

                //Comprueba los dni de cada fila GONZALO
                Cell celdaComprobar= row.getCell(0);
                if(celdaComprobar!=null){
                    //Si las celdas no son siempre String descomentar esto
                    String dniNie="";
                    if(celdaComprobar.getCellType()==CellType.STRING){
                        dniNie= celdaComprobar.getStringCellValue();

                    }else if(celdaComprobar.getCellType()==CellType.NUMERIC){
                        dniNie = String.format("%.0f", celdaComprobar.getNumericCellValue()); 
                      
                    }
                    if(ValidadorNieDni.dniNie(dniNie)==1){
                        System.out.println("Es un DNI");
                        if(ValidadorNieDni.dniValido(dniNie,row, contador)==true){
                            System.out.println("El campo es valido");
                        }else{
                            ValidadorNieDni.dniValido(dniNie,row, contador);
                            //  Aqui hay que hacer que se escriba en el otro fichero que se encuentra en resources
                            System.out.println("El dni no es  correcto");
                        }
                    }else if(ValidadorNieDni.dniNie(dniNie)==2){
                        System.out.println("Es un Nie");
                        if(ValidadorNieDni.nieValido(dniNie,row, contador)==true){
                            System.out.println("El campo es valido");
                        }else{
                            ValidadorNieDni.dniValido(dniNie,row, contador);
                            //Lo mismo que en el caso 1
                            System.out.println("El nie no es  correcto");
                        }

                    }else if(ValidadorNieDni.dniNie(dniNie)==-1){
                        //Lomissmo que en el caso 2
                        ValidadorNieDni.dniValido(dniNie,row, contador);
                        System.out.println("No se trata ni de un Nie ni de un DNI");
                    }
                }else if(celdaComprobar.getCellType()!=CellType.BLANK){
                    
                }

                Iterator<Cell> cellIter = row.cellIterator();

                while (cellIter.hasNext()) {
                    Cell cell = cellIter.next();

                    //Imprime los valores de cada celda --> DEPURACIÓN, se puede comentar más tarde.
                    if (cell.getCellType() == CellType.NUMERIC) {
                        System.out.print((int) cell.getNumericCellValue() + "\t");
                    }
                    else if (cell.getCellType() == CellType.STRING) {
                        System.out.print(cell.getStringCellValue() + "\t");
                    }
                }
                System.out.println();
            }

            wb.close();
            file.close();

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
    * Metodo que permita modificar SistemasVehiculos.xlsx
    * Recibe la fila y la columna de la celda que se desea modificar
    * También recibe el nuevo valor que modificará la celda
    */
    public static void updateExcel (int row_, int col_, Object newVal) {
        //Ruta al documento y número de hoja de los contribuyentes
        String filepath = "resources\\SistemasVehiculos.xlsx";

        try {
            FileInputStream file = new FileInputStream(new File(filepath));
            XSSFWorkbook wb = new XSSFWorkbook(file);
            file.close(); //No es necesario mantener abierto el stream de lectura

            Sheet sheet = wb.getSheetAt(0); //Index 0 -> Hoja 1 -> Contribuyentes

            //Obtiene la fila, o la crea si no existe
            Row row = sheet.getRow(row_);
            if (row == null) {
                row = sheet.createRow(row_);
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
            wb.close();

            System.out.println("El valor de la celda " + row_ + "-" + col_ +" ha sido modificado correctamente\n");

        } catch (IOException e) {
            System.out.println("Error al modificar el archivo " + e.getMessage());
            e.printStackTrace();
        }

    }
}
