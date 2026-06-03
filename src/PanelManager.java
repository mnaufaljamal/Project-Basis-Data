import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class PanelManager extends JFrame {

    private final Color COLOR_PRIMARY = new Color(37, 99, 235); 
    private final Color COLOR_BACKGROUND = new Color(243, 244, 246); 
    private final Color COLOR_WHITE = Color.WHITE;

    private Connection conn;
    
    private JTable table;
    private DefaultTableModel tableModel;
    private CardLayout cardLayout;
    private JPanel mainContent;
    private Map<String, JButton> menuButtons = new HashMap<>();
    private PanelTransaksi transaksiPanel;
    private PanelLaporan laporanPanel;
    
    private JTextField txtIdBarang, txtNama, searchField;
    private JComboBox<String> comboCategory;
    private JSpinner spinHarga, spinStok;
    private JButton btnSimpan, btnBatal, btnCreate, btnUpdate, btnDelete, btnCari;
    
    private String selectedIdBarang = ""; 

    public PanelManager() {
        setTitle("Frenz Elektronik");
        setSize(1200, 760);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // prepare card layout container before building sidebar
        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);

        add(createSidebar(), BorderLayout.WEST);
        add(createMainArea(), BorderLayout.CENTER);
        
        initEventHandlers();
        koneksiDatabase();
        tampilTabel();
    }

    private void koneksiDatabase() {
        try {
            conn = KoneksiDatabase.getConnection();
            System.out.println("Terkoneksi ke Database SSMS!");
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Driver SQL Server tidak ditemukan: " + e.getMessage());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Koneksi Gagal: " + e.getMessage());
        }
    }

    private void tampilTabel() {
        tableModel.setRowCount(0); 
        try {
            String sql = "SELECT * FROM Barang";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) { 
                tableModel.addRow(new Object[]{
                    rs.getString("ID_Barang"),
                    rs.getString("Nama_Barang"),
                    rs.getString("Kategori"),
                    rs.getString("Harga_Satuan"),
                    rs.getString("Stok")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Gagal Menampilkan Data: " + e.getMessage());
        }
    }

    private void cariDataBarang() {
        String keyword = searchField.getText();
        if (keyword.isEmpty() || keyword.equals("Cari Barang...")) {
            tampilTabel();
            return;
        }

        tableModel.setRowCount(0); 
        try {
            String sql = "SELECT * FROM Barang WHERE Nama_Barang LIKE ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, "%" + keyword + "%");
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("ID_Barang"),
                    rs.getString("Nama_Barang"),
                    rs.getString("Kategori"),
                    rs.getString("Harga_Satuan"),
                    rs.getString("Stok")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Pencarian Gagal: " + ex.getMessage());
        }
    }

    private void resetForm() {
        txtIdBarang.setText("");
        txtIdBarang.setEditable(true); 
        
        txtNama.setText("");
        comboCategory.setSelectedIndex(0);
        spinHarga.setValue(0.0);
        spinStok.setValue(0);
        selectedIdBarang = "";
        btnSimpan.setText("Simpan"); 
    }

    private void initEventHandlers() {
        
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                updateSelectedBarang();
            }
        });

        btnCreate.addActionListener(e -> showBarangDialog(false));
        btnUpdate.addActionListener(e -> {
            if (requireSelectedRow()) {
                showBarangDialog(true);
            }
        });
        btnDelete.addActionListener(e -> {
            if (requireSelectedRow()) {
                showDeleteDialog();
            }
        });
        btnCari.addActionListener(e -> cariDataBarang());
        searchField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("Cari Barang...")) {
                    searchField.setText("");
                }
            }
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("Cari Barang...");
                }
            }
        });

    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(COLOR_PRIMARY);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(new EmptyBorder(20, 10, 20, 10));

        JLabel titleLabel = new JLabel("Frenz Elektronik");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(titleLabel);
        sidebar.add(Box.createRigidArea(new Dimension(0, 30)));

        String[] menus = {"Barang", "Transaksi", "Laporan"};
        for (String menu : menus) {
            JButton btn = new JButton(menu);
            btn.setMaximumSize(new Dimension(220, 48));
            btn.setPreferredSize(new Dimension(220, 48));
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            btn.setBackground(COLOR_PRIMARY);
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setBorder(new EmptyBorder(10, 12, 10, 12));
            btn.setContentAreaFilled(false);

            menuButtons.put(menu, btn);
            btn.addActionListener(e -> handleMenuClick(menu));
            sidebar.add(btn);
            sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        }
        // set initial selected menu style
        applySelectedStyle("Barang");
        return sidebar;
    }

    private void handleMenuClick(String menu) {
        // update cards and selected styles
        switch (menu) {
            case "Barang":
                cardLayout.show(mainContent, "manajemen");
                tampilTabel();
                break;
            case "Transaksi":
                cardLayout.show(mainContent, "transaksi");
                break;
            
            case "Laporan":
                cardLayout.show(mainContent, "laporan");
                if (laporanPanel != null) {
                    laporanPanel.refreshData();
                }
                break;
            default:
                break;
        }
        applySelectedStyle(menu);
    }

    private void applySelectedStyle(String selectedMenu) {
        for (Map.Entry<String, JButton> e : menuButtons.entrySet()) {
            JButton b = e.getValue();
            if (e.getKey().equals(selectedMenu)) {
                b.setBackground(new Color(219, 234, 254)); // light blue
                b.setForeground(COLOR_PRIMARY);
                b.setOpaque(true);
                b.setContentAreaFilled(true);
                b.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(59, 130, 246)),
                    new EmptyBorder(10, 12, 10, 12)
                ));
            } else {
                b.setBackground(COLOR_PRIMARY);
                b.setForeground(Color.WHITE);
                b.setOpaque(true);
                b.setContentAreaFilled(false);
                b.setBorder(new EmptyBorder(10, 12, 10, 12));
            }
        }
    }

    private JPanel createMainArea() {
        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(COLOR_BACKGROUND);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_WHITE);
        header.setBorder(new EmptyBorder(12, 20, 12, 20));
        header.add(createCashierProfile(), BorderLayout.EAST);
        mainArea.add(header, BorderLayout.NORTH);

        // prepare cards: manajemen (default) and transaksi + placeholders
        mainContent.setBackground(COLOR_BACKGROUND);

        // Manajemen Barang card
        mainContent.add(createManajemenPanel(), "manajemen");

        // Transaksi card (reuse Transaksi JPanel)
        transaksiPanel = new PanelTransaksi();
        mainContent.add(transaksiPanel, "transaksi");

        // Laporan card
        laporanPanel = new PanelLaporan();
        mainContent.add(laporanPanel, "laporan");

        cardLayout.show(mainContent, "manajemen");

        mainArea.add(mainContent, BorderLayout.CENTER);
        return mainArea;
    }

    private JPanel createManajemenPanel() {
        JPanel contentArea = new JPanel(new BorderLayout(20, 20));
        contentArea.setBorder(new EmptyBorder(20, 20, 20, 20));
        contentArea.setBackground(COLOR_BACKGROUND);

        JLabel pageTitle = new JLabel("Daftar Data Barang");
        pageTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        contentArea.add(pageTitle, BorderLayout.NORTH);

        contentArea.add(createTablePanel(), BorderLayout.CENTER);
        return contentArea;
    }

    private JPanel makePlaceholderPanel(String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(COLOR_BACKGROUND);
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 24));
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(lbl, BorderLayout.CENTER);
        return p;
    }

    private JPanel createCashierProfile() {
        JPanel profile = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        profile.setBackground(COLOR_WHITE);

        JLabel avatar = new JLabel("E", SwingConstants.CENTER);
        avatar.setPreferredSize(new Dimension(34, 34));
        avatar.setOpaque(true);
        avatar.setBackground(COLOR_PRIMARY);
        avatar.setForeground(COLOR_WHITE);
        avatar.setFont(new Font("SansSerif", Font.BOLD, 14));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(COLOR_WHITE);

        JLabel nameLabel = new JLabel("Erfan");
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        JLabel roleLabel = new JLabel("Kasir Aktif");
        roleLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        roleLabel.setForeground(new Color(100, 116, 139));

        textPanel.add(nameLabel);
        textPanel.add(roleLabel);
        profile.add(avatar);
        profile.add(textPanel);
        profile.add(new JLabel("▼"));
        return profile;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout(0, 15));
        tablePanel.setBackground(COLOR_WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(COLOR_WHITE);
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        searchPanel.setBackground(COLOR_WHITE);
        
        searchField = new JTextField("Cari Barang...", 20);
        btnCari = new JButton("Cari");
        stylePrimaryButton(btnCari);
        
        searchPanel.add(searchField);
        searchPanel.add(btnCari);
        
        topBar.add(searchPanel, BorderLayout.WEST);
        
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setBackground(COLOR_WHITE);
        btnCreate = new JButton("Tambah Barang");
        btnUpdate = new JButton("Update");
        btnDelete = new JButton("Delete");
        stylePrimaryButton(btnCreate);
        styleSecondaryButton(btnUpdate);
        styleDangerButton(btnDelete);
        actionPanel.add(btnCreate);
        actionPanel.add(btnUpdate);
        actionPanel.add(btnDelete);
        topBar.add(actionPanel, BorderLayout.EAST);
        
        tablePanel.add(topBar, BorderLayout.NORTH);

        String[] columns = {"ID Barang", "Nama Barang", "Kategori", "Harga Satuan", "Stok"};
        tableModel = new DefaultTableModel(new Object[][]{}, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setBackground(new Color(226, 232, 240));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        
        JScrollPane scrollPane = new JScrollPane(table);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        return tablePanel;
    }

    private JPanel createFormPanel() {
        JPanel rightContainer = new JPanel();
        rightContainer.setLayout(new BoxLayout(rightContainer, BoxLayout.Y_AXIS));
        rightContainer.setBackground(COLOR_WHITE);
        rightContainer.setPreferredSize(new Dimension(300, 0));

        JLabel titleLabel = new JLabel("Form Data Barang");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightContainer.add(titleLabel);
        rightContainer.add(Box.createRigidArea(new Dimension(0, 15)));

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(COLOR_WHITE);
        formPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel formTitle = new JLabel("Isi Data Barang");
        formTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(formTitle);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // PENAMBAHAN: Label dan Input untuk ID Barang
        formPanel.add(new JLabel("ID Barang"));
        txtIdBarang = new JTextField(20);
        txtIdBarang.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtIdBarang.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30)); 
        formPanel.add(txtIdBarang);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        formPanel.add(new JLabel("Nama Barang"));
        txtNama = new JTextField(20);
        txtNama.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtNama.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30)); 
        formPanel.add(txtNama);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        formPanel.add(new JLabel("Kategori"));
        String[] categories = {"Aksesoris", "Gadget", "Komputer", "Lainnya"};
        comboCategory = new JComboBox<>(categories);
        comboCategory.setAlignmentX(Component.LEFT_ALIGNMENT);
        comboCategory.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        formPanel.add(comboCategory);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel priceStockPanel = new JPanel(new GridLayout(2, 2, 10, 5));
        priceStockPanel.setBackground(COLOR_WHITE);
        priceStockPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        priceStockPanel.add(new JLabel("Harga Satuan"));
        priceStockPanel.add(new JLabel("Stok"));
        
        spinHarga = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100000000.0, 1000.0));
        spinStok = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
        
        priceStockPanel.add(spinHarga); 
        priceStockPanel.add(spinStok); 
        priceStockPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        formPanel.add(priceStockPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel actionPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        actionPanel.setBackground(COLOR_WHITE);
        actionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        btnSimpan = new JButton("Simpan");
        stylePrimaryButton(btnSimpan);
        btnBatal = new JButton("Batal");
        
        actionPanel.add(btnSimpan);
        actionPanel.add(btnBatal);
        actionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        formPanel.add(actionPanel);

        rightContainer.add(formPanel);
        return rightContainer;
    }

    private void showBarangDialog(boolean isUpdate) {
        JDialog dialog = new JDialog(this, isUpdate ? "Update Barang" : "Tambah Barang", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setContentPane(createFormPanel());
        dialog.pack();
        dialog.setSize(340, isUpdate ? 410 : 390);
        dialog.setLocationRelativeTo(this);

        if (isUpdate) {
            fillFormFromSelectedRow();
            btnSimpan.setText("Update");
            txtIdBarang.setEditable(false);
        } else {
            resetForm();
            btnSimpan.setText("Simpan");
        }

        btnBatal.addActionListener(e -> dialog.dispose());
        btnSimpan.addActionListener(e -> {
            if (saveBarang(isUpdate)) {
                dialog.dispose();
            }
        });
        dialog.setVisible(true);
    }

    private void showDeleteDialog() {
        int row = table.getSelectedRow();
        String nama = table.getValueAt(row, 1).toString();
        String kategori = table.getValueAt(row, 2).toString();

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(COLOR_WHITE);
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));
        panel.add(makeDialogTitle("Hapus Data Barang"));
        panel.add(Box.createRigidArea(new Dimension(0, 12)));
        panel.add(new JLabel("<html>Yakin ingin menghapus barang ini?<br><br><b>Nama:</b> " + nama + "<br><b>Kategori:</b> " + kategori + "</html>"));
        panel.add(Box.createRigidArea(new Dimension(0, 18)));

        JButton cancelButton = new JButton("Batal");
        JButton deleteButton = new JButton("Delete");
        styleDangerButton(deleteButton);

        JDialog dialog = new JDialog(this, "Delete Barang", true);
        JPanel actions = new JPanel(new GridLayout(1, 2, 10, 0));
        actions.setBackground(COLOR_WHITE);
        actions.add(deleteButton);
        actions.add(cancelButton);
        panel.add(actions);

        cancelButton.addActionListener(e -> dialog.dispose());
        deleteButton.addActionListener(e -> {
            try {
                String sql = "DELETE FROM Barang WHERE ID_Barang=?";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, selectedIdBarang);
                pst.execute();
                JOptionPane.showMessageDialog(this, "Data berhasil dihapus!");
                selectedIdBarang = "";
                tampilTabel();
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal menghapus data: " + ex.getMessage());
            }
        });

        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setSize(330, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private boolean saveBarang(boolean isUpdate) {
        String inputId = txtIdBarang.getText().trim();
        String nama = txtNama.getText().trim();
        String kategori = comboCategory.getSelectedItem().toString();
        String harga = spinHarga.getValue().toString();
        String stok = spinStok.getValue().toString();

        if (inputId.isEmpty() || nama.isEmpty()) {
            JOptionPane.showMessageDialog(this, "ID Barang dan Nama tidak boleh kosong!");
            return false;
        }

        try {
            if (!isUpdate) {
                String sql = "INSERT INTO Barang (ID_Barang, Nama_Barang, Kategori, Harga_Satuan, Stok) VALUES (?,?,?,?,?)";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, inputId);
                pst.setString(2, nama);
                pst.setString(3, kategori);
                pst.setString(4, harga);
                pst.setString(5, stok);
                pst.execute();
                JOptionPane.showMessageDialog(this, "Data berhasil disimpan!");
            } else {
                String sql = "UPDATE Barang SET Nama_Barang=?, Kategori=?, Harga_Satuan=?, Stok=? WHERE ID_Barang=?";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, nama);
                pst.setString(2, kategori);
                pst.setString(3, harga);
                pst.setString(4, stok);
                pst.setString(5, selectedIdBarang);
                pst.execute();
                JOptionPane.showMessageDialog(this, "Data berhasil diupdate!");
            }
            tampilTabel();
            return true;
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal memproses data: " + ex.getMessage());
            return false;
        }
    }

    private boolean requireSelectedRow() {
        updateSelectedBarang();
        if (selectedIdBarang.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih satu barang di tabel terlebih dahulu.");
            return false;
        }
        return true;
    }

    private void updateSelectedBarang() {
        int row = table.getSelectedRow();
        selectedIdBarang = row == -1 ? "" : table.getValueAt(row, 0).toString();
    }

    private void fillFormFromSelectedRow() {
        int row = table.getSelectedRow();
        txtIdBarang.setText(table.getValueAt(row, 0).toString());
        txtNama.setText(table.getValueAt(row, 1).toString());
        comboCategory.setSelectedItem(table.getValueAt(row, 2).toString());
        spinHarga.setValue(Double.parseDouble(table.getValueAt(row, 3).toString()));
        spinStok.setValue(Integer.parseInt(table.getValueAt(row, 4).toString()));
    }

    private JLabel makeDialogTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 16));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void stylePrimaryButton(JButton button) {
        button.setBackground(COLOR_PRIMARY);
        button.setForeground(COLOR_WHITE);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
    }

    private void styleSecondaryButton(JButton button) {
        button.setBackground(new Color(226, 232, 240));
        button.setForeground(new Color(30, 41, 59));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
    }

    private void styleDangerButton(JButton button) {
        button.setBackground(new Color(220, 38, 38));
        button.setForeground(COLOR_WHITE);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new PanelManager().setVisible(true);
        });
    }
}
