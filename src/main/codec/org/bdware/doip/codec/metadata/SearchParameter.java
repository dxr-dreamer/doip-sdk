/*
 *    Copyright (c) [2021] [Peking University]
 *    [BDWare DOIP SDK] is licensed under Mulan PSL v2.
 *    You can use this software according to the terms and conditions of the Mulan PSL v2.
 *    You may obtain a copy of Mulan PSL v2 at:
 *             http://license.coscl.org.cn/MulanPSL2
 *    THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 *    See the Mulan PSL v2 for more details.
 */

package org.bdware.doip.codec.metadata;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SearchParameter {
    public String query;
    public int pageNum;
    public int pageSize;
    public String type;
    Logger logger = LogManager.getLogger(SearchParameter.class);

    public SearchParameter(String query, int pageNum, int pageSize, String sortFields, String type) {
        this.query = query;
        this.pageSize = pageSize;
        this.pageNum = pageNum;
        this.type = type;
        String[] sortFieldRes = sortFields.split(",");
        for (String sortField : sortFieldRes) {
            logger.debug("sort field: " + sortField);
            String[] tmpRes = sortField.split(":");
            if (tmpRes.length != 1 && tmpRes.length != 2) {
                logger.debug("wrong sort fields");
                continue;
            } else {
                if (tmpRes[0].equals("")) {
                    logger.debug("sort field is empty, ignore");
                    continue;
                }
            }


        }


    }

    public static SearchParameter getDefaultParameter() {
        return new SearchParameter("", 0, 1000, "", "id");
    }


}