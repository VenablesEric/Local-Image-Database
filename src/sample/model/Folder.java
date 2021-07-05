package sample.model;

public class Folder {

    private int id;
    private String directory;

    public Folder(int id, String directory)
    {
        this.id = id;
        this.directory = directory;
    }

    public int getId() {
        return id;
    }

    public String getDirectory() {
        return directory;
    }
}
