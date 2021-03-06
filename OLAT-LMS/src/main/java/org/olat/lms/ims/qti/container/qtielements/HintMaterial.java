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

package org.olat.lms.ims.qti.container.qtielements;

import java.util.Locale;

import org.dom4j.Element;
import org.olat.presentation.framework.core.translator.PackageUtil;
import org.olat.presentation.framework.core.translator.Translator;
import org.olat.presentation.ims.qti.QTIResultDetailsController;

/**
 * Initial Date: 24.11.2004
 * 
 * @author Mike Stock
 */
public class HintMaterial extends GenericQTIElement {

    /**
     * Comment for <code>xmlClass</code>
     */
    public static final String xmlClass = "hintmaterial";

    /**
     * @param el_material
     */
    public HintMaterial(final Element el_material) {
        super(el_material);
    }

    /**
	 */
    @Override
    public void render(final StringBuilder buffer, final RenderInstructions ri) {
		final Translator translator = PackageUtil.createPackageTranslator(QTIResultDetailsController.class, (Locale) ri.get(RenderInstructions.KEY_LOCALE));
        buffer.append("<div id=\"o_qti_hints\"><a href=\"#\" onclick=\"b_togglebox('o_qti_hints_inner', this);\" onkeypress=\"b_togglebox('o_qti_hints_inner', this);\">");
        buffer.append(translator.translate("render.hint"));
        buffer.append("</a><div id=\"o_qti_hints_inner\" style=\"display:none\"><div><div class=\"b_important\">");
        super.render(buffer, ri);
        buffer.append("</div></div></div></div>");
    }

}
