/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.presentation.framework.core.components.form.flexible.impl;

import org.olat.presentation.framework.core.GUIInterna;
import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.form.flexible.FormBaseComponentIdProvider;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.system.exception.AssertException;

/**
 * Description:<br>
 * provides a html valid form dispatch id needed in the "form component" renderers. This is then the id which can be found in the hidden field for submit info.
 * <P>
 * Initial Date: 11.01.2007 <br>
 * 
 * @author patrickb
 */
public abstract class FormBaseComponentImpl extends Component implements FormBaseComponentIdProvider {

    public FormBaseComponentImpl(String name) {
        super(name);
    }

    public FormBaseComponentImpl(String name, Translator translator) {
        super(name, translator);
    }

    private long replayDispatchId;

    protected void setReplayableDispatchID(long id) {
        replayDispatchId = id;
    }

    @Override
    public String getFormDispatchId() {
        if (GUIInterna.isLoadPerformanceMode()) {
            return DISPPREFIX + replayDispatchId;
        } else {
            return DISPPREFIX + super.getDispatchID();
        }
    }

    @Override
    protected void doDispatchRequest(UserRequest ureq) {
        // Form elements must render themselves in a way that the dispatching
        // is done by the form manager receiving the dispatch request with the
        // form elements dispatch id.
        throw new AssertException("The form element <" + getComponentName() + "> with id <" + getFormDispatchId() + "> should not be dispatched by GUI framework:");
    }

}
