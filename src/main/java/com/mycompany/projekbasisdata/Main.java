package com.mycompany.projekbasisdata;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author muhammadnaufalmuflijamal
 */
//testing login database
public class Main {
    public static void main(String[] args) {

        if (Koneksi.getConnection() != null) {
            System.out.println("Tes berhasil.");
        } else {
            System.out.println("Tes gagal.");
        }
    
    }
}
