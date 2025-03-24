package ivtm;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import java.io.File;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.HashSet;

public class ValidadorNieDni {
    static ExcelManager manager= new ExcelManager();
    static EditorXML editor = new EditorXML();
    private static final String ruta = "resources\\ErroresNifNie.xml";

    //Metodo que comprueba si es dni o nie, dni=1, nie=2 ninguno -1
    public static int dniNie(String comprobar) {
        if (comprobar.matches("^[XYZ][0-9]{7}[A-Z]$")) {
            return 2;
        }
        // Verifica si es un DNI (8 números + 1 letra)
        if (comprobar.matches("^[0-9]{8}[A-Z]$")) {
            return 1;
        }
        return -1;
    }

    //Metodo  que comprueba que el campo del dni no es null
    public static boolean dniVacio(String dni) {
        if (dni.length() == 0 || dni == "" || dni == null) {
            System.out.println("El campo del dni se encuentra vacio");
            return true;
        } else {
            return false;
        }
    }

    //Metodo que comprueba que el campo nie no es null
    public static boolean nieVacio(String nie) {
        if (nie.length() == 0 || nie == "" || nie == null) {
            System.out.println("El campo del Nie se encuentra vacio");
            return true;
        } else {
            return false;
        }
    }

    //Metodo que comprueba si el nie es valido o no devolviendo true o false, tambien actualiza letra
    public static boolean nieValido(String nie, Row fila, int contador) {
        if (nieVacio(nie)) {
            editor.xmlDniNie(ruta, contador, fila.getCell(0).getStringCellValue(), fila.getCell(3).getStringCellValue(), fila.getCell(1).getStringCellValue(), fila.getCell(2).getStringCellValue(), "NIF BLANCO");
            return false;
        }
        if (nie.length() != 9) {
            editor.xmlDniNie(ruta, contador, fila.getCell(0).getStringCellValue(), fila.getCell(3).getStringCellValue(), fila.getCell(1).getStringCellValue(), fila.getCell(2).getStringCellValue(), "NIF ERRONEO");
            return false;
        }
        String letraInicioNie = nie.substring(0, 1).toUpperCase();
        if (letraInicioNie != "X" && letraInicioNie != "Y" && letraInicioNie != "Z") {
            System.out.println("El Nie no tiene el formato   correcto (letra  inicial)");
            editor.xmlDniNie(ruta, contador, fila.getCell(0).getStringCellValue(), fila.getCell(3).getStringCellValue(), fila.getCell(1).getStringCellValue(), fila.getCell(2).getStringCellValue(), "NIF ERRONEO");
            return false;
        }
        String nieNumero = nie.substring(1, 8);
        if (!nieNumero.matches("\\d{7}")) {
            System.out.println("El Nie no tiene el formato correcto (numero)");
            editor.xmlDniNie(ruta, contador, fila.getCell(0).getStringCellValue(), fila.getCell(3).getStringCellValue(), fila.getCell(1).getStringCellValue(), fila.getCell(2).getStringCellValue(), "NIF ERRONEO");
            return false;
        }
        char nieLetra = nie.charAt(8);
        if (nieLetra != letraCorrectaNie(letraInicioNie, nieNumero)) {
            ExcelManager.updateExcel(contador, 0, letraInicioNie + nieNumero + letraCorrectaNie(letraInicioNie, nieNumero));
            System.out.println("El nie tenia el formato correcto pero la letra no coincidia, se ha modificado la letra");
        }
        //Comprueba que la letra sea correcta
        //Si no letra correcta se modifica la letra y ver en la hoja del la practica lo que tiene que escribir en el archuivo
        System.out.println("El nie es correcto");
        return true;
    }

    //Metodo que  comprueba si el dni es valido o no devolviendo true o false, tambien actualiza letra
    public static boolean dniValido(String dni, Row fila, int contador) {
        //
        if (dniVacio(dni)) {
            editor.xmlDniNie(ruta, contador, fila.getCell(0).getStringCellValue(), fila.getCell(3).getStringCellValue(), fila.getCell(1).getStringCellValue(), fila.getCell(2).getStringCellValue(), "NIF BLANCO");

            return false;
        }
        if (dni.length() > 9 || dni.length() < 8) {
            if (fila.getCell(0).getCellType() == CellType.NUMERIC) {
                String aux = String.format("%.0f", fila.getCell(0).getNumericCellValue());


                //ESTA LINEA PUEDE DAR PROBLEMAS EN UN FUTURO MUCHO CUIDADO

                editor.xmlDniNie(ruta, contador, aux, fila.getCell(3).getStringCellValue(), fila.getCell(1).getStringCellValue(), fila.getCell(2).getStringCellValue(), "NIF ERRONEO");
                return false;
            }
            editor.xmlDniNie(ruta, contador, fila.getCell(0).getStringCellValue(), fila.getCell(3).getStringCellValue(), fila.getCell(1).getStringCellValue(), fila.getCell(2).getStringCellValue(), "NIF ERRONEO");
            return false;
        }
        //Comprueba que el dni tenga el formato corrrecto 8 numero
        String dniNumero = dni.substring(0, 8);
        if (!dniNumero.matches("\\d{8}")) {
            editor.xmlDniNie(ruta, contador, fila.getCell(0).getStringCellValue(), fila.getCell(3).getStringCellValue(), fila.getCell(1).getStringCellValue(), fila.getCell(2).getStringCellValue(), "NIF ERRONEO");
            System.out.println("El dni no tiene el formato correcto (numero)");

            return false;
        }
        //Comprueba  que el noveno carcater sea letra
        char dniLetra = dni.charAt(8);
        if (!Character.isLetter(dniLetra)) {
            editor.xmlDniNie(ruta, contador, fila.getCell(0).getStringCellValue(), fila.getCell(3).getStringCellValue(), fila.getCell(1).getStringCellValue(), fila.getCell(2).getStringCellValue(), "NIF ERRONEO");

            System.out.println("El dni  no tiene el  formato correcto (letra)");
            return false;
        }
        if (dniLetra != letraCorrecta(dniNumero)) {
            //Se corrige la celdacon la letra correcta----Comprobar si lo  actualiza directamente en el excel 
            ExcelManager.updateExcel(contador, 0, dniNumero + letraCorrecta(dniNumero));
            System.out.println("El dni tenia el formato correcto pero la letra no coincidia, se ha modificado la letra");
            return false;
        }
        //Comprueba que la letra sea correcta
        //Si no letra correcta se modifica la letra y ver en la hoja del la practica lo que tiene que escribir en el archuivo
        System.out.println("El dni es correcto");
        return true;
    }

