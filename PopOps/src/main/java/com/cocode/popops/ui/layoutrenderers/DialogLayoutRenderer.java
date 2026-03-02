package com.cocode.popops.ui.layoutrenderers;

import com.cocode.popops.R;
import com.cocode.popops.model.Message;

public class DialogLayoutRenderer {
    public DialogLayoutRenderer() {
    }

    public int getLayout(Message message) {
        switch (message.type) {
            case APP_UPDATE:
                return R.layout.dialog_announcement;
            case WARNING:
                return R.layout.dialog_warning;
            case INFORMATIONAL:
                return R.layout.dialog_informational;
        }

        return R.layout.dialog_warning;
    }
}
