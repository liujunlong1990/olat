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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.data.tagging;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.olat.data.commons.database.DB;
import org.olat.data.commons.database.DBQuery;
import org.olat.system.commons.StringHelper;
import org.olat.system.commons.manager.BasicManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Description:<br>
 * A very simple manager to propose tags.
 * <P>
 * Initial Date: 19 jul. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Repository
public class SimpleTagProposalDao extends BasicManager implements TagProposalDao {

    @Autowired
    DB dbInstance;

    protected SimpleTagProposalDao() {
        //
    }

    @Override
    public List<String> proposeTagsForInputText(String referenceText, boolean onlyExisting) {
        List<String> tokens = new ArrayList<String>();
        StringTokenizer tokenizer = new StringTokenizer(referenceText, " \t\n\r\f.,;:-!?");
        for (; tokenizer.hasMoreTokens();) {
            String next = tokenizer.nextToken().trim();
            next = next.replace(" ", "");
            if (!tokens.contains(next) && StringHelper.containsNonWhitespace(next) && next.length() > 3)
                tokens.add(next);
            if (tokens.size() == 500) {
                break;
            }
        }

        if (onlyExisting) {
            StringBuilder sb = new StringBuilder();
            sb.append("select tag.tag from ").append(TagImpl.class.getName()).append(" tag where tag.tag in (:tokens) group by tag.tag");
            DBQuery query = dbInstance.createQuery(sb.toString());
            query.setParameterList("tokens", tokens);

            @SuppressWarnings("unchecked")
            List<String> currentTags = query.list();
            return currentTags;
        } else {
            return tokens;
        }
    }
}
