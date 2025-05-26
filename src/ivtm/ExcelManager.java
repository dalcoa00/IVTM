package ivtm;

import POJOS.*;
import modelosExcel.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/*Clase para la lectura y modificación de los ficheros Excel*/
public class ExcelManager {
    //Sets en los que se almacenan los datos correctos y subsanados
    HashSet<String> dniSet = new HashSet<>();
    HashSet<String> cccSet = new HashSet<>();
    HashSet<String> correoSet = new HashSet<>();
    HashSet<String> matriculaSet = new HashSet<>();

    //Mapas para relacionar contribuyentes y sus vehículos para generar los recibos
    Map<String, ContribuyenteExcel> contribuyentesMap = new HashMap<>(); // <- Hoja Contribuyentes
    Map<String, List<VehiculoExcel>> vehiculosContribuyentesMap = new HashMap<>(); // <- Hoja vehiculos
    Map<String, List<ReciboExcel>> recibosMap = new HashMap<>(); //Recibos que se generan

    Map<String, Contribuyente> contribuyentesPojosMap = new HashMap<>();
    Map<String, List<Vehiculos>> vehiculosPojosContribuyentesMap = new HashMap<>();
    Map<String, List<Recibos>> recibosPojosMap = new HashMap<>();
    Map<String, List<Ordenanza>> ordenanzasMap = new HashMap<>();

    EditorXML editor = new EditorXML();
    ReciboPDF reciboPDF = new ReciboPDF();
    //ActualizarBD actualizar = new ActualizarBD();
    private final String recibosXML = "resources\\Recibos.xml";
    private final String vehiculosRutaXML = "resources\\ErroresVehiculos.xml";

