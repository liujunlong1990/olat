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

package org.olat.system.commons.resource;

import org.olat.system.commons.encoder.Encoder;
import org.olat.system.exception.AssertException;

/**
 * Description:<br>
 * 
 * @author Felix Jost
 */
public class OresHelper {
    /**
     * TODO: where does this limitation come from, NEEDS description!
     */
    public static final int ORES_TYPE_LENGTH = 60;
    private static final int ORES_TYPE_SUBTYPELENGTH = 15; // so 35 remain for the class name (without package)

    /**
     * Use only when you need to create a new olatresourceable instance.
     * 
     * @param type
     *            the type may not exceed 50 chars!
     * @param key
     *            the key; may not be null!
     * @return OLATResourceable instance
     */
    public static OLATResourceable createOLATResourceableInstance(final String type, final Long key) {
        if (key == null)
            throw new AssertException("key may not be null; type = " + type);
        if (type.length() > ORES_TYPE_LENGTH)
            throw new AssertException("Olatresource type may not exceed " + ORES_TYPE_LENGTH + " chars: type=" + type);
        return new OLATResourceable() {
            @Override
            public String getResourceableTypeName() {
                return type;
            }

            @Override
            public Long getResourceableId() {
                return key;
            }
        };
    }

    /**
     * type could be any string, must not be a class name.
     * 
     * @param type
     * @param subtype
     * @return
     */
    public static OLATResourceable createOLATResourceableType(final String type, final String subtype) {
        String typeStr = type;
        if (type.indexOf(".") == -1) {
            typeStr = "." + type;
        }
        return createOLATResourceableInstance(calculateTypeName(typeStr, subtype), new Long(0));
    }

    public static OLATResourceable createOLATResourceableType(final String type) {
        return createOLATResourceableInstance(type, new Long(0));
    }

    public static OLATResourceable createOLATResourceableType(Class clazz) {
        return createOLATResourceableType(clazz.getName());
    }

    /**
     * Use only when you need to create a new olatresourceable instance. Calculates type name based on class name.
     * 
     * @param aClass
     *            the class this resurceable represents
     * @param key
     *            the key; may not be null!
     * @return OLATResourceable instance
     */
    public static OLATResourceable createOLATResourceableInstance(Class aClass, final Long key) {
        String type = calculateTypeName(aClass);
        return createOLATResourceableInstance(type, key);
    }

    /**
     * used to generate an object (a String) which can be used in maps / set etc. e.g. when ores1 != ores2, but
     * (ores1.getResourceableTypeName().equals(ores2.getResourceableTypeName())) && (ores1.getResourceableId().equals(ores2.getResourceableId())) then use this method to
     * e.g. acquire a lock on a OLATResourceable (which internally works with a map). The reason for this helper method is that there are several implementations of
     * OLATResourceable which we did not want to change in their equals/hashCode behaviour samples: composes a per-ores-unique string: e.g. "editor@Forum::34324324" or
     * "sysadm@Forum::null" or "groupmanagement@BuddyGroup::3423432" etc. <br>
     * 
     * @param ores
     * @param subKey
     *            an indicator of a subresource, e.g. "author", "editorLock", ...
     * @return String representation but not longer than 120 bytes
     */
    public static String createStringRepresenting(OLATResourceable ores, String subKey) {
        String resourcableTypeName = ores.getResourceableTypeName();
        if (resourcableTypeName.length() > 80) {
            // encode into an md5 hash with fixed length of 32 characters otherwise the sting may get too long for locks or db fields
            resourcableTypeName = Encoder.encrypt(resourcableTypeName);
        }
        String derivedString = resourcableTypeName + "::" + ores.getResourceableId();
        if (subKey != null) {
            if (subKey.length() > 25) {
                // 25 is the max if the above becomes 80 + 2 + 12 + @
                // so we only add the last 25 characters
                subKey = subKey.substring(subKey.length() - 25, subKey.length());
            }
            derivedString = subKey + "@" + derivedString;
            // was: derivedString += "::"+subKey;
        }
        return derivedString;
    }

    /**
     * -Not to use normally-<br>
     * Used to compress an olatresourceable with a subkey into a new olatresourceable. Used to fit into API which only offer olatresourceable without subkey.<br>
     * e.g. "RepositoryEntry" : 123 with subkey 'author' turns into "author@RepositoryEntry" : 123
     * 
     * @param ores
     *            the olatresourceable
     * @param subKey
     *            the subkey, may be null (in which case the original olatresourceable is returned)
     * @return an olatresourceable representing the ores together with the subkey
     */
    /*
     * public static OLATResourceable createComposite(OLATResourceable ores, String subKey) { if (subKey == null) { return ores; } else { final String type =
     * subKey+"@"+ores.getResourceableTypeName(); final Long id = ores.getResourceableId(); return createOLATResourceableInstanceWithoutCheck(type, id); } }
     */

