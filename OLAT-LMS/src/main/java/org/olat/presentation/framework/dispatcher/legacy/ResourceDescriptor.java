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

package org.olat.presentation.framework.dispatcher.legacy;

/**
 * Initial Date: 16.06.2003
 * 
 * @author Mike Stock Comment:
 */
public class ResourceDescriptor {

    private String relPath;
    private String contentType = null;
    private long size = -1L;
    private long lastModified = -1L;

    /**
     * @param relPath
     */
    public ResourceDescriptor(final String relPath) {
        this.relPath = relPath;
    }

    /**
     * @return
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @return
     */
    public long getLastModified() {
        return lastModified;
    }

    /**
     * @return
     */
    public String getRelPath() {
        return relPath;
    }

    /**
     * @return
     */
    public long getSize() {
        return size;
    }

    /**
     * @param string
     */
    public void setContentType(final String string) {
        contentType = string;
    }

    /**
     * @param l
     */
    public void setLastModified(final long l) {
        lastModified = l;
    }

    /**
     * @param string
     */
    public void setRelPath(final String string) {
        relPath = string;
    }

    /**
     * @param l
     */
    public void setSize(final long l) {
        size = l;
    }

}
