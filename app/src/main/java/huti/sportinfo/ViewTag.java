package huti.sportinfo;

/**
 * Created by Huti on 07.02.2015.
 */
public class ViewTag {
    public int idpk;
    public String bezeichnung;

    public ViewTag() {
        idpk = 0;
        bezeichnung = "";
    }

    public ViewTag(int idpk, String bezeichnung) {
        this.idpk = idpk;
        this.bezeichnung = bezeichnung;
    }
}
