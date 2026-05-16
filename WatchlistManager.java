import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class WatchlistManager extends JFrame {

    // 1. Declare UI Components
    private JTextField titleField;
    private JButton addButton;
    private JButton plusOneButton;
    private JButton finishedButton;
    private JButton viewButton;
    private JTable showTable;
    private DefaultTableModel tableModel;

    // 2. Database Credentials (Update these with your MySQL details)
    private final String DB_URL = "jdbc:mysql://localhost:3306/watchlist_db?allowPublicKeyRetrieval=true&useSSL=false";
    private final String DB_USER = "root";
    private final String DB_PASSWORD = "AngryBirds@1";

    public WatchlistManager() {
        // 3. Set up the basic window (JFrame)
        setTitle("Watchlist Manager");
        setSize(550, 420);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // 4. Top Panel - Input field with label
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        titleField = new JTextField(20);
        inputPanel.add(new JLabel("Show Title:"));
        inputPanel.add(titleField);

        // 5. Button Panel - All buttons in a row
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        addButton = new JButton("Add New Show");
        plusOneButton = new JButton("+1 Episode");
        finishedButton = new JButton("Finished");
        viewButton = new JButton("View Watchlist");

        buttonPanel.add(addButton);
        buttonPanel.add(plusOneButton);
        buttonPanel.add(finishedButton);
        buttonPanel.add(viewButton);

        // 6. Combine input and buttons into a top section
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.add(inputPanel);
        topSection.add(buttonPanel);

        add(topSection, BorderLayout.NORTH);

        // 7. Center Panel - Table to display shows
        String[] columnNames = {"Title", "Episodes Watched", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        showTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(showTable);
        add(scrollPane, BorderLayout.CENTER);

        // 8. Action Listener for the "Add New Show" button
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String title = titleField.getText().trim();
                if (!title.isEmpty()) {
                    addShow(title);
                    refreshTable();
                } else {
                    JOptionPane.showMessageDialog(null, "Please enter a title.");
                }
            }
        });

        // 9. Action Listener for the "+1 Episode" button
        plusOneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String title = titleField.getText().trim();
                if (!title.isEmpty()) {
                    incrementEpisode(title);
                    refreshTable();
                } else {
                    JOptionPane.showMessageDialog(null, "Please enter a title to update.");
                }
            }
        });

        // 10. Action Listener for the "Finished" button
        finishedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String title = titleField.getText().trim();
                if (!title.isEmpty()) {
                    markFinished(title);
                    refreshTable();
                } else {
                    JOptionPane.showMessageDialog(null, "Please enter a title to mark as finished.");
                }
            }
        });

        // 11. Action Listener for the "View Watchlist" button
        viewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshTable();
            }
        });

        // Load the table on startup
        refreshTable();
    }

    // --- Backend Logic (JDBC) ---

    private void addShow(String title) {
        String query = "INSERT INTO shows (title, episode, finished) VALUES (?, 0, FALSE)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, title);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, title + " added to your watchlist!");
            titleField.setText("");

        } catch (SQLException ex) {
            // Usually triggers if the title already exists because of the UNIQUE constraint in SQL
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
        }
    }

    private void incrementEpisode(String title) {
        String query = "UPDATE shows SET episode = episode + 1 WHERE title = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, title);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Episode incremented for " + title + "!");
            } else {
                JOptionPane.showMessageDialog(this, "Show not found. Please add it first.");
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
        }
    }

    private void markFinished(String title) {
        String query = "UPDATE shows SET finished = TRUE WHERE title = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, title);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, title + " marked as finished! \uD83C\uDF89");
            } else {
                JOptionPane.showMessageDialog(this, "Show not found. Please add it first.");
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
        }
    }

    private void refreshTable() {
        String query = "SELECT title, episode, finished FROM shows ORDER BY finished ASC, title ASC";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            tableModel.setRowCount(0);

            while (rs.next()) {
                String title = rs.getString("title");
                int episode = rs.getInt("episode");
                boolean finished = rs.getBoolean("finished");
                String status = finished ? "✅ Finished" : "📺 Watching";
                tableModel.addRow(new Object[]{title, episode, status});
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
        }
    }

    // Main method to run the application
    public static void main(String[] args) {
        // Run the GUI creation on the Event Dispatch Thread
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new WatchlistManager().setVisible(true);
            }
        });
    }
}
