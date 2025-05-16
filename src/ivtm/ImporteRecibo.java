package ivtm;

import modelosExcel.ContribuyenteExcel;
import modelosExcel.VehiculoExcel;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import java.util.*;

import static ivtm.ExcelManager.isEmpty;

public class ImporteRecibo {

    /*Calcula el importe que se debe pagar por cada vehículo*/
    public void calculaImporte (XSSFSheet ws, Map<String, List<VehiculoExcel>> vehiculosContribuyentesMap, Map<String, ContribuyenteExcel> contribuyentesMap, int anio) {

        for (Map.Entry<String, List<VehiculoExcel>> entry : vehiculosContribuyentesMap.entrySet()) {
            //Obtengo los propietarios para comparar el ayuntamiento
            String nif = entry.getKey();
            ContribuyenteExcel c = contribuyentesMap.get(nif);

            List<VehiculoExcel> vehiculos = entry.getValue();

            if (c == null) continue;

            for (VehiculoExcel vehiculo : vehiculos) {
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
                            //Importe bruto de un año completo sin bonificaciones ni exenciones
                            vehiculo.setImporte(importe);

                            break; //Pasa al siguiente vehículo, no es necesario leer el resto de la hoja -> Solo puede haber una coincidencia
                        }
                    }
                }

                /*Obtengo la bonificacion del propietario y calcula el importe provisional (importe de un año completo)*/
                double importeBruto = vehiculo.getImporte();

                if (c != null) {
                    double bonificacion = c.getBonificacion();
                    //Asumiendo que la bonificacion es un porcentaje que se aplica sobre el importe total
                    double deduccion = importeBruto * (bonificacion / 100);
                    double importe_bonif = importeBruto - deduccion;

                    //Importe de un año completo aplicando la posible bonificación del propietario
                    vehiculo.setImporte(importe_bonif);
                } else {
                    System.out.println("No se ha encontrado el propietario del vehículo.");
                }

                //Calculo el importe total (real) del importe en el año solicitado
                vehiculo.setTotal(calculaTotalRecibo(anio, vehiculo));

            }
        }
    }

    public double calculaTotalRecibo (int anio, VehiculoExcel vehiculo) {

        //Si vehiculo exento
        if (vehiculo.getExencion().equals("S")) {
            vehiculo.setImporte(0);
            return 0;
        }

        int total = -10;
        int anioBaja = -1;

        Date fechaAlta = vehiculo.getFechaAlta();
        Date fechaBaja = vehiculo.getFechaBaja();
        Date fechaBajaTemporal = vehiculo.getFechaBajaTemporal();

        //Obtengo el año de alta del vehiculo
        Calendar calAlta = Calendar.getInstance();
        calAlta.setTime(fechaAlta);
        int anioAlta = calAlta.get(Calendar.YEAR);

        //Si el año de alta es después del año del recibo
        if (anioAlta > anio) {
            return -1;
        }

        //Obtener año de baja si existe
        if (fechaBaja != null) {
            Calendar calBaja = Calendar.getInstance();
            calBaja.setTime(fechaBaja);

            anioBaja = calBaja.get(Calendar.YEAR);
        }

        //Si fue dado de baja DEFINITIVA antes del año pedido
        if (fechaBaja != null && anioBaja < anio) {
            return -1;
        }

        //Define los trimestres del año consultado
        Trimestres trimestres = new Trimestres(anio);
        int trimestresAlta = 0;

        //Comprueba si ha estado de alta cada trimestre individualmente
        for (int i = 1; i <= 4; i++) {
            Date inicio = trimestres.getInicio(i);
            Date fin = trimestres.getFin(i);

            boolean paga = true;

            //No paga si fue dado de alta después de terminar el trimestre
            if (fechaAlta != null && fechaAlta.after(fin)) {
                paga = false;
            }

            //No paga si fue dado de baja antes de empezar el trimestre
            if (fechaBaja != null && fechaBaja.before(inicio)) {
                paga = false;
            }

            //No paga si fue dado de baja temporal antes de comenzar el trimestre
            if (fechaBajaTemporal != null && fechaBajaTemporal.before(inicio)) {
                paga = false;
            }

            //Si true en el trimestre, se incremente en uno los trimestres que hay que pagar
            if (paga) trimestresAlta++;
        }

        //Importe total del año se divide en 4 partes (4 trimestres)
        return (vehiculo.getImporte() / 4.0) * trimestresAlta;
    }



}