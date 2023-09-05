package com.bezkoder.spring.login.controllers;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class test {

    public static void main(String[] args) {

        Map<String, List<Map<String, Integer>>> mapParam = new HashMap<>();
        mapParam.put("Identifiant", List.of(new HashMap<String, Integer>(Map.of("id", 2))));
        mapParam.put("title", List.of(new HashMap<String, Integer>(Map.of("title", 1))));
        mapParam.put("body", List.of(new HashMap<String, Integer>(Map.of("body", 4, "5", 6, "7", 8, "8", 9))));
        mapParam.put("meta", List.of(new HashMap<String, Integer>(Map.of("Customer", 7, "ocit_humeurName", 9))));
        // mapParam.put("date", List.of(20, 21, 23));
        mapParam.put("date", List.of(new HashMap<String, Integer>(Map.of("date", 10))));
//Rq on peut au lieu de faire ça q'on cré une class (l'objet qui par la suite recupere les données du frontend
        String jdbcUrl = "jdbc:mysql://localhost:3306/testdb_spring";
        String username = "root";
        String password = "123456";
        String className = "com.mysql.cj.jdbc.Driver";
        String tableName = "tutorials";
        int numCol = getNumCol(className, password, jdbcUrl, username, tableName);
        System.out.println(numCol);
        List<String> listCol = getNameColumns(className, password, jdbcUrl, username, tableName, numCol);
        System.out.println(listCol);

        insertVlues(jdbcUrl, username, password, "eee", "title1",77);
        insertVlues(jdbcUrl, username, password, "eee", "title2",88);
        insertVlues(jdbcUrl, username, password, "eee", "title3",9);
        readJDBC(jdbcUrl, username, password, className, tableName, numCol);
        JDBCToJSON(mapParam,jdbcUrl,username,password,className,numCol,tableName);









                Connection connection = null;
                String dbUrl = "jdbc:mysql://localhost:3306/sss";
                String myusername = "root";
                String mypassword = "123456";

                try {
                    // Register the JDBC driver
                    Class.forName("com.mysql.cj.jdbc.Driver");

                    // Open a connection
                    connection = DriverManager.getConnection(dbUrl, myusername, mypassword);

                    // If the connection is successful, print a success message
                    System.out.println("Database connection successful!");
                } catch (SQLException e) {
                    // Handle any errors that may occur
                    System.out.println("Database connection failed!");
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    // Handle any errors that may occur while loading the JDBC driver
                    System.out.println("JDBC driver not found!");
                    e.printStackTrace();
                } finally {
                    // Close the connection
                    if (connection != null) {
                        try {
                            connection.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }









    public static int getNumCol(String className, String password, String jdbcUrl, String username, String tableName) {
        int columnCount = 0;
        try {
            Class.forName(className);
            Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tableName);
            ResultSetMetaData metaData = resultSet.getMetaData();
            columnCount = metaData.getColumnCount();
        } catch (Exception e) {
            System.out.println(e);
        }
        return columnCount;
    }

    public static List<String> getNameColumns(String className, String password, String jdbcUrl, String username, String tableName, int columnCount) {
        List<String> listNameColumn = new ArrayList<>();
        try {
            Class.forName(className);
            Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tableName);
            ResultSetMetaData metaData = resultSet.getMetaData();

            for (int i = 1; i <= columnCount; i++) {
                listNameColumn.add(metaData.getCatalogName(i));
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return listNameColumn;
    }

    public static void readJDBC(String jdbcUrl, String username, String password, String className, String tableName, int numCol) {
        try {
            Class.forName(className);
            Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tableName);
            while (resultSet.next()) {
                List<String> values = new ArrayList<>();
                for (int i = 1; i <= numCol; i++) {
                    values.add(resultSet.getString(i));
                }
                System.out.println(values);
            }
            resultSet.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            System.out.println(e);
        }


    }

    public static void JDBCToJSON(Map<String, List<Map<String, Integer>>> mapParam, String jdbcUrl, String username, String password, String className, int numCol, String tableName) {

        try {
            Class.forName(className);
            Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tableName);
            ResultSetMetaData metaData = resultSet.getMetaData();
            while (resultSet.next()) {
                List<String> values = new ArrayList<>();
                for (int i = 1; i <= numCol; i++) {
                    values.add(resultSet.getString(i));
                }
                System.out.println(values);
            }

        } catch (Exception e) {
            System.out.println(e);
        }


    }

    public static void insertVlues(String url, String user, String password, String val1, String val2,int valId) {
        // Configuration de la connexion à la base de données
        // Création de la requête SQL
        String query = "INSERT INTO tutorials (id,description, title) VALUES (?, ?, ?)";

        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement pst = con.prepareStatement(query)) {

            // Définition des valeurs des paramètres de la requête
            pst.setInt(1, valId);
            pst.setString(2, val1);
            pst.setString(3, val2);

            // Exécution de la requête
            pst.executeUpdate();
            System.out.println("Données insérées avec succès");

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

}
