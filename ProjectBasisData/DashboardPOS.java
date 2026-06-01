import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class DashboardPOS extends JFrame {

    private final Color COLOR_PRIMARY = new Color(37, 99, 235); 
    private final Color COLOR_BACKGROUND = new Color(243, 244, 246); 
    private final Color COLOR_WHITE = Color.WHITE;

    private Connection conn;
    
    private JTable table;
    private DefaultTableModel tableModel;
    
    private JTextField txtIdBarang, txtNama;
    private JComboBox<String> comboCategory;
    private JSpinner spinHarga, spinStok;
    private JButton btnSimpan, btnBatal, btnHapus, btnTambahBaru;
    private JLabel lblConfirmDelete;
    
    private String selectedIdBarang = ""; 

    public DashboardPOS() {
        setTitle("Frenz Elektronik POS");
        setSize(1200, 760);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(createSidebar(), BorderLayout.WEST);
        add(createMainArea(), BorderLayout.CENTER);
        
        initEventHandlers();
        koneksiDatabase();
        tampilTabel();
    }

    private void koneksiDatabase() {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=FranzElektronik;encrypt=true;trustServerCertificate=true;";
        String user = "Kasir1";
        String password = "12345";
        
        try {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Terkoneksi ke Database SSMS!");
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

    private void resetForm() {
        txtIdBarang.setText("");
        txtIdBarang.setEditable(true); 
        
        txtNama.setText("");
        comboCategory.setSelectedIndex(0);
        spinHarga.setValue(0.0);
        spinStok.setValue(0);
        selectedIdBarang = "";
        btnSimpan.setText("Simpan"); 
        lblConfirmDelete.setText("<html><small>Pilih barang di tabel untuk menghapus.</small></html>");
        btnHapus.setEnabled(false);
    }

    private void initEventHandlers() {
        
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                int baris = table.getSelectedRow();
                if (baris != -1) {
                    selectedIdBarang = table.getValueAt(baris, 0).toString();
                    String nama = table.getValueAt(baris, 1).toString();
                    String kategori = table.getValueAt(baris, 2).toString();
                    double harga = Double.parseDouble(table.getValueAt(baris, 3).toString());
                    int stok = Integer.parseInt(table.getValueAt(baris, 4).toString());

                    txtIdBarang.setText(selectedIdBarang);
                    txtIdBarang.setEditable(false); 
                    
                    txtNama.setText(nama);
                    comboCategory.setSelectedItem(kategori);
                    spinHarga.setValue(harga);
                    spinStok.setValue(stok);

                    btnSimpan.setText("Update");
                    btnHapus.setEnabled(true);
                    lblConfirmDelete.setText("<html><small>Confirm hapus data Barang<br><b>Nama:</b> " + nama + "<br><b>Kategori:</b> " + kategori + "</small></html>");
                }
            }
        });

        btnTambahBaru.addActionListener(e -> resetForm());
        btnBatal.addActionListener(e -> resetForm());

        btnSimpan.addActionListener(e -> {
            String inputId = txtIdBarang.getText();
            String nama = txtNama.getText();
            String kategori = comboCategory.getSelectedItem().toString();
            String harga = spinHarga.getValue().toString();
            String stok = spinStok.getValue().toString();

            if (inputId.isEmpty() || nama.isEmpty()) {
                JOptionPane.showMessageDialog(this, "ID Barang dan Nama tidak boleh kosong!");
                return;
            }

            try {
                if (btnSimpan.getText().equals("Simpan")) {
                    String sql = "INSERT INTO Barang (ID_Barang, Nama_Barang, Kategori, Harga_Satuan, Stok) VALUES (?,?,?,?,?)";
                    PreparedStatement pst = conn.prepareStatement(sql);
                    pst.setString(1, inputId);
                    pst.setString(2, nama);
                    pst.setString(3, kategori);
                    pst.setString(4, harga);
                    pst.setString(5, stok);
                    pst.execute();
                    JOptionPane.showMessageDialog(null, "Data Berhasil Disimpan!");
                } else {
                    String sql = "UPDATE Barang SET Nama_Barang=?, Kategori=?, Harga_Satuan=?, Stok=? WHERE ID_Barang=?";
                    PreparedStatement pst = conn.prepareStatement(sql);
                    pst.setString(1, nama);
                    pst.setString(2, kategori);
                    pst.setString(3, harga);
                    pst.setString(4, stok);
                    pst.setString(5, selectedIdBarang);
                    pst.execute();
                    JOptionPane.showMessageDialog(null, "Data Berhasil Diupdate!");
                }
                resetForm();
                tampilTabel();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, "Gagal memproses data: " + ex.getMessage());
            }
        });

        btnHapus.addActionListener(e -> {
            if (selectedIdBarang.isEmpty()) return;
            
            int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus barang ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    String sql = "DELETE FROM Barang WHERE ID_Barang=?";
                    PreparedStatement pst = conn.prepareStatement(sql);
                    pst.setString(1, selectedIdBarang);
                    pst.execute();
                    JOptionPane.showMessageDialog(null, "Data berhasil dihapus!");
                    resetForm();
                    tampilTabel();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Gagal menghapus data: " + ex.getMessage());
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

        JLabel titleLabel = new JLabel("Frenz Elektronik POS");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(titleLabel);
        sidebar.add(Box.createRigidArea(new Dimension(0, 30)));

        String[] menus = {"Dashboard", "Manajemen Barang", "Transaksi (Struk)", "Laporan", "Pengaturan"};
        for (String menu : menus) {
            JButton btn = new JButton(menu);
            btn.setMaximumSize(new Dimension(200, 40));
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            btn.setBackground(COLOR_PRIMARY);
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setBorder(new EmptyBorder(10, 15, 10, 15));
            
            if (menu.equals("Manajemen Barang")) {
                btn.setBackground(Color.WHITE);
                btn.setForeground(COLOR_PRIMARY);
                btn.setOpaque(true);
            } else {
                btn.setContentAreaFilled(false);
            }
            
            sidebar.add(btn);
            sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
        }
        return sidebar;
    }

    private JPanel createMainArea() {
        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(COLOR_BACKGROUND);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        header.setBackground(COLOR_WHITE);
        header.add(new JLabel("Kasir: Erfan ▼"));
        mainArea.add(header, BorderLayout.NORTH);

        JPanel contentArea = new JPanel(new BorderLayout(20, 20));
        contentArea.setBorder(new EmptyBorder(20, 20, 20, 20));
        contentArea.setBackground(COLOR_BACKGROUND);

        JLabel pageTitle = new JLabel("Daftar Data Barang");
        pageTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        contentArea.add(pageTitle, BorderLayout.NORTH);

        JPanel splitPanel = new JPanel(new BorderLayout(20, 0));
        splitPanel.setBackground(COLOR_BACKGROUND);
        splitPanel.add(createTablePanel(), BorderLayout.CENTER);
        splitPanel.add(createFormPanel(), BorderLayout.EAST);

        contentArea.add(splitPanel, BorderLayout.CENTER);
        mainArea.add(contentArea, BorderLayout.CENTER);

        return mainArea;
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
        
        JTextField searchField = new JTextField("Cari Barang...", 20);
        topBar.add(searchField, BorderLayout.WEST);
        topBar.add(Box.createRigidArea(new Dimension(20, 0)));
        
        btnTambahBaru = new JButton("Tambah Barang Baru");
        btnTambahBaru.setBackground(COLOR_PRIMARY);
        btnTambahBaru.setForeground(COLOR_WHITE);
        topBar.add(btnTambahBaru, BorderLayout.EAST);
        
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
        table.getTableHeader().setBackground(new Color(226, 232, 240));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        
        JScrollPane scrollPane = new JScrollPane(table);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        return tablePanel;
    }

    private JPanel createFormPanel() {
        JPanel rightContainer = new JPanel();
        rightContainer.setLayout(new BoxLayout(rightContainer, BoxLayout.Y_AXIS));
        rightContainer.setBackground(COLOR_BACKGROUND);
        rightContainer.setPreferredSize(new Dimension(300, 0));

        JLabel titleLabel = new JLabel("Manajemen Data Barang");
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

        JLabel formTitle = new JLabel("Form Data Barang");
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
        btnSimpan.setBackground(COLOR_PRIMARY);
        btnBatal = new JButton("Batal");
        
        actionPanel.add(btnSimpan);
        actionPanel.add(btnBatal);
        actionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        formPanel.add(actionPanel);

        rightContainer.add(formPanel);
        rightContainer.add(Box.createRigidArea(new Dimension(0, 15)));

        JPanel deletePanel = new JPanel();
        deletePanel.setLayout(new BoxLayout(deletePanel, BoxLayout.Y_AXIS));
        deletePanel.setBackground(COLOR_WHITE);
        deletePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        deletePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel deleteTitle = new JLabel("Hapus Data Barang");
        deleteTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        deleteTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        deletePanel.add(deleteTitle);
        deletePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        lblConfirmDelete = new JLabel("<html><small>Pilih barang di tabel untuk menghapus.</small></html>");
        lblConfirmDelete.setAlignmentX(Component.LEFT_ALIGNMENT);
        deletePanel.add(lblConfirmDelete);
        deletePanel.add(Box.createRigidArea(new Dimension(0, 15)));

        btnHapus = new JButton("Hapus Permanen");
        btnHapus.setBackground(new Color(220, 38, 38)); 
        btnHapus.setForeground(COLOR_WHITE);
        btnHapus.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnHapus.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        btnHapus.setOpaque(true);
        btnHapus.setContentAreaFilled(true);
        btnHapus.setBorderPainted(false);
        btnHapus.setEnabled(false); 
        
        deletePanel.add(btnHapus);

        rightContainer.add(deletePanel);
        rightContainer.add(Box.createVerticalGlue());

        return rightContainer;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new DashboardPOS().setVisible(true);
        });
    }
}