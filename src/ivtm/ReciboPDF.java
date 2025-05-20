package ivtm;

import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
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
                String nombreArchivo = salida_path + r.getNifPropietario() +
                        r.getContribuyente().getNombre() + r.getContribuyente().getApellido1() +
                        r.getContribuyente().getApellido2() + r.getVehiculo().getMatricula() + anio;

                PdfWriter writer = new PdfWriter(nombreArchivo);
                PdfDocument pdfDoc = new PdfDocument(writer);
                Document doc = new Document(pdfDoc, PageSize.LETTER);

                //Cabecera ayuntamiento, recibo, IBAN y fechas
                Paragraph empty = new Paragraph("");
                Table tabla1 = new Table(2);
                tabla1.setWidth(500);

                //Paragraph para que sean títulos en negrita??
                Paragraph nom = new Paragraph(r.getContribuyente().getAytoCont());
                Paragraph cif = new Paragraph("CIF: A73901312");
                Paragraph dir1 = new Paragraph("Dirección del ayuntamiento: Calle del Ayuntamiento, 19");
                Paragraph dir2 = new Paragraph("Código postal: 24280 " + r.getContribuyente().getAytoCont() + " León");

                //Ayuntamiento
                Cell ayto = new Cell();
                ayto.setBorder(new SolidBorder(1));
                ayto.setWidth(250);
                ayto.setTextAlignment(TextAlignment.CENTER);

                ayto.add(nom);
                ayto.add(cif);
                ayto.add(dir1);
                ayto.add(dir2);
                tabla1.addCell(ayto);

                //Recibo, IBAN y fechas
                Cell rec = new Cell();
                rec.setBorder(Border.NO_BORDER);
                rec.setPadding(10);
                rec.setTextAlignment(TextAlignment.RIGHT);

                rec.add(new Paragraph("Número de recibo: " + r.getNumRecibo()));
                rec.add(new Paragraph("IBAN: " + r.getContribuyente().getIban()));
                rec.add(new Paragraph("Fecha recibo: " + sdf.format(r.getFechaRecibo())));
                rec.add(new Paragraph("Fecha de matriculación: " + sdf.format(r.getVehiculo().getFechaMatriculacion())));
                rec.add(new Paragraph("Fecha de alta: " + sdf.format(r.getVehiculo().getFechaAlta())));
                tabla1.addCell(rec);

                //Imagen y destinatario
                Table tabla2 = new Table(2);
                tabla2.setWidth(500);

                Image img = new Image(ImageDataFactory.create(img_path));
                img.setBorder(Border.NO_BORDER);
                img.setPadding(10);

                Cell imagen = new Cell();
                imagen.add(img);
                imagen.setBorder(Border.NO_BORDER);
                imagen.setPaddingLeft(23);
                imagen.setPaddingTop(20);
                imagen.setWidth(250);
                tabla2.addCell(imagen);

                Cell dest = new Cell();
                dest.setBorder(new SolidBorder(1));
                dest.setPadding(10);
                dest.setTextAlignment(TextAlignment.RIGHT);

                dest.add(new Paragraph("Destinatario: " + r.getContribuyente().getNombre() + " " + r.getContribuyente().getApellido1() + " " + r.getContribuyente().getApellido2()));
                dest.add(new Paragraph("DNI: " + r.getNifPropietario()));
                dest.add(new Paragraph("Dirección: " + r.getContribuyente().getDireccion()));
                dest.add(new Paragraph("Población: " + r.getContribuyente().getAytoCont()));
                tabla2.addCell(dest);

                //Vehículo
                Table vehic = new Table(3);
                vehic.setWidth(500);
                vehic.setPaddingLeft(10);
                vehic.setPaddingRight(10);
                vehic.setBorder(new SolidBorder(1));

                vehic.addCell(new Paragraph("Tipo: " + r.getVehiculo().getTipoVehiculo().toUpperCase()));
                vehic.addCell(new Paragraph(r.getVehiculo().getMarca() + " " + r.getVehiculo().getModelo().toUpperCase()));
                vehic.addCell(new Paragraph("Matrícula: " + r.getVehiculo().getMatricula().toUpperCase()));

                //Bastidor
                Table bast = new Table(1);
                bast.setWidth(500);
                bast.setBorder(new SolidBorder(1));
                bast.setTextAlignment(TextAlignment.CENTER);

                bast.addCell(new Paragraph("Nº de bastidor: " + r.getVehiculo().getBastidor()));

                //Recibo vehículo
                Table recIvtm = new Table(1);
                recIvtm.setWidth(500);
                recIvtm.setBorder(Border.NO_BORDER);
                recIvtm.setTextAlignment(TextAlignment.CENTER);
                recIvtm.setPaddingLeft(100);
                recIvtm.setPaddingRight(100);
                recIvtm.setPaddingTop(20);
                recIvtm.setPaddingBottom(20);

                Text texto = new Text("Recibo vehículo: Ejercicio " + anio + ". Número de trimestres: " + r.getVehiculo().getNumTrimestres());
                texto.setBorderBottom(new SolidBorder(3));
                Paragraph p = new Paragraph().add(texto);
                recIvtm.addCell(p);

                //Si vehículo de baja
                boolean bajaVehic = false;
                StringBuilder bajaStr = new StringBuilder("baja");
                StringBuilder fechaStr = new StringBuilder(" con fecha: ");
                if (r.getVehiculo().getFechaBaja() != null || r.getVehiculo().getFechaBajaTemporal() != null){
                    if (r.getVehiculo().getFechaBaja() != null) {
                        bajaStr.append(" definitiva");
                        fechaStr.append(sdf.format(r.getVehiculo().getFechaBaja()));
                        bajaVehic = true;
                    }
                    else if (r.getVehiculo().getFechaBajaTemporal() != null) {
                        bajaStr.append(" temporal");
                        fechaStr.append(sdf.format(r.getVehiculo().getFechaBajaTemporal()));
                        bajaVehic = true;
                    }
                }

                Table baja = new Table(1);
                baja.setWidth(500);
                baja.setBorder(new SolidBorder(3));
                baja.setTextAlignment(TextAlignment.CENTER);

                baja.addCell(new Paragraph("Vehículo en " + bajaStr + fechaStr));

                //Características vehículo
                Table caracs = new Table(6);
                caracs.setWidth(500);
                caracs.setBorderTop(new SolidBorder(2));
                caracs.setBorderBottom(new SolidBorder(2));
                caracs.setTextAlignment(TextAlignment.CENTER);

                caracs.addCell(new Paragraph("Tipo"));
                caracs.addCell(new Paragraph("Marca"));
                caracs.addCell(new Paragraph("Modelo"));
                caracs.addCell(new Paragraph("Unidad"));
                caracs.addCell(new Paragraph("Valor unidad"));
                caracs.addCell(new Paragraph("Importe"));
                caracs.addCell(new Paragraph("Descuento"));

                //Características vehículo recibo
                Table vehic2 = new Table(7);
                vehic2.setWidth(500);
                vehic2.setTextAlignment(TextAlignment.CENTER);
                vehic2.setPaddingTop(10);
                vehic2.setPaddingBottom(10);
                vehic2.setBorder(Border.NO_BORDER);

                vehic2.addCell(new Paragraph(r.getVehiculo().getTipoVehiculo().toUpperCase()));
                vehic2.addCell(new Paragraph(r.getVehiculo().getMarca().toUpperCase()));
                vehic2.addCell(new Paragraph(r.getVehiculo().getModelo().toUpperCase()));
                vehic2.addCell(new Paragraph(r.getUnidadCobro()));
                vehic2.addCell(new Paragraph(String.format("%.2f", r.getVehiculo().getValorUnidad())));
                vehic2.addCell(new Paragraph(String.format("%.2f", r.getVehiculo().getImporte())));
                vehic2.addCell(new Paragraph(String.format("%.2f", r.getContribuyente().getBonificacion()) + "%"));

                //Base imponible
                Table base = new Table(2);
                base.setWidth(500);
                base.setBorder(Border.NO_BORDER);
                base.setPaddingTop(10);

                Cell base1 = new Cell();
                base1.setTextAlignment(TextAlignment.LEFT);
                Paragraph b = new Paragraph("TOTAL BASE IMPONIBLE......................");
                base1.add(b);
                base.addCell(base1);

                Cell base2 = new Cell();
                base2.setTextAlignment(TextAlignment.RIGHT);
                Paragraph a = new Paragraph(String.format("%.2f", r.getTotalRecibo()));
                base2.add(a);
                base.addCell(base2);

                //Total
                Table total = new Table(2);
                total.setWidth(500);
                total.setBorderTop(new SolidBorder(2));

                Cell total1 = new Cell();
                total1.setTextAlignment(TextAlignment.LEFT);
                Paragraph h = new Paragraph("TOTAL RECIBO......................");
                total1.add(h);
                total.addCell(base1);

                Cell total2 = new Cell();
                total2.setTextAlignment(TextAlignment.RIGHT);
                Paragraph t = new Paragraph(String.format("%.2f", r.getTotalRecibo()));
                total2.add(t);
                total.addCell(base2);



                doc.add(tabla1);
                tabla1.setMarginTop(10);
                doc.add(tabla2);
                doc.add(vehic);
                vehic.setMarginTop(10);
                doc.add(bast);
                doc.add(recIvtm);
                recIvtm.setMarginTop(20);
                if(bajaVehic){
                    doc.add(baja);
                    baja.setMarginTop(5);
                }
                doc.add(caracs);
                caracs.setMarginTop(20);
                doc.add(vehic2);
                vehic2.setMarginTop(10);
                doc.add(base);
                base.setMarginTop(30);
                doc.add(total);
                total.setMarginTop(20);

                doc.close();
            }
        }


        System.out.println("\n\nNúmero de recibos mapeados: " + recibos.size());
    }






}
