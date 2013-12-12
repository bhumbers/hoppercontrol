package edu.cmu.cs.graphics.hopper.explore;

import java.util.ArrayList;
import java.util.List;

/** Used to record progress of an Explorer */
public class ExplorerLog {
    public List<ExplorerLogEntry> entries;

    public ExplorerLog() {
        entries = new ArrayList<ExplorerLogEntry>();
    }

    public String getCSVHeader()    {
        return "Num Tests, Num Challenges, Num Unsolved, Num Solved, Num Failed,\n";
    }

    //Writes complete log to CSV string
    public String toCSV() {
        StringBuilder sb = new StringBuilder();
        sb.append(getCSVHeader());
        for (ExplorerLogEntry entry : entries)
            sb.append(entry.getCSVRow());
        return sb.toString();
    }
}
