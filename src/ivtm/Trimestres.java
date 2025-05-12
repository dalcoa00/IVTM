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
        int mes = (trimestre - 1) * 3 + (inicio ? 0 : 2);
        /*Si inicio == true -> dia = 1
        * Si inicio == false -> dia = último día del mes (28, 30 o 31 dependiendo del mes)*/
        int dia = inicio ? 1 : cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        cal.set(anio, mes, dia);

        if (!inicio) {
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
        }

        return cal.getTime();
    }
}
