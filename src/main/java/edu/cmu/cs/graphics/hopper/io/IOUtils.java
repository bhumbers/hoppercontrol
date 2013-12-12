package edu.cmu.cs.graphics.hopper.io;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import edu.cmu.cs.graphics.hopper.control.BipedHopperControl;
import edu.cmu.cs.graphics.hopper.control.ControlProvider;
import edu.cmu.cs.graphics.hopper.explore.Explorer;
import edu.cmu.cs.graphics.hopper.explore.ProblemSolutionEntry;
import edu.cmu.cs.graphics.hopper.problems.ObstacleProblemDefinition;
import edu.cmu.cs.graphics.hopper.problems.ProblemDefinition;
import edu.cmu.cs.graphics.hopper.problems.TerrainProblemDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/** Just a class to centralize read/write of various things...
 * not great, modular design, but time is short :) -bh, 12.11.2013 .*/
public class IOUtils {
    private static final Logger log = LoggerFactory.getLogger(IOUtils.class);

    //Singleton
    private static IOUtils _instance;
    public static IOUtils instance() {
        if (_instance == null)
            _instance = new IOUtils();
        return _instance;
    }

    private XStream xstream;

    private IOUtils() {
        xstream = new XStream(new StaxDriver());
        xstream.alias("CtrlProvider", ControlProvider.class);
        xstream.omitField(ControlProvider.class, "currControlIdx");
        xstream.alias("BipedCtrl", BipedHopperControl.class);
        xstream.alias("ProbSolEntry", ProblemSolutionEntry.class);

        xstream.alias("TerrainProbDef", TerrainProblemDefinition.class);
        xstream.alias("ObsProbDef", ObstacleProblemDefinition.class);
    }

    public void ensurePathExists(String path) {
        File filePath = new File(path);
        if (!filePath.exists())     {
            try {
                filePath.mkdirs();
            }
            catch (Exception e) {
                log.error("Error creating file path: " + path);
                logStackTraceError(e);
            }
        }

    }

    public void saveProblemSolutionEntry(ProblemSolutionEntry entry, String path, String filename) {
        String entryXML = xstream.toXML(entry);
        saveToFile(entryXML, path, filename);
    }

    public List<ProblemSolutionEntry> loadAllProblemSolutionEntriesInDir(String path) {
        File[] files = new File(path).listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".sol");
            }
        });

        List<ProblemSolutionEntry> entries = new ArrayList<ProblemSolutionEntry>();
        if (files != null) {
            for (File file : files)
                entries.add(this.loadProblemSolutionEntry(path, file.getName()));
        }
        return entries;
    }

    public ProblemSolutionEntry loadProblemSolutionEntry(String path, String filename) {
        ProblemSolutionEntry entry = null;
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(path + filename);
            entry = (ProblemSolutionEntry)xstream.fromXML(fileReader);
        }
        catch (Exception error) {
            log.error("Problem occurred reading problem solution entry for file " + filename);
            logStackTraceError(error);
        }
        finally {
            try {fileReader.close();}
            catch (Exception error) {
                log.error("Problem occurred while closing file " + filename);
                logStackTraceError(error);
            }
        }

        return entry;
    }

    private void saveToFile(String data, String path, String filename) {
        ensurePathExists(path);

        //Write contents
        Writer writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path + filename), "utf-8"));
            writer.write(data);
        } catch (IOException error) {
            log.error("Error writing file: " + filename);
            logStackTraceError(error);
        } finally {
            try {
                writer.close();
            } catch (Exception error) {
                log.warn("Exception while closing file: " + filename);
                logStackTraceError(error);
            }
        }
    }

    private void logStackTraceError(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        log.error(sw.toString());
    }

}
