CREATE DATABASE db_umkm;
GO

USE db_umkm;
GO

CREATE TABLE toko (
    id_toko VARCHAR(10) PRIMARY KEY,
    nama_toko VARCHAR(100) NOT NULL,
    alamat VARCHAR(255) NOT NULL
);

CREATE TABLE kasir (
    id_kasir VARCHAR(10) PRIMARY KEY,
    nama_kasir VARCHAR(100) NOT NULL
);

CREATE TABLE barang (
    id_barang VARCHAR(10) PRIMARY KEY,
    nama_barang VARCHAR(100) NOT NULL,
    harga DECIMAL(12,2) NOT NULL
);

CREATE TABLE pesanan (
    id_pesanan VARCHAR(20) PRIMARY KEY,
    tanggal DATE NOT NULL,
    jam TIME NOT NULL,
    id_kasir VARCHAR(10) NOT NULL,
    id_toko VARCHAR(10) NOT NULL,
    metode_pembayaran VARCHAR(50),
    diskon DECIMAL(12,2) DEFAULT 0,
    pajak DECIMAL(12,2) DEFAULT 0,

    FOREIGN KEY (id_kasir) REFERENCES kasir(id_kasir),
    FOREIGN KEY (id_toko) REFERENCES toko(id_toko)
);

CREATE TABLE detail_pesanan (
    id_pesanan VARCHAR(20),
    id_barang VARCHAR(10),
    jumlah INT NOT NULL,

    PRIMARY KEY (id_pesanan, id_barang),

    FOREIGN KEY (id_pesanan) REFERENCES pesanan(id_pesanan),
    FOREIGN KEY (id_barang) REFERENCES barang(id_barang)
);

INSERT INTO toko
VALUES ('T01', 'Frenz Soekarno Hatta',
        'Ruko Semangat Timur 10B, Jl. Soekarno Hatta');

INSERT INTO kasir
VALUES ('K01', 'Erfan');

INSERT INTO barang
VALUES ('B01', 'OLINE KD C IP', 70000);
