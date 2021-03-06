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
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.presentation.portal.macartney;

import java.util.Map;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.control.Controller;
import org.olat.presentation.framework.core.control.WindowControl;
import org.olat.presentation.framework.core.control.generic.portal.AbstractPortlet;
import org.olat.presentation.framework.core.control.generic.portal.Portlet;
import org.olat.presentation.framework.core.translator.PackageTranslator;
import org.olat.presentation.framework.core.translator.PackageUtil;

/**
 * Description:<br>
 * Displays the the macartney of the day comic strip
 * <P>
 * Initial Date: 24.08.2005 <br>
 * 
 * @author gnaegi
 */
public class MacartneyPortlet extends AbstractPortlet {
    private Controller runCtr;

    /**
	 */

    protected MacartneyPortlet() {
    }

    @Override
    public Portlet createInstance(final WindowControl wControl, final UserRequest ureq, final Map configuration) {
        final Portlet p = new MacartneyPortlet();
        p.setName(this.getName());
        p.setConfiguration(configuration);
        p.setTranslator(new PackageTranslator(PackageUtil.getPackageName(MacartneyPortlet.class), ureq.getLocale()));
        return p;
    }

    /**
	 */
    @Override
    public String getTitle() {
        return getTranslator().translate("macartney.title");
    }

    /**
	 */
    @Override
    public String getDescription() {
        return getTranslator().translate("macartney.description");
    }

    /**
	 */
    @Override
    public Component getInitialRunComponent(final WindowControl wControl, final UserRequest ureq) {
        if (this.runCtr != null) {
            runCtr.dispose();
        }
        this.runCtr = new MacartneyPortletRunController(ureq, wControl, getConfiguration());
        return this.runCtr.getInitialComponent();
    }

    /**
	 */
    @Override
    public void dispose() {
        disposeRunComponent();
    }

    /**
	 */
    @Override
    public String getCssClass() {
        return "o_portlet_macartney";
    }

    /**
	 */
    @Override
    public void disposeRunComponent() {
        if (this.runCtr != null) {
            this.runCtr.dispose();
            this.runCtr = null;
        }
    }

}
