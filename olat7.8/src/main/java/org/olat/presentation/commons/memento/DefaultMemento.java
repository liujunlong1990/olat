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

package org.olat.presentation.commons.memento;

/**
 * Description:<br>
 * TODO: patrick Class Description for DefaultMemento
 * <P>
 * Initial Date: Nov 25, 2005 <br>
 * 
 * @author patrick
 */
public class DefaultMemento extends Memento {

    private int version = 0;

    /**
	 * 
	 */
    public DefaultMemento() {
        super();
    }

    /**
	 */
    @Override
    protected void resolveVersionIssues() {
        // /*
        // * default implementation nothing to do
        // *
        // * otherwise this would be something like
        // * ----------------------------------------------------------------------
        // * int currentVersion = getVersion()
        // * int instanceVersion = ((Integer)mementoState.get(VERSION)).intValue()
        // * if(instanceVersion <= 1){
        // * do upgrade from version 0 to version 1
        // * }
        // * if(instanceVersion <= 2){
        // * do upgrade from version 1 to version 2
        // * }
        // * .
        // * .
        // * .
        // * if(instanceVersion <= currentVersion){
        // * do upgrade from version previous version to current version
        // * }
        // * !!very important now to
        // * mementoState.put(VERSION,new Integer(currentVersion));
        // *
        // *
        // */
    }

    /**
	 */
    @Override
    protected int getVersion() {
        // default implementation just return 0
        return version;
    }

}
