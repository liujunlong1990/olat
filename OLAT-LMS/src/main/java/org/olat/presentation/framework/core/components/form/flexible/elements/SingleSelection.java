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

package org.olat.presentation.framework.core.components.form.flexible.elements;

/**
 * @author patrickb
 */
public interface SingleSelection extends SelectionElement {

    /**
     * @return
     */
    public String getSelectedKey();

    /**
     * @return
     */
    public boolean isOneSelected();

    /**
     * @return
     */
    public int getSelected();

    /**
     * Set new keys and values in this selection box. Be aware that this does reset the selection index and other parameters. <br />
     * In most cases is is better to create a new SingleSelection Element than set new keys and values for an existing SingleSelection, always check this option.
     * 
     * @param keys
     *            The new keys to use
     * @param values
     *            The new values to use
     * @param cssClasses
     *            The CSS classes that should be used in the form element for each key-value pair or NULL not not use special styling
     */
    public void setKeysAndValues(String[] keys, String[] values, String[] cssClasses);
}
