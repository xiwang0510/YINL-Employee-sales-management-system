import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class SalesManagementSystemGUI extends JFrame {

    // 数据文件路径
    private static final String DATA_FILE = "D:\\LCY collection\\demo5\\untitled\\src\\data.txt";

    // 销售数据存储结构，使用Map表示，外层的键代表销售员的标识，内层的键代表产品的标识，最终的双精度浮点数值则表示销售额
    private Map<Integer, Map<Integer, Double>> salesData;


    // 显示结果的文本区域，用于以可视化的方式向用户呈现操作结果。
    private JTextArea resultTextArea;

    // 主方法，程序入口
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SalesManagementSystemGUI gui = new SalesManagementSystemGUI();
            //创建了一个 SalesManagementSystemGUI 类的实例对象，即创建了应用程序的主窗口。
            gui.createAndShowGUI();
            //调用 gui 对象的 createAndShowGUI 方法。这个方法用于设置和显示应用程序的图形用户界面
        });
    }

    // 构造函数
    private void createAndShowGUI() {
        setTitle("销售管理系统"); // 设置窗口标题
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 设置窗口关闭操作

        // 创建按钮
        JButton calculateTotalSalesByPersonButton = new JButton("统计每人销售总额");
        calculateTotalSalesByPersonButton.addActionListener(e -> performTaskInBackground(this::calculateTotalSalesByPerson));

        JButton querySalesByPersonButton = new JButton("查询销售员销售情况");
        querySalesByPersonButton.addActionListener(e -> performTaskInBackground(this::querySalesByPerson));

        JButton calculateTotalSalesByProductButton = new JButton("统计每种产品总销售额");
        calculateTotalSalesByProductButton.addActionListener(e -> performTaskInBackground(this::calculateTotalSalesByProduct));

        JButton querySalesByProductButton = new JButton("查询产品销售情况");
        querySalesByProductButton.addActionListener(e -> performTaskInBackground(this::querySalesByProduct));

        JButton enterSalesDataButton = new JButton("录入销售数据");
        enterSalesDataButton.addActionListener(e -> performTaskInBackground(this::enterSalesData));

        JButton deleteSalesDataButton = new JButton("删除销售数据");
        deleteSalesDataButton.addActionListener(e -> performTaskInBackground(this::deleteSalesData));

        JButton exitButton = new JButton("退出系统");
        exitButton.addActionListener(e -> {
            System.out.println("退出系统");
            writeDataToFile(); // 添加保存数据的操作
            System.exit(0);
        });


        // 初始化文本区域
        resultTextArea = new JTextArea();
        resultTextArea.setEditable(false);

        // 创建按钮面板，设置为7行1列的GridLayout
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(7, 1));

        // 将按钮添加到按钮面板
        buttonPanel.add(calculateTotalSalesByPersonButton);
        buttonPanel.add(querySalesByPersonButton);
        buttonPanel.add(calculateTotalSalesByProductButton);
        buttonPanel.add(querySalesByProductButton);
        buttonPanel.add(enterSalesDataButton);
        buttonPanel.add(deleteSalesDataButton);
        buttonPanel.add(exitButton);

        // 创建主面板，使用BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(buttonPanel, BorderLayout.WEST);
        mainPanel.add(new JScrollPane(resultTextArea), BorderLayout.CENTER);

        // 将主面板添加到窗口内容面板
        getContentPane().add(mainPanel);

        // 设置窗口大小和居中显示
        setSize(600, 400);
        setLocationRelativeTo(null);
        setVisible(true);

        // 读取数据文件，初始化销售数据
        salesData = readDataFromFile();
    }

    // 在后台执行任务的方法
    private void performTaskInBackground(Runnable task) {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                task.run();
                return null;
            }

            @Override
            protected void done() {
                // 任务完成后更新界面
                try {
                    get();  // 获取 doInBackground 的返回值
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        worker.execute();
    }

    // 从文件读取销售数据的方法
    private Map<Integer, Map<Integer, Double>> readDataFromFile() {
        Map<Integer, Map<Integer, Double>> data = new HashMap<>();

        try (Scanner scanner = new Scanner(new FileReader(DATA_FILE))) {
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split(",");
                int salesmanId = Integer.parseInt(parts[0]);
                int productId = Integer.parseInt(parts[1]);
                double salesAmount = Double.parseDouble(parts[2]);

                data.computeIfAbsent(salesmanId, k -> new HashMap<>()).merge(productId, salesAmount, Double::sum);
            }
            System.out.println("数据读取成功！");
        } catch (IOException e) {
            e.printStackTrace(); // 文件不存在或读取失败
        }

        return data;
    }


    // 将消息追加到结果文本区域的方法
    private void appendToResultArea(String message) {
        resultTextArea.append(message + "\n");
    }

    // 统计每个销售员的销售额的方法
    private void calculateTotalSalesByPerson() {
        appendToResultArea("统计每个人销售额：");

        for (Map.Entry<Integer, Map<Integer, Double>> entry : salesData.entrySet()) {
            int salesmanId = entry.getKey();
            double totalSales = entry.getValue().values().stream().mapToDouble(Double::doubleValue).sum();

            appendToResultArea("销售员 " + salesmanId + ": " + totalSales);
        }
        appendToResultArea("_________________");
    }

    // 查询销售员销售情况的方法
    private void querySalesByPerson() {
        String input = JOptionPane.showInputDialog("请输入销售员编号：");
        if (input != null) {
            int salesmanId = Integer.parseInt(input);
            if (salesData.containsKey(salesmanId)) {
                appendToResultArea("销售员 " + salesmanId + " 的销售情况：");
                salesData.get(salesmanId).forEach((productId, salesAmount) ->
                        appendToResultArea("产品 " + productId + ": " + salesAmount));
            } else {
                appendToResultArea("销售员 " + salesmanId + " 无销售记录。");
            }
            appendToResultArea("_________________");
        }
    }

    // 统计每种产品的总销售额的方法
    private void calculateTotalSalesByProduct() {
        appendToResultArea("统计每种产品的总销售额：");

        Map<Integer, Double> totalSalesByProduct = new HashMap<>();

        for (Map<Integer, Double> productSales : salesData.values()) {
            for (Map.Entry<Integer, Double> entry : productSales.entrySet()) {
                int productId = entry.getKey();
                double salesAmount = entry.getValue();

                totalSalesByProduct.merge(productId, salesAmount, Double::sum);
            }
        }

        totalSalesByProduct.forEach((productId, totalSales) ->
                appendToResultArea("产品 " + productId + ": " + totalSales));
        appendToResultArea("_________________");
    }

    // 查询产品销售情况的方法
    private void querySalesByProduct() {
        String input = JOptionPane.showInputDialog("请输入产品编号：");
        if (input != null) {
            int productId = Integer.parseInt(input);
            double totalSales = salesData.values().stream()
                    .mapToDouble(productSales -> productSales.getOrDefault(productId, 0.0))
                    .sum();

            appendToResultArea("产品 " + productId + " 的销售情况：");
            salesData.forEach((salesmanId, productSales) ->
                    appendToResultArea("销售员 " + salesmanId + ": " + productSales.getOrDefault(productId, 0.0)));

            appendToResultArea("总销售额: " + totalSales);
        }
        appendToResultArea("_________________");
    }

    // 录入销售数据的方法
    private void enterSalesData() {
        String salesmanIdInput = JOptionPane.showInputDialog("请输入销售员编号：");
        String productIdInput = JOptionPane.showInputDialog("请输入产品编号：");
        String salesAmountInput = JOptionPane.showInputDialog("请输入销售额：");

        if (salesmanIdInput != null && productIdInput != null && salesAmountInput != null) {
            int salesmanId = Integer.parseInt(salesmanIdInput);
            int productId = Integer.parseInt(productIdInput);
            double salesAmount = Double.parseDouble(salesAmountInput);

            salesData.computeIfAbsent(salesmanId, k -> new HashMap<>()).merge(productId, salesAmount, Double::sum);
            appendToResultArea("销售数据录入成功。");
        }
        appendToResultArea("_________________");
    }

    private void writeDataToFile() {
        try (PrintWriter writer = new PrintWriter(DATA_FILE)) {
            for (Map.Entry<Integer, Map<Integer, Double>> entry : salesData.entrySet()) {
                int salesmanId = entry.getKey();
                for (Map.Entry<Integer, Double> productSales : entry.getValue().entrySet()) {
                    int productId = productSales.getKey();
                    double salesAmount = productSales.getValue();
                    writer.println(salesmanId + "," + productId + "," + salesAmount);
                }
            }
            System.out.println("数据写入成功！");
        } catch (IOException e) {
            e.printStackTrace(); // 处理文件写入异常
        }
    }

    // 删除销售数据的方法
    private void deleteSalesData() {
        String salesmanIdInput = JOptionPane.showInputDialog("请输入销售员编号：");
        String productIdInput = JOptionPane.showInputDialog("请输入产品编号：");

        if (salesmanIdInput != null && productIdInput != null) {
            int salesmanId = Integer.parseInt(salesmanIdInput);
            int productId = Integer.parseInt(productIdInput);

            if (salesData.containsKey(salesmanId) && salesData.get(salesmanId).containsKey(productId)) {
                double deletedSalesAmount = salesData.get(salesmanId).remove(productId);
                appendToResultArea("销售员 " + salesmanId + " 的产品 " + productId + " 的销售数据删除成功。");
                appendToResultArea("删除的销售额：" + deletedSalesAmount);
            } else {
                appendToResultArea("销售员 " + salesmanId + " 的产品 " + productId + " 无销售记录。");
            }
            appendToResultArea("_________________");
        }
    }
}

