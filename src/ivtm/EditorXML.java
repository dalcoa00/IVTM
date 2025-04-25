package ivtm;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

public class EditorXML {
    //Escribe en ErroresNifNie.xml los errores del archivo
    public void xmlDniNie(String rutaArchivo, int id, String nifNie, String nombre, String primerApellido, String segundoApellido, String tipoDeError) {
        try {
            File xmlFile = new File(rutaArchivo);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            // Obtener el nodo raíz <Contribuyentes>
            Element rootElement = doc.getDocumentElement();

            // Crear nuevo contribuyente
            Element contribuyenteElement = doc.createElement("Contribuyente");
            contribuyenteElement.setAttribute("id", String.valueOf(id));

            // Crear y agregar elementos
            Element nifElement = doc.createElement("NIF_NIE");
            nifElement.appendChild(doc.createTextNode(nifNie));
            contribuyenteElement.appendChild(nifElement);


            Element nombreElement = doc.createElement("Nombre");
            nombreElement.appendChild(doc.createTextNode(nombre));
            contribuyenteElement.appendChild(nombreElement);

            Element primerApellidoElement = doc.createElement("PrimerApellido");
            primerApellidoElement.appendChild(doc.createTextNode(primerApellido));
            contribuyenteElement.appendChild(primerApellidoElement);

            Element segundoApellidoElement = doc.createElement("SegundoApellido");
            segundoApellidoElement.appendChild(doc.createTextNode(segundoApellido));
            contribuyenteElement.appendChild(segundoApellidoElement);

            Element tipoErrorElement = doc.createElement("TipoDeError");
            tipoErrorElement.appendChild(doc.createTextNode(tipoDeError));
            contribuyenteElement.appendChild(tipoErrorElement);

            // Agregar el nuevo contribuyente al XML
            rootElement.appendChild(contribuyenteElement);

            // Guardar los cambios en el XML
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{https://xml.apache.org/xslt}indent-amount", "2");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(rutaArchivo));

            transformer.transform(source, result);

            System.out.println("Contribuyente agregado al XML:");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //Escribe en ErroresCCC.xml  los  errores del archivo
    public void xmlCuenta(String rutaArchivo, int id, String nombre, String primerApellido, String segundoApellido,String nifnie, String cccErroneo, String ibanCorrecto, String tipoDeError) {
        try{

            File xmlFile = new File(rutaArchivo);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            // Obtener el nodo raíz <cuentas>
            Element rootElement = doc.getDocumentElement();

            // Crear nuevo contribuyente
            Element cuentaElement = doc.createElement("Cuenta");
            cuentaElement.setAttribute("id", String.valueOf(id));

            Element nombreElement = doc.createElement("Nombre");
            nombreElement.appendChild(doc.createTextNode(nombre));
            cuentaElement.appendChild(nombreElement);

            Element apellidos = doc.createElement("Apellidos");
            apellidos.appendChild(doc.createTextNode(primerApellido+segundoApellido));
            cuentaElement.appendChild(apellidos);

            Element nifElement = doc.createElement("NIFNIE");
            nifElement.appendChild(doc.createTextNode(nifnie));
            cuentaElement.appendChild(nifElement);


            Element cccError = doc.createElement("CCCErroneo");
            cccError.appendChild(doc.createTextNode(cccErroneo));
            cuentaElement.appendChild(cccError);

            //Hay  dos tipos de fichero: tipo de error e ibanCorrecto
            if (ibanCorrecto.isEmpty()) {
                Element tipoErrorElement = doc.createElement("TipoDeError");
                tipoErrorElement.appendChild(doc.createTextNode(tipoDeError));
                cuentaElement.appendChild(tipoErrorElement);
            }
            else if (tipoDeError.isEmpty()) {
                Element ibancorrecto = doc.createElement("IBANCorrecto");
                ibancorrecto.appendChild(doc.createTextNode(ibanCorrecto));
                cuentaElement.appendChild(ibancorrecto);
            }
            /*if (tipoDeError.equals("IMPOSIBLE GENERAR IBAN")) {
                Element tipoErrorElement = doc.createElement("TipoDeError");
                tipoErrorElement.appendChild(doc.createTextNode(tipoDeError));
                cuentaElement.appendChild(tipoErrorElement);
            }*/


            // Agregar el nuevo contribuyente al XML
            rootElement.appendChild(cuentaElement);

            // Guardar los cambios en el XML
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{https://xml.apache.org/xslt}indent-amount", "2");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(rutaArchivo));

            transformer.transform(source, result);

            System.out.println("Cuenta agregada al XML:");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //Escribe recibos.xml HAY QUE SEPARAR CADA  RECIBO DEMASSIADOS PARAMETROS
    public void xmlRecibo(String rutaArchivo, List<Recibo> recibos, String fechaPadron) {//Escribe vehiculos.xml  los errores del archivo
        try{
            double totalPadron = recibos.stream().mapToDouble(Recibo::getTotalRecibo).sum();
            int numeroTotalRecibos = recibos.size();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element root = doc.createElement("Recibos");
            root.setAttribute("fechaPadron", fechaPadron);
            root.setAttribute("totalPadron", String.format("%.2f", totalPadron));
            root.setAttribute("numeroTotalRecibos", String.valueOf(numeroTotalRecibos));
            doc.appendChild(root);
            for (Recibo r : recibos) {
                Element reciboElem = doc.createElement("Recibo");
                reciboElem.setAttribute("idRecibo", String.valueOf(r.getIdRecibo()));

                appendChild(doc, reciboElem, "Exencion", r.getExencion());
                appendChild(doc, reciboElem, "idFilaExcelVehiculo", String.valueOf(r.getIdFilaExcelVehiculo()));
                appendChild(doc, reciboElem, "nombre", r.getNombre());
                appendChild(doc, reciboElem, "primerApellido", r.getPrimerApellido());
                appendChild(doc, reciboElem, "segundoApellido", r.getSegundoApellido());
                appendChild(doc, reciboElem, "NIF", r.getNif());
                appendChild(doc, reciboElem, "IBAN", r.getIban());
                appendChild(doc, reciboElem, "tipoVehiculo", r.getTipoVehiculo());
                appendChild(doc, reciboElem, "marcaModelo", r.getMarcaModelo());
                appendChild(doc, reciboElem, "matricula", r.getMatricula());
                appendChild(doc, reciboElem, "totalRecibo", String.valueOf(r.getTotalRecibo()));

                root.appendChild(reciboElem);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(rutaArchivo));
            transformer.transform(source, result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void xmlVehiculos(String rutaArchivo, int id, String marca, String modelo, String tipodeError){
        try{
            File xmlFile = new File(rutaArchivo);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            // Obtener el nodo raíz <cuentas>
            Element rootElement = doc.getDocumentElement();

            // Crear nuevo contribuyente
            Element cuentaElement = doc.createElement("Vehiculo");
            cuentaElement.setAttribute("id", String.valueOf(id));

            Element marcaElement = doc.createElement("Marca");
            marcaElement.appendChild(doc.createTextNode(marca));
            cuentaElement.appendChild(marcaElement);

            Element modeloElement = doc.createElement("Modelo");
            modeloElement.appendChild(doc.createTextNode(modelo));
            cuentaElement.appendChild(modeloElement);

            Element tipodeErrorElement = doc.createElement("Error");
            tipodeErrorElement.appendChild(doc.createTextNode(tipodeError));
            cuentaElement.appendChild(tipodeErrorElement);
            // Agregar el nuevo contribuyente al XML

            rootElement.appendChild(cuentaElement);

            // Guardar los cambios en el XML
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{https://xml.apache.org/xslt}indent-amount", "2");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(rutaArchivo));

            transformer.transform(source, result);

            System.out.println("Vehiculo agregado al XML:");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //Metodo auxiliar para añadir de  forma correcta los recibos
    private void appendChild(Document doc, Element parent, String tagName, String textContent) {
        Element elem = doc.createElement(tagName);
        elem.setTextContent(textContent);
        parent.appendChild(elem);
    }
}

