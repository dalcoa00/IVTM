package ivtm;

import POJOS.Ordenanza;
import POJOS.Vehiculos;
import modelosExcel.ContribuyenteExcel;
import modelosExcel.VehiculoExcel;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import java.util.*;
import static ivtm.ExcelManager.isEmpty;

public class ImporteRecibo {

    /*Calcula el importe que se debe pagar por cada vehículo*/
    public void calculaImporte (XSSFSheet ws, Map<String, List<VehiculoExcel>> vehiculosContribuyentesMap, Map<String, ContribuyenteExcel> contribuyentesMap, Map<String, List<Ordenanza>> ordenanzasMap, Map<String, List<Vehiculos>> vehiculosPojosContribuyentesMap, int anio) {

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
                        System.out.println("\nFila sin datos\n");

                        continue;
                    }

                    for (Cell cell : row) {
                        if (cell == null || cell.getCellType() == CellType.BLANK) row = rowIter.next();
                    }

                    Cell aytoCell = row.getCell(0);
                    Cell tipoCell = row.getCell(1);
                    Cell unidadCell = row.getCell(2);
                    Cell minUnidadCell = row.getCell(3);
                    Cell maxUnidadCell = row.getCell(4);
                    Cell importeCell = row.getCell(5);

                    if (aytoCell == null || aytoCell.getCellType() == CellType.BLANK) {
                        //System.out.println("❌ Celda de ayuntamiento vacía en fila " + row.getRowNum());
                        continue;
                    }

                    String aytoOrdenanza = aytoCell.getStringCellValue().trim().toUpperCase();
                    if (aytoOrdenanza.isEmpty()) {
                        //System.out.println("❌ Ayuntamiento vacío tras trim en fila " + row.getRowNum());
                        continue;
                    }

                    Integer idFilaOrdenanza = row.getRowNum();
                    String tipo = tipoCell.getStringCellValue();
                    String unidadStr = unidadCell.getStringCellValue();
                    double minUnidad = minUnidadCell.getNumericCellValue();
                    double maxUnidad = maxUnidadCell.getNumericCellValue();
                    double importe = importeCell.getNumericCellValue();

                    //Obtener la lista de ordenanzas si ya existe o hay que crear una nueva
                    String claveOrdenanza = aytoOrdenanza + tipo + unidadStr + minUnidad + maxUnidad;
                    List<Ordenanza> listaOrdenanzas = ordenanzasMap.computeIfAbsent(claveOrdenanza, k -> new ArrayList<>());

                    //Busca si ya existe la ordenanza
                    Ordenanza ordenanza = listaOrdenanzas.stream().filter(ord -> ord.getImporte() == importe).findFirst().orElse(null);

                    //Si no existe, se crea y mapea
                    if (ordenanza == null) {
                        Ordenanza o = new Ordenanza();

                        //o.setId(idFilaOrdenanza);
                        o.setAyuntamiento(aytoOrdenanza);
                        o.setTipoVehiculo(tipo);
                        o.setUnidad(unidadStr);
                        o.setMinimoRango(String.format("%.2f", minUnidad));
                        o.setMaximoRango(String.format("%.2f", maxUnidad));
                        o.setImporte(Math.round(importe * 100.0) / 100.0);

                        for (List<Vehiculos> listaVehiculos : vehiculosPojosContribuyentesMap.values()) {
                            for (Vehiculos vP : listaVehiculos) {
                                if (vP.getMatricula().equalsIgnoreCase(vehiculo.getMatricula())) {
                                    //Si al vehiculo actual hay que asignarle esta ordenanza
                                    if (c.getAytoCont().equals(aytoOrdenanza) && vP.getTipo().equals(tipo)) {
                                        o.getVehiculoses().add(vP);
                                    }
                                }
                            }
                        }
                        listaOrdenanzas.add(o);
                    }
                    else {
                        for (List<Vehiculos> listaVehiculos : vehiculosPojosContribuyentesMap.values()) {
                            for (Vehiculos vP : listaVehiculos) {
                                if (vP.getMatricula().equalsIgnoreCase(vehiculo.getMatricula())) {
                                    //Si al vehiculo actual hay que asignarle esta ordenanza
                                    if (c.getAytoCont().equals(aytoOrdenanza) && vP.getTipo().equals(tipo)) {
                                        ordenanza.getVehiculoses().add(vP);
                                    }

                                }
                            }
                        }
                    }

                    if (c.getAytoCont().equals(aytoOrdenanza) && vehiculo.getTipoVehiculo().equals(tipo)) {
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
                    vehiculo.setImporte_bonif(importe_bonif);
                } else {
                    System.out.println("No se ha encontrado el propietario del vehículo.");
                }

                //Calculo el importe total (real) del importe en el año solicitado
                double totalRecibo = calculaTotalRecibo(anio, vehiculo);

                vehiculo.setTotal(totalRecibo);
            }
        }
    }

    public double calculaTotalRecibo (int anio, VehiculoExcel vehiculo) {
        Date fechaAlta = vehiculo.getFechaAlta();
        Date fechaBaja = vehiculo.getFechaBaja();
        Date fechaBajaTemporal = vehiculo.getFechaBajaTemporal();

        //Obtengo el año de alta del vehiculo
        Calendar calAlta = Calendar.getInstance();
        calAlta.setTime(fechaAlta);
        int anioAlta = calAlta.get(Calendar.YEAR);

        //Si el año de alta es después del año del recibo
        if (anioAlta > anio) {
            vehiculo.setNumTrimestres(0);
            return 0.0;
        }

        // Validación año de baja definitiva (antes del año actual)
        if (fechaBaja != null) {
            Calendar calBaja = Calendar.getInstance();
            calBaja.setTime(fechaBaja);
            int anioBaja = calBaja.get(Calendar.YEAR);

            if (anioBaja < anio) {
                vehiculo.setNumTrimestres(0);
                return 0.0;
            }
        }

        Date fechaFinUso = (vehiculo.getFechaBajaTemporal() != null) ? vehiculo.getFechaBajaTemporal() : vehiculo.getFechaBaja();

        Trimestres trimestres = new Trimestres(anio);
        int trimestresAlta = trimestres.calculaTrimestresVehiculo(vehiculo.getFechaAlta(), fechaFinUso);

        vehiculo.setNumTrimestres(trimestresAlta);

        //Si vehiculo exento
        if (vehiculo.getExencion() == 'S') {
            return 0.0;
        }

        if (trimestresAlta == 0) {
            vehiculo.setNumTrimestres(0);
            return 0.0;
        }

        //Importe total del año se divide en 4 partes (4 trimestres)
        return (vehiculo.getImporte_bonif() / 4.0) * trimestresAlta;
    }
}