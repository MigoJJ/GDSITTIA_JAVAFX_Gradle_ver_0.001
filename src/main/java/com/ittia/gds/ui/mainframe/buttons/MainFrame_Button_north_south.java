package com.ittia.gds.ui.mainframe.buttons;

/**
 * A utility class to dispatch button actions for the main frame's north and south panels.
 * This class acts as an entry point for UI events (single and double clicks) and
 * delegates the actual execution to the MainFrameButtonExe class. This separates
 * event handling from action implementation.
 */
public class MainFrame_Button_north_south {

    /**
     * Processes single-click button events by delegating to the execution class.
     * This is called directly from the button's mouse listener.
     *
     * @param btn The name of the button that was clicked.
     * @param location The panel where the button resides ("north" or "south").
     */
    public static void EMR_B_1entryentry(String btn, String location) {
        MainFrameButtonExe.executeSingleClick(btn, location);
    }

    /**
     * Processes double-click button events by delegating to the execution class.
     * This is called directly from the button's mouse listener.
     *
     * @param btn The name of the button that was double-clicked.
     * @param location The panel where the button resides ("north" or "south").
     */
    public static void EMR_B_2entryentry(String btn, String location) {
        MainFrameButtonExe.executeDoubleClick(btn, location);
    }
}
