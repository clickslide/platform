/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.formatter.admin.internal.util;

import edu.emory.mathcs.backport.java.util.Collections;
import org.wso2.carbon.databridge.commons.AttributeType;

import java.util.HashMap;
import java.util.Map;

public final class PropertyAttributeTypeConstants {

    private PropertyAttributeTypeConstants() {
    }

    public static final String ATTR_TYPE_FLOAT = "java.lang.Float";
    public static final String ATTR_TYPE_DOUBLE = "java.lang.Double";
    public static final String ATTR_TYPE_INTEGER = "java.lang.Integer";
    public static final String ATTR_TYPE_LONG = "java.lang.Long";
    public static final String ATTR_TYPE_STRING = "java.lang.String";
    public static final String ATTR_TYPE_BOOL = "java.lang.Boolean";


    public static final Map<String, AttributeType> STRING_ATTRIBUTE_TYPE_MAP = Collections.unmodifiableMap(new HashMap<String, AttributeType>() {{
        put(ATTR_TYPE_BOOL, AttributeType.BOOL);
        put(ATTR_TYPE_STRING, AttributeType.STRING);
        put(ATTR_TYPE_DOUBLE, AttributeType.DOUBLE);
        put(ATTR_TYPE_FLOAT, AttributeType.FLOAT);
        put(ATTR_TYPE_INTEGER, AttributeType.INT);
        put(ATTR_TYPE_LONG, AttributeType.LONG);
    }});


}