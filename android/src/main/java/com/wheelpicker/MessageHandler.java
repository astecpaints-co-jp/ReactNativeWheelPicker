package com.wheelpicker;
import android.os.Handler;
import android.os.Message;

// Referenced classes of package com.qingchifan.view:
//            LoopView

final class MessageHandler extends Handler {

    final LoopView loopview;

    MessageHandler(LoopView loopview) {
        super();
        this.loopview = loopview;
    }

    @Override
    public final void handleMessage(Message paramMessage) {
        if (paramMessage.what == 1000)
            this.loopview.invalidate();
        while (true) {
            if (paramMessage.what == 2000) {
                LoopView.smoothScroll(loopview);
            }
            else if (paramMessage.what == 3000) {
                int selected = loopview.getSelectedItem();
                if (loopview.isItemDisabled(selected)) {
                    int nearest = loopview.findNearestEnabled(selected);
                    if (nearest != -1 && nearest != selected) {
                        loopview.setSelectedItem(nearest);
                        return;
                    }
                }
                this.loopview.itemSelected();
            }
            super.handleMessage(paramMessage);
            return;
        }
    }

}
