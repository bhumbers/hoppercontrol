package edu.cmu.cs.graphics.hopper.eval;

import edu.cmu.cs.graphics.hopper.control.Avatar;
import edu.cmu.cs.graphics.hopper.control.BipedHopper;
import edu.cmu.cs.graphics.hopper.control.ControlPrim;
import edu.cmu.cs.graphics.hopper.problems.ProblemDefinition;
import edu.cmu.cs.graphics.hopper.problems.ProblemInstance;
import org.jbox2d.common.Color3f;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.contacts.Contact;

import java.text.DecimalFormat;
import java.util.List;

/** An evaluator that judges success/fitness based on obstacle-clearing behavior of a biped hopper.
 * Only applicable to problems where biped hopper is used, since we use a bunch of type-specific knowledge about
 * when the hopper meets with various success/failure conditions. */
public class BipedObstacleEvaluator extends Evaluator {
    static DecimalFormat numFormat = new DecimalFormat( "#,###,###,##0.000" );

    protected final BipedObstacleEvaluatorDefinition def;

    //Fitness evaluation for current run
    protected float fitness;
    protected Status status;

    protected float timeThatMinXReached;
    protected float timeThatUprightReached;

    protected float timeInSuccessRegion;
    protected float timeUpright;
    protected float timeUprightInSuccessRegion;

    protected boolean avatarFellOver;

    //Debug graphics stuff
    Color3f inSuccessRegionStatusColor = new Color3f();
    Color3f timeUprightStatusColor = new Color3f();
    Color3f timeUprightInSuccessRegionStatusColor = new Color3f();

    BipedObstacleEvaluator(BipedObstacleEvaluatorDefinition def) {
        this.def = def;
    }

    @Override
    public void init() {
        status = Status.RUNNING;
        timeThatMinXReached = -1.0f;
        timeThatUprightReached = -1.0f;
        timeInSuccessRegion = 0.0f;
        timeUpright = 0.0f;
        timeUprightInSuccessRegion = 0.0f;
        avatarFellOver = false;
    }

    @Override
    public Status getStatus() { return status; }

    @Override
    public float getFitness() { return fitness; }

    @Override
    public void updateEvaluation(ProblemInstance problem) {
        //If main body touches ground at any point, then we consider that failure
        BipedHopper hopper = (BipedHopper)problem.getAvatar();
        if (!avatarFellOver) {
            //Check each contact... if one is the avatar chassis & one is the ground, mark it as falling over
            for (Contact c : problem.getCurrentContacts()) {
                Body chassis = hopper.getMainBody();
                Body ground = problem.getGroundBody();
                Body bodyA = c.getFixtureA().getBody();
                Body bodyB = c.getFixtureB().getBody();
                if ((bodyA == chassis && bodyB == ground) ||
                        (bodyA == ground && bodyB == chassis))  {
                    avatarFellOver = true;
                }
            }
        }

        //Update whether we've crossed minimum x threshold
        if (problem.getAvatar().getMainBody().getPosition().x > def.minXForSuccess) {
            if (timeThatMinXReached < 0)
                timeThatMinXReached = problem.getSimTime();
            timeInSuccessRegion = problem.getSimTime() - timeThatMinXReached;
        }
        else {
            timeThatMinXReached = -1.0f;
            timeInSuccessRegion = 0.0f;
        }

        //Update whether & for how long hopper has been upright
        if (Math.abs(problem.getAvatar().getMainBody().getAngle()) <= def.maxUprightDeviation)  {
            if (timeThatUprightReached < 0)
                timeThatUprightReached = problem.getSimTime();
            timeUpright = problem.getSimTime() - timeThatUprightReached;
        }
        else {
            timeThatUprightReached = -1.0f;
            timeUpright = 0.0f;
        }

        //Update status colors
        if (timeInSuccessRegion > 0.0f)
            inSuccessRegionStatusColor.set(Color3f.GREEN);
        else
            inSuccessRegionStatusColor.set(Color3f.RED);
        if (timeUpright > 0.0f)
            timeUprightStatusColor.set(Color3f.GREEN);
        else
            timeUprightStatusColor.set(Color3f.RED);
        if (timeUprightInSuccessRegion > 0.0f)
            timeUprightInSuccessRegionStatusColor.set(Color3f.WHITE);
        else
            timeUprightInSuccessRegionStatusColor.set(Color3f.BLACK);

        //Update time upright while at or beyond minimum x threshold
        if (timeThatMinXReached > 0 && timeThatUprightReached > 0) {
            if (timeThatUprightReached >= timeThatMinXReached)  //upright after entering success region
                timeUprightInSuccessRegion = problem.getSimTime() - timeThatUprightReached;
            else //upright before entering success region
                timeUprightInSuccessRegion = timeInSuccessRegion;
        }
        else
            timeUprightInSuccessRegion = 0;


        //Update our success/failure status
        if (avatarFellOver || problem.getSimTime() > def.maxTime)
            status = Status.FAILURE;
        //If we've reached success threshold x, we may have succeeded...
        else if (timeThatMinXReached > 0) {
            //Ensure we're stable before declaring success (otherwise, might have fallen over at or beyond success line)
            //Success if no upright time is required in success region or if we've been upright long enough
            if (def.minConsecutiveUprightTimeAfterMinXReached < 0 ||
                    timeUprightInSuccessRegion > def.minConsecutiveUprightTimeAfterMinXReached) {
                status = Status.SUCCESS;
            }
        }
//        else
//            status = Status.RUNNING;
    }

    @Override
    public void finishEvaluation(ProblemInstance problem) {
        //TODO: Set fitness based on distance that avatar moved forward
    }

    @Override
    public void appendDebugTextLines(List<String> lines, List<Color3f> colors) {
        super.appendDebugTextLines(lines, colors);

        lines.add("Time in success region:         " + numFormat.format(timeInSuccessRegion));
        colors.add(inSuccessRegionStatusColor);
        lines.add("Upright time:                              " + numFormat.format(timeUpright));
        colors.add(timeUprightStatusColor);
        lines.add("Time upright in success region: " + numFormat.format(timeUprightInSuccessRegion));
        colors.add(timeUprightInSuccessRegionStatusColor);

    }
}
