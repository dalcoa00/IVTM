package ivtm;

import java.math.BigInteger;
import java.util.HashSet;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.util.HashSet;

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
