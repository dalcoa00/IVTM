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

public class ImporteVehiculo {
    /*
    * Le paso el ws (hoja) de ordenanza y los Maps de contribuyentes y vehiculos
    * primero compruebo si tiene exención -> no paga nada
    * Si no:
    * Comparo cada vehiculo con las celdas de Ordenanza para saber qué importe tendría que pagar
    * Calcular el importe total (provisional) en base a los trimestres que esté dado de alta
    *
    * Una vez calculado esto -> Si el propietario tiene bonificación hacer el cálculo para obterner el importe total
    * Ya con todo generar el recibo*/

    public void calculaImporte (XSSFSheet ws, Map<String, ContribuyenteExcel> contribuyentesMap, Map<String, List<VehiculoExcel>> vehiculosContribuyentesMap) {
        /*Coger MapVehiculosContribuyente y segun voy iterando por los elementos almacenados del Map
         * itero por las filas-columnas de la Ordenanza para determinar el importe que debe pagar
         * según el tipo de vehículo, unidad de cobro y el valor de la unidad??
         *
         * O almacenar todos los importes en variables (24) y a partir de ahí comparar con todos los vehiculos
         * mapeados???
         *
         * Recorrer la hoja de forma recursiva?? Una función que con cada elemento del Map itere sobre la
         * hoja para determinar el importe en función de las características del vehículo ->
         * Estaría más o menos optimizado porque solo se abre el archivo una vez, se pasa el workbook como argumento
         * , los iteradores, el map y algo más si hace falta. Dentro de esa función almacenaría el importe en el
         * elemento del Map y pasaría al siguiente hasta que no queden elementos por comprobar
         * -> En una clase a parte o en esta misma, algo tipo "comprobarImporteVehiculo"*/


        //Iteración por las filas de la hoja
        Iterator<Row> rowIter = ws.iterator();

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


            System.out.println();

            while (cellIter.hasNext()) {
                Cell cell = cellIter.next();



            }


        }
    }

}
