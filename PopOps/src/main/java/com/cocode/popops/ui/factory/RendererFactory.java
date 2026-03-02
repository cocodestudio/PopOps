package com.cocode.popops.ui.factory;

import com.cocode.popops.model.PresentationType;
import com.cocode.popops.ui.renderers.DialogRenderer;
import com.cocode.popops.ui.renderers.ToastRenderer;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry of presentation renderers. Add new renderers here.
 */
public final class RendererFactory {
    private static final Map<PresentationType, PresentationRenderer> MAP = new HashMap<>();

    static {
        MAP.put(PresentationType.DIALOG, new DialogRenderer());
        MAP.put(PresentationType.TOAST, new ToastRenderer());
    }

    public static PresentationRenderer get(PresentationType p) {
        PresentationRenderer renderer = MAP.get(p);
        return renderer != null ? renderer : MAP.get(PresentationType.DIALOG);
    }
}