    /**
     * used to generate an object (a String) which can be used in maps / set etc. e.g. when ores1 != ores2, but
     * (ores1.getResourceableTypeName().equals(ores2.getResourceableTypeName())) && (ores1.getResourceableId().equals(ores2.getResourceableId())) then use this method to
     * e.g. acquire a lock on a OLATResourceable (which internally works with a map). The reason for this helper method is that there are several implementations of
     * OLATResourceable which we did not want to change in their equals/hashCode behaviour <br>
     * samples: composes a per-ores-unique string: e.g. "Forum::null" or "Forum::null" or "BuddyGroup::null" etc.
     * 
     * @param ores
     * @return String representation
     */
    public static String createStringRepresenting(OLATResourceable ores) {
        return createStringRepresenting(ores, null);
    }

    /**
     * @param aClass
     *            the class representing the type: NOTE: aClass.getName() may not exceed 50 chars
     * @return OLATResourceable
     */
    public static OLATResourceable lookupType(Class aClass) {
        return lookupType(aClass, null);
    }

    /**
     * Calculates a type name based on the class with subtype 'null'
     * 
     * @param aClass
     * @return Calculated type name
     */
    public static String calculateTypeName(Class aClass) {
        return calculateTypeName(aClass, null);
    }

    /**
     * Calculates a type name based on the class with a subtype.
     * 
     * @param aClass
     * @param subType
     *            May not exceed 15 characters
     * @return Calculated type name
     */
    public static String calculateTypeName(Class aClass, String subType) {
        String comp = aClass.getName();
        return calculateTypeName(comp, subType);
    }

    /**
     * Calculates a type name based on an input type expected to be a class name representation (actually could be any string containing dots in it) and a subtype.
     * 
     * @param type
     * @param subType
     * @return
     */
    protected static String calculateTypeName(String type, String subType) {
        String comp = type;
        int pos = comp.lastIndexOf(".");
        if (pos == -1)
            throw new AssertException("class name without package!! (contains no dot):" + comp);
        comp = comp.substring(pos + 1);
        if (subType != null) {
            if (subType.length() > ORES_TYPE_SUBTYPELENGTH)
                throw new AssertException("subtype may not be longer than 15 chars!:" + subType);
            comp = comp + ":" + subType;
        }
        if (comp.length() > ORES_TYPE_LENGTH)
            throw new AssertException("Olatresource type may not exceed 50 chars: " + comp);
        return comp;
    }

    /**
     * Check whether the given ores is of type aClass
     * 
     * @param ores
     * @param aClass
     * @return True if ores is of type aClass
     */
    public static boolean isOfType(OLATResourceable ores, Class aClass) {
        String type = ores.getResourceableTypeName();
        String calcName = calculateTypeName(aClass, null);
        boolean ok = (type.equals(calcName));
        return ok;
    }

    /**
     * @param aClass
     * @param subType
     *            may only contain a..z and 0..9; and may only be 15 chars long
     * @return OLATResourceable
     */
    public static OLATResourceable lookupType(Class aClass, String subType) {
        final String type = calculateTypeName(aClass, subType);
        OLATResourceable ores = new OLATResourceable() {
            @Override
            public String getResourceableTypeName() {
                return type;
            }

            @Override
            public Long getResourceableId() {
                return null;
            }
        };
        return ores;
    }

    /**
     * return whether two OLATResourceable are equal: - the type must be both non-null and the same - the keys must either be non-null or have the same long value
     * 
     * @param a
     * @param b
     * @return true if equal
     */
    public static boolean equals(OLATResourceable a, OLATResourceable b) {
        if (a == null || b == null)
            return false;

        Long aKey = a.getResourceableId();
        String aType = a.getResourceableTypeName();
        Long bKey = b.getResourceableId();
        String bType = b.getResourceableTypeName();

        boolean same = (bType.equals(aType) && ((aKey != null && aKey.equals(bKey)) || (aKey == null && bKey == null)));
        return same;
    }

    public static OLATResourceable createOLATResourceableTypeWithoutCheck(String type) {
        return createOLATResourceableInstanceWithoutCheck(type, new Long(0));
    }

    public static OLATResourceable createOLATResourceableInstanceWithoutCheck(final String type, final Long key) {
        if (key == null)
            throw new AssertException("key may not be null; type = " + type);
        return new OLATResourceable() {
            @Override
            public String getResourceableTypeName() {
                return type;
            }

            @Override
            public Long getResourceableId() {
                return key;
            }
        };
    }

}
