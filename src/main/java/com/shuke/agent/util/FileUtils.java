/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.shuke.agent.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;


/**
 * file utils
 */
public class FileUtils {
    public static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    public static ArrayList<String> file2list(String path, String encoder) {

        ArrayList<String> alline= new ArrayList<String>();

        try {

            BufferedReader in = new BufferedReader( new InputStreamReader( new FileInputStream(path),encoder));

            String str= new String();

            while ((str=in.readLine())!= null ) {

                alline.add(str);

            }

            in.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return alline;

    }



}
