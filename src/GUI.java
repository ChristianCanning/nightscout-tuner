import net.miginfocom.swing.MigLayout; // I am using migLayout for my layout manager http://www.miglayout.com/

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.*;

// The gui class handles most of the gui elements, as well as user input
public class GUI implements ActionListener
{
    private String urlString; // Nightscout URL
    private ZonedDateTime dateStart; // Start date, inclusive
    private ZonedDateTime dateEnd; // End date, non-inclusive

    private JFrame frame;

    private JPanel mainPanel; // Panel that fills up the entire frame

    private JPanel leftPanel; // Upper left side of the main panel

    private JPanel chartPanel; // Right side of the main panel
    private JScrollPane chartScroll;

    private JPanel basalPanel; // Lower left side of the main panel, below leftPanel
    private JTable basalTable;
    private JScrollPane basalScroll;

    private JPanel panel1; // Panel w/ url input box
    private JTextField urlInput;
    private JLabel urlText;

    private JPanel panel2; // Panel w/ date start and end selection
    private JLabel dateToText;
    private JComboBox dateStartBox;
    private JComboBox dateEndBox;

    private JPanel panel3; // Panel w/ options to enable charts and correct basals
    private JCheckBox selectBasalChart;
    private JComboBox basalChartPeriod; // View basals by 30/60 minute intervals
    private JCheckBox selectBGChart;
    private JCheckBox selectCOBChart;
    private JTextField COBChartRateInput; // Carbs absorbed per hour, input by user
    private JCheckBox selectAdjustBasalRates; // Option to enable correcting basals based on average blood sugars and basals that were given
    private JTextField ISFInput; // Insulin Sensitivity Factor = how many BG points 1 unit of insulin drops, input by user
    private JTextField weightInput; // Weight of user in KG
    private JTextField minimumBGInput; // The minimum BG that adjustBasalRates can bring the blood sugar down to. Will also try its best to bring low BGs above this,
    //but all it ensures is that the extra insulin given by the adjusted basal rates won't bring any high blood sugars below the minimum BG set by the user.
    private JTextField insulinPoolInput; //

    private JPanel panel4; // Panel w/ run button which starts the code
    private JButton button;

    public GUI()
    {
        // Create array of String start and end dates to put into the dateStart and dateEnd selection boxes
        ZonedDateTime currentDay = ZonedDateTime.now();
        String[] availableDaysEnd = new String[60];
        String[] availableDaysStart = new String[60];
        for (int i = 1; i < 61; i++)
        {
            availableDaysEnd[i-1] = currentDay.minusDays(i).toLocalDate().toString();
            availableDaysStart[i-1] = currentDay.minusDays(i).toLocalDate().toString();
        }

        // The rest of the gui() constructor code sets how the gui will be placed and displayed.
        panel1 = new JPanel();
        panel1.setLayout(new MigLayout());

        frame = new JFrame();

        urlText = new JLabel("URL:   ");
        panel1.add(urlText, "width :30:, height :30:");

        urlInput = new JTextField(20);
        panel1.add(urlInput, "width :330:, height :30:");

        panel2 = new JPanel();
        panel2.setLayout(new MigLayout());
        dateStartBox = new JComboBox(availableDaysStart);
        panel2.add(dateStartBox, "width :160:, height :30:");

        dateToText = new JLabel("   to");
        panel2.add(dateToText, "width :40:, height :30:");

        dateEndBox = new JComboBox(availableDaysEnd);
        panel2.add(dateEndBox, "width :160:, height :30:");

        panel3 = new JPanel(new MigLayout());
        selectBasalChart = new JCheckBox("Basal Chart");
        panel3.add(selectBasalChart, "width :30:");
        String[] basalChartPeriods = new String[]{ "30", "60"};
        basalChartPeriod = new JComboBox(basalChartPeriods);
        panel3.add(basalChartPeriod, "align left, width :35:");
        panel3.add(new JLabel("Minute Interval"), "wrap");

        selectBGChart = new JCheckBox("BG Chart");
        panel3.add(selectBGChart, "width :30: ,wrap");

        selectCOBChart = new JCheckBox("COB Chart");
        panel3.add(selectCOBChart, "width :30:");
        COBChartRateInput = new JTextField();
        panel3.add(COBChartRateInput, "align left, width :35:");
        panel3.add(new JLabel ("Carbs absorbed per hour"), "wrap");

        selectAdjustBasalRates = new JCheckBox("Adjust Basal Rates");
        panel3.add(selectAdjustBasalRates, "width :30:");
        ISFInput = new JTextField();
        panel3.add(ISFInput, "align left, width :35:");
        panel3.add(new JLabel("Insulin Sensitivity Factor"), "wrap");

        panel3.add(new JLabel());
        weightInput = new JTextField();
        panel3.add(weightInput, "align left, width :35:");
        panel3.add(new JLabel("Weight (In KG. *NOT* POUNDS)"), "wrap");

        panel3.add(new JLabel());
        minimumBGInput = new JTextField();
        panel3.add(minimumBGInput, "align left, width :35:");
        panel3.add(new JLabel("Minimum BG"), "wrap");

        panel3.add(new JLabel());
        insulinPoolInput = new JTextField();
        panel3.add(insulinPoolInput, "align left, width :35:");
        panel3.add(new JLabel("Insulin Pooling Amount (mins)"), "wrap");

        panel4 = new JPanel(new MigLayout());
        button = new JButton("GO");
        button.addActionListener(this);
        panel4.add(button, "width :360:, height 50:50:");

        basalPanel = new JPanel();
        basalPanel.setLayout(new MigLayout());
        basalScroll = new JScrollPane(basalPanel);

        leftPanel = new JPanel();
        leftPanel.setLayout(new MigLayout());
        leftPanel.setPreferredSize(new Dimension(360, 700));
        leftPanel.add(panel1, "wrap");
        leftPanel.add(panel2, "wrap");
        leftPanel.add(panel3, "wrap");
        leftPanel.add(panel4, "wrap");
        leftPanel.add(basalScroll, "width :360:, height :420:");

        chartPanel = new JPanel();
        chartPanel.setLayout(new MigLayout());
        chartScroll = new JScrollPane(chartPanel);
        chartScroll.setPreferredSize(new Dimension(740, 700));

        mainPanel = new JPanel();
        mainPanel.setLayout(new MigLayout());
        mainPanel.setPreferredSize(new Dimension(1100, 700));
        mainPanel.add(leftPanel, "width :360:");
        mainPanel.add(chartScroll, "width :740:");


        frame.setSize(1100, 620);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(mainPanel);
        frame.setTitle("NIGHTSCOUT TUNER");
        frame.pack();
        frame.setVisible(true);

    }
    // Method to add add a chart to the chartPanel on the right
    public void addChart(JPanel chart)
    {
        chartPanel.add(chart, "wrap");
        frame.setVisible(true);
    }

