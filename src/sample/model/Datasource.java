package sample.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Datasource {
    private static Datasource instance = new Datasource();

    public static final String DATABASE_NAME = "\\MyData.db";

    public static final String TABLE_FOLDERS = "folders";
    public static final String COLUMN_FOLDERS_ID = "_id";
    public static final String COLUMN_FOLDERS_DIRECTORY = "directory";

    public static final String TABLE_IMAGES = "images";
    public static final String COLUMN_IMAGES_ID = "_id";
    public static final String COLUMN_IMAGES_LOCATION = "location";

    public static final String TABLE_TAGS = "tags";
    public static final String COLUMN_TAGS_ID = "_id";
    public static final String COLUMN_TAGS_TAG = "tag";

    public static final String TABLE_IMAGE_TAGS = "image_tags";
    public static final String COLUMN_IMAGE_TAGS_IMAGE_ID = "image_id";
    public static final String COLUMN_IMAGES_TAGS_TAG_ID = "tag_id";

    // Add unique to some of them
    private static final String CREATE_TABLE_FOLDERS = String.format(
            "CREATE TABLE IF NOT EXISTS %s (" +
                    "%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "%s VARCHAR(32767) UNIQUE);",
            TABLE_FOLDERS, COLUMN_FOLDERS_ID, COLUMN_FOLDERS_DIRECTORY);

    private static final String CREATE_TABLE_IMAGES = String.format(
            "CREATE TABLE IF NOT EXISTS %s (" +
                    "%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "%s VARCHAR(32767) UNIQUE);",
            TABLE_IMAGES, COLUMN_IMAGES_ID, COLUMN_IMAGES_LOCATION);

    private static final String CREATE_TABLE_TAGS = String.format(
            "CREATE TABLE IF NOT EXISTS %s (" +
                    "%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "%s VARCHAR(255) UNIQUE);", TABLE_TAGS, COLUMN_TAGS_ID, COLUMN_TAGS_TAG);

    private static final String CREATE_TABLE_IMAGE_TAGS = String.format(
            "CREATE TABLE IF NOT EXISTS %s (" +
                    "%s INTEGER NOT NULL, " +
                    "%s INTEGER NOT NULL," +
                    "FOREIGN KEY(%s) REFERENCES %s(%s) ON DELETE CASCADE, " +
                    "FOREIGN KEY(%s) REFERENCES %s(%s) ON DELETE CASCADE, " +
                    "PRIMARY KEY(%s, %s));",
            TABLE_IMAGE_TAGS, COLUMN_IMAGE_TAGS_IMAGE_ID, COLUMN_IMAGES_TAGS_TAG_ID,
            COLUMN_IMAGE_TAGS_IMAGE_ID, TABLE_IMAGES, COLUMN_IMAGES_ID,
            COLUMN_IMAGES_TAGS_TAG_ID, TABLE_TAGS, COLUMN_TAGS_ID,
            COLUMN_IMAGE_TAGS_IMAGE_ID,COLUMN_IMAGES_TAGS_TAG_ID);

    private static final String QUERY_FOLDERS = String.format(
            "SELECT %s FROM %s;",
            COLUMN_FOLDERS_DIRECTORY, TABLE_FOLDERS);

    private static final String INSERT_IMAGES = String.format(
            "INSERT INTO %s (%s) VALUES(?);",
            TABLE_IMAGES, COLUMN_IMAGES_LOCATION);

    private static final String SELECT_IMAGES = String.format(
            "SELECT * FROM %s LIMIT ?,?;",
            TABLE_IMAGES);

    private static final String INSERT_FOLDERS = String.format(
            "INSERT INTO %s (%s) VALUES(?);",
            TABLE_FOLDERS, COLUMN_FOLDERS_DIRECTORY);

    private static final String QUERY_IMAGES_COUNT = String.format(
        "SELECT COUNT(*) AS count FROM %s", TABLE_IMAGES );

    private static final String QUERY_IMAGE_TAGS = String.format(
      "SELECT %s.%s FROM %s " +
              "INNER JOIN %s " +
              "ON %s.%s = %s.%s " +
              "INNER JOIN %s " +
              "ON %s.%s = %s.%s " +
              "WHERE %s.%s = ?",
            TABLE_TAGS, COLUMN_TAGS_TAG, TABLE_TAGS,
            TABLE_IMAGE_TAGS,
            TABLE_IMAGE_TAGS,COLUMN_IMAGES_TAGS_TAG_ID,TABLE_TAGS,COLUMN_TAGS_ID,
            TABLE_IMAGES,
            TABLE_IMAGE_TAGS,COLUMN_IMAGE_TAGS_IMAGE_ID,TABLE_IMAGES,COLUMN_IMAGES_ID,
            TABLE_IMAGES,COLUMN_IMAGES_LOCATION);

    private static final String INSERT_TAGS = String.format(
            "INSERT INTO %s (%s) VALUES(?);",
            TABLE_TAGS, COLUMN_TAGS_TAG);

    private static final String INSERT_IMAGE_TAGS = String.format(
            "INSERT INTO %s (%s, %s) VALUES (?, ?)",
            TABLE_IMAGE_TAGS, COLUMN_IMAGE_TAGS_IMAGE_ID, COLUMN_IMAGES_TAGS_TAG_ID);

    private static final String QUERY_IMAGE = String.format(
      "SELECT %s FROM %s WHERE %s = ?",
            COLUMN_IMAGES_ID, TABLE_IMAGES, COLUMN_IMAGES_LOCATION);

    private static final String QUERY_TAG = String.format(
            "SELECT %s FROM %s WHERE %s = ?",
            COLUMN_TAGS_ID, TABLE_TAGS, COLUMN_TAGS_TAG);

    private static final String QUERY_TAGS_ALL = String.format(
      "SELECT %s FROM %s",
      COLUMN_TAGS_TAG, TABLE_TAGS);

    private PreparedStatement insertIntoImages;
    private PreparedStatement selectFromImages;
    private PreparedStatement insertIntoFolders;
    private PreparedStatement queryImageTags;
    private PreparedStatement insertIntoTags;
    private PreparedStatement insertIntoImageTags;
    private PreparedStatement queryImage;
    private PreparedStatement queryTag;

    private Connection connection;

    private Datasource() {
    }

    public static Datasource getInstance() {
        return instance;
    }

    public boolean open() {

        boolean connected = false;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + System.getProperty("user.dir") + DATABASE_NAME); // Load or create new database at .exe path

            connected = DatabaseSetup();

            insertIntoImages = connection.prepareStatement(INSERT_IMAGES);
            selectFromImages = connection.prepareStatement(SELECT_IMAGES);
            insertIntoFolders = connection.prepareStatement(INSERT_FOLDERS);
            queryImageTags = connection.prepareStatement(QUERY_IMAGE_TAGS);
            insertIntoTags = connection.prepareStatement(INSERT_TAGS);
            insertIntoImageTags = connection.prepareStatement(INSERT_IMAGE_TAGS);
            queryImage = connection.prepareStatement(QUERY_IMAGE);
            queryTag = connection.prepareStatement(QUERY_TAG);

            System.out.println("Connected to database");

            return connected;

        } catch (SQLException e) {
            System.out.println("Couldn't connect to database: " + e.getMessage());
            return false;
        }
    }

    public void close() {
        try {
            if (insertIntoImages != null) {
                insertIntoImages.close();
            }
            if (selectFromImages != null) {
                selectFromImages.close();
            }
            if (insertIntoFolders != null) {
                insertIntoFolders.close();
            }
            if (queryImageTags != null) {
                queryImageTags.close();
            }
            if (insertIntoTags != null) {
                insertIntoTags.close();
            }
            if (insertIntoImageTags != null) {
                insertIntoImageTags.close();
            }
            if (queryImage != null) {
                queryImage.close();
            }
            if (queryTag != null) {
                queryTag.close();
            }
            if (connection != null) {
                connection.close();
                System.out.println("Connection to database has been closed");
            }
        } catch (SQLException e) {
            System.out.println("Couldn't close connection: " + e.getMessage());
        }
    }

    private boolean DatabaseSetup() {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(CREATE_TABLE_FOLDERS);
            statement.executeUpdate(CREATE_TABLE_IMAGES);
            statement.executeUpdate(CREATE_TABLE_TAGS);
            statement.executeUpdate(CREATE_TABLE_IMAGE_TAGS);
            System.out.println("Database setup complete");
            return true;
        } catch (SQLException e) {
            System.out.println("Database setup failed: " + e.getMessage());
            return false;
        }
    }

    public List<String> queryFolders() {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(QUERY_FOLDERS)) {

            List<String> folders = new ArrayList<>();
            while (resultSet.next()) {
                folders.add((resultSet.getString("directory")));
            }

            return folders;

        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return null;
        }
    }

    public void insertFolder(String directory)
    {
        try
        {
            insertIntoFolders.setString(1,directory);
            int affectedRows = insertIntoFolders.executeUpdate();

            if (affectedRows != 1) {
                System.out.println("Couldn't insert into images!");
            }
        } catch (SQLException e) {
            System.out.println("Insert failed: " + e.getMessage());
        }
    }

    // Need to check if it is already in the table
    public void insertImage(String directoryName) {
        try {
            insertIntoImages.setString(1, directoryName);

            int affectedRows = insertIntoImages.executeUpdate();

            if (affectedRows != 1) {
                System.out.println("Couldn't insert into images!");
            }
        } catch (SQLException e) {
            System.out.println("Insert failed: " + e.getMessage());
        }
    }

    // Check if numbers are good!!!
    // Change to int for resultset.get
    public List<String> queryImages(int from, int next) {
        try {
            selectFromImages.setInt(1, from);
            selectFromImages.setInt(2, next);
            try (ResultSet resultSet = selectFromImages.executeQuery()) {
                List<String> images = new ArrayList<>();

                while (resultSet.next()) {
                    images.add((resultSet.getString(COLUMN_IMAGES_LOCATION)));
                }

                return images;

            } catch (SQLException e) {
                System.out.println("Query failed: " + e.getMessage());
                return null;
            }
        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return null;
        }
    }

    public int queryImagesCount(){
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(QUERY_IMAGES_COUNT)) {

            return resultSet.getInt("count");

        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return 0;
        }
    }

    public List<String> queryTagsForImage(String imagePath){
        try {
            queryImageTags.setString(1, imagePath);

            try (ResultSet resultSet = queryImageTags.executeQuery()) {
                List<String> tags = new ArrayList<>();

                while (resultSet.next()) {
                    tags.add((resultSet.getString(COLUMN_TAGS_TAG)));
                }

                return tags;

            } catch (SQLException e) {
                System.out.println("Query failed: " + e.getMessage());
                return null;
            }
        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return null;
        }
    }

    public void insertNewTag(String imagePath, String tag){
        int imageID = selectImageID(imagePath);
        insertTag(tag);
        int tagID = selectTagID(tag);

        if(imageID == -1 || tagID == -1)
            return;
        System.out.println("Test: " + imageID + " || " + tagID);
        try {
            insertIntoImageTags.setInt(1, imageID);
            insertIntoImageTags.setInt(2, tagID);

            int affectedRows = insertIntoImageTags.executeUpdate();

            if (affectedRows != 1) {
                System.out.println("Couldn't insert into images!");
            }
        } catch (SQLException e) {
            System.out.println("Insert failed: " + e.getMessage());
        }
    }

    private int selectImageID(String imagePath)
    {
        try {
            queryImage.setString(1, imagePath);
            try (ResultSet resultSet = queryImage.executeQuery()) {

                if (resultSet.next()) {
                    return resultSet.getInt(COLUMN_IMAGES_ID);
                }

                return -1;

            } catch (SQLException e) {
                System.out.println("Query failed: " + e.getMessage());
                return -1;
            }
        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return -1;
        }
    }

    private void insertTag(String tag)
    {
        try {
            insertIntoTags.setString(1, tag);

            int affectedRows = insertIntoTags.executeUpdate();

            if (affectedRows != 1) {
                System.out.println("Couldn't insert into images!");
            }
        } catch (SQLException e) {
            System.out.println("Insert failed: " + e.getMessage());
        }
    }

    private int selectTagID(String tag)
    {
        try {
            queryTag.setString(1, tag);

            try (ResultSet resultSet = queryTag.executeQuery()) {
                List<String> tags = new ArrayList<>();

                if (resultSet.next()) {
                    return resultSet.getInt(COLUMN_TAGS_ID);
                }

                return -1;

            } catch (SQLException e) {
                System.out.println("Query failed: " + e.getMessage());
                return -1;
            }
        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return -1;
        }
    }

    public List<String> queryTagsAll() {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(QUERY_TAGS_ALL)) {

            List<String> folders = new ArrayList<>();
            while (resultSet.next()) {
                folders.add((resultSet.getString(COLUMN_TAGS_TAG)));
            }

            return folders;

        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return null;
        }
    }
}
