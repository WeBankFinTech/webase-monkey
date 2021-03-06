/**
 * Copyright 2014-2019 the original author or authors.
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
package com.webank.webasemonkey.enums;

import org.apache.commons.lang.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * JavaTypeEnum
 *
 * @Description: JavaTypeEnum
 * @author maojiayu
 * @data Apr 18, 2019 11:29:46 AM
 *
 */
@AllArgsConstructor
@Getter
@Slf4j
public enum JavaTypeEnum {

    BIGINTEGER("BigInteger", "Long", "bigint", "BigIntegerUtils.toLong"),
    BOOL("Boolean", "String", "varchar(8)", "String.valueOf"),
    STRING("String", "String", "varchar(255)", "String.valueOf"),
    ByteArray("byte[]", "String", "varchar(10240)", "String.valueOf" ),
    LISTByteArray("List<byte[]>", "String", "varchar(10240)", "String.valueOf" ),
    LISTString("List<String>", "String", "varchar(10240)", "String.valueOf" ),
    LISTBigInteger("List<BigInteger>", "String", "varchar(4096)", "String.valueOf" ),
    LIST("List", "String", "varchar(10240)", "JacksonUtils.toJson" )
    ;

    private String javaType;
    private String entityType;
    private String sqlType;
    private String typeMethod;

    public static JavaTypeEnum parse(String javaType) {
        for (JavaTypeEnum type : JavaTypeEnum.values()) {
            if ((type.getJavaType().equalsIgnoreCase(StringUtils.substringBefore(javaType, "<"))
                    && !javaType.contains(">")) || type.getJavaType().equalsIgnoreCase(javaType)) {
                return type;
            }
        }
        log.error("javaType {} can't be converted.", javaType);
        return null;
    }

}
