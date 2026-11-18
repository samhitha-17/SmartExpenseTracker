import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;

public class ExpenseTracker extends JFrame {

    Color bg = new Color(250, 249, 247);
    Color purple = new Color(230, 220, 250);
    Color peach = new Color(255, 228, 210);
    Color mint = new Color(216, 242, 226);
    Color pink = new Color(255, 220, 232);
    Color textColor = new Color(65, 65, 65);

    JTextField title, amount, date;
    JComboBox<String> category;
    JTable table;
    DefaultTableModel model;

    JLabel totalLabel, monthlyLabel, budgetLabel, remainingLabel;

    PieChart chart;

    ExpenseTracker() {

        setTitle("Smart Expense Tracker");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        getContentPane().setBackground(bg);
        setLayout(new BorderLayout(15, 15));

        // HEADER
        JLabel heading = new JLabel("  Smart Expense Tracker");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 26));
        heading.setForeground(textColor);

        JLabel subtitle = new JLabel(
            "  Manage your money, understand your spending"
        );

        subtitle.setForeground(Color.GRAY);

        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setBackground(bg);
        header.add(heading);
        header.add(subtitle);

        add(header, BorderLayout.NORTH);

        // MAIN CONTENT
        JPanel content = new JPanel(new BorderLayout(15, 15));
        content.setBackground(bg);

        // SUMMARY CARDS
        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 12));
        cards.setBackground(bg);

        totalLabel = new JLabel("₹0");
        monthlyLabel = new JLabel("₹0");
        budgetLabel = new JLabel("₹0");
        remainingLabel = new JLabel("₹0");

        cards.add(createCard("Total Expenses", totalLabel, purple));
        cards.add(createCard("This Month", monthlyLabel, peach));
        cards.add(createCard("Monthly Budget", budgetLabel, mint));
        cards.add(createCard("Remaining", remainingLabel, pink));

        // INPUT PANEL
        JPanel input = new JPanel(new GridLayout(4, 2, 10, 10));
        input.setBackground(Color.WHITE);
        input.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        title = new JTextField();
        amount = new JTextField();
        date = new JTextField(LocalDate.now().toString());

        category = new JComboBox<>(new String[]{
            "Food", "Travel", "Shopping", "Education", "Other"
        });

        input.add(new JLabel("Expense Name"));
        input.add(title);

        input.add(new JLabel("Amount"));
        input.add(amount);

        input.add(new JLabel("Category"));
        input.add(category);

        input.add(new JLabel("Date"));
        input.add(date);

        // BUTTONS
        JPanel buttons = new JPanel(new GridLayout(2, 4, 10, 10));
        buttons.setBackground(bg);

        JButton add = createButton("Add Expense", purple);
        JButton edit = createButton("Edit Expense", mint);
        JButton delete = createButton("Delete Expense", pink);
        JButton budget = createButton("Set Budget", peach);
        JButton summary = createButton("Monthly Summary", purple);
        JButton analysis = createButton("Category Analysis", mint);
		JButton export = createButton("Export to CSV", peach);

        buttons.add(add);
        buttons.add(edit);
        buttons.add(delete);
        buttons.add(budget);
        buttons.add(summary);
        buttons.add(analysis);
		buttons.add(export);

        JPanel form = new JPanel(new BorderLayout(10, 10));
        form.setBackground(bg);

        JLabel formTitle = new JLabel("  Add or Manage Expenses");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));

        form.add(formTitle, BorderLayout.NORTH);
        form.add(input, BorderLayout.CENTER);
        form.add(buttons, BorderLayout.SOUTH);

        // TABLE
        model = new DefaultTableModel(
            new String[]{"ID", "Expense", "Amount", "Category", "Date"}, 0
        );

        table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        table.getTableHeader().setFont(
            new Font("Segoe UI", Font.BOLD, 13)
        );

        table.getTableHeader().setBackground(purple);
        table.setSelectionBackground(pink);
        table.setGridColor(new Color(235, 235, 235));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        JPanel history = new JPanel(new BorderLayout(5, 10));
        history.setBackground(bg);

        JLabel historyTitle = new JLabel("  Expense History");
        historyTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));

        history.add(historyTitle, BorderLayout.NORTH);
        history.add(scroll, BorderLayout.CENTER);

        // PIE CHART
        chart = new PieChart();

        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );

        JLabel chartTitle = new JLabel("Category Spending");
        chartTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));

        chartPanel.add(chartTitle, BorderLayout.NORTH);
        chartPanel.add(chart, BorderLayout.CENTER);

        JPanel lower = new JPanel(new GridLayout(1, 2, 15, 0));
        lower.setBackground(bg);

        lower.add(history);
        lower.add(chartPanel);

        content.add(cards, BorderLayout.NORTH);

        JPanel middle = new JPanel(new BorderLayout(15, 15));
        middle.setBackground(bg);

        middle.add(form, BorderLayout.NORTH);
        middle.add(lower, BorderLayout.CENTER);

        content.add(middle, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);

        // ACTIONS
        add.addActionListener(e -> addExpense());
        edit.addActionListener(e -> editExpense());
        delete.addActionListener(e -> deleteExpense());
        budget.addActionListener(e -> setBudget());
        summary.addActionListener(e -> monthlySummary());
        analysis.addActionListener(e -> categoryAnalysis());
		export.addActionListener(e -> exportCSV());

        table.getSelectionModel().addListSelectionListener(e -> {

            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();

                if (row >= 0) {
                    title.setText(model.getValueAt(row, 1).toString());
                    amount.setText(model.getValueAt(row, 2).toString());
                    category.setSelectedItem(
                        model.getValueAt(row, 3).toString()
                    );
                    date.setText(model.getValueAt(row, 4).toString());
                }
            }
        });

        loadExpenses();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    JPanel createCard(String name, JLabel value, Color color) {

        JPanel card = new JPanel(new GridLayout(2, 1, 5, 5));

        card.setBackground(color);
        card.setBorder(
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        );

        JLabel label = new JLabel(name);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        value.setFont(new Font("Segoe UI", Font.BOLD, 22));

        card.add(label);
        card.add(value);

        return card;
    }

    JButton createButton(String name, Color color) {

        JButton button = new JButton(name);

        button.setBackground(color);
        button.setForeground(textColor);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));

        button.setFocusPainted(false);
        button.setBorderPainted(false);

        button.setPreferredSize(new Dimension(150, 38));

        return button;
    }

    void addExpense() {

        try {

            String name = title.getText().trim();
            double amt = Double.parseDouble(amount.getText());
            String dt = date.getText().trim();

            LocalDate.parse(dt);

            if (name.isEmpty() || amt <= 0)
                throw new Exception();

            try (Connection con = DBConnection.connect();
                 PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO expenses(title,amount,category,date) VALUES(?,?,?,?)"
                 )) {

                ps.setString(1, name);
                ps.setDouble(2, amt);
                ps.setString(3, category.getSelectedItem().toString());
                ps.setString(4, dt);

                ps.executeUpdate();
            }

            clearFields();
            loadExpenses();
            checkBudget();

            JOptionPane.showMessageDialog(this, "Expense Added!");

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                this, "Enter valid expense details!"
            );
        }
    }

    void loadExpenses() {

        model.setRowCount(0);

        double total = 0;

        try (Connection con = DBConnection.connect();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT * FROM expenses ORDER BY date DESC"
             )) {

            while (rs.next()) {

                double amt = rs.getDouble("amount");

                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("title"),
                    amt,
                    rs.getString("category"),
                    rs.getString("date")
                });

                total += amt;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        double monthly = getMonthlyTotal();
        double budget = getBudget();

        totalLabel.setText("₹" + total);
        monthlyLabel.setText("₹" + monthly);
        budgetLabel.setText("₹" + budget);
        remainingLabel.setText("₹" + (budget - monthly));

        chart.updateData(getCategoryData());
    }

    void deleteExpense() {

        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an expense!");
            return;
        }

        try {

            int id = Integer.parseInt(
                model.getValueAt(row, 0).toString()
            );

            try (Connection con = DBConnection.connect();
                 PreparedStatement ps = con.prepareStatement(
                     "DELETE FROM expenses WHERE id=?"
                 )) {

                ps.setInt(1, id);
                ps.executeUpdate();
            }

            clearFields();
            loadExpenses();

            JOptionPane.showMessageDialog(this, "Expense Deleted!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void editExpense() {

        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an expense!");
            return;
        }

        try {

            int id = Integer.parseInt(
                model.getValueAt(row, 0).toString()
            );

            String name = title.getText().trim();
            double amt = Double.parseDouble(amount.getText());
            String dt = date.getText().trim();

            LocalDate.parse(dt);

            if (name.isEmpty() || amt <= 0)
                throw new Exception();

            try (Connection con = DBConnection.connect();
                 PreparedStatement ps = con.prepareStatement(
                     "UPDATE expenses SET title=?,amount=?,category=?,date=? WHERE id=?"
                 )) {

                ps.setString(1, name);
                ps.setDouble(2, amt);
                ps.setString(3, category.getSelectedItem().toString());
                ps.setString(4, dt);
                ps.setInt(5, id);

                ps.executeUpdate();
            }

            clearFields();
            loadExpenses();
            checkBudget();

            JOptionPane.showMessageDialog(this, "Expense Updated!");

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                this, "Enter valid expense details!"
            );
        }
    }

    void setBudget() {

        String input = JOptionPane.showInputDialog(
            this, "Enter Monthly Budget:"
        );

        if (input == null) return;

        try {

            double b = Double.parseDouble(input);

            if (b <= 0) throw new Exception();

            try (Connection con = DBConnection.connect();
                 PreparedStatement ps = con.prepareStatement(
                     "INSERT OR REPLACE INTO settings(name,value) VALUES('budget',?)"
                 )) {

                ps.setString(1, String.valueOf(b));
                ps.executeUpdate();
            }

            loadExpenses();
            checkBudget();

            JOptionPane.showMessageDialog(this, "Budget Saved!");

        } catch (Exception e) {

            JOptionPane.showMessageDialog(this, "Enter a valid budget!");
        }
    }

    double getBudget() {

        try (Connection con = DBConnection.connect();
             ResultSet rs = con.createStatement().executeQuery(
                 "SELECT value FROM settings WHERE name='budget'"
             )) {

            if (rs.next())
                return rs.getDouble("value");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    double getMonthlyTotal() {

        double total = 0;

        try (Connection con = DBConnection.connect();
             PreparedStatement ps = con.prepareStatement(
                 "SELECT SUM(amount) FROM expenses WHERE substr(date,1,7)=?"
             )) {

            ps.setString(1, YearMonth.now().toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    total = rs.getDouble(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return total;
    }

    Map<String, Double> getCategoryData() {

        Map<String, Double> data = new LinkedHashMap<>();

        try (Connection con = DBConnection.connect();
             PreparedStatement ps = con.prepareStatement(
                 "SELECT category,SUM(amount) AS total " +
                 "FROM expenses WHERE substr(date,1,7)=? " +
                 "GROUP BY category"
             )) {

            ps.setString(1, YearMonth.now().toString());

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    data.put(
                        rs.getString("category"),
                        rs.getDouble("total")
                    );
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    void monthlySummary() {

        double total = getMonthlyTotal();
        double budget = getBudget();

        JOptionPane.showMessageDialog(
            this,
            "MONTHLY SUMMARY\n\n" +
            "Month: " + YearMonth.now() +
            "\nTotal Spent: ₹" + total +
            "\nBudget: ₹" + budget +
            "\nRemaining: ₹" + (budget - total)
        );
    }

    void categoryAnalysis() {

        StringBuilder result = new StringBuilder(
            "CATEGORY-WISE SPENDING\n\n"
        );

        Map<String, Double> data = getCategoryData();

        for (String key : data.keySet()) {
            result.append(key)
                  .append(": ₹")
                  .append(data.get(key))
                  .append("\n");
        }

        JOptionPane.showMessageDialog(this, result.toString());
    }

    void checkBudget() {

        double budget = getBudget();
        double total = getMonthlyTotal();

        if (budget > 0 && total > budget) {

            JOptionPane.showMessageDialog(
                this,
                "Monthly Budget Exceeded!\n" +
                "Spent: ₹" + total +
                "\nBudget: ₹" + budget
            );
        }
    }

    void clearFields() {

        title.setText("");
        amount.setText("");
        date.setText(LocalDate.now().toString());
        category.setSelectedIndex(0);
        table.clearSelection();
    }

    // CUSTOM PIE CHART
    class PieChart extends JPanel {

        Map<String, Double> data = new LinkedHashMap<>();

        Color[] colors = {
            new Color(220, 190, 245),
            new Color(255, 190, 190),
            new Color(180, 225, 195),
            new Color(255, 220, 160),
            new Color(190, 215, 245)
        };

        PieChart() {
            setBackground(Color.WHITE);
        }

        void updateData(Map<String, Double> values) {
            data = values;
            repaint();
        }
protected void paintComponent(Graphics g) {

    super.paintComponent(g);

    Graphics2D g2 = (Graphics2D) g;

    double total = 0;

    for (double value : data.values())
        total += value;

    if (total == 0) {
        g2.setColor(Color.GRAY);
        g2.drawString("No expenses this month", 30, 100);
        return;
    }

    int start = 0;
    int i = 0;

    int size = Math.min(150, getHeight() - 30);

    int x = 15;
    int y = 15;

    for (String key : data.keySet()) {

        double value = data.get(key);
        int angle = (int) Math.round(value / total * 360);

        g2.setColor(colors[i % colors.length]);
        g2.fillArc(x, y, size, size, start, angle);

        start += angle;
        i++;
    }

    // Legend beside pie chart
    int legendX = size + 40;
    int legendY = 45;

    i = 0;

    for (String key : data.keySet()) {

        g2.setColor(colors[i % colors.length]);
        g2.fillRoundRect(legendX, legendY, 13, 13, 5, 5);

        g2.setColor(textColor);
        g2.drawString(
            key + " - Rs." + data.get(key),
            legendX + 22,
            legendY + 12
        );

        legendY += 28;
        i++;
    }
}
    }

	void exportCSV() {

    JFileChooser chooser = new JFileChooser();

    chooser.setSelectedFile(new File("Expenses.csv"));

    int result = chooser.showSaveDialog(this);

    if (result == JFileChooser.APPROVE_OPTION) {

        File file = chooser.getSelectedFile();

        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getAbsolutePath() + ".csv");
        }

        try (BufferedWriter writer = Files.newBufferedWriter(
                file.toPath(), StandardCharsets.UTF_8)) {

            writer.write("ID,Expense,Amount,Category,Date");
            writer.newLine();

            for (int i = 0; i < model.getRowCount(); i++) {

                for (int j = 0; j < model.getColumnCount(); j++) {

                    if (j > 0) writer.write(",");

                    String value = String.valueOf(
                        model.getValueAt(i, j)
                    );

                    writer.write("\"" + value.replace("\"", "\"\"") + "\"");
                }

                writer.newLine();
            }

            JOptionPane.showMessageDialog(
                this, "Expenses exported successfully!"
            );

        } catch (Exception e) {
    e.printStackTrace();
    JOptionPane.showMessageDialog(this,
        "Error: " + e.getMessage());
}
    }
}

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() ->
            new ExpenseTracker()
        );
    }
}