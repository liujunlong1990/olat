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

package org.olat.lms.ims.qti.parser;

import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.olat.lms.ims.qti.objects.OutcomesProcessing;

/**
 * Initial Date: Sep 23, 2003
 * 
 * @author gnaegi<br>
 *         Comment: Parser for the outcomes processing elements (at assessment level). Takes all the attributes found in the decvar element, all other elements or
 *         attributes are ignored. </pre>
 */
public class OutcomesProcessingParser implements IParser {

    /**
	 */
    @Override
    public Object parse(final Element element) {
        // assert element.getName().equalsIgnoreCase("outcomes_processing");

        final OutcomesProcessing outcomesProcessing = new OutcomesProcessing();

        final List decvars = element.selectNodes("*/decvar");
        if (decvars.size() == 0) {
            return outcomesProcessing;
        }

        final Element decvar = (Element) decvars.get(0);
        for (final Iterator iter = decvar.attributeIterator(); iter.hasNext();) {
            final Attribute attr = (Attribute) iter.next();
            outcomesProcessing.setField(attr.getName(), attr.getValue());
        }
        return outcomesProcessing;
    }
}
