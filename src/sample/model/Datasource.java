package sample.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A singleton class that connects to a database that stores image locations and image tags.
 * The class will create the database if it does not already exists. The databases will be in the folder with the .exe
 * The Database consists of 5 tables called: folders, images, imagesFolders, tags, imageTags.
 * folders table: stores the folder directory that will be used to pull images from.
 * images table: stores the directory of images.
 * imagesFolders: stories the link between the image directory and which parent folder it is apart of.
 * tags:  stores unique tag names.
 * imageTags: stores which images has which tags.
 * Uses sqlite-jdbc-3.34.0.jar
 */
class Datasource {
    private static Datasource instance = new Datasource();

    private static final String DATABASE_NAME = "\\MyData.db";

    // Folder table
    private static final String TABLE_FOLDERS = "folders";
    private static final String COLUMN_FOLDERS_ID = "_id";
    private static final String COLUMN_FOLDERS_DIRECTORY = "directory";

    // Images table
    private static final String TABLE_IMAGES = "images";
    private static final String COLUMN_IMAGES_ID = "_id";
    private static final String COLUMN_IMAGES_LOCATION = "location";

    // Image folders table
    private static final String TABLE_IMAGE_FOLDERS = "imageFolders";
    private static final String COLUMN_IMAGES_FOLDER_IMAGE_ID = "image_id";
    private static final String COLUMN_IMAGES_FOLDER_FOLDER_ID = "folder_id";

    // Tags table
    private static final String TABLE_TAGS = "tags";
    private static final String COLUMN_TAGS_ID = "_id";
    private static final String COLUMN_TAGS_TAG = "tag";

    // Image tags table
    private static final String TABLE_IMAGE_TAGS = "image_tags";
    private static final String COLUMN_IMAGE_TAGS_IMAGE_ID = "image_id";
    private static final String COLUMN_IMAGES_TAGS_TAG_ID = "tag_id";

    private static final String ENABLE_FOREIGN_KEYS = "PRAGMA foreign_keys = ON;";

