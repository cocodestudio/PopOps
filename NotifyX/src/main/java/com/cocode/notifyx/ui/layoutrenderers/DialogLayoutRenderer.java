package com.cocode.notifyx.ui.layoutrenderers;

import com.cocode.notifyx.R;
import com.cocode.notifyx.model.Message;

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
