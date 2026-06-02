import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class PanelLaporan extends JPanel {

    private final Color COLOR_PRIMARY = new Color(37, 99, 235);    
    private final Color COLOR_BACKGROUND = new Color(243, 244, 246);  
    private final Color COLOR_WHITE = Color.WHITE;
    private final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);

    private JTable tableLaporan;
    private DefaultTableModel tableModelLaporan;
    private JTextField txtSearch;
    private JComboBox<String> comboMetodeFilter;
    private JButton btnCari, btnRefresh;

    private JLabel lblTotalPendapatan, lblTotalTransaksi, lblRataRataTransaksi;

    public PanelLaporan() {
        setLayout(new BorderLayout());
        setBackground(COLOR_BACKGROUND);

        JPanel headerPadding = new JPanel();
        headerPadding.setBackground(COLOR_WHITE);
        headerPadding.setPreferredSize(new Dimension(0, 8));
        add(headerPadding, BorderLayout.NORTH);

        add(createContentArea(), BorderLayout.CENTER);

        initEventHandlers();
        muatDataLaporan();
    }

    private JPanel createContentArea() {
        JPanel contentArea = new JPanel(new BorderLayout(20, 15));
        contentArea.setBorder(new EmptyBorder(8, 20, 20, 20));
        contentArea.setBackground(COLOR_BACKGROUND);

        JLabel pageTitle = new JLabel("Laporan Penjualan & Riwayat Pesanan");
        pageTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        contentArea.add(pageTitle, BorderLayout.NORTH);

        JPanel mainLayout = new JPanel(new BorderLayout(0, 20));
        mainLayout.setBackground(COLOR_BACKGROUND);

        mainLayout.add(createSummaryCardsPanel(), BorderLayout.NORTH);
        mainLayout.add(createTableLaporanPanel(), BorderLayout.CENTER);

        contentArea.add(mainLayout, BorderLayout.CENTER);
        return contentArea;
    }

    private JPanel createSummaryCardsPanel() {
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        cardsPanel.setBackground(COLOR_BACKGROUND);

        lblTotalPendapatan = new JLabel("Rp 0");
        cardsPanel.add(createKPIComponent("Total Pendapatan", lblTotalPendapatan, new Color(219, 234, 254)));

        lblTotalTransaksi = new JLabel("0");
        cardsPanel.add(createKPIComponent("Total Transaksi", lblTotalTransaksi, new Color(241, 245, 249)));

        lblRataRataTransaksi = new JLabel("Rp 0");
        cardsPanel.add(createKPIComponent("Rata-rata Keranjang", lblRataRataTransaksi, new Color(241, 245, 249)));

        return cardsPanel;
    }

    private JPanel createKPIComponent(String title, JLabel valueLabel, Color bgCard) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(COLOR_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            new EmptyBorder(15, 20, 15, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        titleLabel.setForeground(COLOR_TEXT_MUTED);
        
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        valueLabel.setForeground(Color.BLACK);

        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(valueLabel);
        
        return card;
    }

    private JPanel createTableLaporanPanel() {
        JPanel tablePanel = new JPanel(new BorderLayout(0, 15));
        tablePanel.setBackground(COLOR_WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(COLOR_WHITE);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setBackground(COLOR_WHITE);

        txtSearch = new JTextField(15);
        txtSearch.setPreferredSize(new Dimension(150, 30));
        
        comboMetodeFilter = new JComboBox<>(new String[]{"Semua Metode", "Tunai", "Debit", "Kredit", "QRIS"});
        comboMetodeFilter.setPreferredSize(new Dimension(120, 30));

        btnCari = new JButton("Filter & Cari");
        stylePrimaryButton(btnCari);
        
        btnRefresh = new JButton("Reset");
        styleSecondaryButton(btnRefresh);

        filterPanel.add(new JLabel("ID Transaksi:"));
        filterPanel.add(txtSearch);
        filterPanel.add(new JLabel("Metode:"));
        filterPanel.add(comboMetodeFilter);
        filterPanel.add(btnCari);
        filterPanel.add(btnRefresh);

        topBar.add(filterPanel, BorderLayout.WEST);
        tablePanel.add(topBar, BorderLayout.NORTH);

        String[] columns = {"ID Transaksi", "Total Bayar", "Tunai", "Kembalian", "Metode Pembayaran", "Diskon", "Pajak"};
        tableModelLaporan = new DefaultTableModel(new Object[][]{}, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        
        tableLaporan = new JTable(tableModelLaporan);
        tableLaporan.setRowHeight(30);
        tableLaporan.setFillsViewportHeight(true);
        tableLaporan.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableLaporan.getTableHeader().setBackground(new Color(226, 232, 240));
        tableLaporan.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(tableLaporan);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        return tablePanel;
    }

    private void initEventHandlers() {
        btnCari.addActionListener(e -> muatDataLaporan());
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            comboMetodeFilter.setSelectedIndex(0);
            muatDataLaporan();
        });
    }

    private void muatDataLaporan() {
        tableModelLaporan.setRowCount(0);
        
        String keyword = txtSearch.getText().trim();
        String filterMetode = comboMetodeFilter.getSelectedItem().toString();

        long totalPendapatan = 0;
        int totalTransaksi = 0;

        try (Connection conn = KoneksiDatabase.getConnection()) {

            StringBuilder sql = new StringBuilder("SELECT ID_Transaksi, Total_Bayar, Tunai, Kembalian, metode_pembayaran, diskon, pajak FROM Pesanan WHERE 1=1");
            
            if (!keyword.isEmpty()) {
                sql.append(" AND ID_Transaksi LIKE ?");
            }
            if (!filterMetode.equals("Semua Metode")) {
                sql.append(" AND metode_pembayaran = ?");
            }

            sql.append(" ORDER BY ID_Transaksi DESC");

            try (PreparedStatement pst = conn.prepareStatement(sql.toString())) {
                int paramIndex = 1;
                if (!keyword.isEmpty()) {
                    pst.setString(paramIndex++, "%" + keyword + "%");
                }
                if (!filterMetode.equals("Semua Metode")) {
                    pst.setString(paramIndex++, filterMetode);
                }

                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        double totalBayar = rs.getDouble("Total_Bayar");

                        totalPendapatan += (long) totalBayar;
                        totalTransaksi++;

                        tableModelLaporan.addRow(new Object[]{
                            rs.getString("ID_Transaksi"),
                            formatRupiah((long) totalBayar),
                            formatRupiah((long) rs.getDouble("Tunai")),
                            formatRupiah((long) rs.getDouble("Kembalian")),
                            rs.getString("metode_pembayaran"),
                            formatRupiah((long) rs.getDouble("diskon")),
                            formatRupiah((long) rs.getDouble("pajak"))
                        });
                    }
                }
            }

            lblTotalPendapatan.setText(formatRupiah(totalPendapatan));
            lblTotalTransaksi.setText(String.valueOf(totalTransaksi));
            
            long rataRata = totalTransaksi == 0 ? 0 : totalPendapatan / totalTransaksi;
            lblRataRataTransaksi.setText(formatRupiah(rataRata));

        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Driver Database Tidak Ditemukan: " + e.getMessage());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal mengambil data laporan: " + e.getMessage());
        }
    }

    private String formatRupiah(long value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        DecimalFormat df = new DecimalFormat("#,###", symbols);
        return "Rp " + df.format(value);
    }

    private void stylePrimaryButton(JButton button) {
        button.setBackground(COLOR_PRIMARY);
        button.setForeground(COLOR_WHITE);
        button.setPreferredSize(new Dimension(100, 30));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
    }

    private void styleSecondaryButton(JButton button) {
        button.setBackground(new Color(226, 232, 240));
        button.setForeground(new Color(30, 41, 59));
        button.setPreferredSize(new Dimension(80, 30));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
    }
}
