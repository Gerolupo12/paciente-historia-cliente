package models;

public abstract class Base {
    
    private int id;
    private Boolean eliminado;

    public Base(int id) {
        this.id = id;
        this.eliminado = false;
    }

    public Base() {
    }
    

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Boolean getEliminado() {
        return eliminado;
    }

    public void setEliminado(Boolean eliminado) {
        this.eliminado = eliminado;
    }
     
}
