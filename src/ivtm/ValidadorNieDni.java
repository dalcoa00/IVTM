package ivtm;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;



public class ValidadorNieDni {
    private static final String ruta= "resources\\ErroresNifNie.xml";

    //antes de estos dos metodos  hay que comprobar si es dni o nie
    public static int dniNie(Cell celda){
        String comprobar= celda.getStringCellValue().trim();

        if (comprobar.matches("^[XYZ][0-9]{7}[A-Z]$")) {
            return 2;
        }
        // Verifica si es un DNI (8 números + 1 letra)
        if (comprobar.matches("^[0-9]{8}[A-Z]$")) {
            return 1;
        }
        return -1;
    }



    //Metodo  que comprueba que el campo del dni no es null
    public static boolean dniVacio(String dni) {
        if(dni.length()==0|| dni=="" || dni==null){
            System.out.println("El campo del dni se encuentra vacio");
            return true;
        }else{
            return false;
        }
    }
    //Metodo que comprueba que el campo nie no es null
    public static boolean nieVacio(String nie){
        if(nie.length()==0|| nie==""|| nie==null){
            System.out.println("El campo del Nie se encuentra vacio");
            return true;
        }else{
            return false;
        }
    }
    public static boolean nieValido(Cell celda, Row  fila, int contador){
        String nie =  celda.getStringCellValue().trim();
        if(nieVacio(nie)){
            escribirContribuyentes(ruta, contador, fila.getCell(0).getStringCellValue(),fila.getCell(3).getStringCellValue(), fila.getCell(1).getStringCellValue(), fila.getCell(2).getStringCellValue(),"NIF BLANCO" );
            return false;
        }
        if(nie.length()!=9){
            escribirContribuyentes(ruta, contador, fila.getCell(0).getStringCellValue(),fila.getCell(3).getStringCellValue(), fila.getCell(1).getStringCellValue(), fila.getCell(2).getStringCellValue(),"NIF ERRONEO" );
            return false;
        }
        String letraInicioNie= nie.substring(0, 1).toUpperCase();
        if(letraInicioNie!="X"&&letraInicioNie!="Y"&&letraInicioNie!="Z"){
               System.out.println("El Nie no tiene el formato   correcto (letra  inicial)";
               escribirContribuyentes(ruta, contador, fila.getCell(0).getStringCellValue(),fila.getCell(3).getStringCellValue(), fila.getCell(1).getStringCellValue(), fila.getCell(2).getStringCellValue(),"NIF ERRONEO" );
               return false;
        }
        String nieNumero=  nie.substring(1,8);
        if(!nieNumero.matches("\\d{7}")){
            System.out.println("El Nie no tiene el formato correcto (numero)");
            escribirContribuyentes(ruta, contador, fila.getCell(0).getStringCellValue(),fila.getCell(3).getStringCellValue(), fila.getCell(1).getStringCellValue(), fila.getCell(2).getStringCellValue(),"NIF ERRONEO" );
            return false;
        }
        char nieLetra= nie.charAt(8);
        if(nieLetra!=letraCorrectaNie(letraInicioNie,nieNumero)){
            celda.setCellValue(letraInicioNie+nieNumero+letraCorrectaNie(letraInicioNie,nieNumero));
            System.out.println("El nie tenia el formato correcto pero la letra no coincidia, se ha modificado la letra");
        }
        //Comprueba que la letra sea correcta
        //Si no letra correcta se modifica la letra y ver en la hoja del la practica lo que tiene que escribir en el archuivo
        System.out.println("El nie es correcto");
        return true;
        }


    //Devuelve true si el dni es correcto y 
    public static boolean dniValido(Cell celda, Row fila, int contador) {
        String dni = celda.getStringCellValue().trim();
        if(dniVacio(dni)){
            escribirContribuyentes(ruta, contador, fila.getCell(0).getStringCellValue(),fila.getCell(3).getStringCellValue(), fila.getCell(1).getStringCellValue(), fila.getCell(2).getStringCellValue(),"NIF BLANCO" );

            return false;
        }
        if(dni.length()!=9){
            escribirContribuyentes(ruta, contador, fila.getCell(0).getStringCellValue(),fila.getCell(3).getStringCellValue(), fila.getCell(1).getStringCellValue(), fila.getCell(2).getStringCellValue(),"NIF ERRONEO" );
            return false;
        }
        //Comprueba que el dni tenga el formato corrrecto 8 numero
        String dniNumero= dni.substring(0,8);
        if(!dniNumero.matches("\\d{8}")){
            escribirContribuyentes(ruta, contador, fila.getCell(0).getStringCellValue(),fila.getCell(3).getStringCellValue(), fila.getCell(1).getStringCellValue(), fila.getCell(2).getStringCellValue(),"NIF ERRONEO" );
            System.out.println("El dni no tiene el formato correcto (numero)");

            return false;
        }
        //Comprueba  que el noveno carcater sea letra
        char dniLetra= dni.charAt(8);
        if(Character.isLetter(dniLetra)){
            escribirContribuyentes(ruta, contador, fila.getCell(0).getStringCellValue(),fila.getCell(3).getStringCellValue(), fila.getCell(1).getStringCellValue(), fila.getCell(2).getStringCellValue(),"NIF ERRONEO" );

            System.out.println("El dni  no tiene el  formato correcto (letra)");
            return false;
        }
        if(dniLetra!=letraCorrecta(dniNumero)){
            //Se corrige la celdacon la letra correcta----Comprobar si lo  actualiza directamente en el excel 
            celda.setCellValue(dniNumero+letraCorrecta(dniNumero));
            System.out.println("El dni tenia el formato correcto pero la letra no coincidia, se ha modificado la letra");
            return false;
        }
            //Comprueba que la letra sea correcta
            //Si no letra correcta se modifica la letra y ver en la hoja del la practica lo que tiene que escribir en el archuivo
        System.out.println("El dni es correcto");
        return true;
    }

    public static char letraCorrectaNie(String letraInicio, String numeroNie){
        String aux="";
        if (letraInicio.toUpperCase() == "X") {
            aux="0";
        }else if(letraInicio.toUpperCase() == "Y") {
            aux="1";
        }else if(letraInicio.toUpperCase()=="Z"){
            aux="2";
        }
        String letraYNumero= aux+numeroNie;
        int nieNumero=Integer.parseInt(letraYNumero);
        char[] letras = "TRWAGMYFPDXBNJZSQVHLCKE".toCharArray();
        return letras[nieNumero % 23];
    }
    public static char letraCorrecta(String numeros){
        int dniNumero= Integer.parseInt(numeros);
        char[] letras = "TRWAGMYFPDXBNJZSQVHLCKE".toCharArray();
        return letras[dniNumero % 23];
    }
    public static void escribirContribuyentes(String rutaArchivo, int id, String nifNie, String nombre, String primerApellido, String segundoApellido, String tipoDeError){
        try{
            File xmlFile= new File(rutaArchivo);
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
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(rutaArchivo));

            transformer.transform(source, result);

            System.out.println("Contribuyente agregado al XML:");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


