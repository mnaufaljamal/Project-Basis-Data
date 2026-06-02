import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
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

    private PanelGrafik panelGrafikBatang;

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
        JPanel contentArea = new JPanel(new BorderLayout(0, 15));
        contentArea.setBorder(new EmptyBorder(8, 20, 20, 20));
        contentArea.setBackground(COLOR_BACKGROUND);

        JLabel pageTitle = new JLabel("Laporan & Visualisasi Analitik Penjualan");
        pageTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        contentArea.add(pageTitle, BorderLayout.NORTH);

        JPanel mainLayout = new JPanel();
        mainLayout.setLayout(new BoxLayout(mainLayout, BoxLayout.Y_AXIS));
        mainLayout.setBackground(COLOR_BACKGROUND);

        mainLayout.add(createSummaryCardsPanel());
        mainLayout.add(Box.createRigidArea(new Dimension(0, 15)));

        panelGrafikBatang = new PanelGrafik();
        mainLayout.add(panelGrafikBatang);
        mainLayout.add(Box.createRigidArea(new Dimension(0, 15)));

        mainLayout.add(createTableLaporanPanel());

        JScrollPane scrollPage = new JScrollPane(mainLayout);
        scrollPage.setBorder(null);
        scrollPage.getVerticalScrollBar().setUnitIncrement(16);
        contentArea.add(scrollPage, BorderLayout.CENTER);

        return contentArea;
    }

    private JPanel createSummaryCardsPanel() {
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        cardsPanel.setBackground(COLOR_BACKGROUND);
        cardsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        lblTotalPendapatan = new JLabel("Rp 0");
        cardsPanel.add(createKPIComponent("Total Pendapatan", lblTotalPendapatan));

        lblTotalTransaksi = new JLabel("0");
        cardsPanel.add(createKPIComponent("Total Transaksi", lblTotalTransaksi));

        lblRataRataTransaksi = new JLabel("Rp 0");
        cardsPanel.add(createKPIComponent("Rata-rata Keranjang", lblRataRataTransaksi));

        return cardsPanel;
    }

    private JPanel createKPIComponent(String title, JLabel valueLabel) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(COLOR_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            new EmptyBorder(12, 20, 12, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        titleLabel.setForeground(COLOR_TEXT_MUTED);
        
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        valueLabel.setForeground(Color.BLACK);

        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
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
        tablePanel.setPreferredSize(new Dimension(1000, 320));

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
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        tableLaporan = new JTable(tableModelLaporan);
        tableLaporan.setRowHeight(28);
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

        ArrayList<Double> daftarNilaiBayar = new ArrayList<>();
        ArrayList<String> daftarLabelId = new ArrayList<>();

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
                if (!keyword.isEmpty()) pst.setString(paramIndex++, "%" + keyword + "%");
                if (!filterMetode.equals("Semua Metode")) pst.setString(paramIndex++, filterMetode);

                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        double totalBayar = rs.getDouble("Total_Bayar");
                        String idTrx = rs.getString("ID_Transaksi");
                        
                        totalPendapatan += (long) totalBayar;
                        totalTransaksi++;

                        if (daftarNilaiBayar.size() < 8) {
                            daftarNilaiBayar.add(totalBayar);
                            daftarLabelId.add(idTrx);
                        }

                        tableModelLaporan.addRow(new Object[]{
                            idTrx,
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

            panelGrafikBatang.perbaruiData(daftarNilaiBayar, daftarLabelId);

        } catch (ClassNotFoundException | SQLException e) {
            JOptionPane.showMessageDialog(this, "Koneksi Error: " + e.getMessage());
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
        button.setPreferredSize(new Dimension(110, 30));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
    }

    private void styleSecondaryButton(JButton button) {
        button.setBackground(new Color(226, 232, 240));
        button.setForeground(new Color(30, 41, 59));
        button.setPreferredSize(new Dimension(80, 30));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
    }

    private class PanelGrafik extends JPanel {
        private ArrayList<Double> dataNilai = new ArrayList<>();
        private ArrayList<String> dataLabel = new ArrayList<>();

        public PanelGrafik() {
            setBackground(COLOR_WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(15, 15, 15, 15)
            ));

            setPreferredSize(new Dimension(1000, 220));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        }

        public void perbaruiData(ArrayList<Double> nilai, ArrayList<String> label) {
            this.dataNilai = nilai;
            this.dataLabel = label;
            repaint(); 
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int padding = 40;

            g2.setColor(Color.BLACK);
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.drawString("Grafik Komparasi Nilai Transaksi Terkini", 20, 25);

            if (dataNilai.isEmpty()) {
                g2.setColor(COLOR_TEXT_MUTED);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
                g2.drawString("Tidak ada data transaksi untuk divisualisasikan.", width / 2 - 120, height / 2 + 10);
                return;
            }

            double maxNilai = 0;
            for (double val : dataNilai) {
                if (val > maxNilai) maxNilai = val;
            }
            if (maxNilai == 0) maxNilai = 1;

            int graphHeight = height - (2 * padding) - 20;
            int graphWidth = width - (2 * padding);
            int barWidth = graphWidth / dataNilai.size() - 20;

            for (int i = 0; i < dataNilai.size(); i++) {
                double value = dataNilai.get(i);
                int barHeight = (int) ((value / maxNilai) * graphHeight);

                int x = padding + i * (barWidth + 20) + 10;
                int y = height - padding - barHeight;

                g2.setColor(COLOR_PRIMARY);
                g2.fillRect(x, y, barWidth, barHeight);

                g2.setColor(COLOR_PRIMARY.darker());
                g2.drawRect(x, y, barWidth, barHeight);

                g2.setColor(Color.BLACK);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
                String labelStr = dataLabel.get(i);
                g2.drawString(labelStr, x + (barWidth / 2) - (g2.getFontMetrics().stringWidth(labelStr) / 2), height - padding + 15);

                g2.setFont(new Font("SansSerif", Font.BOLD, 10));
                g2.setColor(COLOR_PRIMARY.darker());
                String valStr = "K" + (int)(value / 1000) + "k"; 
                g2.drawString(valStr, x + (barWidth / 2) - (g2.getFontMetrics().stringWidth(valStr) / 2), y - 5);
            }

            g2.setColor(new Color(203, 213, 225));
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(padding, height - padding, width - padding, height - padding);
        }
    }
}
