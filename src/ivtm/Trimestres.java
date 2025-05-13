package ivtm;

import java.util.Calendar;
import java.util.Date;

public class Trimestres {
    private final int anio;

    public Trimestres (int anio) {
        this.anio = anio;
    }

    public Date getInicio (int trimestre) {
        return getFechaInicioFin(trimestre, true);
    }

    public Date getFin (int trimestre) {
        return getFechaInicioFin(trimestre, false);
    }

    private Date getFechaInicioFin (int trimestre, boolean inicio) {
        Calendar cal = Calendar.getInstance();

        //Establecer mes del trimestre
        int mes = (trimestre - 1) * 3 + (inicio ? 0 : 2); //Mes inicial o final del trimestre
        cal.set(Calendar.YEAR, anio);
        cal.set(Calendar.MONTH, mes);

        //Establecer el día del mes
        if (inicio) {
            //Primer día del mes
            cal.set(Calendar.DAY_OF_MONTH, 1);
        }
        else {
            //Día final del mes
            int ultimo = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            cal.set(Calendar.DAY_OF_MONTH, ultimo);
        }

        return cal.getTime();
    }
}
