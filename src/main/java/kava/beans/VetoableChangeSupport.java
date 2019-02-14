/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package kava.beans;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

// FIXME: obviously need synchronization, when access listeners

public class VetoableChangeSupport implements Serializable {

    private static final long serialVersionUID = -5090210921595982017l;

    private Hashtable<String, VetoableChangeSupport> children = new Hashtable<String, VetoableChangeSupport>();

    private transient ArrayList<kava.beans.VetoableChangeListener> globalListeners = new ArrayList<kava.beans.VetoableChangeListener>();

    private Object source;
    
    @SuppressWarnings("unused")
    // for serialization
    private int vetoableChangeSupportSerializedDataVersion = 2;

    public VetoableChangeSupport(Object sourceBean) {
        if (sourceBean == null) {
            throw new NullPointerException();
        }
        this.source = sourceBean;
    }

    public synchronized void removeVetoableChangeListener(String propertyName,
            kava.beans.VetoableChangeListener listener) {
        if ((propertyName != null) && (listener != null)) {
            VetoableChangeSupport listeners = children.get(propertyName);

            if (listeners != null) {
                listeners.removeVetoableChangeListener(listener);
            }
        }
    }

    public synchronized void addVetoableChangeListener(String propertyName,
            kava.beans.VetoableChangeListener listener) {
        if (propertyName != null && listener != null) {
            VetoableChangeSupport listeners = children.get(propertyName);

            if (listeners == null) {
                listeners = new VetoableChangeSupport(source);
                children.put(propertyName, listeners);
            }
            listeners.addVetoableChangeListener(listener);
        }
    }

    public synchronized kava.beans.VetoableChangeListener[] getVetoableChangeListeners(
            String propertyName) {
        VetoableChangeSupport listeners = null;

        if (propertyName != null) {
            listeners = children.get(propertyName);
        }
        return (listeners == null) ? new kava.beans.VetoableChangeListener[] {}
                : getAsVetoableChangeListenerArray(listeners);
    }

    public synchronized boolean hasListeners(String propertyName) {
        boolean result = globalListeners.size() > 0;
        if (!result && propertyName != null) {
            VetoableChangeSupport listeners = children.get(propertyName);
            if (listeners != null) {
                result = listeners.globalListeners.size() > 0;
            }
        }
        return result;
    }

    public synchronized void removeVetoableChangeListener(
            kava.beans.VetoableChangeListener listener) {
        if (listener != null) {
            globalListeners.remove(listener);
        }
    }

    public synchronized void addVetoableChangeListener(
            kava.beans.VetoableChangeListener listener) {
        if (listener != null) {
            if (listener instanceof kava.beans.VetoableChangeListenerProxy) {
                kava.beans.VetoableChangeListenerProxy proxy = (kava.beans.VetoableChangeListenerProxy) listener;
                addVetoableChangeListener(proxy.getPropertyName(),
                        (kava.beans.VetoableChangeListener) proxy.getListener());
            } else {
                globalListeners.add(listener);
            }
        }
    }

    public synchronized kava.beans.VetoableChangeListener[] getVetoableChangeListeners() {
        List<kava.beans.VetoableChangeListener> result = new ArrayList<kava.beans.VetoableChangeListener>();
        if (globalListeners != null) {
            result.addAll(globalListeners);
        }

        for (Iterator<String> iterator = children.keySet().iterator(); iterator
                .hasNext();) {
            String propertyName = iterator.next();
            VetoableChangeSupport namedListener = children
                    .get(propertyName);
            kava.beans.VetoableChangeListener[] childListeners = namedListener
                    .getVetoableChangeListeners();
            for (int i = 0; i < childListeners.length; i++) {
                result.add(new VetoableChangeListenerProxy(propertyName,
                        childListeners[i]));
            }
        }
        return (result
                .toArray(new kava.beans.VetoableChangeListener[result.size()]));
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        kava.beans.VetoableChangeListener[] copy = new kava.beans.VetoableChangeListener[globalListeners
                .size()];
        globalListeners.toArray(copy);
        for (kava.beans.VetoableChangeListener listener : copy) {
            if (listener instanceof Serializable) {
                oos.writeObject(listener);
            }
        }
        // Denotes end of list
        oos.writeObject(null);

    }

