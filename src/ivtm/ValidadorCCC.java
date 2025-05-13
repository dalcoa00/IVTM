package ivtm;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Map;

import POJOS.*;
import modelosExcel.ContribuyenteExcel;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ValidadorCCC {
    ExcelManager manager = new ExcelManager();
    EditorXML editor = new EditorXML();
    private final String rutaXML = "resources\\ErroresCCC.xml";

    public void generacionEmail(Row row, HashSet<String> email, XSSFWorkbook wb,String ruta, int sheet){

        String dni= comprobarTipoDni(row.getCell(0));
        String apellido1= row.getCell(1).getStringCellValue();
        String apellido2= row.getCell(2).getStringCellValue();
        String nombre = row.getCell(3).getStringCellValue();
        String contador= String.format("%02d", 0);
        String correoGenerado="";

        if (dni != null && apellido1 != null && nombre != null){
            if (apellido2 != null) {
                correoGenerado=("" + nombre.charAt(0) + apellido1.charAt(0) + apellido2.charAt(0)).toUpperCase() + contador + "@vehiculos2025.com";

            } else {
                correoGenerado=("" + nombre.charAt(0) + apellido1.charAt(0)).toUpperCase() + contador + "@vehiculos2025.com";
            }

            int  contadorNum=0;

            while (true) {
                String base = "" + nombre.charAt(0) + apellido1.charAt(0);

                if (apellido2 != null) {
                    base += apellido2.charAt(0);
                }

                correoGenerado = (base + String.format("%02d", contadorNum)).toUpperCase() + "@vehiculos2025.com";

                if (email.add(correoGenerado)) {
                    break; // Salimos del bucle si no está repetido
                }

                contadorNum++;
            }

            manager.updateExcel(wb, ruta, sheet, row.getRowNum(), 6, correoGenerado);
        }
    }

    public String comprobarTipoDni(Cell celdaComprobar) {
        String dniNie = "";

        if (celdaComprobar != null) {
            if (celdaComprobar.getCellType() == CellType.STRING) {
                dniNie = celdaComprobar.getStringCellValue();
            }
            else if (celdaComprobar.getCellType() == CellType.NUMERIC) {
                dniNie = String.format("%.0f", celdaComprobar.getNumericCellValue());
            }
        }
        return dniNie;
    }
    public void comprobarCCC(Row row, XSSFWorkbook wb, String  ruta, int sheet, HashSet<String> cccSet, HashSet<String> dniSet, HashSet<String> correoSet, Cell dniCell, Cell cccCell,  Map<String, ContribuyenteExcel> contribuyentesMap) {
        //Una vez que se comprueba el CCC hay que generar el IBAN
        //Aunque el CCC sea erróneo (o se ha subsanado), si se puede generar el IBAN se genera y se incluye en erroresCCC o se actualiza en el Excel
        Integer[] factores = {1, 2, 4, 8, 5, 10, 9, 7, 3, 6};
        String ccc = cccCell.getStringCellValue().trim();
        System.out.println(ccc.length());

        //Si el formato del CCC es correcto -> CCC correcto o subsanable
        if (comprobarFormatoCCC(ccc)) {
            int codigoEntidadBancaria = Integer.parseInt(ccc.substring(0, 4));
            int identificacionOficina = Integer.parseInt(ccc.substring(4, 8));

            String codigoMasIdentificacion = "00" + String.format("%04d", codigoEntidadBancaria) + String.format("%04d", identificacionOficina);

            int validador1 = Integer.parseInt(ccc.substring(8, 9));
            //System.out.println("validador1: " + validador1);

            int validador2 = Integer.parseInt(ccc.substring(9, 10));
            //System.out.println("validador2: " + validador2);

            String numeroCuenta = ccc.substring(10, 20);
            System.out.println("codigo " + codigoMasIdentificacion + " " + " " + validador1 + " " + validador2 + " " + numeroCuenta);
            System.out.println("numero total " + ccc);

            int suma1 = 0;
            int suma2 = 0;

            //Coge los primeros 10  numeros codigo entidad bancaria e identificador oficicina con los 00 añadidos y se multiplica
            for (int i = 0; i < factores.length; i++) {
                suma1 = suma1 + (factores[i] * Character.getNumericValue(codigoMasIdentificacion.charAt(i)));
            }

            int resto = suma1 % 11;
            int primerDigitoValidacionCalculado = 11 - resto;

            //Calculo de segundo digito de control
            for (int i = 0; i < factores.length; i++) {
                suma2 = suma2 + (factores[i] * Character.getNumericValue(numeroCuenta.charAt(i)));
            }

            int resto2 = suma2 % 11;
            int segundoDigitoValidacionCalculado = 11 - resto2;
            //System.out.println(resto);
            //System.out.println(resto2);
            String base1 = "";
            String base2 = "";

            if (primerDigitoValidacionCalculado == 10) {
                primerDigitoValidacionCalculado = 1;
            }
            else if (primerDigitoValidacionCalculado == 11) {
                primerDigitoValidacionCalculado = 0;
            }

            if (segundoDigitoValidacionCalculado == 10) {
                segundoDigitoValidacionCalculado = 1;
            }
            else if (segundoDigitoValidacionCalculado == 11) {
                segundoDigitoValidacionCalculado = 0;
            }

            //System.out.println(primerDigitoValidacionCalculado);
            //System.out.println(segundoDigitoValidacionCalculado);

            if (primerDigitoValidacionCalculado != validador1) {
                System.out.println("El primer digito de control es incorrecto");
                base1 = "" + String.format("%04d", codigoEntidadBancaria) + String.format("%04d", identificacionOficina)+String.format("%01d", primerDigitoValidacionCalculado);
            }
            if (segundoDigitoValidacionCalculado != validador2) {
                System.out.println("El segundo digito de control es incorrecto");
                base2 = "" + segundoDigitoValidacionCalculado + numeroCuenta;
            }

            String nuevoCCC = String.format("%04d", codigoEntidadBancaria)
                    + String.format("%04d", identificacionOficina)
                    + primerDigitoValidacionCalculado
                    + segundoDigitoValidacionCalculado
                    + numeroCuenta;

            //Añade el nuevo CCC al set, y si ya está almacenado -> duplicado
            if (!cccSet.add(nuevoCCC)) {
                System.out.println("El CCC está duplicado.");
            }

            //Actualiza la hoja con el CCC correcto o subsanado
            manager.updateExcel(wb, ruta, sheet, row.getRowNum(), 9, nuevoCCC);

            //El IBAN se genera únicamente si NIF y CCC válidos o subsanados
            //El e-mail igual
            String dni;

            if (dniCell.getCellType() == CellType.STRING) {
                dni = dniCell.getStringCellValue().trim();
            }
            else if (dniCell.getCellType() == CellType.NUMERIC) {
                dni = String.valueOf((long) dniCell.getNumericCellValue());
                //System.out.println("\nLa celda del DNI era numérica, casteado a String es: " + dni + "\n");
            }
            else if (dniCell.getCellType() == CellType.BLANK) {
                dni = "";
            }
            else {
                throw new IllegalStateException("Tipo de celda de DNI no soportado: " + dniCell.getCellType());
            }

            if (dniSet.contains(dni) && cccSet.contains(nuevoCCC)) {
                //Genera el IBAN
                String iban = generaIBAN(row, dniCell.getStringCellValue(), nuevoCCC);

                //Actualiza la hoja con el IBAN generado
                manager.updateExcel(wb, ruta, sheet, row.getRowNum(), 10, iban);

                //Genera el correo electrónico y actualiza la hoja
                generacionEmail(row, correoSet, wb, ruta, sheet);

                //SI EL CCC HA SIDO SUBSANADO, EN EL XML -> CCC ERRÓNEO (ccc) (ccc != nuevoCCC??) e IBAN generado con nuevoCCC
                if (!ccc.equals(nuevoCCC)) {
                    editor.xmlCuenta(rutaXML, row.getRowNum(), row.getCell(3).getStringCellValue(), row.getCell(1).getStringCellValue(), row.getCell(2).getStringCellValue(), dniCell.getStringCellValue(), ccc, iban, "");
                }

                //Agrega el contribuyente al map para generar los recibos
                agregarContribuyente(contribuyentesMap, row);
            }

        }
        else {
            System.out.println("El CCC es incorrecto y/o no subsanable. Imposible generar el IBAN asociado.");
            // XML - IMPOSIBLE GENERAR IBAN
            editor.xmlCuenta(rutaXML, row.getRowNum(), row.getCell(3).getStringCellValue(), row.getCell(1).getStringCellValue(), row.getCell(2).getStringCellValue(), dniCell.getStringCellValue(), ccc, "", "IMPOSIBLE GENERAR IBAN");

        }
    }

    public boolean comprobarFormatoCCC (String ccc) {

        if (ccc.length() != 20) {
            System.out.println("La cantidad de digitos del CCC es invalida");

            return false;
        }
        else {
            for (int i = 0; i < ccc.length(); i++) {
                char comprobar = ccc.charAt(i);

                if (!Character.isDigit(comprobar)) {
                    System.out.println("El digito "+ i + "no es un numero");

                    return false;
                }
            }

            return true;
        }
    }

    //Genera el IBAN si el DNI y el CCC son válidos o subsanables
    public String generaIBAN (Row row, String dni, String ccc){
        String paisCCC = row.getCell(8).getStringCellValue();

        String numPais = codigoPais(paisCCC);
        String aux_ = ccc + numPais + "00";
        BigInteger aux = new BigInteger(aux_);

        //Calculo del algoritmo del IBAN
        int resto = aux.mod(BigInteger.valueOf(97)).intValue();
        int control = 98 - resto;
        String numControl = String.format("%02d", control);

        //Se obtiene el IBAN generado
        String iban = paisCCC + numControl + ccc;

        System.out.println("IBAN generado para el DNI " + dni + ": " + iban);

        return iban;
    }

    //Calcula el codigo dependiendo de las letras del pais
    public String codigoPais (String paisCCC) {
        StringBuilder numPais = new StringBuilder();

        for (char c : paisCCC.toCharArray()) {
            int valor = Character.getNumericValue(c);

            if (valor < 10 || valor > 35) {
                throw new IllegalArgumentException("Código de país no válido: " + paisCCC);
            }
            numPais.append(valor);
        }

        return numPais.toString();
    }

    /*Metodo que agrega el contribuyente al Map una vez sus datos han sido verificados o subsanados*/
    public void agregarContribuyente (Map<String, ContribuyenteExcel> contribuyentesMap, Row row) {
        String nombre = row.getCell(3).getStringCellValue();
        String apellido1 = row.getCell(1).getStringCellValue();
        String apellido2 = row.getCell(2).getStringCellValue();
        String nifnie = row.getCell(0).getStringCellValue();
        String direccion = row.getCell(4).getStringCellValue();
        String iban = row.getCell(10).getStringCellValue();
        Double bonificacion = row.getCell(11).getNumericCellValue();
        //Falta la bonificacion que se añade más adelante, al leer Ordenanzas

        ContribuyenteExcel c = new ContribuyenteExcel();
        c.setNombre(nombre);
        c.setApellido1(apellido1);
        c.setApellido2(apellido2);
        c.setNifnie(nifnie);
        c.setDireccion(direccion);
        c.setIban(iban);
        c.setBonificacion(bonificacion);

        contribuyentesMap.put(nifnie, c);
    }

}
