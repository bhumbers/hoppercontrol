package edu.cmu.cs.graphics.hopper.control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** An instantiable definition of a control provider */
public class ControlProviderDefinition<C extends Control> {
    public final List<C> controls;

    public ControlProviderDefinition(List<C> controls) {
        this.controls = Collections.unmodifiableList(new ArrayList<C>(controls));
    }

    public ControlProvider<C> create() {
        ControlProvider<C> provider = new ControlProvider<C>();
        for (int i = 0; i < this.controls.size(); i++)
            provider.controls.add((C)this.controls.get(i).duplicate());
        return provider;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ControlProviderDefinition that = (ControlProviderDefinition) o;

        if (!controls.equals(that.controls)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return controls.hashCode();
    }
}
