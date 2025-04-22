package ivtm;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.util.HashSet;

public class ValidadorNieDni {
    ExcelManager manager = new ExcelManager();
    EditorXML editor = new EditorXML();
    private final String rutaXML = "resources\\ErroresNifNie.xml";

    public void validaDNI(Cell celdaComprobar, String ruta, Row row, int numFila, XSSFWorkbook wb, HashSet<String> dniSet) {
        String dniNie = "";

        if (celdaComprobar.getCellType() == CellType.STRING) {
            dniNie = celdaComprobar.getStringCellValue();
        } else if (celdaComprobar.getCellType() == CellType.NUMERIC) {
            dniNie = String.format("%.0f", celdaComprobar.getNumericCellValue());
        }

        if (dniNie(dniNie) == 1) {
            System.out.println("Es un DNI");

            if (!dniValido(dniNie, wb, ruta, row, numFila, dniSet)) {
                System.out.println("El campo no es valido\n");
            }
        } else if (dniNie(dniNie) == 2) {
            System.out.println("Es un Nie");

            if (!nieValido(dniNie, wb, ruta, row, numFila, dniSet)) {
                System.out.println("El campo es valido\n");
            }
        } else if (dniNie(dniNie) == -1) {
            //Lo mismo que en el caso 2
            dniValido(dniNie, wb, ruta, row, numFila, dniSet);
            System.out.println("No se trata ni de un Nie ni de un DNI\n");
        }
    }

