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
package org.olat.lms.commons.i18n;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.olat.data.commons.fileutil.FileVisitor;
import org.olat.system.logging.log4j.LoggerHelper;

/**
 * Description:<br>
 * This visitor adds all bundles to the I18nModule that contain i18n files
 * <P>
 * Initial Date: 29.08.2008 <br>
 * 
 * @author gnaegi
 */
class I18nDirectoriesVisitor implements FileVisitor {
    private static final Logger log = LoggerHelper.getLogger();

    private String basePath;
    private List<String> bundles = new LinkedList<String>();

    /**
     * Packag scope constructor
     * 
     * @param basePathConfig
     *            The base path to be subtracted from the file name to get the classname
     */
    I18nDirectoriesVisitor(String basePathConfig) {
        basePath = basePathConfig;
    }

    /**
	 */
    @Override
    public void visit(File file) {
        // collect all bundles for a drop-down list later;
        if (file.isFile()) { // regular file
            String toBeChechedkFilName = file.getName();
            // match?
            I18nManager i18nMgr = I18nManager.getInstance();
            List<String> referenceLangKeys = I18nModule.getTransToolReferenceLanguages();
            for (String langKey : referenceLangKeys) {
                String computedFileName = I18nModule.LOCAL_STRINGS_FILE_PREFIX + langKey + I18nModule.LOCAL_STRINGS_FILE_POSTFIX;
                if (toBeChechedkFilName.equals(computedFileName)) {
                    File parentFile = file.getParentFile();
                    if (!(parentFile.getName()).equals(I18nManager.I18N_DIRNAME)) {
                        if (!file.getAbsolutePath().contains("org/olat/lms/commons/i18n/junittestdata/"))
                            log.warn(computedFileName + " must be in a >>.../" + I18nManager.I18N_DIRNAME + "<< folder! instead of " + parentFile.getAbsolutePath());
                        continue;
                    }
                    File grandParentFile = parentFile.getParentFile();
                    String pPath = grandParentFile.getPath();
                    String res = pPath.substring(basePath.length() + 1); // add one for
                    // the '/'
                    char c = File.separatorChar;
                    res = res.replace(c, '.');
                    if (!bundles.contains(res))
                        bundles.add(res);
                    break;
                }
            }

        }
        // else ignore
    }

    /**
     * @return all the bundles found so far
     */
    List<String> getBundlesContainingI18nFiles() {
        return bundles;
    }

}
