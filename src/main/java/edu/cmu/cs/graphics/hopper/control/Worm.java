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
public class Worm extends Avatar<WormControl> {
    private static final Logger log = LoggerFactory.getLogger(Worm.class);

    private final float JOINT_PROP_GAIN = 50.0f;
    private final float JOINT_DRAG_GAIN = 5.0f;

    protected int numLinks;
    protected float controlTimestep;
    protected ControlProvider<WormControl> controlProvider;

    protected List<RevoluteJoint> joints;
    protected List<Body> bodies;

    protected float timeSinceControlStep;

    public Worm(int numLinks, float controlTimestep) {
        this.numLinks = numLinks;
        this.controlTimestep = controlTimestep;

        //Default control provider
        setControlProvider(new ControlProvider<WormControl>());
        controlProvider.specifyControlForIndex(new WormControl(numLinks), 0);
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
    public void setControlProvider(ControlProvider<WormControl> provider) {
        this.controlProvider = provider;
    }

    public WormControl getCurrentControl() {
        return controlProvider.getCurrControl();
    }

    @Override
    public void update(float dt) {
        //Update to next control from provider if enough time has passed
        if (timeSinceControlStep > controlTimestep) {
            controlProvider.goToNextControl();
            timeSinceControlStep = 0;
        }

        if (controlProvider.getCurrControl() != null) {
            for (int i = 0; i < joints.size(); i++) {
                ControlUtils.servoTowardAngle(joints.get(i), controlProvider.getCurrControl().targetLinkAngles[i], JOINT_PROP_GAIN, JOINT_DRAG_GAIN);
            }
        }

        timeSinceControlStep += dt;
    }
}
