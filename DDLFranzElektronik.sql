--buat user dulu nama: Kasir1, pw: 12345
CREATE database FranzElektronik
go
CREATE TABLE Barang (
    ID_Barang VARCHAR(10) PRIMARY KEY, 
    Nama_Barang VARCHAR(100) NOT NULL,
    Kategori VARCHAR(50) NOT NULL,
    Harga_Satuan DECIMAL(18,2) NOT NULL,
    Stok INT NOT NULL DEFAULT 0
);
CREATE TABLE Toko (
    ID_Toko INT IDENTITY(1,1) PRIMARY KEY,
    Nama_Toko VARCHAR(100) NOT NULL,
    Alamat VARCHAR(255) NOT NULL,
    No_Telp VARCHAR(15)
);

CREATE TABLE Kasir (
    ID_Kasir VARCHAR(10) PRIMARY KEY,
    Nama_Kasir VARCHAR(100) NOT NULL
);
CREATE TABLE Pesanan (
    ID_Transaksi VARCHAR(20) PRIMARY KEY, -- Contoh: 'TRX-20260601-001'
    Tanggal_Transaksi DATETIME DEFAULT GETDATE(),
    Total_Bayar DECIMAL(18,2) NOT NULL,
    Tunai DECIMAL(18,2) NOT NULL,
    Kembalian DECIMAL(18,2) NOT NULL,
    ID_Kasir VARCHAR(10),
    ID_Toko INT,
    FOREIGN KEY (ID_Kasir) REFERENCES Kasir(ID_Kasir),
    FOREIGN KEY (ID_Toko) REFERENCES Toko(ID_Toko)
);

CREATE TABLE Detail_Pesanan (
    ID_Detail INT IDENTITY(1,1) PRIMARY KEY,
    ID_Transaksi VARCHAR(20),
    ID_Barang VARCHAR(10),
    Kuantitas INT NOT NULL,
    Subtotal DECIMAL(18,2) NOT NULL,
    FOREIGN KEY (ID_Transaksi) REFERENCES Pesanan(ID_Transaksi) ON DELETE CASCADE,
    FOREIGN KEY (ID_Barang) REFERENCES Barang(ID_Barang)
);