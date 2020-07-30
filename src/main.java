import javax.swing.*;

// This is where the program starts.
public class main
{
    private static gui mainGUI; // Define the gui object so that we can reference it later to change elements
    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException
    {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); // Set the UI theme to match the operating system the program runs on.
        mainGUI = new gui(); // Start the mainGUI gui object
    }
    //return the mainGUI gui object
    public static gui getGUI()
    {
        return mainGUI;
    }
}

