package com.cocode.notifyx.ui;

import com.cocode.notifyx.model.Message;
import com.cocode.notifyx.ui.factory.PresentationRenderer;
import com.cocode.notifyx.ui.factory.RendererFactory;

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
