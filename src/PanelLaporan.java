import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class PanelLaporan extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtDari, txtSampai;
    private JButton btnTampil;

    private Connection conn;

    public PanelLaporan() {
        setLayout(new BorderLayout());

        koneksiDatabase();
        initUI();
    }

    private void koneksiDatabase() {
        try {
            conn = KoneksiDatabase.getConnection(); 
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Koneksi gagal: " + e.getMessage());
        }
    }

    private void initUI() {

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        topPanel.add(new JLabel("Dari (YYYY-MM-DD):"));
        txtDari = new JTextField(10);
        topPanel.add(txtDari);

        topPanel.add(new JLabel("Sampai:"));
        txtSampai = new JTextField(10);
        topPanel.add(txtSampai);

        btnTampil = new JButton("Tampilkan");
        topPanel.add(btnTampil);

        add(topPanel, BorderLayout.NORTH);

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

    private void loadData() {
        model.setRowCount(0);

        String dari = txtDari.getText().trim();
        String sampai = txtSampai.getText().trim();

        if (dari.isEmpty() || sampai.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Isi tanggal terlebih dahulu!");
            return;
        }

        try {
            String sql =
                "SELECT t.id_transaksi, t.tanggal_transaksi, " +
                "p.nama_pelanggan, t.total_harga, t.metode_pembayaran " +
                "FROM transaksi t " +
                "JOIN pelanggan p ON t.id_pelanggan = p.id_pelanggan " +
                "WHERE CAST(t.tanggal_transaksi AS DATE) BETWEEN ? AND ?";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, dari);
            ps.setString(2, sampai);

            ResultSet rs = ps.executeQuery();

            double totalSemua = 0;

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id_transaksi"),
                    rs.getString("tanggal_transaksi"),
                    rs.getString("nama_pelanggan"),
                    rs.getDouble("total_harga"),
                    rs.getString("metode_pembayaran")
                });

                totalSemua += rs.getDouble("total_harga");
            }

            JOptionPane.showMessageDialog(this,
                "Total Pendapatan: " + totalSemua);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Gagal load data: " + e.getMessage());
        }
    }
}