    //Metodo que comprueba si es dni o nie, dni=1, nie=2 ninguno -1
    public int dniNie(String comprobar) {
        //Verifica si es un NIE
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
    public boolean dniVacio(String dni) {
        if (dni == null || dni.isEmpty()) {
            System.out.println("El campo del dni se encuentra vacio");

            return true;
        } else {
            return false;
        }
    }

    //Metodo que comprueba que el campo nie no es null
    public boolean nieVacio(String nie) {
        if (nie == null || nie.isEmpty()) {
            System.out.println("El campo del Nie se encuentra vacio");

            return true;
        } else {
            return false;
        }
    }

    //Metodo que comprueba si el nie es valido o no devolviendo true o false, tambien actualiza letra
    public boolean nieValido(String nie, XSSFWorkbook wb, String ruta, Row fila, int contador, HashSet<String> dniSet) {
        if (nieVacio(nie)) {
            editor.xmlDniNie(rutaXML, contador, fila.getCell(0).getStringCellValue(),
                    fila.getCell(3).getStringCellValue(), fila.getCell(1).getStringCellValue(),
                    fila.getCell(2).getStringCellValue(), "NIF BLANCO");
            return false;
        }

        if (nie.length() != 9) {
            editor.xmlDniNie(rutaXML, contador, fila.getCell(0).getStringCellValue(),
                    fila.getCell(3).getStringCellValue(), fila.getCell(1).getStringCellValue(),
                    fila.getCell(2).getStringCellValue(), "NIF ERRONEO");
            return false;
        }

        String letraInicioNie = nie.substring(0, 1).toUpperCase();
        if (!letraInicioNie.equals("X") && !letraInicioNie.equals("Y") && !letraInicioNie.equals("Z")) {
            System.out.println("El NIE no tiene el formato correcto (letra inicial)");
            editor.xmlDniNie(rutaXML, contador, fila.getCell(0).getStringCellValue(),
                    fila.getCell(3).getStringCellValue(), fila.getCell(1).getStringCellValue(),
                    fila.getCell(2).getStringCellValue(), "NIF ERRONEO");
            return false;
        }

        String nieNumero = nie.substring(1, 8);
        if (!nieNumero.matches("\\d{7}")) {
            System.out.println("El NIE no tiene el formato correcto (número)");
            editor.xmlDniNie(rutaXML, contador, fila.getCell(0).getStringCellValue(),
                    fila.getCell(3).getStringCellValue(), fila.getCell(1).getStringCellValue(),
                    fila.getCell(2).getStringCellValue(), "NIF ERRONEO");
            return false;
        }

        char nieLetra = nie.charAt(8);
        char letraCorrecta = letraCorrectaNie(letraInicioNie, nieNumero);
        if (nieLetra != letraCorrecta) {
            String nieSubsanado = letraInicioNie + nieNumero + letraCorrecta;

            System.out.println("El NIE tenía el formato correcto pero la letra no coincidía, se ha modificado la letra");

            // Actualiza en el Excel y en memoria (celda)
            if (!dniSet.contains(nieSubsanado)) {
                manager.updateExcel(wb, ruta, 0, contador - 1, 0, nieSubsanado);
                fila.getCell(0).setCellValue(nieSubsanado);  // <-- importante para coherencia
            }
            if (!dniSet.add(nieSubsanado)) {
                System.out.println("\n***************NIE no se ha podido añadir al SET -- Debe estar duplicado! ***************\n");
                editor.xmlDniNie(rutaXML, contador, fila.getCell(0).getStringCellValue(),
                        fila.getCell(3).getStringCellValue(), fila.getCell(1).getStringCellValue(),
                        fila.getCell(2).getStringCellValue(), "NIF DUPLICADO");
                return false;
            }

            return true;
        }

        System.out.println("El NIE es correcto");

        if (!dniSet.add(nie)) {
            System.out.println("\n***************NIE no se ha podido añadir al SET -- Debe estar duplicado! ***************\n");
            editor.xmlDniNie(rutaXML, contador, fila.getCell(0).getStringCellValue(),
                    fila.getCell(3).getStringCellValue(), fila.getCell(1).getStringCellValue(),
                    fila.getCell(2).getStringCellValue(), "NIF DUPLICADO");
            return false;
        }

        return true;
    }

    //Metodo que  comprueba si el dni es valido o no devolviendo true o false, tambien actualiza letra
    public boolean dniValido(String dni, XSSFWorkbook wb, String ruta, Row fila, int contador, HashSet<String> dniSet) {
        Sheet hoja = wb.getSheetAt(0);

        if (dniVacio(dni)) {
            editor.xmlDniNie(rutaXML, contador, fila.getCell(0).getStringCellValue(),
                    fila.getCell(3).getStringCellValue(), fila.getCell(1).getStringCellValue(),
                    fila.getCell(2).getStringCellValue(), "NIF BLANCO");
            return false;
        }

        if (dni.length() > 9 || dni.length() < 8) {
            String aux = fila.getCell(0).getCellType() == CellType.NUMERIC
                    ? String.format("%.0f", fila.getCell(0).getNumericCellValue())
                    : fila.getCell(0).getStringCellValue();

            editor.xmlDniNie(rutaXML, contador, aux,
                    fila.getCell(3).getStringCellValue(), fila.getCell(1).getStringCellValue(),
                    fila.getCell(2).getStringCellValue(), "NIF ERRONEO");
            return false;
        }

        String dniNumero = dni.substring(0, 8);

        if (!dniNumero.matches("\\d{8}")) {
            editor.xmlDniNie(rutaXML, contador, fila.getCell(0).getStringCellValue(),
                    fila.getCell(3).getStringCellValue(), fila.getCell(1).getStringCellValue(),
                    fila.getCell(2).getStringCellValue(), "NIF ERRONEO");
            System.out.println("El dni no tiene el formato correcto (numero)");
            return false;
        }

        char dniLetra = dni.charAt(8);
        if (!Character.isLetter(dniLetra)) {
            editor.xmlDniNie(rutaXML, contador, fila.getCell(0).getStringCellValue(),
                    fila.getCell(3).getStringCellValue(), fila.getCell(1).getStringCellValue(),
                    fila.getCell(2).getStringCellValue(), "NIF ERRONEO");
            System.out.println("El dni no tiene el formato correcto (letra)");
            return false;
        }

        char letraCorrecta = letraCorrecta(dniNumero);
        if (dniLetra != letraCorrecta) {
            String dniSubsanado = dniNumero + letraCorrecta;

            System.out.println("El dni tenía el formato correcto pero la letra no coincidía, se ha modificado la letra");

            // Actualiza en el Excel y en memoria (celda)
            if (!dniSet.contains(dniSubsanado)) {
                manager.updateExcel(wb, ruta, 0, contador - 1, 0, dniSubsanado);
                fila.getCell(0).setCellValue(dniSubsanado);  // <-- Esto es clave
            }
            if (!dniSet.add(dniSubsanado)) {
                System.out.println("\n***************DNI no se ha podido añadir al SET -- Debe estar duplicado! ***************\n");
                editor.xmlDniNie(rutaXML, contador, fila.getCell(0).getStringCellValue(),
                        fila.getCell(3).getStringCellValue(), fila.getCell(1).getStringCellValue(),
                        fila.getCell(2).getStringCellValue(), "NIF DUPLICADO");
                return false;
            }

            return true;
        }

        System.out.println("El dni es correcto");

        if (!dniSet.add(dni)) {
            System.out.println("\n***************DNI no se ha podido añadir al SET -- Debe estar duplicado! ***************\n");
            editor.xmlDniNie(rutaXML, contador, fila.getCell(0).getStringCellValue(),
                    fila.getCell(3).getStringCellValue(), fila.getCell(1).getStringCellValue(),
                    fila.getCell(2).getStringCellValue(), "NIF DUPLICADO");
            return false;
        }

        return true;
    }

    //Metodo que comprueba la letra correcta de un nie
    public char letraCorrectaNie(String letraInicio, String numeroNie) {
        String aux = "";

        switch (letraInicio.toUpperCase()) {
            case "X":
                aux = "0";
                break;
            case "Y":
                aux = "1";
                break;
            case "Z":
                aux = "2";
                break;
        }

        String letraYNumero = aux + numeroNie;
        int nieNumero = Integer.parseInt(letraYNumero);
        char[] letras = "TRWAGMYFPDXBNJZSQVHLCKE".toCharArray();

        return letras[nieNumero % 23];
    }

    //Metodo que comprueba la letra correcta de un dni
    public char letraCorrecta(String numeros) {
        int dniNumero = Integer.parseInt(numeros);
        char[] letras = "TRWAGMYFPDXBNJZSQVHLCKE".toCharArray();

        return letras[dniNumero % 23];
    }
}
    /*
    Los metodos que hay aqui abajo se utilizan para ver si el correo se genera de la forma correctaa
    Viendo si hay que generarlo o no
     */


