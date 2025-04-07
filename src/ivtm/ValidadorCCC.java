package ivtm;

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
}
