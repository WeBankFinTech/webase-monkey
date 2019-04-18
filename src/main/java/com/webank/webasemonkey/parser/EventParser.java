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
package com.webank.webasemonkey.parser;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.web3j.protocol.core.methods.response.Log;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.webank.webasemonkey.constants.ParserConstants;
import com.webank.webasemonkey.enums.JavaTypeEnum;
import com.webank.webasemonkey.tools.JacksonUtils;
import com.webank.webasemonkey.tools.PropertiesUtils;
import com.webank.webasemonkey.tools.StringStyleUtils;
import com.webank.webasemonkey.vo.EventMetaInfo;
import com.webank.webasemonkey.vo.FieldVO;

import cn.hutool.core.text.StrSpliter;
import lombok.extern.slf4j.Slf4j;

/**
 * EventParser can parse contract files of java to standard event data structures.
 *
 * @author maojiayu
 * @date 2018-11-7 16:11:05
 * 
 */
@Slf4j
@Service
public class EventParser implements ContractJavaParserInterface<EventMetaInfo> {

    public List<EventMetaInfo> parseToInfoList(Class<?> clazz) {
        Class<?>[] subClass = clazz.getClasses();
        List<EventMetaInfo> lists = Lists.newArrayList();
        for (Class<?> c : subClass) {
            // filter web3sdk 2.0 embedded contract subclass EventValuesWithLog.
            if (c.getSimpleName().equals("EventValuesWithLog")) {
                continue;
            }
            EventMetaInfo event = new EventMetaInfo();
            event.setContractName(clazz.getSimpleName());
            event.setName(StringUtils.substringBefore(c.getSimpleName(), ParserConstants.EVENT_RESPONSE));
            String generatedFlag = PropertiesUtils.getPropertyWithoutDefault(ParserConstants.MONITOR,
                    event.getContractName(), event.getName(), "generated");
            if (generatedFlag != null && generatedFlag.equalsIgnoreCase("false")) {
                continue;
            }

            String ignoreStr = PropertiesUtils.getPropertyWithoutDefault(ParserConstants.MONITOR,
                    event.getContractName(), event.getName(), ParserConstants.IGNORE_PARAM);
            List<String> ignoreParam = StrSpliter.split(ignoreStr, ',', 0, true, true);
            event.setIgnoreParams(ignoreParam);
            int shardingNO = Integer.parseInt(PropertiesUtils.getGlobalProperty(ParserConstants.SYSTEM,
                    event.getContractName(), event.getName(), ParserConstants.SHARDINGNO, "1"));
            event.setShardingNO(shardingNO);
            Field[] fields = c.getFields();
            ArrayList<FieldVO> fieldList = Lists.newArrayList();
            for (Field f : fields) {
                // web3sdk 2.0 has a Log type, skip it temporary
                if (f.getType() == Log.class) {
                    continue;
                }
                System.out.println(f.getName());
                FieldVO vo = new FieldVO();
                String k = f.getName();
                if (ignoreParam.contains(k)) {
                    log.info("Contract:{}, event:{}, ignores param:{}", event.getContractName(), event.getName(), k);
                    continue;
                }
                String v = cleanType(f.getGenericType().getTypeName());
                System.out.println(v);
                // get the personal length
                String length = PropertiesUtils.getGlobalProperty(ParserConstants.LENGTH, event.getContractName(),
                        event.getName(), k, "0");
                vo.setSqlName(StringStyleUtils.upper2underline(k)).setJavaName(k)
                        .setSqlType(JavaTypeEnum.parse(v).getSqlType()).setJavaType(v)
                        .setEntityType(JavaTypeEnum.parse(v).getEntityType()).setJavaCapName(StringUtils.capitalize(k))
                        .setTypeMethod(JavaTypeEnum.parse(v).getTypeMethod()).setLength(Integer.parseInt(length));
                System.out.println("event " + JacksonUtils.toJson(vo));
                fieldList.add(vo);
            }
            event.setList(fieldList);
            lists.add(event);
        }
        return lists;
    }

    public String cleanType(String genericType) {
        if (genericType.contains("<")) {
            return StringUtils.substringAfterLast(StringUtils.substringBefore(genericType, "<"), ".") + "<"
                    + StringUtils.substringAfterLast(StringUtils.substringAfter(genericType, "<"), ".");
        } else {
            return StringUtils.substringAfterLast(genericType, ".");
        }
    }
}
