package edu.cmu.cs.graphics.hopper.retrieval;

import edu.cmu.cs.graphics.hopper.control.AvatarDefinition;
import edu.cmu.cs.graphics.hopper.control.BipedHopperDefinition;
import edu.cmu.cs.graphics.hopper.control.ControlProviderDefinition;
import edu.cmu.cs.graphics.hopper.eval.BipedObstacleEvaluatorDefinition;
import edu.cmu.cs.graphics.hopper.eval.Evaluator;
import edu.cmu.cs.graphics.hopper.eval.EvaluatorDefinition;
import edu.cmu.cs.graphics.hopper.net.*;
import edu.cmu.cs.graphics.hopper.problems.ProblemDefinition;
import edu.cmu.cs.graphics.hopper.problems.ProblemInstance;
import edu.cmu.cs.graphics.hopper.problems.TerrainProblemDefinition;
import org.apache.commons.cli.*;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Program that can load a hopper problem, obtain a predicted control, and reports the result
 * to a crowdanim control server*/
public class RetrievalTest {

    private static final Logger log = LoggerFactory.getLogger(RetrievalTest.class);

    public static void main(String[] args) {
        //HACK: "Dear HTTPClient logging: Please, shut up!"
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");

        DOMConfigurator.configure("config/log4j.xml");

        Options options = new Options();
        options.addOption("configFile", true, "Retrieval configuration file name/path");

        CommandLineParser parser = new GnuParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        }
        catch (ParseException parseError) {
            log.error("Error occurred while parsing command line inputs");
            parseError.printStackTrace();
            return;
        }

        String configFilePath = cmd.getOptionValue("configFile");
        log.info("Loading config file: " + configFilePath);
        Configuration config = null;
        if (configFilePath != null && !configFilePath.isEmpty()) {
            try {
                File configFile = new File(configFilePath);
                String fullFilePath = configFile.getAbsolutePath();
                config = new PropertiesConfiguration(fullFilePath);
            } catch (ConfigurationException e) {
                log.error("Error while trying to load exploration config file: " + configFilePath);
                e.printStackTrace();
                return;
            }
        }
        else {
            log.error("No config file specified! Exiting... ");
            return;
        }

        int numProblems = config.getInt("numProblems");
        int terrainSeed = config.getInt("terrainSeed");
        int terrainLength = config.getInt("terrainLength");
        float terrainDeltaX = config.getFloat("terrainDeltaX");
        String[] terrainMaxAmpStrs = config.getStringArray("terrainMaxAmp");
        float[] terrainMaxAmps = new float[terrainMaxAmpStrs.length];
        for (int i = 0; i < terrainMaxAmps.length; i++)
            terrainMaxAmps[i] = Float.parseFloat(terrainMaxAmpStrs[i]);

        long t0 = System.currentTimeMillis();

        List<ProblemDefinition> problems = new ArrayList<ProblemDefinition>();

        //Terrain test
        for (int ampIdx = 0; ampIdx < terrainMaxAmps.length; ampIdx++) {
            float terrainMaxAmp = terrainMaxAmps[ampIdx];
            Random r = new Random();
            r.setSeed(terrainSeed);
            for (int i = 0; i < numProblems; i++) {
                float y = 0.0f;
                List<Float> verts = new ArrayList<Float>(terrainLength);
                verts.add(0.0f);      //initial "ground" node
                for (int j = 0; j < terrainLength; j++) {
                    y = terrainMaxAmp*(r.nextFloat());
                    if (y < 0)
                        y = 0;
                    verts.add(y);
                }
                problems.add(new TerrainProblemDefinition(verts, terrainDeltaX));
            }
        }

        ContolServerInterface server = new ContolServerInterface("gs13099.sp.cs.cmu.edu", 8081);
        server.sendTestMsg();

        AvatarDefinition avatarDef = new BipedHopperDefinition();

        float maxTime = 15.0f;
        float minXForSuccess = terrainLength * terrainDeltaX;
        float maxUprightDeviation = 1.0f;
        float minConsecutiveUprightTimeAfterMinXReached = 3.0f;
        EvaluatorDefinition evalDef = new BipedObstacleEvaluatorDefinition(maxTime, minXForSuccess, maxUprightDeviation, minConsecutiveUprightTimeAfterMinXReached);

        int i = 0;
        for (ProblemDefinition probDef : problems) {
            ProblemInstance prob = new ProblemInstance(probDef, avatarDef, evalDef, null);
            prob.init();

            //TODO: Set control received from control server
            PlayContext context = new PlayContext();
            context.avatarState = prob.getAvatar().getState();
            context.problemState = probDef.getState();
            ControlProviderDefinition controlDef = server.getControlForContext(context);

            prob.getAvatar().setControlProvider(controlDef.create());

            prob.run();

            Evaluator.Status evalResult = prob.getStatus();
            log.info("Problem #" + i + " eval: " + evalResult);

            i++;
        }

    }
}
