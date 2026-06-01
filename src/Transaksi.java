import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class Transaksi extends JPanel {

    private final Color COLOR_PRIMARY = new Color(37, 99, 235);
    private final Color COLOR_BACKGROUND = new Color(243, 244, 246);
    private final Color COLOR_WHITE = Color.WHITE;

    private DefaultTableModel transaksiTableModel;
    private JTable transaksiTable;
    private JLabel lblTotalItem, lblGrandTotal;
    private JTextField txtCustomerSearch, txtCustomerName, txtPhone;
    private JComboBox<String> comboPayment;
    private JTextField txtJumlahBayar, txtKembalian;
    private JButton btnTambahPesanan, btnProses, btnBatalTrx;

    public Transaksi() {
        setLayout(new BorderLayout());
        setBackground(COLOR_BACKGROUND);
        
        add(createHeader(), BorderLayout.NORTH);
        add(createContentArea(), BorderLayout.CENTER);
    }


    private JPanel createHeader() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        header.setBackground(COLOR_WHITE);
        header.add(new JLabel("Kasir: Erfan ▼"));
        return header;
    }

    private JPanel createContentArea() {
        JPanel contentArea = new JPanel(new BorderLayout(20, 20));
        contentArea.setBorder(new EmptyBorder(20, 20, 20, 20));
        contentArea.setBackground(COLOR_BACKGROUND);

        JLabel pageTitle = new JLabel("Input Pesanan Transaksi Baru (POS)");
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

        transaksiTableModel.addRow(new Object[]{"1", "KABEL 2M", "KABEL HDMI 2M", "2", "30.000", "0", "60.000", "🗑"});
        transaksiTableModel.addRow(new Object[]{"2", "BT-SPK", "SPEAKER BLUETOOTH", "1", "150.000", "0", "150.000", "🗑"});

        tablePanel.add(new JScrollPane(transaksiTable), BorderLayout.CENTER);
        return tablePanel;
    }

    private JPanel createTransaksiDetailPanel() {
        JPanel rightContainer = new JPanel();
        rightContainer.setLayout(new BoxLayout(rightContainer, BoxLayout.Y_AXIS));
        rightContainer.setBackground(COLOR_BACKGROUND);
        rightContainer.setPreferredSize(new Dimension(300, 0));

        JLabel titleLabel = new JLabel("Detail Pembayaran & Pelanggan");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightContainer.add(titleLabel);
        rightContainer.add(Box.createRigidArea(new Dimension(0, 15)));

        rightContainer.add(createCustomerPanel());
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

        JPanel summaryDetails = new JPanel(new GridLayout(4, 2, 10, 10));
        summaryDetails.setBackground(COLOR_WHITE);
        summaryDetails.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        summaryDetails.add(new JLabel("Subtotal"));
        summaryDetails.add(new JLabel("Rp 210.000"));
        summaryDetails.add(new JLabel("Total Diskon"));
        summaryDetails.add(new JLabel("Rp 0"));
        summaryDetails.add(new JLabel("Pajak (PPN 11%)"));
        summaryDetails.add(new JLabel("Rp 23.100"));
        summaryDetails.add(new JLabel("Grand Total"));
        lblGrandTotal = new JLabel("Rp 233.100");
        lblGrandTotal.setFont(new Font("SansSerif", Font.BOLD, 14));
        summaryDetails.add(lblGrandTotal);
        summaryPanel.add(summaryDetails);
        summaryPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JPanel actionPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        actionPanel.setBackground(COLOR_WHITE);
        actionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        btnProses = new JButton("Proses Transaksi");
        btnProses.setBackground(COLOR_PRIMARY);
        btnProses.setForeground(COLOR_WHITE);
        btnProses.setOpaque(true);
        
        btnBatalTrx = new JButton("Batal Transaksi");
        btnBatalTrx.setBackground(new Color(220, 38, 38));
        btnBatalTrx.setForeground(COLOR_WHITE);
        btnBatalTrx.setOpaque(true);
        
        actionPanel.add(btnProses);
        actionPanel.add(btnBatalTrx);
        actionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
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
        txtCustomerSearch.setText("");
        txtCustomerName.setText("");
        txtPhone.setText("");
        comboPayment.setSelectedIndex(0);
        txtJumlahBayar.setText("");
        txtKembalian.setText("");
        lblTotalItem.setText("0");
        lblGrandTotal.setText("Rp 0");
    }

    
}
