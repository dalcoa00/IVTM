package ivtm;

import modelosExcel.ReciboExcel;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.*;

import java.util.List;
import java.util.Map;
import java.io.File;
import java.text.SimpleDateFormat;

public class ReciboPDF {
    /*
    *Clase para generar los recibos en formato PDF
    *
    * Se puede generar el Resumen PDF aquí también, pero como es mucha repetición de código igual
    * hacer en otra clase para que no nos líe el día de la modificación*/

    private static final String img_path = "resources/ivtm.png";
    private static final String salida_path = "resources/recibos/";

    public void generaRecibo(Map<String, List<ReciboExcel>> recibos, int anio) {
        //Crea el directorio si no existe
        File dir = new File(salida_path);

        if (!dir.exists()) dir.mkdirs();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        for (Map.Entry<String, List<ReciboExcel>> entry : recibos.entrySet()) {
            for (ReciboExcel r : entry.getValue()) {
                String nombreArchivo = salida_path + r.getNifPropietario() +
                        r.getContribuyente().getNombre() + r.getContribuyente().getApellido1() +
                        r.getContribuyente().getApellido2() + r.getVehiculo().getMatricula() + anio;

                PdfWriter writer = new PdfWriter();
            }
        }


        System.out.println("\n\nNúmero de recibos mapeados: " + recibos.size());
    }






}
