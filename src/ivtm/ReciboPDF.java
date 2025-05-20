package ivtm;

import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import modelosExcel.ReciboExcel;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.text.SimpleDateFormat;

/*Clase para generar los recibos en formato PDF*/
public class ReciboPDF {
    private static final String img_path = "resources/ivtm.png";
    private static final String salida_path = "resources/recibos/";

    public void generaRecibo(Map<String, List<ReciboExcel>> recibos, int anio) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        for (Map.Entry<String, List<ReciboExcel>> entry : recibos.entrySet()) {
            for (ReciboExcel r : entry.getValue()) {
                String nombreArchivo = salida_path + (r.getNifPropietario() +
                        r.getContribuyente().getNombre() + r.getContribuyente().getApellido1() +
                        r.getContribuyente().getApellido2() + r.getVehiculo().getMatricula() + anio + ".pdf");

                PdfWriter writer = new PdfWriter(nombreArchivo);
                PdfDocument pdfDoc = new PdfDocument(writer);
                Document doc = new Document(pdfDoc, PageSize.LETTER);

                Table tabla1 = new Table(new float[]{1, 1}).useAllAvailableWidth();

                Paragraph nom = new Paragraph(r.getContribuyente().getAytoCont());
                Paragraph cif = new Paragraph("CIF: A73901312");
                Paragraph dir1 = new Paragraph("Calle del Ayuntamiento, 19");
                Paragraph dir2 = new Paragraph("24280 " + r.getContribuyente().getAytoCont() + " León");

                Cell ayto = new Cell()
                        .setBorder(new SolidBorder(1))
                        .setTextAlignment(TextAlignment.CENTER)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                        .add(nom)
                        .add(cif)
                        .add(dir1)
                        .add(dir2);
                tabla1.addCell(ayto);

                Cell rec = new Cell()
                        .setBorder(Border.NO_BORDER)
                        .setPadding(10)
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                        .add(new Paragraph("Número de recibo: " + r.getNumRecibo()))
                        .add(new Paragraph("IBAN: " + r.getContribuyente().getIban()))
                        .add(new Paragraph("Fecha recibo: " + sdf.format(r.getFechaRecibo())))
                        .add(new Paragraph("Fecha de matriculación: " + sdf.format(r.getVehiculo().getFechaMatriculacion())))
                        .add(new Paragraph("Fecha de alta: " + sdf.format(r.getVehiculo().getFechaAlta())));
                tabla1.addCell(rec);

                Table tabla2 = new Table(new float[]{1, 1}).useAllAvailableWidth();

                Image img = new Image(ImageDataFactory.create(img_path))
                        .scaleToFit(80, 80)
                        .setHorizontalAlignment(HorizontalAlignment.CENTER);

                Cell imagen = new Cell()
                        .setBorder(Border.NO_BORDER)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setHeight(80)
                        .add(img);
                tabla2.addCell(imagen);

                Cell dest = new Cell()
                        .setBorder(new SolidBorder(1))
                        .setPadding(10)
                        .setTextAlignment(TextAlignment.LEFT)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                        .add(new Paragraph("Destinatario:").setUnderline())
                        .add(new Paragraph(r.getContribuyente().getNombre() + " " + r.getContribuyente().getApellido1() + " " + r.getContribuyente().getApellido2()))
                        .add(new Paragraph("DNI: " + r.getNifPropietario()))
                        .add(new Paragraph("Dirección: " + r.getContribuyente().getDireccion()))
                        .add(new Paragraph("Población: " + r.getContribuyente().getAytoCont()));
                tabla2.addCell(dest);

                Table vehic = new Table(3).useAllAvailableWidth().setBorder(new SolidBorder(1));
                vehic.addCell(new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER).add(new Paragraph("Tipo: " + r.getVehiculo().getTipoVehiculo().toUpperCase())));
                vehic.addCell(new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER).add(new Paragraph(r.getVehiculo().getMarca() + " " + r.getVehiculo().getModelo().toUpperCase())));
                vehic.addCell(new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER).add(new Paragraph("Matrícula: " + r.getVehiculo().getMatricula().toUpperCase())));

                Table bast = new Table(1).useAllAvailableWidth().setBorder(new SolidBorder(1));
                bast.addCell(new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER).add(new Paragraph("Nº de bastidor: " + r.getVehiculo().getBastidor())));

                Table recIvtm = new Table(1).useAllAvailableWidth().setBorder(Border.NO_BORDER);
                Text texto = new Text("\nRecibo vehículo: Ejercicio " + anio + ". Número de trimestres: " + r.getVehiculo().getNumTrimestres() + "\n");
                recIvtm.addCell(new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER).add(new Paragraph().add(texto)));

                boolean bajaVehic = false;
                StringBuilder bajaStr = new StringBuilder("baja");
                StringBuilder fechaStr = new StringBuilder(" con fecha: ");

                if (r.getVehiculo().getFechaBaja() != null) {
                    bajaStr.append(" definitiva");
                    fechaStr.append(sdf.format(r.getVehiculo().getFechaBaja()));
                    bajaVehic = true;
                } else if (r.getVehiculo().getFechaBajaTemporal() != null) {
                    bajaStr.append(" temporal");
                    fechaStr.append(sdf.format(r.getVehiculo().getFechaBajaTemporal()));
                    bajaVehic = true;
                }

                Table baja = new Table(1).useAllAvailableWidth().setBorder(new SolidBorder(3));
                if (bajaVehic) {
                    baja.addCell(new Cell().setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE).setTextAlignment(TextAlignment.CENTER).add(new Paragraph("Vehículo en " + bajaStr + fechaStr)));
                }

                //Características del vehículo
                Table caracs = new Table(7).useAllAvailableWidth().setBorderTop(new SolidBorder(2));
                String[] headers = {"Tipo", "Marca", "Modelo", "Unidad", "Valor unidad", "Importe", "Descuento"};
                for (String h : headers) {
                    caracs.addCell(new Cell().setBorderBottom(new SolidBorder(2)).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER).add(new Paragraph(h)));
                }

                //Table vehic2 = new Table(7).useAllAvailableWidth().setBorder(Border.NO_BORDER);
                caracs.addCell(new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER).add(new Paragraph(r.getVehiculo().getTipoVehiculo().toUpperCase())));
                caracs.addCell(new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER).add(new Paragraph(r.getVehiculo().getMarca().toUpperCase())));
                caracs.addCell(new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER).add(new Paragraph(r.getVehiculo().getModelo().toUpperCase())));
                caracs.addCell(new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER).add(new Paragraph(r.getUnidadCobro())));
                caracs.addCell(new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER).add(new Paragraph(String.format("%.2f", r.getVehiculo().getValorUnidad()))));
                caracs.addCell(new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER).add(new Paragraph(String.format("%.2f", r.getVehiculo().getImporte()))));
                caracs.addCell(new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER).add(new Paragraph(String.format("%.2f", r.getContribuyente().getBonificacion()) + "%")));

                Table base = new Table(2).useAllAvailableWidth().setBorder(Border.NO_BORDER);
                base.addCell(new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT).add(new Paragraph("TOTAL BASE IMPONIBLE..........................")));
                base.addCell(new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT).add(new Paragraph(String.format("%.2f", r.getTotalRecibo()))));

                Table total = new Table(2).useAllAvailableWidth().setBorderTop(new SolidBorder(2));
                total.addCell(new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT).add(new Paragraph("TOTAL RECIBO.......................................")));
                total.addCell(new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT).add(new Paragraph(String.format("%.2f", r.getTotalRecibo()))));

                doc.add(tabla1.setMarginTop(10));
                doc.add(tabla2);
                doc.add(vehic.setMarginTop(10));
                doc.add(bast);
                doc.add(recIvtm.setMarginTop(20));
                if (bajaVehic) doc.add(baja.setMarginTop(5));
                doc.add(caracs.setMarginTop(20));
                //doc.add(vehic2.setMarginTop(10));
                doc.add(base.setMarginTop(30));
                doc.add(total.setMarginTop(20));

                doc.close();
            }
        }

    }






}
