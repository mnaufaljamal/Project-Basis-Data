import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class PanelTransaksi extends JPanel {

    private final Color COLOR_PRIMARY = new Color(37, 99, 235);
    private final Color COLOR_BACKGROUND = new Color(243, 244, 246);
    private final Color COLOR_WHITE = Color.WHITE;

    private DefaultTableModel transaksiTableModel;
    private JTable transaksiTable;
    private JLabel lblTotalItem, lblGrandTotal;
    private JLabel lblSubtotal, lblTotalDiskon, lblPajak;
    private JTextField txtCustomerSearch, txtCustomerName, txtPhone;
    private JComboBox<String> comboPayment;
    private JTextField txtJumlahBayar, txtKembalian, txtDiskon;
    private JButton btnTambahPesanan, btnProses, btnBatalTrx;
    private String lastTransactionError = "";
    private String selectedKasirId = "";

    public PanelTransaksi() {
        setLayout(new BorderLayout());
        setBackground(COLOR_BACKGROUND);
        
        add(createHeader(), BorderLayout.NORTH);
        add(createContentArea(), BorderLayout.CENTER);

        btnProses.addActionListener(e -> processPaymentFromPanel());
        btnBatalTrx.addActionListener(e -> {
            if (transaksiTableModel != null) transaksiTableModel.setRowCount(0);
            resetForm();
        });
        btnTambahPesanan.addActionListener(e -> showProductSearchDialog());
    }


    private JPanel createHeader() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        header.setBackground(COLOR_WHITE);
        header.setPreferredSize(new Dimension(0, 8));
        return header;
    }

    private JPanel createContentArea() {
        JPanel contentArea = new JPanel(new BorderLayout(20, 12));
        contentArea.setBorder(new EmptyBorder(8, 20, 20, 20));
        contentArea.setBackground(COLOR_BACKGROUND);

        JLabel pageTitle = new JLabel("Input Pesanan Transaksi Baru");
        pageTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        contentArea.add(pageTitle, BorderLayout.NORTH);

        JPanel splitPanel = new JPanel(new BorderLayout(20, 0));
        splitPanel.setBackground(COLOR_BACKGROUND);
        splitPanel.add(createTransaksiTablePanel(), BorderLayout.CENTER);
        splitPanel.add(createTransaksiDetailPanel(), BorderLayout.EAST);
        contentArea.add(splitPanel, BorderLayout.CENTER);

        return contentArea;
    }

    private JPanel createTransaksiTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout(0, 15));
        tablePanel.setBackground(COLOR_WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(COLOR_WHITE);
        
        btnTambahPesanan = new JButton("Tambah Pesanan Baru");
        btnTambahPesanan.setBackground(COLOR_PRIMARY);
        btnTambahPesanan.setForeground(COLOR_WHITE);
        btnTambahPesanan.setContentAreaFilled(true);
        btnTambahPesanan.setBorderPainted(false);
        topBar.add(btnTambahPesanan, BorderLayout.WEST);

        JPanel rightTopBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightTopBar.setBackground(COLOR_WHITE);
        rightTopBar.add(new JLabel("Total Item"));
        lblTotalItem = new JLabel("0");
        lblTotalItem.setFont(new Font("SansSerif", Font.BOLD, 14));
        rightTopBar.add(lblTotalItem);
        topBar.add(rightTopBar, BorderLayout.EAST);
        tablePanel.add(topBar, BorderLayout.NORTH);

        String[] columns = {"No", "Kode/Barcode", "Nama Produk", "Qty", "Harga Satuan", "Diskon", "Subtotal", "Aksi"};
        transaksiTableModel = new DefaultTableModel(new Object[][]{}, columns) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        transaksiTable = new JTable(transaksiTableModel);
        transaksiTable.setRowHeight(30);
        transaksiTable.getTableHeader().setBackground(new Color(226, 232, 240));
        transaksiTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        transaksiTable.removeColumn(transaksiTable.getColumnModel().getColumn(5));

        transaksiTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                int viewCol = transaksiTable.columnAtPoint(me.getPoint());
                int row = transaksiTable.rowAtPoint(me.getPoint());
                if (viewCol == -1 || row == -1) {
                    return;
                }

                int modelCol = transaksiTable.convertColumnIndexToModel(viewCol);
                if (modelCol == 7) {
                    transaksiTableModel.removeRow(row);
                    for (int i = 0; i < transaksiTableModel.getRowCount(); i++) {
                        transaksiTableModel.setValueAt(String.valueOf(i + 1), i, 0);
                    }
                    computeTotals();
                }
            }
        });

        tablePanel.add(new JScrollPane(transaksiTable), BorderLayout.CENTER);
        return tablePanel;
    }

    private JPanel createTransaksiDetailPanel() {
        JPanel rightContainer = new JPanel();
        rightContainer.setLayout(new BoxLayout(rightContainer, BoxLayout.Y_AXIS));
        rightContainer.setBackground(COLOR_BACKGROUND);
        rightContainer.setPreferredSize(new Dimension(300, 0));

        JLabel titleLabel = new JLabel("Detail Pembayaran");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightContainer.add(titleLabel);
        rightContainer.add(Box.createRigidArea(new Dimension(0, 15)));

        rightContainer.add(createPaymentPanel());
        rightContainer.add(Box.createRigidArea(new Dimension(0, 15)));
        rightContainer.add(createSummaryPanel());
        rightContainer.add(Box.createVerticalGlue());

        return rightContainer;
    }

    private JPanel createCustomerPanel() {
        JPanel customerPanel = new JPanel();
        customerPanel.setLayout(new BoxLayout(customerPanel, BoxLayout.Y_AXIS));
        customerPanel.setBackground(COLOR_WHITE);
        customerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        customerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel customerTitle = new JLabel("Pencarian Pelanggan");
        customerTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        customerTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        customerPanel.add(customerTitle);
        customerPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        txtCustomerSearch = new JTextField();
        txtCustomerSearch.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtCustomerSearch.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        customerPanel.add(txtCustomerSearch);
        customerPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        customerPanel.add(makeLabel("Nama Pelanggan (Opsional)"));
        txtCustomerName = new JTextField();
        txtCustomerName.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtCustomerName.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        customerPanel.add(txtCustomerName);
        customerPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        customerPanel.add(makeLabel("No. Telepon"));
        txtPhone = new JTextField();
        txtPhone.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtPhone.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        customerPanel.add(txtPhone);

        return customerPanel;
    }

    private JPanel createPaymentPanel() {
        JPanel paymentPanel = new JPanel();
        paymentPanel.setLayout(new BoxLayout(paymentPanel, BoxLayout.Y_AXIS));
        paymentPanel.setBackground(COLOR_WHITE);
        paymentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        paymentPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel paymentTitle = new JLabel("Metode Pembayaran");
        paymentTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        paymentTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        paymentPanel.add(paymentTitle);
        paymentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        comboPayment = new JComboBox<>(new String[]{"Tunai", "Debit", "Kredit", "QRIS"});
        comboPayment.setAlignmentX(Component.LEFT_ALIGNMENT);
        comboPayment.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        paymentPanel.add(comboPayment);
        paymentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        paymentPanel.add(makeLabel("Jumlah Bayar"));
        txtJumlahBayar = new JTextField();
        txtJumlahBayar.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtJumlahBayar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        txtJumlahBayar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateKembalianPreview(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateKembalianPreview(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateKembalianPreview(); }
        });
        paymentPanel.add(txtJumlahBayar);
        paymentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        paymentPanel.add(makeLabel("Kembalian"));
        txtKembalian = new JTextField();
        txtKembalian.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtKembalian.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        txtKembalian.setEditable(false);
        paymentPanel.add(txtKembalian);

        return paymentPanel;
    }
    

    private JPanel createSummaryPanel() {
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setBackground(COLOR_WHITE);
        summaryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        summaryPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));

        summaryPanel.add(makeLabel("Diskon Transaksi"));
        txtDiskon = new JTextField("0");
        txtDiskon.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtDiskon.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        txtDiskon.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { computeTotals(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { computeTotals(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { computeTotals(); }
        });
        summaryPanel.add(txtDiskon);
        summaryPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        JPanel summaryDetails = new JPanel(new GridLayout(4, 2, 10, 10));
        summaryDetails.setBackground(COLOR_WHITE);
        summaryDetails.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        summaryDetails.add(new JLabel("Subtotal"));
        lblSubtotal = new JLabel("Rp 0");
        summaryDetails.add(lblSubtotal);

        summaryDetails.add(new JLabel("Total Diskon"));
        lblTotalDiskon = new JLabel("Rp 0");
        summaryDetails.add(lblTotalDiskon);

        summaryDetails.add(new JLabel("Pajak (PPN 11%)"));
        lblPajak = new JLabel("Rp 0");
        summaryDetails.add(lblPajak);

        summaryDetails.add(new JLabel("Grand Total"));
        lblGrandTotal = new JLabel("Rp 0");
        lblGrandTotal.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblGrandTotal.setForeground(COLOR_PRIMARY);
        summaryDetails.add(lblGrandTotal);
        summaryPanel.add(summaryDetails);
        summaryPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.setBackground(COLOR_WHITE);
        actionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnProses = new JButton("Proses Transaksi") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnProses.setBackground(COLOR_PRIMARY);
        btnProses.setForeground(COLOR_WHITE);
        btnProses.setContentAreaFilled(true);
        btnProses.setBorderPainted(false);
        btnProses.setFocusPainted(false);
        btnProses.setBorder(new EmptyBorder(8, 12, 8, 12));
        btnProses.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnProses.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnBatalTrx = new JButton("Batal Transaksi") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnBatalTrx.setBackground(new Color(220, 38, 38));
        btnBatalTrx.setForeground(COLOR_WHITE);
        btnBatalTrx.setFocusPainted(false);
        btnBatalTrx.setContentAreaFilled(true);
        btnBatalTrx.setBorderPainted(false);
        btnBatalTrx.setBorder(new EmptyBorder(8, 12, 8, 12));
        btnBatalTrx.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnBatalTrx.setAlignmentX(Component.CENTER_ALIGNMENT);

        actionPanel.add(btnProses);
        actionPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        actionPanel.add(btnBatalTrx);
        actionPanel.setMaximumSize(new Dimension(260, 96));
        summaryPanel.add(actionPanel);

        return summaryPanel;
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    public JLabel getLblTotalItem() { return lblTotalItem; }
    public JLabel getLblGrandTotal() { return lblGrandTotal; }
    
    public JTable getTransaksiTable() { return transaksiTable; }
    public DefaultTableModel getTransaksiTableModel() { return transaksiTableModel; }
    
    public JTextField getTxtCustomerSearch() { return txtCustomerSearch; }
    public JTextField getTxtCustomerName() { return txtCustomerName; }
    public JTextField getTxtPhone() { return txtPhone; }
    
    public JComboBox<String> getComboPayment() { return comboPayment; }
    public JTextField getTxtJumlahBayar() { return txtJumlahBayar; }
    public JTextField getTxtKembalian() { return txtKembalian; }
    
    public JButton getBtnTambahPesanan() { return btnTambahPesanan; }
    public JButton getBtnProses() { return btnProses; }
    public JButton getBtnBatalTrx() { return btnBatalTrx; }
    
    public void setTotalItem(int total) {
        lblTotalItem.setText(String.valueOf(total));
    }
    
    public void setGrandTotal(String total) {
        lblGrandTotal.setText(total);
    }
    
    public void resetForm() {
        if (txtCustomerSearch != null) txtCustomerSearch.setText("");
        if (txtCustomerName != null) txtCustomerName.setText("");
        if (txtPhone != null) txtPhone.setText("");
        comboPayment.setSelectedIndex(0);
        txtJumlahBayar.setText("");
        txtKembalian.setText("");
        if (txtDiskon != null) txtDiskon.setText("0");
        lblTotalItem.setText("0");
        if (lblSubtotal != null) lblSubtotal.setText("Rp 0");
        if (lblTotalDiskon != null) lblTotalDiskon.setText("Rp 0");
        if (lblPajak != null) lblPajak.setText("Rp 0");
        lblGrandTotal.setText("Rp 0");
    }

    private long parseRupiah(String s) {
        if (s == null) return 0;
        String value = s.trim();
        if (value.isEmpty()) return 0;

        if (value.toLowerCase().contains("rp")) {
            String digits = value.replaceAll("[^0-9]", "");
            if (digits.isEmpty()) return 0;
            try { return Long.parseLong(digits); } catch (NumberFormatException e) { return 0; }
        }

        try {
            if (value.matches(".*\\.\\d{1,2}$")) {
                return Math.round(Double.parseDouble(value.replace(",", "")));
            }
            String digits = value.replaceAll("[^0-9]", "");
            if (digits.isEmpty()) return 0;
            return Long.parseLong(digits);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String formatRupiah(long value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        DecimalFormat df = new DecimalFormat("#,###", symbols);
        return "Rp " + df.format(value);
    }

    // compute subtotal (sum harga*qty) from table
    private long subtotalBeforeDiscount() {
        long subtotal = 0;
        for (int i = 0; i < transaksiTableModel.getRowCount(); i++) {
            int qty = 0; try { qty = Integer.parseInt(transaksiTableModel.getValueAt(i, 3).toString()); } catch(Exception e) { qty = 0; }
            long harga = parseRupiah(transaksiTableModel.getValueAt(i, 4).toString());
            subtotal += harga * qty;
        }
        return subtotal;
    }

    // compute total diskon (sum diskon column) from table
    private long totalDiskonValue() {
        return parseRupiah(txtDiskon == null ? "0" : txtDiskon.getText());
    }

    public void setSelectedKasirId(String idKasir) {
        selectedKasirId = idKasir == null ? "" : idKasir;
    }

    private String getSelectedKasirId() {
        return selectedKasirId;
    }

    private boolean saveTransactionToDatabase(long grand, long bayar, long kembalian, String metode, long subtotalBefore, long totalDiskon) {
        lastTransactionError = "";
        String idTransaksi = null;
        long taxable = Math.max(0, subtotalBefore - totalDiskon);
        long pajak = Math.round(taxable * 0.11);
        Connection c = null;

        try {
            c = KoneksiDatabase.getConnection();
            c.setAutoCommit(false);

            try {
                long seq = getNextSequenceValue(c);
                idTransaksi = String.format("TRX%06d", seq);
            } catch (SQLException seqEx) {
                idTransaksi = "TRX" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            }

            String idKasir = getSelectedKasirId();
            Object idToko = firstValue(c, "Toko", "ID_Toko");

            if (idKasir.isEmpty()) {
                throw new SQLException("Pilih kasir terlebih dahulu. Jika daftar kasir kosong, tambahkan data di tabel Kasir.");
            }
            if (idToko == null) {
                throw new SQLException("Data toko belum ada di tabel Toko.");
            }

            boolean adaMetodePembayaran = columnExists(c, "Pesanan", "metode_pembayaran");
            boolean adaDiskon = columnExists(c, "Pesanan", "diskon");
            boolean adaPajak = columnExists(c, "Pesanan", "pajak");

            StringBuilder kolomPesanan = new StringBuilder("ID_Transaksi, Total_Bayar, Tunai, Kembalian, ID_Kasir, ID_Toko");
            StringBuilder valuePesanan = new StringBuilder("?,?,?,?,?,?");
            if (adaMetodePembayaran) {
                kolomPesanan.append(", metode_pembayaran");
                valuePesanan.append(",?");
            }
            if (adaDiskon) {
                kolomPesanan.append(", diskon");
                valuePesanan.append(",?");
            }
            if (adaPajak) {
                kolomPesanan.append(", pajak");
                valuePesanan.append(",?");
            }

            String insertPesanan = "INSERT INTO Pesanan (" + kolomPesanan + ") VALUES (" + valuePesanan + ")";
            try (PreparedStatement pst = c.prepareStatement(insertPesanan)) {
                int index = 1;
                pst.setString(index++, idTransaksi);
                pst.setDouble(index++, (double) grand);
                pst.setDouble(index++, (double) bayar);
                pst.setDouble(index++, (double) kembalian);
                pst.setObject(index++, idKasir);
                pst.setObject(index++, idToko);
                if (adaMetodePembayaran) pst.setString(index++, metode);
                if (adaDiskon) pst.setDouble(index++, (double) totalDiskon);
                if (adaPajak) pst.setDouble(index++, (double) pajak);
                pst.executeUpdate();
            }

            String insertDetail = "INSERT INTO Detail_Pesanan (ID_Transaksi, ID_Barang, Kuantitas, Subtotal) VALUES (?,?,?,?)";
            String updateStok = "UPDATE Barang SET Stok = Stok - ? WHERE ID_Barang = ? AND Stok >= ?";
            try (PreparedStatement pstDet = c.prepareStatement(insertDetail);
                 PreparedStatement pstStok = c.prepareStatement(updateStok)) {
                for (int i = 0; i < transaksiTableModel.getRowCount(); i++) {
                    String idBarang = transaksiTableModel.getValueAt(i, 1).toString();
                    int qty = 0; try { qty = Integer.parseInt(transaksiTableModel.getValueAt(i, 3).toString()); } catch(Exception e) { qty = 0; }
                    long sub = 0; try { sub = parseRupiah(transaksiTableModel.getValueAt(i, 6).toString()); } catch(Exception e) { sub = 0; }

                    pstStok.setInt(1, qty);
                    pstStok.setString(2, idBarang);
                    pstStok.setInt(3, qty);
                    int updatedRows = pstStok.executeUpdate();
                    if (updatedRows == 0) {
                        throw new SQLException("Stok barang " + idBarang + " tidak cukup atau barang tidak ditemukan.");
                    }

                    pstDet.setString(1, idTransaksi);
                    pstDet.setString(2, idBarang);
                    pstDet.setInt(3, qty);
                    pstDet.setDouble(4, (double) sub);
                    pstDet.addBatch();
                }
                pstDet.executeBatch();
            }

            c.commit();
            return true;
        } catch (Exception ex) {
            if (c != null) {
                try { c.rollback(); } catch (SQLException rollbackEx) { rollbackEx.printStackTrace(); }
            }
            lastTransactionError = ex.getMessage();
            ex.printStackTrace();
            return false;
        } finally {
            if (c != null) {
                try {
                    c.setAutoCommit(true);
                    c.close();
                } catch (SQLException closeEx) {
                    closeEx.printStackTrace();
                }
            }
        }
    }

    private long getNextSequenceValue(Connection c) throws SQLException {
        // Try to get next value; if sequence does not exist, create it then get value
        String nextSql = "SELECT NEXT VALUE FOR seqPesanan AS seq";
        try (PreparedStatement pst = c.prepareStatement(nextSql)) {
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getLong("seq");
            }
        } catch (SQLException ex) {
            // create sequence and retry
            try (Statement st = c.createStatement()) {
                st.execute("CREATE SEQUENCE seqPesanan START WITH 1 INCREMENT BY 1;");
            }
            try (PreparedStatement pst2 = c.prepareStatement(nextSql)) {
                try (ResultSet rs2 = pst2.executeQuery()) {
                    if (rs2.next()) return rs2.getLong("seq");
                }
            }
        }
        throw new SQLException("Unable to obtain sequence value for seqPesanan");
    }

    private Object firstValue(Connection c, String tableName, String columnName) throws SQLException {
        String sql = "SELECT TOP 1 " + columnName + " FROM " + tableName + " ORDER BY " + columnName;
        try (PreparedStatement pst = c.prepareStatement(sql)) {
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() ? rs.getObject(columnName) : null;
            }
        }
    }

    private boolean columnExists(Connection c, String tableName, String columnName) throws SQLException {
        String sql = "SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ? AND COLUMN_NAME = ?";
        try (PreparedStatement pst = c.prepareStatement(sql)) {
            pst.setString(1, tableName);
            pst.setString(2, columnName);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void processPaymentFromPanel() {
        if (transaksiTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Belum ada barang di transaksi.");
            return;
        }
        if (getSelectedKasirId().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih kasir terlebih dahulu. Jika daftar kasir kosong, tambahkan data di tabel Kasir.");
            return;
        }

        long subtotal = subtotalBeforeDiscount();
        long totalDiskon = Math.min(totalDiskonValue(), subtotal);
        long taxable = Math.max(0, subtotal - totalDiskon);
        long pajak = Math.round(taxable * 0.11);
        long grand = taxable + pajak;

        // update labels
        lblSubtotal.setText(formatRupiah(subtotal));
        lblTotalDiskon.setText(formatRupiah(totalDiskon));
        lblPajak.setText(formatRupiah(pajak));
        lblGrandTotal.setText(formatRupiah(grand));

        String bayarStr = txtJumlahBayar.getText().replaceAll("[^0-9]", "");
        long bayar = 0; if (!bayarStr.isEmpty()) try { bayar = Long.parseLong(bayarStr); } catch(Exception e) { bayar = 0; }

        if (bayar < grand) {
            JOptionPane.showMessageDialog(this, "Jumlah bayar kurang!");
            return;
        }

        long kembalian = bayar - grand;

        boolean saved = saveTransactionToDatabase(grand, bayar, kembalian, comboPayment.getSelectedItem().toString(), subtotal, totalDiskon);
        if (saved) {
            txtKembalian.setText(formatRupiah(kembalian));
            JOptionPane.showMessageDialog(this, "Pembayaran berhasil. Kembalian: " + formatRupiah(kembalian));
            transaksiTableModel.setRowCount(0);
            resetForm();
        } else {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan transaksi ke database:\n" + lastTransactionError);
        }
    }

    private void showPaymentDialog() {
        long grand = parseRupiah(lblGrandTotal.getText());

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Pembayaran", Dialog.ModalityType.APPLICATION_MODAL);
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(12,12,12,12));
        p.setBackground(COLOR_WHITE);

        JLabel lbl = new JLabel("Total: " + formatRupiah(grand));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lbl);
        p.add(Box.createRigidArea(new Dimension(0,10)));

        p.add(new JLabel("Metode Pembayaran"));
        JComboBox<String> cb = new JComboBox<>(new String[]{"Tunai","Debit","Kredit","QRIS"});
        cb.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(cb);
        p.add(Box.createRigidArea(new Dimension(0,10)));

        p.add(new JLabel("Jumlah Bayar"));
        JTextField txtBayar = new JTextField();
        txtBayar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        p.add(txtBayar);
        p.add(Box.createRigidArea(new Dimension(0,10)));

        JLabel lblKembalian = new JLabel("Kembalian: Rp 0");
        lblKembalian.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lblKembalian);
        p.add(Box.createRigidArea(new Dimension(0,12)));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setBackground(COLOR_WHITE);
        JButton ok = new JButton("Bayar");
        ok.setBackground(COLOR_PRIMARY);
        ok.setForeground(COLOR_WHITE);
        JButton cancel = new JButton("Batal");
        cancel.setBackground(new Color(220,38,38));
        cancel.setForeground(COLOR_WHITE);
        actions.add(ok);
        actions.add(cancel);
        p.add(actions);

        txtBayar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
            public void insertUpdate(javax.swing.event.DocumentEvent e){ update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e){ update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e){ update(); }
            private void update(){
                String val = txtBayar.getText().replaceAll("[^0-9]", "");
                long bayar = 0;
                if (!val.isEmpty()){
                    try{ bayar = Long.parseLong(val); } catch(Exception ex){ bayar = 0; }
                }
                long k = bayar - grand;
                lblKembalian.setText("Kembalian: " + (k < 0 ? "Rp 0" : formatRupiah(k)));
            }
        });

        ok.addActionListener(e -> {
            String val = txtBayar.getText().replaceAll("[^0-9]", "");
            long bayar = 0;
            if (!val.isEmpty()) try{ bayar = Long.parseLong(val); } catch(Exception ex){ bayar = 0; }
            if (bayar < grand) {
                JOptionPane.showMessageDialog(dialog, "Jumlah bayar kurang!");
                return;
            }
            long k = bayar - grand;
            // try save to database
            boolean saved = saveTransactionToDatabase(grand, bayar, k, cb.getSelectedItem().toString(), subtotalBeforeDiscount(), totalDiskonValue());
            if (saved) {
                JOptionPane.showMessageDialog(dialog, "Pembayaran berhasil. Kembalian: " + formatRupiah(k));
                // clear transaksi
                transaksiTableModel.setRowCount(0);
                resetForm();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Pembayaran gagal disimpan ke database.");
            }
        });

        cancel.addActionListener(e -> dialog.dispose());

        dialog.setContentPane(p);
        dialog.pack();
        dialog.setSize(360, 260);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showProductSearchDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Pilih Produk", Dialog.ModalityType.APPLICATION_MODAL);
        JPanel p = new JPanel(new BorderLayout(8,8));
        p.setBorder(new EmptyBorder(12,12,12,12));

        JPanel top = new JPanel(new BorderLayout(6,6));
        top.setBackground(COLOR_WHITE);
        JTextField search = new JTextField();
        JButton btnSearch = new JButton("Cari");
        top.add(search, BorderLayout.CENTER);
        top.add(btnSearch, BorderLayout.EAST);

        String[] cols = {"ID_Barang","Nama_Barang","Kategori","Harga_Satuan","Stok"};
        DefaultTableModel prodModel = new DefaultTableModel(new Object[][]{}, cols) {
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        JTable prodTable = new JTable(prodModel);
        prodTable.setRowHeight(26);

        // load initially
        loadProductsToModel(prodModel, "");

        btnSearch.addActionListener(e -> loadProductsToModel(prodModel, search.getText()));

        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(prodTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(new JLabel("Jumlah"));
        JSpinner spinJumlah = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        spinJumlah.setPreferredSize(new Dimension(70, 28));
        actions.add(spinJumlah);

        JButton tambah = new JButton("Tambah");
        JButton batal = new JButton("Batal");
        actions.add(tambah);
        actions.add(batal);
        p.add(actions, BorderLayout.SOUTH);

        tambah.addActionListener(e -> {
            int r = prodTable.getSelectedRow();
            if (r == -1) { JOptionPane.showMessageDialog(dialog, "Pilih produk terlebih dahulu"); return; }
            String id = prodModel.getValueAt(r, 0).toString();
            String name = prodModel.getValueAt(r, 1).toString();
            String priceStr = prodModel.getValueAt(r, 3).toString();
            int stok = 0;
            try { stok = Integer.parseInt(prodModel.getValueAt(r, 4).toString()); } catch (Exception ex) { stok = 0; }
            long price = parseRupiah(priceStr);
            int qty = (Integer) spinJumlah.getValue();
            if (stok <= 0) {
                JOptionPane.showMessageDialog(dialog, "Stok barang habis.");
                return;
            }
            if (qty > stok) {
                JOptionPane.showMessageDialog(dialog, "Jumlah melebihi stok. Stok tersedia: " + stok);
                return;
            }
            String hargaDisplay = formatRupiah(price);
            String subtotal = formatRupiah(price * qty);
            String aksi = "Hapus";

            for (int i = 0; i < transaksiTableModel.getRowCount(); i++) {
                String existingId = transaksiTableModel.getValueAt(i, 1).toString();
                if (existingId.equals(id)) {
                    int oldQty = Integer.parseInt(transaksiTableModel.getValueAt(i, 3).toString());
                    int newQty = oldQty + qty;
                    if (newQty > stok) {
                        JOptionPane.showMessageDialog(dialog, "Total jumlah melebihi stok. Stok tersedia: " + stok);
                        return;
                    }
                    transaksiTableModel.setValueAt(String.valueOf(newQty), i, 3);
                    transaksiTableModel.setValueAt(formatRupiah(price * newQty), i, 6);
                    computeTotals();
                    dialog.dispose();
                    return;
                }
            }

            int no = transaksiTableModel.getRowCount() + 1;
            transaksiTableModel.addRow(new Object[]{String.valueOf(no), id, name, String.valueOf(qty), hargaDisplay, "0", subtotal, aksi});
            computeTotals();
            dialog.dispose();
        });

        prodTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && prodTable.getSelectedRow() != -1) {
                    tambah.doClick();
                }
            }
        });

        batal.addActionListener(e -> dialog.dispose());

        dialog.setContentPane(p);
        dialog.setSize(720, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void loadProductsToModel(DefaultTableModel model, String keyword) {
        model.setRowCount(0);
        try (Connection c = KoneksiDatabase.getConnection()) {
            String sql = "SELECT ID_Barang, Nama_Barang, Kategori, Harga_Satuan, Stok FROM Barang" + (keyword == null || keyword.isEmpty() ? "" : " WHERE Nama_Barang LIKE ?");
            try (PreparedStatement pst = c.prepareStatement(sql)) {
                if (keyword != null && !keyword.isEmpty()) pst.setString(1, "%" + keyword + "%");
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        model.addRow(new Object[]{rs.getString("ID_Barang"), rs.getString("Nama_Barang"), rs.getString("Kategori"), rs.getString("Harga_Satuan"), rs.getString("Stok")});
                    }
                }
            }
        } catch (ClassNotFoundException cnf) {
            JOptionPane.showMessageDialog(this, "Driver JDBC tidak ditemukan: " + cnf.getMessage());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat produk: " + ex.getMessage());
        }
    }

    private void computeTotals() {
        int totalItems = 0;
        long subtotalBeforeDiscount = 0;
        long totalDiskon = 0;

        for (int i = 0; i < transaksiTableModel.getRowCount(); i++) {
            int qty = 0; try { qty = Integer.parseInt(transaksiTableModel.getValueAt(i, 3).toString()); } catch(Exception e) { qty = 0; }
            long harga = parseRupiah(transaksiTableModel.getValueAt(i, 4).toString());

            long rowSubtotal = harga * qty;
            transaksiTableModel.setValueAt(formatRupiah(rowSubtotal), i, 6);

            totalItems += qty;
            subtotalBeforeDiscount += harga * qty;
        }

        totalDiskon = Math.min(totalDiskonValue(), subtotalBeforeDiscount);
        long taxable = Math.max(0, subtotalBeforeDiscount - totalDiskon);
        long pajak = Math.round(taxable * 0.11);
        long grand = taxable + pajak;

        setTotalItem(totalItems);
        lblSubtotal.setText(formatRupiah(subtotalBeforeDiscount));
        lblTotalDiskon.setText(formatRupiah(totalDiskon));
        lblPajak.setText(formatRupiah(pajak));
        setGrandTotal(formatRupiah(grand));
        updateKembalianPreview();
    }

    private void updateKembalianPreview() {
        if (txtJumlahBayar == null || txtKembalian == null || lblGrandTotal == null) {
            return;
        }

        long grand = parseRupiah(lblGrandTotal.getText());
        long bayar = parseRupiah(txtJumlahBayar.getText());
        long kembalian = bayar - grand;
        txtKembalian.setText(kembalian > 0 ? formatRupiah(kembalian) : "Rp 0");
    }

    
}
