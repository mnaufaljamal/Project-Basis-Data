import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class PanelLaporan extends JPanel {

    JTable table;
    DefaultTableModel model;
    JTextField txtDari, txtSampai;
    JButton btnTampil;

    Connection conn;

    public PanelLaporan() {
        setLayout(new BorderLayout());

        connect();

        JPanel panelTop = new JPanel(new GridLayout(2,3));
        panelTop.add(new JLabel("Dari (YYYY-MM-DD)"));
        txtDari = new JTextField();
        panelTop.add(txtDari);

        panelTop.add(new JLabel("Sampai (YYYY-MM-DD)"));
        txtSampai = new JTextField();
        panelTop.add(txtSampai);

        btnTampil = new JButton("Tampilkan");
        panelTop.add(new JLabel());
        panelTop.add(btnTampil);

        add(panelTop, BorderLayout.NORTH);

        model = new DefaultTableModel();
        model.addColumn("ID Transaksi");
        model.addColumn("Tanggal");
        model.addColumn("Pelanggan");
        model.addColumn("Total");
        model.addColumn("Metode");

        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        btnTampil.addActionListener(e -> loadData());
    }

    void connect() {
        try {
            conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/nama_db", "root", "");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Koneksi gagal");
        }
    }

    void loadData() {
        model.setRowCount(0);

        if (txtDari.getText().isEmpty() || txtSampai.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tanggal tidak boleh kosong!");
            return;
        }

        try {
            String sql =
                "SELECT t.id_transaksi, t.tanggal_transaksi, p.nama_pelanggan, t.total_harga, t.metode_pembayaran " +
                "FROM transaksi t " +
                "JOIN pelanggan p ON t.id_pelanggan = p.id_pelanggan " +
                "WHERE DATE(t.tanggal_transaksi) BETWEEN ? AND ?";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, txtDari.getText());
            ps.setString(2, txtSampai.getText());

            ResultSet rs = ps.executeQuery();

            double grandTotal = 0;

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id_transaksi"),
                    rs.getString("tanggal_transaksi"),
                    rs.getString("nama_pelanggan"),
                    rs.getDouble("total_harga"),
                    rs.getString("metode_pembayaran")
                });

                grandTotal += rs.getDouble("total_harga");
            }

            JOptionPane.showMessageDialog(this,
                "Total Pendapatan: " + grandTotal);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error: " + e.getMessage());
        }
    }
}
