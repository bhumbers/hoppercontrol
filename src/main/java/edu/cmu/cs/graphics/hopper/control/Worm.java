package edu.cmu.cs.graphics.hopper.control;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.RevoluteJoint;
import org.jbox2d.dynamics.joints.RevoluteJointDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/** A simple, stable worm-like avatar */
public class Worm extends Avatar {
    private static final Logger log = LoggerFactory.getLogger(Worm.class);

    private final float JOINT_PROP_GAIN = 50.0f;
    private final float JOINT_DRAG_GAIN = 5.0f;

    int numLinks;
    protected WormControl currControl;
    protected List<RevoluteJoint> joints;
    protected List<Body> bodies;

    public Worm(int numLinks) {
        this.numLinks = numLinks;
        setCurrentControl(new WormControl(numLinks)); //default control
    }

    @Override
    public void init(World world) {
        bodies = new ArrayList<Body>();
        joints = new ArrayList<RevoluteJoint>();

        final float LINK_LENGTH = 2.0f;
        final float LINK_WIDTH = 0.2f;

        //TODO: init bodies & joints for worm
        Vec2 offset = new Vec2(-LINK_LENGTH * numLinks,LINK_WIDTH + 0.01f);
        for (int i = 0; i < numLinks; i++) {
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(LINK_LENGTH*0.5f, LINK_WIDTH*0.5f);

            Body link;
            FixtureDef fd = new FixtureDef();
            fd.density = 1.0f;
            fd.friction = 2.0f;
            fd.shape = shape;
            fd.filter.groupIndex = -1;
            BodyDef bd = new BodyDef();
            bd.type = BodyType.DYNAMIC;
            bd.position.set(offset);
            link = world.createBody(bd);
            link.createFixture(fd);
            bodies.add(link);

            if (i > 0) {
                RevoluteJointDef jd = new RevoluteJointDef();
                jd.initialize(bodies.get(i-1), link, (link.getPosition().add(bodies.get(i-1).getPosition())).mulLocal(0.5f));
                jd.collideConnected = false;
                joints.add((RevoluteJoint) world.createJoint(jd));
            }

            offset.x += LINK_LENGTH;
        }
    }

    @Override
    public List<Body> getBodies() {
        return bodies;
    }

    @Override
    public List<? extends Joint> getJoints() {
        return joints;
    }

    @Override
    public Body getMainBody() {
        return bodies.get(0);
    }

    @Override
    public void setCurrentControl(Control control) {
        //Not ideal OO design, but such is life
        if (!(control instanceof  WormControl)) {
            log.error("Tried to assign a non-WormControl object to a Worm avatar");
            return;
        }

        this.currControl = (WormControl)control;
    }

    public Control getCurrentControl() {
        return currControl;
    }


    @Override
    public void update(float dt) {
        if (currControl != null) {
            for (int i = 0; i < joints.size(); i++) {
                ControlUtils.servoTowardAngle(joints.get(i), currControl.targetLinkAngles[i], JOINT_PROP_GAIN, JOINT_DRAG_GAIN);
            }
        }
    }
}