    private void readObject(ObjectInputStream ois) throws IOException,
            ClassNotFoundException {
        ois.defaultReadObject();
        this.globalListeners = new ArrayList<kava.beans.VetoableChangeListener>();
        if (null == this.children) {
            this.children = new Hashtable<String, VetoableChangeSupport>();
        }
        Object listener;
        do {
            // Reads a listener _or_ proxy
            listener = ois.readObject();
            addVetoableChangeListener((kava.beans.VetoableChangeListener) listener);
        } while (listener != null);
    }

    @SuppressWarnings("boxing")
    public void fireVetoableChange(String propertyName, boolean oldValue,
            boolean newValue) throws kava.beans.PropertyVetoException
	{
        kava.beans.PropertyChangeEvent event = createPropertyChangeEvent(propertyName,
                oldValue, newValue);
        doFirePropertyChange(event);
    }

    @SuppressWarnings("boxing")
    public void fireVetoableChange(String propertyName, int oldValue,
            int newValue) throws kava.beans.PropertyVetoException
	{
        kava.beans.PropertyChangeEvent event = createPropertyChangeEvent(propertyName,
                oldValue, newValue);
        doFirePropertyChange(event);
    }

    public void fireVetoableChange(String propertyName, Object oldValue,
            Object newValue) throws kava.beans.PropertyVetoException
	{
        kava.beans.PropertyChangeEvent event = createPropertyChangeEvent(propertyName,
                oldValue, newValue);
        doFirePropertyChange(event);
    }

    public void fireVetoableChange(kava.beans.PropertyChangeEvent event)
            throws kava.beans.PropertyVetoException
	{
        doFirePropertyChange(event);
    }

    private kava.beans.PropertyChangeEvent createPropertyChangeEvent(String propertyName,
            Object oldValue, Object newValue) {
        return new kava.beans.PropertyChangeEvent(source, propertyName, oldValue, newValue);
    }

    private void doFirePropertyChange(kava.beans.PropertyChangeEvent event)
            throws kava.beans.PropertyVetoException
	{
        String propName = event.getPropertyName();
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();

        if (newValue != null && oldValue != null && newValue.equals(oldValue)) {
            return;
        }

        /* Take note of who we are going to notify (and potentially un-notify) */

        kava.beans.VetoableChangeListener[] listensToAll;
        VetoableChangeSupport listeners = null;
        // property change
        synchronized (this) {
            listensToAll = globalListeners
                    .toArray(new kava.beans.VetoableChangeListener[0]);
            String propertyName = event.getPropertyName();
            if (propertyName != null) {
                listeners = children.get(propertyName);
            }
        }

        try {
            for (kava.beans.VetoableChangeListener listener : listensToAll) {
                listener.vetoableChange(event);
            }
        } catch (kava.beans.PropertyVetoException pve) {
            // Tell them we have changed it back
            PropertyChangeEvent revertEvent = createPropertyChangeEvent(
                    propName, newValue, oldValue);
            for (kava.beans.VetoableChangeListener listener : listensToAll) {
                try {
                    listener.vetoableChange(revertEvent);
                } catch (PropertyVetoException ignored) {
                    // expected
                }
            }
            throw pve;
        }
        if (listeners != null) {
            listeners.fireVetoableChange(event);
        }
    }

    private static kava.beans.VetoableChangeListener[] getAsVetoableChangeListenerArray(
            VetoableChangeSupport listeners) {
        return listeners.globalListeners.toArray(new VetoableChangeListener[0]);
    }
}
