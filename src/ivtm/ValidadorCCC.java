package ivtm;

import java.math.BigInteger;
import java.util.HashSet;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javafx.scene.control.Cell;

public class ValidadorCCC {
    ExcelManager manager = new ExcelManager();

    public void generacionEmail(Row row, HashSet<String> email, XSSFWorkbook wb,String ruta, int sheet){
        String dni= comprobarTipoDni(row.getCell(0));
        String apellido1= row.getCell(1).getStringCellValue();
        String apellido2= row.getCell(2).getStringCellValue();
        String nombre = row.getCell(3).getStringCellValue();
        String contador= String.format("%02d", 0);
        String correoGenerado="";
        if(dni!=null&&apellido1!=null&&nombre!=null){
            if(apellido2!=null){
                correoGenerado=(""+nombre.charAt(0)+apellido1.charAt(0)+apellido2.charAt(0)).toUpperCase()+contador+"@vehiculos2025.com";

            }else{
                correoGenerado=(""+nombre.charAt(0)+apellido1.charAt(0)).toUpperCase()+contador+"@vehiculos2025.com";
            }
            int  contadorNum=0;
            while (true) {
                String base = "" + nombre.charAt(0) + apellido1.charAt(0);
                if (apellido2!=null) {
                    base += apellido2.charAt(0);
                }

                correoGenerado = (base + String.format("%02d", contadorNum)).toUpperCase() + "@vehiculos2025.com";

            if (email.add(correoGenerado)) {
                break; // Salimos del bucle si no está repetido
            }

            contadorNum++;
        }

            manager.updateExcel(wb,ruta,sheet,row.getRowNum(),6,correoGenerado);
        }
    }
    
    public String comprobarTipoDni(Cell celdaComprobar) {
        String dniNie = "";
        if (celdaComprobar != null) {
            if (celdaComprobar.getCellType() == CellType.STRING) {
                dniNie = celdaComprobar.getStringCellValue();
            } else if (celdaComprobar.getCellType() == CellType.NUMERIC) {
                dniNie = String.format("%.0f", celdaComprobar.getNumericCellValue());
            }
        }
        return dniNie;
    }

    public void comprobarCCC(Row row, XSSFWorkbook wb, String  ruta, int sheet, HashSet<String> cccSet){
        Integer[] factores = {1, 2, 4, 8, 5, 10, 9, 7, 3, 6};
        String ccc=  row.getCell(9).getStringCellValue().trim();
        System.out.println(ccc.length());
        if(comprobarFormatoCCC(ccc)) {
            int codigoEntidadBancaria = Integer.parseInt(ccc.substring(0, 4));
            int identificacionOficina = Integer.parseInt(ccc.substring(4, 8));
            String codigoMasIdentificacion = "00" + String.format("%04d", codigoEntidadBancaria) + String.format("%04d", identificacionOficina);
            int validador1 = Integer.parseInt(ccc.substring(8, 9));
            System.out.println("validador1: "+ validador1);
            int validador2 = Integer.parseInt(ccc.substring(9, 10));
            System.out.println("validador2: "+ validador2);
            String numeroCuenta = ccc.substring(10, 20);
            System.out.println("codigo "+ codigoMasIdentificacion+ " "+identificacionOficina +""+validador1+validador2+numeroCuenta);
            System.out.println("numero total "+ ccc);
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
            System.out.println(resto);
            System.out.println(resto2);
            String base1 = "";
            String base2 = "";
            if (primerDigitoValidacionCalculado == 10) {
                primerDigitoValidacionCalculado = 1;
            }else if(segundoDigitoValidacionCalculado==10){
                segundoDigitoValidacionCalculado=1;
            }
            if(primerDigitoValidacionCalculado==11){
                primerDigitoValidacionCalculado=0;
            }else if(segundoDigitoValidacionCalculado==11){
                segundoDigitoValidacionCalculado=0;
            }
                System.out.println(primerDigitoValidacionCalculado);
                System.out.println(segundoDigitoValidacionCalculado);
            if (primerDigitoValidacionCalculado != validador1) {
                System.out.println("El primer digito de control es incorrecto");
                base1 = "" + String.format("%04d", codigoEntidadBancaria) + String.format("%04d", identificacionOficina)+String.format("%01d", (11-resto));
            }
            if (segundoDigitoValidacionCalculado != validador2) {
                System.out.println("El segundo digito de control es incorrecto");
                base2 = "" + (11-resto2) + numeroCuenta;
            }
            
            //Ambos digitos son correctos
            if (base1.isEmpty() && base2.isEmpty()) {
                System.out.println("Ambos  digitos  de control son correctos");

                cccSet.add(ccc);
            } else if (!base1.isEmpty() && !base2.isEmpty()) {//Ambos digitos son incorrectos
                String nuevoCCC = base1 + base2;
                manager.updateExcel(wb, ruta, sheet, row.getRowNum(), 9, nuevoCCC);
                cccSet.add(nuevoCCC);

            } else if (base1.isEmpty()) {//Segundo digito de control es incorrecto
                String nuevoCCC = String.valueOf(codigoEntidadBancaria) + String.valueOf(identificacionOficina) + String.valueOf(validador1) + base2;
                manager.updateExcel(wb, ruta, sheet, row.getRowNum(), 9, nuevoCCC);
                cccSet.add(nuevoCCC);
            } else{//Primer digito de control es correcto
                String nuevoCCC = base1 + String.valueOf(validador2) + String.valueOf(numeroCuenta);
                manager.updateExcel(wb, ruta, sheet, row.getRowNum(), 9, nuevoCCC);
                cccSet.add(nuevoCCC);
            }
            System.out.println(base1);
            System.out.println(base2);

        }
    }

    public boolean comprobarFormatoCCC(String ccc){
        if(ccc.length()!=20){
            System.out.println("La cantidad de digitos del CCC es invalida");
            return false;
        }else{
            for (int i = 0; i < ccc.length(); i++) {
                char comprobar= ccc.charAt(i);
                if(!Character.isDigit(comprobar)){
                    System.out.println("El digito "+ i+ "no es un numero");
                    return false;
                }
            }
            return true;
        }
    }

    //Genera el IBAN si el DNI y el CCC son válidos o subsanables
    public void generaIBAN (XSSFWorkbook wb, String ruta, int sheet, Row row, String dni, String ccc, HashSet<String> dniSet, HashSet<String> cccSet){
        String paisCCC = row.getCell(8).getStringCellValue();

        //Si ya estan en el Set son válidos o subsanados
        if (!dniSet.add(dni) && !cccSet.add(ccc)) {
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

            //Actualiza el excel con el IBAN generado
            manager.updateExcel(wb, ruta, sheet, row.getRowNum(), 8, iban);
        }
    }

    //Calcula el codigo dependiendo de las letras del pais
    public String codigoPais (String paisCCC) {
        StringBuilder numPais = new StringBuilder();

        for (char c : paisCCC.toCharArray()) {
            int valor = Character.getNumericValue(c);

            if (valor < 10 || valor > 35) {
                throw new IllegalArgumentException("Código de país no válido: " + paisCCC);
            }
            numPais.append(c);
        }

        return numPais.toString();
    }

}
