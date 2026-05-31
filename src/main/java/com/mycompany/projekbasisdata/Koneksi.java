/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.projekbasisdata;

/**
 *
 * @author muhammadnaufalmuflijamal
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Koneksi {

    private static final String URL ="jdbc:sqlserver://localhost:1433;databaseName=db_umkm;encrypt=false;trustServerCertificate=true;";
    private static final String USER = "SA";
    private static final String PASSWORD = "yourStrong(!)Password";

    public static Connection getConnection() {

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            Connection conn = DriverManager.getConnection(URL,USER,PASSWORD);
            System.out.println("Tersambung ke database.");
            return conn;

        } catch (ClassNotFoundException e) {
            System.out.println("Gagal tersambung ke driver.");
        } catch (SQLException e) {
            System.out.println("Gagal tersambung ke database.");
            System.out.println(e.getMessage());
        }

        return null;
    }
}



