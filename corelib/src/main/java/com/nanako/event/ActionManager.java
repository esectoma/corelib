package com.nanako.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ActionManager {

    private static ActionManager sInstance;
    private HashMap<Class, List<Listener>> mActionListenerMap = new HashMap<>();

    private ActionManager() {

    }

    public static ActionManager getInstance() {
        if (sInstance == null) {
            synchronized (ActionManager.class) {
                if (sInstance == null) {
                    sInstance = new ActionManager();
                }
            }
        }
        return sInstance;
    }

    public void addInterestedActions(Listener listener, Class[] actionClasses) {
        for (Class ac : actionClasses) {
            addInterestedAction(listener, ac);
        }
    }

    public void addInterestedAction(Listener listener, Class actionClass) {
        List<Listener> listeners = mActionListenerMap.get(actionClass);
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        listeners.add(listener);
        mActionListenerMap.put(actionClass, listeners);
    }

    public void removeInterestedActions(Listener listener) {
        Iterator<Map.Entry<Class, List<Listener>>> it = mActionListenerMap.entrySet().iterator();
        List<Listener> value;
        int index;
        while (it.hasNext()) {
            value = it.next().getValue();
            if ((index = value.indexOf(listener)) != -1) {
                value.remove(index);
                if (value.isEmpty()) {
                    it.remove();
                }
            }
        }
    }

    public void triggerAction(Object action) {
        List<Listener> listeners = mActionListenerMap.get(action.getClass());
        if (listeners != null && !listeners.isEmpty()) {
            int size = listeners.size();
            for (int i = size - 1; i >= 0; i--) {
                listeners.get(i).onActionTriggered(action);
            }
        }
    }

    public interface Listener {
        void onActionTriggered(Object action);
    }
}
