package ivtm;

import com.itextpdf.io.font.otf.ContextualRule;
import ivtm.IvtmSession;
import ivtm.IvtmTransaction;
import org.hibernate.Session;
import org.hibernate.query.Query;
import POJOS.*;

import java.util.List;
import java.util.Map;

public class InsertarActualizarDB {

    /*Metodo que inserta/actualiza todos los contribuyentes correctos*/
    public void insertaActualizaContribuyentes (Map<Integer, Contribuyente> contribuyentesPojosMap) {
        Session s = null;
        IvtmTransaction trans = new IvtmTransaction();

        try {
            s = IvtmSession.open();
            trans.beginTrans(s);

            int contador = 0;

            for (Map.Entry<Integer, Contribuyente> entry : contribuyentesPojosMap.entrySet()) {
                Contribuyente cont = entry.getValue();

                //Busca si ya existe en la DB (IdContribuyente)
                Query<Contribuyente> query = s.createQuery(
                        "from Contribuyente c where c.idContribuyente = :idContribuyente", Contribuyente.class);

                query.setParameter("idContribuyente", cont.getIdContribuyente());

                Contribuyente existe = query.uniqueResult();

                //Si el contribuyente ya estaba en la DB, se actualiza
                if (existe != null) {
                    existe.setNombre(cont.getNombre());
                    existe.setApellido1(cont.getApellido1());
                    existe.setApellido2(cont.getApellido2());
                    existe.setNifnie(cont.getNifnie());
                    existe.setDireccion(cont.getDireccion());
                    existe.setNumero(cont.getNumero());
                    existe.setPaisCcc(cont.getPaisCcc());
                    existe.setCcc(cont.getCcc());
                    existe.setIban(cont.getIban());
                    existe.setEmail(cont.getEmail());
                    existe.setBonificacion(cont.getBonificacion());
                    existe.setAyuntamiento(cont.getAyuntamiento());
                    existe.setReciboses(cont.getReciboses());
                    existe.setVehiculoses(cont.getVehiculoses());

                    s.update(existe);
                }
                else {
                    s.save(cont);
                }

                //Manejo de sobrecarga de memoria
                if (++contador % 50 == 0) {
                    s.flush();
                    s.clear();
                }
            }

            trans.commitTrans();
            System.out.println("Contribuyentes insertados/actualizados correctamente en la DB");
        } catch (Exception e) {
            e.printStackTrace();
            //Deshace la operación si hay algún error
            trans.rollbackTrans();
        } finally {
            if (s != null) {
                s.close();
            }
            FactorySession.closeSessionFactory();
        }
    }

    /*Metodo que inserta/actualiza todos los vehiculos que han generado recibo*/
    public void insertaActualizaVehiculos(Map<Integer, List<Recibos>> recibosPojosMap) {
        Session s = null;
        IvtmTransaction trans = new IvtmTransaction();

        try {
            s = IvtmSession.open();
            trans.beginTrans(s);

            int contador = 0;

            for (Map.Entry<Integer, List<Recibos>> entry : recibosPojosMap.entrySet()) {
                List<Recibos> listaRecibos = entry.getValue();

                for (Recibos rec : listaRecibos) {
                    Vehiculos v = rec.getVehiculos();

                    //Busca si ya existe en la DB (IdVehiculo)
                    Query<Vehiculos> query = s.createQuery(
                            "from Vehiculos v where v.idVehiculo = :idVehiculo", Vehiculos.class);

                    query.setParameter("idVehiculo", v.getIdVehiculo());

                    Vehiculos existe = query.uniqueResult();

                    //Si el contribuyente ya estaba en la DB, se actualiza
                    if (existe != null) {
                        existe.setContribuyente(v.getContribuyente());
                        existe.setOrdenanza(v.getOrdenanza());
                        existe.setTipo(v.getTipo());
                        existe.setMarca(v.getMarca());
                        existe.setModelo(v.getModelo());
                        existe.setMatricula(v.getMatricula());
                        existe.setNumeroBastidor(v.getNumeroBastidor());
                        existe.setCaballosFiscales(v.getCaballosFiscales());
                        existe.setPlazas(v.getPlazas());
                        existe.setCentrimetroscubicos(v.getCentrimetroscubicos());
                        existe.setKgcarga(v.getKgcarga());
                        existe.setExencion(v.getExencion());
                        existe.setFechaMatriculacion(v.getFechaMatriculacion());
                        existe.setFechaAlta(v.getFechaAlta());
                        existe.setFechaBaja(v.getFechaBaja());
                        existe.setFechaBajaTemporal(v.getFechaBajaTemporal());
                        existe.setReciboses(v.getReciboses());

                        if (v.getOrdenanza() != null) {
                            v.getOrdenanza().getVehiculoses().add(existe);
                        }

                        s.update(existe);
                    } else {
                        Ordenanza ordenanza = v.getOrdenanza();

                        if (ordenanza != null) {
                            ordenanza.getVehiculoses().add(existe);
                        }

                        s.save(v);
                    }

                    //Manejo de sobrecarga de memoria
                    if (++contador % 50 == 0) {
                        s.flush();
                        s.clear();
                    }
                }
            }

                trans.commitTrans();
                System.out.println(" insertados/actualizados correctamente en la DB");

        } catch(Exception e){
            e.printStackTrace();
            //Deshace la operación si hay algún error
            trans.rollbackTrans();
        } finally{
            if (s != null) {
                s.close();
            }
            FactorySession.closeSessionFactory();
        }

    }