    /*Lee el archivo excel indicado en la ruta que recibe y el número de hoja especificado*/
    public void readExcel(String filepath, int sheet) {
        //Hoja "Contribuyentes" -> Comprobación de DNIs, CCCs y generación de e-mails e IBANs
        if (filepath.equals("resources\\SistemasVehiculos.xlsx") && sheet == 0) {
            try {
                FileInputStream file = new FileInputStream(filepath);
                XSSFWorkbook wb = new XSSFWorkbook(file);
                file.close(); //No es necesario mantenerlo abierto

                ValidadorNieDni validadorNieDni = new ValidadorNieDni();
                ValidadorCCC validadorCCC= new ValidadorCCC();

                //Se lee la hoja
                XSSFSheet ws = wb.getSheetAt(sheet);
                System.out.println("\nLeyendo hoja \"" + ws.getSheetName() + "\"");

                //Iteración por las filas de la hoja
                Iterator<Row> rowIter = ws.iterator();

                //Para empezar en la fila 1
                if(rowIter.hasNext()){
                    rowIter.next();
                }

                int dniLeidos = 0;

                //Va fila por fila
                while (rowIter.hasNext()) {
                    Row row = rowIter.next();

                    //Si la  fila esta vacia se las salta
                    if(isEmpty(row)){
                        System.out.println("Fila sin datos\n");

                        continue;
                    }

                    Iterator<Cell> cellIter = row.cellIterator();

                    //Celdas que se comprobarán
                    Cell dniCell = null;
                    Cell cccCell = null;

                    //Lee columna por columna (celdas de la fila)
                    while (cellIter.hasNext()) {
                        Cell cell = cellIter.next();

                        if (cell.getCellType() == CellType.NUMERIC) {
                            System.out.print((int) cell.getNumericCellValue() + "\t");
                        }
                        else if (cell.getCellType() == CellType.STRING) {
                            System.out.print(cell.getStringCellValue() + "\t");
                        }

                        if (cell.getColumnIndex() == 0) {
                            dniCell = cell;
                        }

                        if (cell.getColumnIndex() == 9) {
                            cccCell = cell;
                        }
                    }

                    System.out.println();

                    //Valida el DNI/NIE si la celda 0 es no nula
                    if (dniCell != null) {
                        dniLeidos++;
                        validadorNieDni.validaDNI(dniCell, filepath, row, row.getRowNum() + 1, wb, dniSet);
                    }

                    //Valida el CCC si la celda 9 es no nula
                    //Genera IBAN y correo si NIF/NIE y CCC correctos o subsanados
                    if (cccCell != null) {
                        validadorCCC.comprobarCCC(row, wb, filepath, sheet, cccSet, dniSet, correoSet, dniCell, cccCell, contribuyentesMap, contribuyentesPojosMap);
                    }

                }

                wb.close();

                System.out.println("\nSe ha completado la lectura del archivo\n");

                //DEPURACION
                System.out.println("Número de NIFs leídos: " + dniLeidos);
                System.out.println("Número de NIFs correctos o subsanados: " + dniSet.size());
                System.out.println("Número de CCCs correctos o subsanados: " + cccSet.size());
                System.out.println("Número de e-mails generados: " + correoSet.size());
            }
            catch (IOException e) {
                System.out.println("Error al leer el archivo " + e.getMessage());
                e.printStackTrace();
            }
        }
        //Hoja "Vehiculos" -> Comprobación de vehiculos
        else if (filepath.equals("resources\\SistemasVehiculos.xlsx") && sheet == 1) {
            try {
                FileInputStream file = new FileInputStream(filepath);
                XSSFWorkbook wb = new XSSFWorkbook(file);
                file.close(); //No es necesario mantenerlo abierto

                //Se lee la hoja
                XSSFSheet ws = wb.getSheetAt(sheet);
                System.out.println("\nLeyendo hoja \"" + ws.getSheetName() + "\"");

                //Iteración por las filas de la hoja
                Iterator<Row> rowIter = ws.iterator();

                ValidadorVehiculo validaVehiculo = new ValidadorVehiculo();

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

                    Cell tipoVehiculoCell = null;
                    Cell matriculaCell = null;
                    Cell fechaMatriculacionCell = null;
                    Cell fechaAltaCell = null;
                    Cell fechaBajaCell = null;
                    Cell fechaBajaTempCell = null;
                    Cell nifPropietarioCell = null;

                    System.out.println();

                    while (cellIter.hasNext()) {
                        Cell cell = cellIter.next();

                        if ((cell.getColumnIndex() == 0 || cell.getColumnIndex() == 3) && cell.getCellType() == CellType.NUMERIC) {
                            //Si es celda formato fecha
                            if (DateUtil.isCellDateFormatted(cell)) {
                                System.out.print(cell.getDateCellValue() + "\t");
                            } else {
                                System.out.print((int) cell.getNumericCellValue() + "\t");
                            }
                        }
                        else if ((cell.getColumnIndex() == 0 || cell.getColumnIndex() == 3) && cell.getCellType() == CellType.STRING) {
                            System.out.print(cell.getStringCellValue() + "\t");
                        }

                        if (cell.getColumnIndex() == 0) {
                            tipoVehiculoCell = cell;
                        }

                        if (cell.getColumnIndex() == 3) {
                            matriculaCell = cell;
                        }

                        if(cell.getColumnIndex() == 10) {
                            fechaMatriculacionCell = cell;
                        }

                        if(cell.getColumnIndex() == 11) {
                            fechaAltaCell = cell;
                        }

                        if(cell.getColumnIndex() == 12) {
                            fechaBajaCell = cell;
                        }

                        if(cell.getColumnIndex() == 13) {
                            fechaBajaTempCell = cell;
                        }

                        if(cell.getColumnIndex() == 14) {
                            nifPropietarioCell = cell;
                        }
                    }

                    //Comprueba que los datos del vehiculo son correctos
                    if (matriculaCell != null && tipoVehiculoCell != null) {
                        validaVehiculo.comprobarVehiculo(wb, row, matriculaSet, dniSet, matriculaCell, tipoVehiculoCell, fechaMatriculacionCell, fechaAltaCell, fechaBajaCell, fechaBajaTempCell, nifPropietarioCell, vehiculosContribuyentesMap, vehiculosPojosContribuyentesMap, contribuyentesPojosMap);
                    }
                    else {
                        System.out.println("No es posible comprobar los datos del vehículo.");
                    }

                }

                wb.close();

                System.out.println("\nSe ha completado la lectura del archivo\n");

                //DEPURACION
                System.out.println("Número de matrículas correctas: " + matriculaSet.size());
            }
            catch (IOException e) {
                System.out.println("Error al leer el archivo " + e.getMessage());
                e.printStackTrace();
            }
        }
        /*Lee la hoja Ordenanza para determinar el importe a pagar por cada vehiculo*/
        else if (filepath.equals("resources\\SistemasOrdenanzas.xlsx") && sheet == 0) {
            try {
                FileInputStream file = new FileInputStream(filepath);
                XSSFWorkbook wb = new XSSFWorkbook(file);
                file.close(); //No es necesario mantenerlo abierto

                //Se lee la hoja
                XSSFSheet ws = wb.getSheetAt(sheet);
                System.out.println("\nSe van a calcular los importes correspondientes a cada vehículo \"" + ws.getSheetName() + "\"");

                //Pido por pantalla el año del que se quieren generar los recibos
                System.out.println("\n\n\nIntroduce el año del que se quieren generar los recibos con formato \"aaaa\" (ej. 2024): ");
                int anio = solicitaAnyo();

                ImporteRecibo importe = new ImporteRecibo();
                //Meter en contribuyentes map (cuando se mapea en validadorCCC) el ayuntamiento (nuevo atributo)
                // para calcular el importe correcto cuando recorre ordenanza en base al ayto
                importe.calculaImporte(ws, vehiculosContribuyentesMap, contribuyentesMap, ordenanzasMap, vehiculosPojosContribuyentesMap, anio);

                wb.close();

                //DEPURACION
                contribuyentesPojosMap.forEach((key, value) -> {
                    System.out.println("ID: " + key + ", NIF: " + value.getNifnie());
                });

                /*Imprime por pantalla los recibos de los vehículos del año solicitado*/
                printContribuyentesVehiculos(contribuyentesMap, vehiculosContribuyentesMap, anio, contribuyentesPojosMap, vehiculosPojosContribuyentesMap);
            }
            catch (IOException e) {
                System.out.println("Error al leer el archivo " + e.getMessage());
                e.printStackTrace();
            }
        }
        else {
            System.out.println("Ruta u hoja errónea.");
        }
    }

    /*
    * Metodo que permite modificar el libro que se está leyendo con la ruta pasada como parámetro
    * Recibe la fila y la columna de la hoja que se desea modificar
    * También recibe el nuevo valor que modificará la celda
    */
    public void updateExcel (XSSFWorkbook wb, String filepath, int sheet, int row_, int col_, Object newVal) {
        try {
            Sheet hojaModif = wb.getSheetAt(sheet); //Index 0 -> Hoja 1 -> Contribuyentes

            //Obtiene la fila, o la crea si no existe
            Row row = hojaModif.getRow(row_);
            if (row == null) {
                row = hojaModif.createRow(row_);
            }

            //Obtiene la celda, o la crea si no existe
            Cell cell = row.getCell(col_);
            if (cell == null) {
                cell = row.createCell(col_);
            }

            //Obtiene el tipo de dato original de la celda -> No se pueden cambiar los tipos de datos
            Object cellType = cell.getCellType();
            System.out.println("La celda que se quiere modificar es de tipo " + cellType);

            //Modifica el valor de la celda según el tipo de dato de la misma
            if (cellType == CellType.STRING && newVal instanceof String) {
                cell.setCellValue((String) newVal);
                System.out.println("El nuevo valor de la celda tras modificarse es: " + newVal + " [String]");
            }
            else if (cellType == CellType.NUMERIC && newVal instanceof Number) {
                cell.setCellValue(((Number) newVal).longValue()); // Los números en Apache POI se almacenan como double
                System.out.println("El nuevo valor de la celda tras modificarse es: " + newVal + " [Number]");
            }
            else if (cellType == CellType.STRING && newVal instanceof Number) {
                cell.setCellValue(String.format("%.0f", ((Number) newVal).doubleValue())); // Los números en Apache POI se almacenan como double
                System.out.println("El nuevo valor de la celda tras modificarse es: " + newVal + " [Number]");
            }
            else if (cellType == CellType.NUMERIC && newVal instanceof String) {
                cell.setCellValue(Double.parseDouble((String) newVal)); // Los números en Apache POI se almacenan como double
                System.out.println("El nuevo valor de la celda tras modificarse es: " + newVal + " [Number]");
            }
            else {
                //Si la celda es de tipo BLANK
                if (cellType == CellType.BLANK && newVal instanceof String) {
                    cell.setCellValue((String) newVal);
                    System.out.println("El valor de la nueva celda creada es: " + newVal + " [String]");
                }
                else if (cellType == CellType.BLANK && newVal instanceof Number) {
                    cell.setCellValue(((Number) newVal).longValue());
                    System.out.println("El valor de la nueva celda creada es: " + newVal + " [Number]");
                }
                else {
                    throw new IllegalArgumentException("Tipo de dato incompatible con el tipo de celda existente.");
                }
            }

            //Guarda los cambios en el archivo
            FileOutputStream out = new FileOutputStream(filepath);
            wb.write(out);
            out.close();

            System.out.println("El valor de la celda " + row_ + "-" + col_ +" ha sido modificado correctamente\n");

        } catch (IOException e) {
            System.out.println("Error al modificar el archivo " + e.getMessage());
            e.printStackTrace();
        }

    }

    /*Metodo que comprueba si una fila esta vacia o no*/
    public static boolean isEmpty(Row comprobar){
        if (comprobar == null){

            return true;
        }

        for(Cell cell: comprobar){
            if (cell.getCellType() != CellType.BLANK && cell.getCellType() != CellType._NONE){

                return false;
            }
        }

        return true;
    }

    /*Pide al usuario el año del que se quieren generar los recibos*/
    private int solicitaAnyo () {
        Scanner scan = new Scanner(System.in);

        int anio = scan.nextInt();

        scan.close();

        return anio;
    }

    /* Metodo que imprime los recibos por pantalla y loa guarda en un Map (solo los que hay que generar)*/
    private void printContribuyentesVehiculos(Map<String, ContribuyenteExcel> contribuyentesMap, Map<String, List<VehiculoExcel>> vehiculosContribuyentesMap, int anio, Map<String, Contribuyente> contribuyentesPojosMap, Map<String, List<Vehiculos>> vehiculosPojosContribuyentesMap) throws IOException {
        //Obtengo el día de hoy (Día que se genera el recibo)
        LocalDate hoy = LocalDate.now();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaPadron = "01/01/" + anio;

        System.out.println("\n------------------  RECIBOS GENERADOS  ------------------\n");

        int i = 1;
        int totalVehiculos = 0;
        double totalPadron = 0.0;

        for (Map.Entry<String, List<VehiculoExcel>> entry : vehiculosContribuyentesMap.entrySet()) {
            String nif = entry.getKey().trim();
            List<VehiculoExcel> vehiculos = entry.getValue();


            for (VehiculoExcel v : vehiculos) {
                ContribuyenteExcel c = contribuyentesMap.get(nif);

                //Si el propietario es erróneo
                if (c == null) {
                    List<String> errores = new ArrayList<>();
                    errores.add("Vehiculo con propietario erroneo.");
                    editor.xmlVehiculos(vehiculosRutaXML, v.getIdFila()+1, v.getMarca(), v.getModelo(), errores);
                    continue;
                }

                //Se pide el recibo de un año en el que el vehículo todavía no ha sido de alta
                //El importe será 0 pero genera recibo si no se comprueba
                Calendar calAlta = Calendar.getInstance();
                calAlta.setTime(v.getFechaAlta());
                int anioAlta = calAlta.get(Calendar.YEAR);

                if (anio < anioAlta || v.getNumTrimestres() <= 0) {
                    continue;
                }

                totalPadron = totalPadron + v.getTotal();

                //Imprime los datos del contribuyente/propietario
                System.out.println("Nombre: " + c.getNombre());
                System.out.println("Apellido 1: " + c.getApellido1());
                System.out.println("Apellido 2: " + c.getApellido2());
                System.out.println("NIFNIE: " + c.getNifnie());
                System.out.println("Direccion: " + c.getDireccion());
                System.out.println("IBAN: " + c.getIban());
                System.out.println("Bonificacion: " + c.getBonificacion());

                //Fecha del recibo
                System.out.println("\nFecha del recibo: " + hoy.format(formato) + "\n"); //Día que ha sido generado el recibo

                String unidadCobro;
                //Paso la unidad de cobro a String para imprimir
                switch (v.getUnidadCobro()) {
                    case 1:
                        unidadCobro = "CABALLOS";
                        break;
                    case 2:
                        unidadCobro = "PLAZAS";
                        break;
                    case 3:
                        unidadCobro = "KG";
                        break;
                    case 4:
                        unidadCobro = "CC";
                        break;
                    default:
                        unidadCobro = "No definido";
                        break;
                }

                System.out.println("Tipo de vehículo: " + v.getTipoVehiculo());
                System.out.println("Marca: " + v.getMarca());
                System.out.println("Modelo: " + v.getModelo());
                System.out.println("Matricula: " + v.getMatricula());
                System.out.println("Número de bastidor: " + v.getBastidor());
                System.out.println("Unidad de cobro: " + unidadCobro);
                System.out.println("Valor de la unidad de cobro: " + v.getValorUnidad());
                System.out.println("Importe (sin exenciones ni bonificaciones): " + v.getImporte());
                System.out.println("Exencion: " + v.getExencion());
                System.out.println("\nImporte total del recibo: " + v.getTotal());
                System.out.println("\n---------------------------------------------------------------------------\n");

                totalVehiculos++;

                //Añade el recibo a Recibos.xml
                editor.xmlRecibo(recibosXML, fechaPadron, 0,totalVehiculos,totalVehiculos, v.getExencion(), v.getIdFila()+1, c.getNombre(), c.getApellido1(), c.getApellido2(), c.getNifnie(), c.getIban(), v.getTipoVehiculo(), v.getMarca(), v.getModelo(), v.getMatricula(), v.getTotal());

                String matricula = v.getMatricula().trim().toUpperCase();
                List<Vehiculos> vehiculosPojos = vehiculosPojosContribuyentesMap.get(matricula);

                if (vehiculosPojos == null ||vehiculosPojos.isEmpty()) {
                    System.out.println("No se encontró ningun POJO de vehículo con matrícula " + matricula);
                    continue;
                }

                //Por si acaso hay duplicados (no debería)
                for (Vehiculos vPojos : vehiculosPojos) {
                    if (vPojos == null) {
                        System.out.println("Un vehículo en la lista con matrícula " + matricula + "es nulo");
                        continue;
                    }

                    Contribuyente contribuyente = vPojos.getContribuyente();

                    if (contribuyente == null) {
                        System.out.println("El vehículo no tiene un propietario válido.");
                        continue;
                    }

                    String nifContribuyente = contribuyente.getNifnie();
                    Contribuyente cPojos = contribuyentesPojosMap.get(nifContribuyente);

                    if (cPojos == null) {
                        System.out.println("No se encontró contribuyente POJO con NIF: " + nifContribuyente);
                        continue;
                    }

                    if (v.getImporte() >= 0.0 || v.getExencion() == 'S') {
                        mapeaRecibos(c, v, i, vPojos, cPojos);
                        i++;
                    }

                }

                /*for (Map.Entry<String, List<Vehiculos>> entry2 : vehiculosPojosContribuyentesMap.entrySet()) {
                    List<Vehiculos> vehiculosPojos = entry2.getValue();

                    // Validar lista de vehículos
                    if (vehiculosPojos == null || vehiculosPojos.isEmpty()) {
                        System.out.println("La lista de vehículos está vacía para la clave: " + entry2.getKey());
                        continue;
                    }

                    for (Vehiculos vPojos : vehiculosPojos) {
                        Contribuyente contribuyente = vPojos.getContribuyente();

                        if (contribuyente == null) {
                            System.out.println("Advertencia: Vehículo con matrícula " + vPojos.getMatricula()
                                    + " no tiene un contribuyente asociado. Se omite.");
                            continue; // Omitir este vehículo
                        }

                        if (vPojos == null) {
                            System.out.println("Un vehículo en la lista es nulo.");
                            continue;
                        }

                        String nifContribuyente = vPojos.getContribuyente().getNifnie();
                        Contribuyente cPojos = contribuyentesPojosMap.get(nifContribuyente);

                        // Validar contribuyente
                        if (cPojos == null) {
                            System.out.println("No se encontró contribuyente con NIF: " + nifContribuyente);
                            continue;
                        }

                        //Añade el recibo al Map que almacena los recibos que se deben generar
                        //Tanto de Pojos como los propios
                        if (v.getImporte() >= 0.0 || v.getExencion() == 'S') {
                            mapeaRecibos(c, v, i, vPojos, cPojos);
                            i++;
                        }
                    }
                }*/
            }
        }

        //Redondea el total del padrón a 2 decimales
        totalPadron = Math.round(totalPadron * 100.0) / 100.0;
        String fecha= "IVTM de "+anio;
        editor.modificarAtributosPadron(recibosXML, totalPadron, fecha, totalVehiculos);
        System.out.println("Número de recibos generados: " + totalVehiculos);

        System.out.println("\nIVTM de " + fechaPadron); //Siempre el 1 de enero del año solicitado
        System.out.println("Importe total del padrón (Suma de todos los recibos generados): " + totalPadron + "€");
        editor.ordenarVehiculosPorId(vehiculosRutaXML);
        editor.ordenarRecibosPorIdFila(recibosXML);

        System.out.println("\n Número de recibos mapeados: " + recibosMap.size());

        //Generación de recibos y resumen en formato PDF
        reciboPDF.generaRecibos(recibosMap, anio);
        reciboPDF.generaResumen(anio, totalPadron, recibosMap.size()+1);

    }

    /*Metodo que mapea los recibos generados para un año usando como clave el nif del contribuyente*/
    public void mapeaRecibos(ContribuyenteExcel c, VehiculoExcel v, int numRecibo, Vehiculos vPojos, Contribuyente cPojos) {
        LocalDate localDate = LocalDate.now();
        Date fechaRecibo = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        int anioPadron = LocalDate.now().getYear();
        LocalDate unoDelUno = LocalDate.of(anioPadron, 1, 1);
        Date fechaPadron = Date.from(unoDelUno.atStartOfDay(ZoneId.systemDefault()).toInstant());

        String nifPropietario = v.getNifPropietario().trim();

        ReciboExcel r = new ReciboExcel();

        //r.setNumRecibo(numRecibo);
        r.setContribuyente(c);
        r.setVehiculo(v);
        r.setNifPropietario(nifPropietario);
        r.setFechaRecibo(fechaRecibo);
        r.setFechaPadron(fechaPadron);
        r.setUnidadCobro(v.getUnidadCobro());
        r.setTotalRecibo(v.getTotal());

        //Para cada propietario, crea una lista de recibos
        //Si solo tiene un recibo, es una lista de 1 elementos
        //Si tiene más, es una lista de varios recibos asignados a ese nif
        recibosMap.computeIfAbsent(nifPropietario, k -> new ArrayList<>()).add(r);

        //Mapeo recibos POJOS
        Recibos rec = new Recibos();
        //rec.setNumRecibo(numRecibo);
        rec.setContribuyente(cPojos);
        rec.setVehiculos(vPojos);
        rec.setFechaPadron(fechaPadron);
        rec.setFechaRecibo(fechaRecibo);
        rec.setNifContribuyente(nifPropietario);
        rec.setDireccionCompleta(cPojos.getDireccion());
        rec.setIban(cPojos.getIban());
        rec.setTipoVehiculo(vPojos.getTipo());
        rec.setMarcaModelo(vPojos.getMarca() + " " + vPojos.getModelo());

        switch (v.getUnidadCobro()) {
            case 1:
                rec.setUnidad("CABALLOS");
                break;
            case 2:
                rec.setUnidad("PLAZAS");
                break;
            case 3:
                rec.setUnidad("KG");
                break;
            case 4:
                rec.setUnidad("CC");
                break;
        }

        switch (vPojos.getTipo()) {
            case "TURISMO":
            case "TRACTOR":
                rec.setValorUnidad(vPojos.getCaballosFiscales());
                break;
            case "CAMION":
            case "REMOLQUE":
                rec.setValorUnidad(vPojos.getKgcarga());
                break;
            case "MOTOCICLETA":
            case "CICLOMOTOR":
                rec.setValorUnidad(vPojos.getCentimetroscubicos());
            case "AUTOBUS":
                rec.setValorUnidad(vPojos.getPlazas());
        }

        rec.setTotalRecibo(v.getTotal());
        rec.setExencion(vPojos.getExencion());
        rec.setBonificacion(cPojos.getBonificacion());
        rec.setEmail(cPojos.getEmail());
        rec.setAyuntamiento(cPojos.getAyuntamiento());

        String claveRecibos = nifPropietario + fechaPadron.toString() + vPojos.getMarca() + vPojos.getModelo();

        recibosPojosMap.computeIfAbsent(claveRecibos, k -> new ArrayList<>()).add(rec);

        //Mapea los datos que faltaban en Contribuyente y Vehiculos
        mapeaRestantes(rec);
    }

    public void mapeaRestantes(Recibos rec) {
        Contribuyente  c = rec.getContribuyente();
        Vehiculos v = rec.getVehiculos();

        for (List<Ordenanza> listaOrdenanzas : ordenanzasMap.values()) {
            for (Ordenanza o : listaOrdenanzas) {
                Set<Vehiculos> vehiculosOrdenanza = o.getVehiculoses();

                //Si el vehículo está en el Set de la ordenanza
                if (vehiculosOrdenanza.contains(v)) {
                    //Setteo la ordenanza al vehículo
                    v.setOrdenanza(o);

                    //Añado el recibo al Set de recibos del vehiculo
                    v.getReciboses().add(rec);
                    break;
                }
            }
        }

        c.getVehiculoses().add(v);
        c.getReciboses().add(rec);
    }

    /*Metodo que envía los Maps a los métodos que insertan/actualizan la DB*/
    public void actualizaDB() {
        InsertarActualizarDB db = new InsertarActualizarDB();
        db.insertaActualizaContribuyentes(contribuyentesPojosMap);
        db.insertaActualizaOrdenanzas(ordenanzasMap);
        db.insertaActualizaVehiculos(recibosPojosMap);
        db.insertaActualizaRecibos(recibosPojosMap);
    }

    /*Metodo que limpia los sets al finalizar la ejecución*/
    public void cleanSets() {
        //Despues de leer el documento, vació el HashSet para no consumir memoria
        dniSet.clear();
        cccSet.clear();
        correoSet.clear();
        matriculaSet.clear();
        contribuyentesMap.clear();
        vehiculosContribuyentesMap.clear();
        recibosMap.clear();
        contribuyentesPojosMap.clear();
        vehiculosPojosContribuyentesMap.clear();
        recibosPojosMap.clear();
        ordenanzasMap.clear();
    }

}
