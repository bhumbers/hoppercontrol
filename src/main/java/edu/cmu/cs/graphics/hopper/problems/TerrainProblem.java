package edu.cmu.cs.graphics.hopper.problems;

import org.jbox2d.collision.shapes.ChainShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents some problem space for an avatar to navigate
 * Terrain problems use heightmap representations
 */
public class TerrainProblem extends Problem {
    protected List<Float> heights;
    protected List<Float> peakXDeltas;

    protected Body terrainBody;

    /** Creates a new terrain problem with specified heights and constant peakXDelta between peak heights */
    public TerrainProblem(List<Float> heights, float peakXDelta) {
        this(heights, Collections.nCopies(heights.size() - 1, peakXDelta));
    }

    /** Creates a new terrain problem with specified heights and constant peakXDelta between peak heights
     * peakXDeltas should be heights.size() - 1 in length*/
    public TerrainProblem(List<Float> heights, List<Float> peakXDeltas) {
        this.heights = new ArrayList<Float>();
        this.heights.addAll(heights);

        this.peakXDeltas = new ArrayList<Float>();
        this.peakXDeltas.addAll(peakXDeltas);
    }

    @Override
    public void init(World world) {
        //TODO: Break up the single terrain body into multiple bodies for better performance on long terrains?
        BodyDef bd = new BodyDef();
        bd.type = BodyType.STATIC;
        terrainBody = world.createBody(bd);

        FixtureDef fd = new FixtureDef();
        fd.density = 0.0f;
        fd.shape = new PolygonShape();

        Vec2[] verts = new Vec2[heights.size()];
        float x = 0.0f;
        for (int i = 0; i < heights.size(); i++) {
            verts[i] = new Vec2(x, heights.get(i));
            if (i < heights.size() - 1)
                x += peakXDeltas.get(i);
        }
        ChainShape shape = new ChainShape();
        shape.createChain(verts, verts.length);
        terrainBody.createFixture(shape, 0.0f);
    }
}
