package sample.model;

/**
 * A class the stores the id and directory from {@code DatasourceController.queryFolders()}.
 */
public class QueryFolderEntry {

    private int id;
    private String folderDirectory;

    /**
     * @param id              the id of the entry.
     * @param folderDirectory the directory of the folder.
     */
    public QueryFolderEntry(int id, String folderDirectory) {
        this.id = id;
        this.folderDirectory = folderDirectory;
    }

    /**
     * @return the {@code id}.
     */
    public int getId() {
        return id;
    }

    /**
     * @return the {@code folderDirectory}.
     */
    public String getFolderDirectory() {
        return folderDirectory;
    }
}
