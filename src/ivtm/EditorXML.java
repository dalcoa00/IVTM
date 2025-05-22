package ivtm;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;


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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void xmlRecibo(String rutaArchivo, String fechaPadron, double totalPadron, int numeroTotalRecibos,
                      int idRecibo, Character exencion, int idFila, String nombre, String apellido1,
                      String apellido2, String nif, String iban, String tipo, String marca, String modelo,
                      String matricula, double totalR) {

        try {
            Document doc;
            Element root;

            File archivo = new File(rutaArchivo);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            if (archivo.exists()) {
                // Leer el documento existente
                doc = builder.parse(archivo);
                root = doc.getDocumentElement();
            } else {
                // Crear nuevo documento
                doc = builder.newDocument();
                root = doc.createElement("Recibos");
                root.setAttribute("fechaPadron", fechaPadron);
                root.setAttribute("totalPadron", String.format("%.2f", totalPadron));
                root.setAttribute("numeroTotalRecibos", String.valueOf(numeroTotalRecibos));
                doc.appendChild(root);
            }

            // Crear el nodo <Recibo>
            Element reciboElem = doc.createElement("Recibo");
            reciboElem.setAttribute("idRecibo", String.valueOf(idRecibo));
            appendChild(doc, reciboElem, "Exencion", String.valueOf(exencion));
            appendChild(doc, reciboElem, "idFilaExcelVehiculo", String.valueOf(idFila));
            appendChild(doc, reciboElem, "nombre", nombre);
            appendChild(doc, reciboElem, "primerApellido", apellido1);
            appendChild(doc, reciboElem, "segundoApellido", apellido2);
            appendChild(doc, reciboElem, "NIF", nif);
            appendChild(doc, reciboElem, "IBAN", iban);
            appendChild(doc, reciboElem, "tipoVehiculo", tipo);
            String marcaModelo= marca+" "+modelo;
            appendChild(doc, reciboElem, "marcaModelo", marcaModelo);
            appendChild(doc, reciboElem, "matricula", matricula);
            appendChild(doc, reciboElem, "totalRecibo", String.format("%.2f", totalR));

            // Añadir el recibo al nodo raíz
            root.appendChild(reciboElem);

            // Guardar el documento actualizado
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(archivo);
            transformer.transform(source, result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void xmlVehiculos(String rutaArchivo, int id, String marca, String modelo, List<String> errores){

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
            String erroresTexto = String.join(". ", errores);
            if (!erroresTexto.endsWith(".")) {
                erroresTexto += "."; 
            }
            tipodeErrorElement.appendChild(doc.createTextNode(erroresTexto));
            cuentaElement.appendChild(tipodeErrorElement);

            rootElement.appendChild(cuentaElement);

            // Guardar los cambios en el XML
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{https://xml.apache.org/xslt}indent-amount", "2");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(rutaArchivo));

            transformer.transform(source, result);

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

    //Metodo que permite modificar los atributos  del xml  de recibos para hacerlo al final 
    public void modificarAtributosPadron(String rutaArchivo, double nuevoTotalPadron, String fechaPadron, int numeroTotalRecibos) {

        try {
            File archivo = new File(rutaArchivo);

            if (!archivo.exists()) {
                System.err.println("El archivo XML no existe: " + rutaArchivo);
                return;
            }

            // Cargar el documento XML existente
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(archivo);

            // Obtener el nodo raíz <Recibos>
            NodeList listaRecibos = doc.getElementsByTagName("Recibos");

            if (listaRecibos.getLength() == 0) {
                System.err.println("El documento no contiene un nodo raíz <Recibos>.");
                return;
            }

            Element root = (Element) listaRecibos.item(0);

            // Modificar los atributos
            root.setAttribute("fechaPadron", fechaPadron);
            root.setAttribute("totalPadron", String.format("%.2f", nuevoTotalPadron));
            root.setAttribute("numeroTotalRecibos", String.valueOf(numeroTotalRecibos));


            // Guardar los cambios en el mismo archivo
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(archivo);
            transformer.transform(source, result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void ordenarVehiculosPorId(String rutaArchivo) {

        try {
            File xmlFile = new File(rutaArchivo);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement(); // <Vehiculos>

            NodeList vehiculoNodes = root.getElementsByTagName("Vehiculo");

            List<Element> vehiculos = new ArrayList<>();
            for (int i = 0; i < vehiculoNodes.getLength(); i++) {
                vehiculos.add((Element) vehiculoNodes.item(i));
            }

            // Ordenar por el atributo id numéricamente
            vehiculos.sort(Comparator.comparingInt(e -> Integer.parseInt(e.getAttribute("id"))));

            // Eliminar nodos actuales
            for (Element v : vehiculos) {
                root.removeChild(v);
            }

            // Volver a agregar nodos en orden
            for (Element v : vehiculos) {
                root.appendChild(v);
            }

            //Eliminar nodos vacíos de texto (líneas en blanco)
            eliminarNodosVacios(doc);

            // Guardar de nuevo el XML
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(rutaArchivo));
            transformer.transform(source, result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void ordenarRecibosPorIdFila(String rutaArchivo) {

        try {
            File archivo = new File(rutaArchivo);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(archivo);
            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement();
            NodeList recibosList = root.getElementsByTagName("Recibo");

            List<Element> recibos = new ArrayList<>();

            for (int i = 0; i < recibosList.getLength(); i++) {
                Node node = recibosList.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    recibos.add((Element) node);
                }
            }

            // Ordenar por idFilaExcelVehiculo
            recibos.sort(Comparator.comparingInt(e ->
                Integer.parseInt(e.getElementsByTagName("idFilaExcelVehiculo").item(0).getTextContent())));

            // Eliminar todos los nodos <Recibo> actuales
            for (Element recibo : recibos) {
                root.removeChild(recibo);
            }

            // Reinsertar con idRecibo actualizado
            int nuevoId = 1;

            for (Element recibo : recibos) {
                recibo.setAttribute("idRecibo", String.valueOf(nuevoId++));
                root.appendChild(recibo);
            }

            //Eliminar nodos vacíos (saltos de línea / espacios)
            eliminarNodosVacios(doc);

            // Guardar el archivo actualizado
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(archivo);
            transformer.transform(source, result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Metodo auxiliar para eliminar nodos de texto vacíos
    private void eliminarNodosVacios(Node node) {
        NodeList hijos = node.getChildNodes();

        for (int i = 0; i < hijos.getLength(); ) {
            Node actual = hijos.item(i);

            if (actual.getNodeType() == Node.TEXT_NODE && actual.getTextContent().trim().isEmpty()) {
                node.removeChild(actual);
                // no incrementes i ya que el NodeList se actualiza
            } else {
                eliminarNodosVacios(actual); // recursivo
                i++;
            }
        }
    }




}

