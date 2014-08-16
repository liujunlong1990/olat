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

package org.olat.lms.ims.qti.process.elements.section;

import java.util.List;

import org.dom4j.Element;
import org.olat.lms.ims.qti.process.QTIHelper;
import org.olat.lms.ims.qti.process.elements.ScoreBooleanEvaluable;

/**
 * @author Felix Jost
 */

public class QTI_and_test implements ScoreBooleanEvaluable {

    /*
     * (non-Javadoc)
     */
    @Override
    public boolean eval(final Element boolElement, final float score) {
        final List elems = boolElement.elements();
        final int size = elems.size();
        boolean ev = true;
        for (int i = 0; i < size; i++) {
            final Element child = (Element) elems.get(i);
            final String name = child.getName();
            final ScoreBooleanEvaluable bev = QTIHelper.getSectionBooleanEvaluableInstance(name);
            final boolean res = bev.eval(child, score);
            ev = ev && res;
            if (!ev) {
                return false;
            }
        }
        return true;
    }

}
