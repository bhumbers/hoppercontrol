package edu.cmu.cs.graphics.hopper.problems;

import edu.cmu.cs.graphics.hopper.control.ControlProvider;
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
public final class TerrainProblemDefinition extends ProblemDefinition {
    //Definition
    final protected List<Float> heights;
    final protected List<Float> peakXDeltas;

    /** Creates a new terrain problem with specified heights and constant peakXDelta between peak heights */
    public TerrainProblemDefinition(List<Float> heights, float peakXDelta) {
        this(heights, Collections.nCopies(heights.size() - 1, peakXDelta));
    }

    /** Creates a new terrain problem with specified heights and constant peakXDelta between peak heights
     * peakXDeltas should be heights.size() - 1 in length*/
    public TerrainProblemDefinition(List<Float> heights, List<Float> peakXDeltas) {
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
        Body terrainBody = world.createBody(bd);

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

    @Override
    public double[] getParamsArray() {
        int n = heights.size() + peakXDeltas.size();
        double[] params = new double[n];
        int i = 0;
        for (Float height : heights)
            params[i++] = height;
        for (Float peakXDelta : peakXDeltas)
            params[i++] = peakXDelta;
        return params;
    }

    @Override
    public Object getState() {
        TerrainProblemState s = new TerrainProblemState();

        s.width = this.heights.size();
        s.height = 1; //this is just a 2D problem, hence just a single terrain row
        s.heights = new double[s.width*s.height];
        s.peakXDeltas = new double[s.width*s.height];

        int i = 0;
        for (Float height : heights)
            s.heights[i++] = height;
        i = 0;
        for (Float peakXDelta : peakXDeltas)
            s.peakXDeltas[i++] = peakXDelta;

        return s;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TerrainProblemDefinition that = (TerrainProblemDefinition) o;

        if (!heights.equals(that.heights)) return false;
        if (!peakXDeltas.equals(that.peakXDeltas)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = heights.hashCode();
        result = 31 * result + peakXDeltas.hashCode();
        return result;
    }
}

class TerrainProblemState {
    int width;
    int height;
    double[] heights;
    double[] peakXDeltas;
}