    //Metodo que comprueba la letra correcta de un nie
    public static char letraCorrectaNie(String letraInicio, String numeroNie) {
        String aux = "";
        if (letraInicio.toUpperCase() == "X") {
            aux = "0";
        } else if (letraInicio.toUpperCase() == "Y") {
            aux = "1";
        } else if (letraInicio.toUpperCase() == "Z") {
            aux = "2";
        }
        String letraYNumero = aux + numeroNie;
        int nieNumero = Integer.parseInt(letraYNumero);
        char[] letras = "TRWAGMYFPDXBNJZSQVHLCKE".toCharArray();
        return letras[nieNumero % 23];
    }

    //Metodo que comprueba la letra correcta de un dni
    public static char letraCorrecta(String numeros) {
        int dniNumero = Integer.parseInt(numeros);
        char[] letras = "TRWAGMYFPDXBNJZSQVHLCKE".toCharArray();
        return letras[dniNumero % 23];
    }


    public void validarExcelDni(String filepath, int sheet) {
        try {
            FileInputStream file = new FileInputStream(new File(filepath));
            XSSFWorkbook wb = new XSSFWorkbook(file);

            if (sheet < 0 || sheet >= wb.getNumberOfSheets()) {
                System.out.println("El número de hoja indicado no es válido. El archivo tiene " + wb.getNumberOfSheets() + " hojas.");
                wb.close();
                file.close();
                return;
            }

            XSSFSheet ws = wb.getSheetAt(sheet);
            HashSet<String> dniSet = new HashSet<>();
            System.out.println("\nLeyendo hoja \"" + ws.getSheetName() + "\" con validación de DNI/NIE");

            Iterator<Row> rowIter = ws.iterator();
            if (rowIter.hasNext()) {
                rowIter.next(); // Saltar la primera fila (encabezado)
            }
            int contador = 1;
            while (rowIter.hasNext() && contador <= 500) {
                contador++;
                Row row = rowIter.next();

                if (manager.isEmpty(row)) {
                    continue;
                }

                Cell celdaComprobar = row.getCell(0);
                if (celdaComprobar != null) {
                    String dniNie = "";
                    if (celdaComprobar.getCellType() == CellType.STRING) {
                        dniNie = celdaComprobar.getStringCellValue();
                    } else if (celdaComprobar.getCellType() == CellType.NUMERIC) {
                        dniNie = String.format("%.0f", celdaComprobar.getNumericCellValue());
                    }

                    if(dniNie(dniNie)==1){
                        System.out.println("Es un DNI");
                        if(dniValido(dniNie,row, contador)==true){
                            System.out.println("El campo es valido");
                        }else{
                            dniValido(dniNie,row, contador);
                            //  Aqui hay que hacer que se escriba en el otro fichero que se encuentra en resources
                            System.out.println("El dni no es  correcto");
                        }
                    }else if(dniNie(dniNie)==2){
                        System.out.println("Es un Nie");
                        if(nieValido(dniNie,row, contador)==true){
                            System.out.println("El campo es valido");
                        }else{
                            dniValido(dniNie,row, contador);
                            //Lo mismo que en el caso 1
                            System.out.println("El nie no es  correcto");
                        }

                    }else if(dniNie(dniNie)==-1){
                        //Lomissmo que en el caso 2
                        dniValido(dniNie,row, contador);
                        System.out.println("No se trata ni de un Nie ni de un DNI");
                    }
                    //Devuelve falso si ya esta añadido
                    if(celdaComprobar.getCellType()!=CellType.BLANK){
                        if(!dniSet.add(dniNie)){
                            editor.xmlDniNie(ruta, contador, row.getCell(0).getStringCellValue(), row.getCell(3).getStringCellValue(), row.getCell(1).getStringCellValue(), row.getCell(2).getStringCellValue(), "NIF DUPLICADO");
                            System.out.println("El dni esta repetido");
                        }
                    }
                }

            }
            wb.close();
            file.close();
            System.out.println("\nSe ha completado la lectura y validación del archivo\n");

        } catch (IOException e) {
            System.out.println("Error al leer el archivo " + e.getMessage());
            e.printStackTrace();
        }
    }
}


