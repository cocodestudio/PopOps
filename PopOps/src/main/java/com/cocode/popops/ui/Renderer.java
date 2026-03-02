package com.cocode.popops.ui;

import com.cocode.popops.model.Message;
import com.cocode.popops.ui.factory.PresentationRenderer;
import com.cocode.popops.ui.factory.RendererFactory;

public final class Renderer {
    private Renderer() {
    }

    public static void render(Message m) {
        if (m == null) return;
        PresentationRenderer r = RendererFactory.get(m.presentation);
        if (r == null) return;
        r.render(m);
    }
}