    //Start running this code when 'button' is pressed
    public void actionPerformed(ActionEvent e)
    {
        urlString = urlInput.getText();
        dateStart = LocalDate.parse(dateStartBox.getSelectedItem().toString()).atStartOfDay().atZone(ZoneId.systemDefault());
        //Add one day to the dateEnd because it is normally non-inclusive. Adding a day, makes sure that if the user input
        //the same day for dateStart and dateEnd, that it will actually show the output for that one day, rather than showing nothing
        dateEnd = LocalDate.parse(dateEndBox.getSelectedItem().toString()).atStartOfDay().atZone(ZoneId.systemDefault()).plusDays(1);

        // Only download the profile if we haven't already downloaded it. Since the entire profile is downloaded from nightscout,
        // which disregards what our start and end date are, we don't want to keep downloading the same thing every time the user
        // presses the 'button'
        ParseJSON.setProfile(urlString);

        this.chartPanel.removeAll(); // Clear the chartPanel
        this.basalPanel.removeAll(); // Clear the basalPanel

        Thread display = new Thread(() ->
        {
            if(selectAdjustBasalRates.isSelected())
            {
                int period = Integer.parseInt((String)basalChartPeriod.getSelectedItem()); // Choose to adjust basals by 30 or 60 minutes
                double weight = Double.parseDouble(weightInput.getText()); // Weight of user in KG
                double[] basalAverages = Chart.averageBasals(urlString, dateStart, dateEnd, period, false);
                // Get the Duration of Insulin Activity for every 5 minute period in the day.
                // Each 5 minute period takes into account the previous minutes based on what the insulinPoolInput is.
                double[] DIA = Calculations.getDIA(ParseJSON.getCorrectionBolus(urlString, dateStart, dateEnd), urlString, dateStart, dateEnd, weight, Integer.parseInt(insulinPoolInput.getText()));

                // Get the corrected basals in the 4th row of a matrix. The first row has the basal times, the second has the basals from the profile,
                // and the third has the basals that actually ran, which includes the profile basals and temp basals
                String[][] correctedBasals = Chart.adjustAverageBGs(urlString, dateStart, dateEnd, Integer.parseInt(minimumBGInput.getText()), Double.parseDouble(ISFInput.getText()), period, weight, DIA, basalAverages);
                String[] columnNames = {"", "", "", ""}; // Let Swing know to show a table with 4 columns
                basalTable = new JTable(correctedBasals, columnNames);
                basalPanel.add(basalTable);
                frame.setVisible(true);
            }
        });
        display.start();

        // Do nothing until we finish adjusting the basals and displaying the chart
        while(display.isAlive())
        {

        }

        // If a chart is selected, run the code to display the chart
        if(selectBasalChart.isSelected())
            Chart.averageBasals(urlString, dateStart, dateEnd, Integer.parseInt((String)basalChartPeriod.getSelectedItem()), true);
        if(selectBGChart.isSelected() && !selectAdjustBasalRates.isSelected())
            Chart.averageBGs(urlString, dateStart, dateEnd, true);
        if(selectCOBChart.isSelected())
            Chart.averageCOB(urlString, dateStart, dateEnd, Double.parseDouble(COBChartRateInput.getText()));


    }

}
