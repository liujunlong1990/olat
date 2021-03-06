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

package org.olat.presentation.framework.core.components.htmlheader;

import org.olat.presentation.framework.core.UserRequest;
import org.olat.presentation.framework.core.components.Component;
import org.olat.presentation.framework.core.components.ComponentRenderer;

/**
 * Initial Date: Jun 14, 2004
 * 
 * @author gnaegi
 */
public class HtmlHeaderComponent extends Component {
    private static final ComponentRenderer RENDERER = new HtmlHeaderRenderer();

    private String jsBodyOnLoad;
    private String headerInclude;

    /**
     * Constructor for java script onload and html header component
     * 
     * @param componentName
     * @param jsBodyOnLoad
     * @param headerInclude
     */
    public HtmlHeaderComponent(String componentName, String jsBodyOnLoad, String headerInclude) {
        super(componentName);
        this.jsBodyOnLoad = jsBodyOnLoad;
        this.headerInclude = headerInclude;
    }

    /**
	 */
    @Override
    protected void doDispatchRequest(UserRequest ureq) {
        // do nothing
    }

    /**
     * @return Returns the headerInclude.
     */
    protected String getHeaderInclude() {
        return headerInclude;
    }

    /**
     * @param headerInclude
     *            The headerInclude to set.
     */
    public void setHeaderInclude(String headerInclude) {
        setDirty(true);
        this.headerInclude = headerInclude;
    }

    /**
     * @return Returns the jsBodyOnLoad.
     */
    protected String getJsBodyOnLoad() {
        return jsBodyOnLoad;
    }

    /**
     * @param jsBodyOnLoad
     *            The jsBodyOnLoad to set.
     */
    public void setJsBodyOnLoad(String jsBodyOnLoad) {
        setDirty(true);
        this.jsBodyOnLoad = jsBodyOnLoad;
    }

    @Override
    public ComponentRenderer getHTMLRendererSingleton() {
        return RENDERER;
    }
}
