
public class Mysort implements Comparable<Mysort> {
    private String order = "EsmTeiKolNelReedeLau";
    private String string;

    public Mysort(String string) {
        this.string = string;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    @Override
    public int compareTo(Mysort o) {
        return order.indexOf(this.string.substring(this.string.length()-3,this.string.length())) - order.indexOf(o.string.substring(o.string.length()-3,o.string.length()));
    }

}