    private static final String CREATE_TABLE_FOLDERS = String.format(
            "CREATE TABLE IF NOT EXISTS %s (" +
                    "%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "%s VARCHAR(32767) NOT NULL UNIQUE);",
            TABLE_FOLDERS, COLUMN_FOLDERS_ID, COLUMN_FOLDERS_DIRECTORY);

    private static final String CREATE_TABLE_IMAGES = String.format(
            "CREATE TABLE IF NOT EXISTS %s (" +
                    "%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "%s VARCHAR(32767) NOT NULL UNIQUE); ",
            TABLE_IMAGES, COLUMN_IMAGES_ID, COLUMN_IMAGES_LOCATION);

    private static final String CREATE_TABLE_TAGS = String.format(
            "CREATE TABLE IF NOT EXISTS %s (" +
                    "%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "%s VARCHAR(255) NOT NULL UNIQUE);", TABLE_TAGS, COLUMN_TAGS_ID, COLUMN_TAGS_TAG);

    private static final String CREATE_TABLE_IMAGE_FOLDERS = String.format(
            "CREATE TABLE IF NOT EXISTS %s (" +
                    "%s INTEGER NOT NULL, " +
                    "%s INTEGER NOT NULL, " +
                    "FOREIGN KEY(%s) REFERENCES %s(%s) ON DELETE CASCADE, " +
                    "FOREIGN KEY(%s) REFERENCES %s(%s) ON DELETE CASCADE, " +
                    "PRIMARY KEY(%s, %s));",
            TABLE_IMAGE_FOLDERS, COLUMN_IMAGES_FOLDER_IMAGE_ID, COLUMN_IMAGES_FOLDER_FOLDER_ID,
            COLUMN_IMAGES_FOLDER_IMAGE_ID, TABLE_IMAGES, COLUMN_IMAGES_ID,
            COLUMN_IMAGES_FOLDER_FOLDER_ID, TABLE_FOLDERS, COLUMN_FOLDERS_ID,
            COLUMN_IMAGES_FOLDER_IMAGE_ID, COLUMN_IMAGES_FOLDER_FOLDER_ID);

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
            COLUMN_IMAGE_TAGS_IMAGE_ID, COLUMN_IMAGES_TAGS_TAG_ID);

    private static final String INSERT_FOLDER = String.format(
            "INSERT INTO %s (%s) VALUES(?);",
            TABLE_FOLDERS, COLUMN_FOLDERS_DIRECTORY);

    private static final String INSERT_IMAGE = String.format(
            "INSERT INTO %s (%s) VALUES (?);",
            TABLE_IMAGES, COLUMN_IMAGES_LOCATION);

    private static final String INSERT_TAG = String.format(
            "INSERT INTO %s (%s) VALUES(?);",
            TABLE_TAGS, COLUMN_TAGS_TAG);

    private static final String INSERT_IMAGE_FOLDER = String.format(
            "INSERT INTO %s (%s, %s) VALUES(?, ?);",
            TABLE_IMAGE_FOLDERS, COLUMN_IMAGES_FOLDER_IMAGE_ID, COLUMN_IMAGES_FOLDER_FOLDER_ID);

    private static final String INSERT_IMAGE_TAG = String.format(
            "INSERT INTO %s (%s, %s) VALUES (?, ?)",
            TABLE_IMAGE_TAGS, COLUMN_IMAGE_TAGS_IMAGE_ID, COLUMN_IMAGES_TAGS_TAG_ID);

    private static final String DELETE_FOLDER = String.format(
            "DELETE FROM %s WHERE %s = ?;",
            TABLE_FOLDERS, COLUMN_FOLDERS_DIRECTORY
    );

    private static final String DELETE_IMAGE = String.format(
            "DELETE FROM %s WHERE location = ?;",
            TABLE_IMAGES
    );

    private static final String DELETE_UNUSED_IMAGES = String.format(
            "DELETE FROM %s WHERE %s.%s IN(" +
                    "SELECT %s.%s FROM %s " +
                    "LEFT JOIN %s ON %s.%s = %s.%s " +
                    "WHERE %s.%s is NULL);",
            TABLE_IMAGES, TABLE_IMAGES, COLUMN_IMAGES_ID,
            TABLE_IMAGES, COLUMN_IMAGES_ID, TABLE_IMAGES,
            TABLE_IMAGE_FOLDERS, TABLE_IMAGE_FOLDERS, COLUMN_IMAGES_FOLDER_IMAGE_ID, TABLE_IMAGES, COLUMN_IMAGES_ID,
            TABLE_IMAGE_FOLDERS, COLUMN_IMAGES_FOLDER_FOLDER_ID);

    private static final String DELETE_UNUSED_TAGS = String.format(
            "DELETE FROM %s WHERE %s.%s IN(" +
                    "SELECT %s.%s FROM %s " +
                    "LEFT JOIN %s ON %s.%s = %s.%s " +
                    "WHERE %s.%s IS NULL);",
            TABLE_TAGS, TABLE_TAGS, COLUMN_TAGS_ID,
            TABLE_TAGS, COLUMN_TAGS_ID, TABLE_TAGS,
            TABLE_IMAGE_TAGS, TABLE_IMAGE_TAGS, COLUMN_IMAGES_TAGS_TAG_ID, TABLE_TAGS, COLUMN_TAGS_ID,
            TABLE_IMAGE_TAGS, COLUMN_IMAGES_TAGS_TAG_ID);

    private static final String DELETE_IMAGE_TAG = String.format(
            "DELETE FROM %s WHERE %s = ? AND %s = ?;",
            TABLE_IMAGE_TAGS, COLUMN_IMAGE_TAGS_IMAGE_ID, COLUMN_IMAGES_TAGS_TAG_ID);

    private static final String QUERY_FOLDERS = String.format(
            "SELECT * FROM %s;",
            TABLE_FOLDERS);

    private static final String QUERY_IMAGE_WHERE = String.format(
            "SELECT %s FROM %s WHERE %s = ?",
            COLUMN_IMAGES_ID, TABLE_IMAGES, COLUMN_IMAGES_LOCATION);

    private static final String QUERY_IMAGES_LIMIT = String.format(
            "SELECT * FROM %s LIMIT ?,?;",
            TABLE_IMAGES);

    private static final String QUERY_IMAGES_WITH_TAGS_LIMIT_PART_1 = String.format(
            "SELECT %s.%s, count(*) AS count FROM %s " +
                    "JOIN %s ON %s.%s = %s.%s " +
                    "JOIN %s ON %s.%s == %s.%s " +
                    "WHERE %s.%s IN (",
            TABLE_IMAGES, COLUMN_IMAGES_LOCATION, TABLE_IMAGES,
            TABLE_IMAGE_TAGS, TABLE_IMAGE_TAGS, COLUMN_IMAGE_TAGS_IMAGE_ID, TABLE_IMAGES, COLUMN_IMAGES_ID,
            TABLE_TAGS, TABLE_TAGS, COLUMN_TAGS_ID, TABLE_IMAGE_TAGS, COLUMN_IMAGES_TAGS_TAG_ID,
            TABLE_TAGS, COLUMN_TAGS_TAG);

    private static final String QUERY_IMAGES_WITH_TAGS_LIMIT_PART_2 = String.format(
            ") " +
                    "GROUP BY %s.%s " +
                    "HAVING count = ? " +
                    "LIMIT ?,?;",
            TABLE_IMAGES, COLUMN_IMAGES_ID);

    private static final String QUERY_IMAGES_COUNT = String.format(
            "SELECT COUNT(*) AS count FROM %s", TABLE_IMAGES);

    private static final String QUERY_IMAGES_COUNT_WHERE_PART_1 = String.format(
            "SELECT count(*) AS count FROM ( " +
                    "SELECT count(*) AS count FROM %s " +
                    "JOIN %s ON %s.%s = %s.%s " +
                    "JOIN %s ON %s.%s == %s.%s " +
                    "WHERE %s.%s IN (",
            TABLE_IMAGES,
            TABLE_IMAGE_TAGS, TABLE_IMAGE_TAGS, COLUMN_IMAGE_TAGS_IMAGE_ID, TABLE_IMAGES, COLUMN_IMAGES_ID,
            TABLE_TAGS, TABLE_TAGS, COLUMN_TAGS_ID, TABLE_IMAGE_TAGS, COLUMN_IMAGES_TAGS_TAG_ID,
            TABLE_TAGS, COLUMN_TAGS_TAG);

    private static final String QUERY_IMAGES_COUNT_WHERE_PART_2 = String.format(
            ") " +
                    "GROUP BY %s.%s " +
                    "HAVING count = ?);",
            TABLE_IMAGES, COLUMN_IMAGES_ID);

    private static final String QUERY_TAG_WHERE = String.format(
            "SELECT %s FROM %s WHERE %s = ?",
            COLUMN_TAGS_ID, TABLE_TAGS, COLUMN_TAGS_TAG);

    private static final String QUERY_TAGS = String.format(
            "SELECT %s FROM %s",
            COLUMN_TAGS_TAG, TABLE_TAGS);

    private static final String QUERY_TAGS_ON_IMAGE = String.format(
            "SELECT %s.%s FROM %s " +
                    "INNER JOIN %s " +
                    "ON %s.%s = %s.%s " +
                    "INNER JOIN %s " +
                    "ON %s.%s = %s.%s " +
                    "WHERE %s.%s = ?",
            TABLE_TAGS, COLUMN_TAGS_TAG, TABLE_TAGS,
            TABLE_IMAGE_TAGS,
            TABLE_IMAGE_TAGS, COLUMN_IMAGES_TAGS_TAG_ID, TABLE_TAGS, COLUMN_TAGS_ID,
            TABLE_IMAGES,
            TABLE_IMAGE_TAGS, COLUMN_IMAGE_TAGS_IMAGE_ID, TABLE_IMAGES, COLUMN_IMAGES_ID,
            TABLE_IMAGES, COLUMN_IMAGES_LOCATION);

    private Connection connection;

    private PreparedStatement insertFolderStatement;
    private PreparedStatement insertImageStatement;
    private PreparedStatement insertTagStatement;
    private PreparedStatement insertImageFolderStatement;
    private PreparedStatement insertImageTagStatement;

    private PreparedStatement deleteFolderStatement;
    private PreparedStatement deleteImageStatement;
    private PreparedStatement deleteImageTagStatement;

    private PreparedStatement queryImagesLimitStatement;
    private PreparedStatement queryImageWhereStatement;
    private PreparedStatement queryTagWhereStatement;
    private PreparedStatement queryTagsOnImageStatement;

    /**
     * Only private instances.
     */
    private Datasource() {
    }

    /**
     * @return the singleton instance.
     */
    public static Datasource getInstance() {
        return instance;
    }

    /**
     * Opens or creates a new database. Sets up prepare statements.
     *
     * @return has the database been open.
     */
    public boolean open() {
        try {

            if (connection != null && !connection.isClosed()) {
                System.out.println("Database is already connected");
                return false;
            }

            // Load or create new database at .exe path
            connection = DriverManager.getConnection("jdbc:sqlite:" + System.getProperty("user.dir") + DATABASE_NAME);

            // Create tables if absent
            boolean connected = databaseSetup();

            insertFolderStatement = connection.prepareStatement(INSERT_FOLDER);
            insertImageStatement = connection.prepareStatement(INSERT_IMAGE, Statement.RETURN_GENERATED_KEYS);
            insertTagStatement = connection.prepareStatement(INSERT_TAG, Statement.RETURN_GENERATED_KEYS);
            insertImageTagStatement = connection.prepareStatement(INSERT_IMAGE_TAG);
            insertImageFolderStatement = connection.prepareStatement(INSERT_IMAGE_FOLDER);

            deleteFolderStatement = connection.prepareStatement(DELETE_FOLDER);
            deleteImageStatement = connection.prepareStatement(DELETE_IMAGE);
            deleteImageTagStatement = connection.prepareStatement(DELETE_IMAGE_TAG);

            queryImageWhereStatement = connection.prepareStatement(QUERY_IMAGE_WHERE);
            queryImagesLimitStatement = connection.prepareStatement(QUERY_IMAGES_LIMIT);
            queryTagWhereStatement = connection.prepareStatement(QUERY_TAG_WHERE);
            queryTagsOnImageStatement = connection.prepareStatement(QUERY_TAGS_ON_IMAGE);

            System.out.println("Connected to database");

            return connected;

        } catch (SQLException e) {
            System.out.println("Couldn't connect to database: " + e.getMessage());
            return false;
        }
    }

    /**
     * Create table for database if they are absent
     *
     * @return false if the tables failed to be created.
     */
    private boolean databaseSetup() {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(ENABLE_FOREIGN_KEYS);
            statement.executeUpdate(CREATE_TABLE_FOLDERS);
            statement.executeUpdate(CREATE_TABLE_IMAGES);
            statement.executeUpdate(CREATE_TABLE_IMAGE_FOLDERS);
            statement.executeUpdate(CREATE_TABLE_TAGS);
            statement.executeUpdate(CREATE_TABLE_IMAGE_TAGS);
            System.out.println("Database setup complete");
            return true;
        } catch (SQLException e) {
            System.out.println("Database setup failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Close all prepare statements and database.
     */
    public void close() {
        try {
            if (insertFolderStatement != null) {
                insertFolderStatement.close();
            }
            if (insertImageStatement != null) {
                insertImageStatement.close();
            }
            if (insertTagStatement != null) {
                insertTagStatement.close();
            }
            if (insertImageTagStatement != null) {
                insertImageTagStatement.close();
            }
            if (insertImageFolderStatement != null) {
                insertImageFolderStatement.close();
            }
            if (deleteFolderStatement != null) {
                deleteFolderStatement.close();
            }
            if (deleteImageStatement != null) {
                deleteImageStatement.close();
            }
            if (deleteImageTagStatement != null) {
                deleteImageTagStatement.close();
            }
            if (queryImageWhereStatement != null) {
                queryImageWhereStatement.close();
            }
            if (queryImagesLimitStatement != null) {
                queryImagesLimitStatement.close();
            }
            if (queryTagWhereStatement != null) {
                queryTagWhereStatement.close();
            }
            if (queryTagsOnImageStatement != null) {
                queryTagsOnImageStatement.close();
            }
            if (connection != null) {
                connection.close();
                System.out.println("Connection to database has been closed");
            }
        } catch (SQLException e) {
            System.out.println("Couldn't close connection: " + e.getMessage());
        }
    }

    /**
     * Insert a folder directory that will be use to search for images.
     *
     * @param directory folder path to find images from.
     */
    public void insertFolder(String directory) {
        try {
            insertFolderStatement.setString(1, directory);
            insertFolderStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Insert folder failed: " + e.getMessage());
        }
    }

    /**
     * Insert a image directory and link it to its corresponding folder
     *
     * @param directory image directory.
     * @param folderID  an id that can be found in the folders table.
     */
    private void insertImage(String directory, int folderID) {

        if (hasImage(directory))
            return;

        try {
            insertImageStatement.setString(1, directory);
            insertImageStatement.executeUpdate();

            try (ResultSet resultSet = insertImageStatement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    int lastInsertedID = resultSet.getInt(1);
                    insertImageFolder(lastInsertedID, folderID);
                }
            } catch (SQLException e) {
                System.out.println("Could not get result set from last insert: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Insert image failed: " + e.getMessage());
        }
    }

    /**
     * Insert a list of image directories and link them to their corresponding folder.
     *
     * @param directories list of image directories.
     * @param folderID    an id that can be found in the folders table.
     */
    public void insertImages(List<String> directories, int folderID) {
        for (String imageDirectory : directories) {
            insertImage(imageDirectory, folderID);
        }
    }

    /**
     * Insert a unique tag into the tags table.
     *
     * @param tag to be added to the tag table
     * @return tag's id in the table. -1 represents a failed insertion.
     */
    private int insertTag(String tag) {

        int tagID = queryTagID(tag); // get the tag id if it is already in the tags table.
        if (tagID != -1)
            return tagID;

        try {
            insertTagStatement.setString(1, tag);
            insertTagStatement.executeUpdate();

            try (ResultSet resultSet = insertTagStatement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1); // Return tag's ID of new entry.
                }
            } catch (SQLException e) {
                System.out.println("Failed to get id of lst inserted tag: " + e.getMessage());
                return -1;
            }

        } catch (SQLException e) {
            System.out.println("Insert tag failed: " + e.getMessage());
            return -1;
        }

        return -1;
    }

    /**
     * Insert the link between an image and its corresponding folder.
     *
     * @param imageID  id of an image in the images table.
     * @param folderID id of a folder in the folders table.
     */
    private void insertImageFolder(int imageID, int folderID) {
        try {
            insertImageFolderStatement.setInt(1, imageID);
            insertImageFolderStatement.setInt(2, folderID);
            insertImageFolderStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Insert image folder failed: " + e.getMessage());
        }
    }

    /**
     * Insert the link between an image and tag into the imageTag table.
     *
     * @param directory directory of an image.
     * @param tag       name of a tag.
     */
    public void insertImageTag(String directory, String tag) {
        int imageID = queryImageID(directory);
        int tagID = insertTag(tag);

        if (imageID == -1 || tagID == -1)
            return;

        try {
            insertImageTagStatement.setInt(1, imageID);
            insertImageTagStatement.setInt(2, tagID);
            insertImageTagStatement.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Insert image tag failed: " + e.getMessage());
        }
    }

    /**
     * Remove directory of folder from the folders table.
     *
     * @param directory the folder directory.
     */
    public void deleteFolder(String directory) {
        try {
            deleteFolderStatement.setString(1, directory);
            deleteFolderStatement.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Folder deletion failed: " + e.getMessage());
        }
    }

    /**
     * Remove image directory from the images table.
     *
     * @param directory the image directory.
     */
    public void deleteImage(String directory) {
        try {
            deleteImageStatement.setString(1, directory);
            deleteImageStatement.executeUpdate();

            deleteUnusedTags();
        } catch (SQLException e) {
            System.out.println("Could not delete image: " + e.getMessage());
        }
    }

    /**
     * Remove images from image table that does not have a corresponding parent folder.
     */
    public void deleteUnusedImages() {
        try (PreparedStatement preparedStatement = connection.prepareStatement(DELETE_UNUSED_IMAGES)) {
            preparedStatement.executeUpdate();

            deleteUnusedTags();
        } catch (SQLException e) {
            System.out.println("Could not delete unused images: " + e.getMessage());
        }
    }

    /**
     * Remove tag from an image.
     *
     * @param directory the image directory.
     * @param tag       tag to remove.
     */
    public void deleteImageTag(String directory, String tag) {
        int imageID = queryImageID(directory);
        int tagID = queryTagID(tag);

        if (imageID == -1 || tagID == -1)
            return;

        try {
            deleteImageTagStatement.setInt(1, imageID);
            deleteImageTagStatement.setInt(2, tagID);
            deleteImageTagStatement.executeUpdate();

            deleteUnusedTags();
        } catch (SQLException e) {
            System.out.println("Delete tag from image failed: " + e.getMessage());
        }
    }

    /**
     * Remove all unused tags.
     */
    private void deleteUnusedTags() {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(DELETE_UNUSED_TAGS);

        } catch (SQLException e) {
            System.out.println("Failed deleting unused tags: " + e.getMessage());
        }
    }

    /**
     * Get all entries and rows in the folders table.
     *
     * @return a list that contains folder directory and folder id.
     */
    public List<QueryFolderEntry> queryFolders() {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(QUERY_FOLDERS)) {

            List<QueryFolderEntry> queryFolderEntries = new ArrayList<>();
            while (resultSet.next()) {
                QueryFolderEntry queryFolderEntry = new QueryFolderEntry(
                        resultSet.getInt(COLUMN_FOLDERS_ID),
                        resultSet.getString(COLUMN_FOLDERS_DIRECTORY)
                );
                queryFolderEntries.add(queryFolderEntry);
            }

            return queryFolderEntries;

        } catch (SQLException e) {
            System.out.println("Query folders failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get a list of folder directories from the folders table.
     *
     * @return a list of folder directories.
     */
    public List<String> queryFoldersForDirectory() {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(QUERY_FOLDERS)) {

            List<String> folders = new ArrayList<>();
            while (resultSet.next()) {
                folders.add(resultSet.getString(COLUMN_FOLDERS_DIRECTORY));
            }

            return folders;

        } catch (SQLException e) {
            System.out.println("Query folders For directory failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Is image directory present in images table.
     *
     * @param directory image directory.
     * @return true if image if present in images table.
     */
    public boolean hasImage(String directory) {
        return queryImageID(directory) != -1 ? true : false;
    }

    /**
     * Get id of image directory that is in the images table.
     *
     * @param directory image directory.
     * @return the id of image directory in images table. -1 represents not in table or failed to work.
     */
    private int queryImageID(String directory) {
        try {
            queryImageWhereStatement.setString(1, directory);
            try (ResultSet resultSet = queryImageWhereStatement.executeQuery()) {

                if (resultSet.next()) {
                    return resultSet.getInt(COLUMN_IMAGES_ID);
                }

                return -1;

            } catch (SQLException e) {
                System.out.println("Could not get result set for Image id : " + e.getMessage());
                return -1;
            }
        } catch (SQLException e) {
            System.out.println("Query Image id failed: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Get a limited list of image directories from image table.
     *
     * @param from start point from the images table.
     * @param next how many image directories from image table.
     * @return list of image directories.
     */
    public List<String> queryImages(int from, int next) {
        try {
            queryImagesLimitStatement.setInt(1, from);
            queryImagesLimitStatement.setInt(2, next);
            try (ResultSet resultSet = queryImagesLimitStatement.executeQuery()) {
                List<String> images = new ArrayList<>();

                while (resultSet.next()) {
                    images.add((resultSet.getString(COLUMN_IMAGES_LOCATION)));
                }

                return images;

            } catch (SQLException e) {
                System.out.println("Could not get result set for images: " + e.getMessage());
                return null;
            }
        } catch (SQLException e) {
            System.out.println("Query images failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get the total number of image directories in image table.
     *
     * @return the number of images in the images table.
     */
    public int queryCountImages() {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(QUERY_IMAGES_COUNT)) {

            return resultSet.getInt("count");

        } catch (SQLException e) {
            System.out.println("Query count images failed: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Get a limited list of image directories from image table that have matching tags.
     *
     * @param tags list of tags to filter by.
     * @param from start point from the images table.
     * @param next how many image directories from image table.
     * @return list of image directories which matching tags.
     */
    public List<String> queryImagesWithTags(List<String> tags, int from, int next) {

        // Create new prepare statement to include all tags.
        try (PreparedStatement statement = connection.prepareStatement(createQueryImagesWithTagsLimitStatement(tags))) {

            // Add all tags to prepared statement
            int i = 1;
            while (i <= tags.size()) {
                statement.setString(i, tags.get(i - 1));
                i++;
            }

            statement.setInt(i, tags.size());
            statement.setInt(i + 1, from);
            statement.setInt(i + 2, next);

            try (ResultSet resultSet = statement.executeQuery()) {
                List<String> imageList = new ArrayList<String>();

                while (resultSet.next()) {
                    imageList.add((resultSet.getString(COLUMN_IMAGES_LOCATION)));
                }

                return imageList;

            } catch (SQLException e) {
                System.out.println("Could not get result set for images with tags: " + e.getMessage());
                return null;
            }
        } catch (SQLException e) {
            System.out.println("Query images with tags failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Create a prepare statement to query images with tags limit with any amount of tags.
     *
     * @param tags size of the list will be use to add ? into the prepare statement.
     * @return query statement for images with tags with a limit.
     */
    private String createQueryImagesWithTagsLimitStatement(List<String> tags) {
        StringBuilder sb = new StringBuilder();
        sb.append(QUERY_IMAGES_WITH_TAGS_LIMIT_PART_1);

        for (int i = 0; i < tags.size(); i++) {
            if (i < tags.size() - 1)
                sb.append("?, ");
            else
                sb.append("?");
        }

        sb.append(QUERY_IMAGES_WITH_TAGS_LIMIT_PART_2);

        return sb.toString();
    }

    /**
     * Get the total number of image directories in image table with matching tags.
     *
     * @param tags list of tags to filter by.
     * @return number of images with matching tags in images table.
     */
    public int queryCountImagesWithTags(List<String> tags) {

        try (PreparedStatement statement = connection.prepareStatement(createQueryImagesCountWithTagsStatement(tags))) {

            int i = 1;
            while (i <= tags.size()) {
                statement.setString(i, tags.get(i - 1));
                i++;
            }

            statement.setInt(i, tags.size());

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.getInt("count");

            } catch (SQLException e) {
                System.out.println("Could not get result set for count images with tags: " + e.getMessage());
                return 0;
            }
        } catch (SQLException e) {
            System.out.println("Query count images with tags failed: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Create a prepare statement to query number of images with tags.
     *
     * @param tags size of the list will be use to add ? into the prepare statement.
     * @return query statement for Image count with tags.
     */
    private String createQueryImagesCountWithTagsStatement(List<String> tags) {

        StringBuilder sb = new StringBuilder();
        sb.append(QUERY_IMAGES_COUNT_WHERE_PART_1);

        for (int i = 0; i < tags.size(); i++) {
            if (i < tags.size() - 1)
                sb.append("?, ");
            else
                sb.append("?");
        }

        sb.append(QUERY_IMAGES_COUNT_WHERE_PART_2);

        return sb.toString();
    }

    /**
     * Get the id for the matching tag in the tags table.
     *
     * @param tag name of the tag
     * @return the tag id in the tags table or -1 if absent from table
     */
    private int queryTagID(String tag) {
        try {
            queryTagWhereStatement.setString(1, tag);

            try (ResultSet resultSet = queryTagWhereStatement.executeQuery()) {
                List<String> tags = new ArrayList<>();

                if (resultSet.next()) {
                    return resultSet.getInt(COLUMN_TAGS_ID);
                }

                return -1;

            } catch (SQLException e) {
                System.out.println("Could not get result set for tagID: " + e.getMessage());
                return -1;
            }
        } catch (SQLException e) {
            System.out.println("Query TagID failed: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Get a list of all tags in tags table.
     *
     * @return list of tags.
     */
    public List<String> queryTags() {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(QUERY_TAGS)) {

            List<String> folders = new ArrayList<>();
            while (resultSet.next()) {
                folders.add((resultSet.getString(COLUMN_TAGS_TAG)));
            }

            return folders;

        } catch (SQLException e) {
            System.out.println("Query Tags failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get a list of all tags that are linked with the selected image directory.
     *
     * @param directory image directory.
     * @return list of tags linked with image directory.
     */
    public List<String> queryTagsOnImage(String directory) {
        try {
            queryTagsOnImageStatement.setString(1, directory);

            try (ResultSet resultSet = queryTagsOnImageStatement.executeQuery()) {
                List<String> tags = new ArrayList<>();

                while (resultSet.next()) {
                    tags.add((resultSet.getString(COLUMN_TAGS_TAG)));
                }

                return tags;

            } catch (SQLException e) {
                System.out.println("Could not get result set for tags on image: " + e.getMessage());
                return null;
            }
        } catch (SQLException e) {
            System.out.println("Query tags on image  failed: " + e.getMessage());
            return null;
        }
    }
}