    /*//Metodo que comprueba si el dni  es correcto no escribe
    public boolean dniValido(String dni) {
        //
        if (dniVacio(dni)) {
            return false;
        }
        if (dni.length() > 9 || dni.length() < 8) {
            return false;
        }
        //Comprueba que el dni tenga el formato corrrecto 8 numero
        String dniNumero = dni.substring(0, 8);
        if (!dniNumero.matches("\\d{8}")) {
            System.out.println("El dni no tiene el formato correcto (numero)");

            return false;
        }
        //Comprueba  que el noveno carcater sea letra
        char dniLetra = dni.charAt(8);
        if (!Character.isLetter(dniLetra)) {

            System.out.println("El dni  no tiene el  formato correcto (letra)");
            return false;
        }
        if (dniLetra != letraCorrecta(dniNumero)) {
            //Se corrige la celdacon la letra correcta----Comprobar si lo  actualiza directamente en el excel
            System.out.println("El dni tenia el formato correcto pero la letra no coincidia, se ha modificado la letra");
            return true;
        }
        //Comprueba que la letra sea correcta
        //Si no letra correcta se modifica la letra y ver en la hoja del la practica lo que tiene que escribir en el archuivo
        System.out.println("El dni es correcto");
        return true;
    }
    //Metodo que comprueba si el nie es correcto no escribe
    public boolean nieValido(String nie) {
        if (nieVacio(nie)) {
            return false;
        }
        if (nie.length() != 9) {
            return false;
        }
        String letraInicioNie = nie.substring(0, 1).toUpperCase();
        if (letraInicioNie != "X" && letraInicioNie != "Y" && letraInicioNie != "Z") {
            System.out.println("El Nie no tiene el formato   correcto (letra  inicial)");
            return false;
        }
        String nieNumero = nie.substring(1, 8);
        if (!nieNumero.matches("\\d{7}")) {
            System.out.println("El Nie no tiene el formato correcto (numero)");
            return false;
        }
        char nieLetra = nie.charAt(8);
        if (nieLetra != letraCorrectaNie(letraInicioNie, nieNumero)) {
            System.out.println("El nie tenia el formato correcto pero la letra no coincidia, se ha modificado la letra");
        }
        //Comprueba que la letra sea correcta
        //Si no letra correcta se modifica la letra y ver en la hoja del la practica lo que tiene que escribir en el archuivo
        System.out.println("El nie es correcto");
        return true;
    }*/