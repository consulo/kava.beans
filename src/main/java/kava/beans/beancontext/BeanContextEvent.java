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

package kava.beans.beancontext;

import java.util.EventObject;

public abstract class BeanContextEvent extends EventObject {

    private static final long serialVersionUID = 7267998073569045052L;
    
    /**
     * @serial
     */
    protected kava.beans.beancontext.BeanContext propagatedFrom;

    protected BeanContextEvent(kava.beans.beancontext.BeanContext bc) {
        super(bc);
    }

    public kava.beans.beancontext.BeanContext getBeanContext() {
        return (kava.beans.beancontext.BeanContext) super.getSource();
    }

    public synchronized kava.beans.beancontext.BeanContext getPropagatedFrom() {
        return this.propagatedFrom;
    }

    public synchronized boolean isPropagated() {
        return (this.propagatedFrom != null);
    }

    public synchronized void setPropagatedFrom(BeanContext bc) {
        this.propagatedFrom = bc;
    }
}