    /*Metodo que inserta/actualiza las ordenanzas aplicadas a los vehículos que generan recibo*/
    public void insertaActualizaOrdenanzas(Map<Integer, List<Ordenanza>> ordenanzasMap) {
        Session s = null;
        IvtmTransaction trans = new IvtmTransaction();

        try {
            s = IvtmSession.open();
            trans.beginTrans(s);

            int contador = 0;

            for (Map.Entry<Integer, List<Ordenanza>> entry : ordenanzasMap.entrySet()) {
                List<Ordenanza> listaOrdenanzas = entry.getValue();

                for (Ordenanza ord : listaOrdenanzas) {
                    Ordenanza o = new Ordenanza();

                    //Busca si ya existe en la DB (id)
                    Query<Ordenanza> query = s.createQuery(
                            "from Ordenanza o where o.id = :id", Ordenanza.class);

                    query.setParameter("id", o.getId());

                    Ordenanza existe = query.uniqueResult();

                    //Si la ordenanza ya estaba en la DB, se actualiza
                    if (existe != null) {
                        existe.setAyuntamiento(o.getAyuntamiento());
                        existe.setTipoVehiculo(o.getTipoVehiculo());
                        existe.setUnidad(o.getUnidad());
                        existe.setMinimoRango(o.getMinimoRango());
                        existe.setMaximoRango(o.getMaximoRango());
                        existe.setImporte(o.getImporte());
                        existe.setVehiculoses(o.getVehiculoses());

                        s.update(existe);
                    } else {
                        s.save(o);
                    }

                    //Manejo de sobrecarga de memoria
                    if (++contador % 50 == 0) {
                        s.flush();
                        s.clear();
                    }
                }
            }

            trans.commitTrans();
            System.out.println(" insertados/actualizados correctamente en la DB");

        } catch(Exception e){
            e.printStackTrace();
            //Deshace la operación si hay algún error
            trans.rollbackTrans();
        } finally{
            if (s != null) {
                s.close();
            }
            FactorySession.closeSessionFactory();
        }
    }

    /*Metodo que inserta/actualiza todos los recibos generados*/
    public void insertaActualizaRecibos(Map<Integer, List<Recibos>> recibosPojosMap) {
        Session s = null;
        IvtmTransaction trans = new IvtmTransaction();

        try {
            s = IvtmSession.open();
            trans.beginTrans(s);

            int contador = 0;

            for (Map.Entry<Integer, List<Recibos>> entry : recibosPojosMap.entrySet()) {
                List<Recibos> listaRecibos = entry.getValue();

                for (Recibos rec : listaRecibos) {
                    Recibos r = new Recibos();

                    //Busca si ya existe en la DB (numRecibo)
                    Query<Recibos> query = s.createQuery(
                            "from Recibos r where r.numRecibo = :numRecibo", Recibos.class);

                    query.setParameter("numRecibo", r.getNumRecibo());

                    Recibos existe = query.uniqueResult();

                    //Si el contribuyente ya estaba en la DB, se actualiza
                    if (existe != null) {
                        existe.setContribuyente(r.getContribuyente());
                        existe.setVehiculos(r.getVehiculos());
                        existe.setFechaPadron(r.getFechaPadron());
                        existe.setFechaRecibo(r.getFechaRecibo());
                        existe.setNifContribuyente(r.getNifContribuyente());
                        existe.setDireccionCompleta(r.getDireccionCompleta());
                        existe.setIban(r.getIban());
                        existe.setTipoVehiculo(r.getTipoVehiculo());
                        existe.setMarcaModelo(r.getMarcaModelo());
                        existe.setUnidad(r.getUnidad());
                        existe.setValorUnidad(r.getValorUnidad());
                        existe.setTotalRecibo(r.getTotalRecibo());
                        existe.setExencion(r.getExencion());
                        existe.setBonificacion(r.getBonificacion());
                        existe.setEmail(r.getEmail());
                        existe.setAyuntamiento(r.getAyuntamiento());

                        s.update(existe);
                    } else {
                        s.save(r);
                    }

                    //Manejo de sobrecarga de memoria
                    if (++contador % 50 == 0) {
                        s.flush();
                        s.clear();
                    }
                }
            }

            trans.commitTrans();
            System.out.println(" insertados/actualizados correctamente en la DB");

        } catch (Exception e) {
            e.printStackTrace();
            //Deshace la operación si hay algún error
            trans.rollbackTrans();
        } finally {
            if (s != null) {
                s.close();
            }
            FactorySession.closeSessionFactory();
        }
    }
}
