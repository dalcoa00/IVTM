package ivtm;

import modelosExcel.ContribuyenteExcel;
import modelosExcel.VehiculoExcel;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static ivtm.ExcelManager.isEmpty;

public class ImporteRecibo {
    /*
    * Le paso el ws (hoja) de ordenanza y los Maps de contribuyentes y vehiculos
    * primero compruebo si tiene exención -> no paga nada
    * Si no:
    * Comparo cada vehiculo con las celdas de Ordenanza para saber qué importe tendría que pagar
    * Calcular el importe total (provisional) en base a los trimestres que esté dado de alta
    *
    * Una vez calculado esto -> Si el propietario tiene bonificación hacer el cálculo para obterner el importe total
    * Ya con todo generar el recibo*/

    /*Calcula el importe que se debe pagar por cada vehículo*/
    public void calculaImporte (XSSFSheet ws, Map<String, List<VehiculoExcel>> vehiculosContribuyentesMap, Map<String, ContribuyenteExcel> contribuyentesMap) {

        for (Map.Entry<String, List<VehiculoExcel>> entry : vehiculosContribuyentesMap.entrySet()) {
            List<VehiculoExcel> vehiculos = entry.getValue();

            for (VehiculoExcel vehiculo : vehiculos) {
                //Primero se calcula si el vehículo está exento de impuestos
                if (vehiculo.getExencion().equals("S")) {
                    vehiculo.setImporte(0);
                    vehiculo.setTotal(0);
                }
                else {
                    //Iteración por las filas de la hoja
                    Iterator<Row> rowIter = ws.iterator();

                    //Para empezar en la fila 1
                    if(rowIter.hasNext()){
                        rowIter.next();
                    }

                    while (rowIter.hasNext()) {
                        Row row = rowIter.next();

                        //Si la  fila esta vacia se las salta
                        if(isEmpty(row)){
                            System.out.println("Fila sin datos\n");

                            continue;
                        }

                        Cell tipoCell = row.getCell(1);
                        Cell unidadCell = row.getCell(2);
                        Cell minUnidadCell = row.getCell(3);
                        Cell maxUnidadCell = row.getCell(4);
                        Cell importeCell = row.getCell(5);

                        String tipo = tipoCell.getStringCellValue();
                        String unidadStr = unidadCell.getStringCellValue();
                        double minUnidad = minUnidadCell.getNumericCellValue();
                        double maxUnidad = maxUnidadCell.getNumericCellValue();
                        double importe = importeCell.getNumericCellValue();

                        if (vehiculo.getTipoVehiculo().equals(tipo)) {
                            if (vehiculo.getValorUnidad() >= minUnidad && vehiculo.getValorUnidad() <= maxUnidad) {
                                vehiculo.setImporte(importe);

                                break; //Pasa al siguiente vehículo, no es necesario leer el resto de la hoja -> Solo puede haber una coincidencia
                            }
                        }
                    }
                }
                /* Una vez que he calculado el importe (provisional) de cada vehiculo:
                 *   - Calculo otro importe provisional en base a la posible bonificación que tenga el propietario
                 *      Para cada vehículo (estoy dentro del for) busco su propietario en el Map contribuyentes
                 *      y calculo el importe total en base a la bonificación del propietario)
                 *
                 *      ** ESTO FUERA DEL BUCLE - CUANDO TODOS LOS IMPORTES (DEL AÑO COMPLETO) ESTÁN CALCULADOS **
                 *   - Calcular los trimestre que se deberían de pagar en base a la fecha de alta y una posible fecha de baja
                 *       ++ Si no está dado de baja, es en el año de alta donde cabe la posibilidad de pagar menos porque
                 *          se dio de alta en el segundo o tercer o cuarto trimestre
                 *               Si el vehiculo fue dado de alta en 2023 y te piden el importe de 2024 el total es todo el año*/


            }
        }



    }
}
