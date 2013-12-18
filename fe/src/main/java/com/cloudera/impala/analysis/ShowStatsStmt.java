// Copyright 2012 Cloudera Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.cloudera.impala.analysis;

import com.cloudera.impala.authorization.Privilege;
import com.cloudera.impala.catalog.AuthorizationException;
import com.cloudera.impala.catalog.Table;
import com.cloudera.impala.catalog.View;
import com.cloudera.impala.common.AnalysisException;
import com.cloudera.impala.thrift.TShowStatsParams;

/**
 * Representation of a SHOW TABLE/COLUMN STATS statement for displaying
 * column and table statistics for a given table.
 */
public class ShowStatsStmt extends StatementBase {
  protected final boolean isShowColStats_;
  protected final TableName tableName_;

  // Set during analysis.
  protected Table table_;

  public ShowStatsStmt(TableName tableName, boolean isShowColStats) {
    this.tableName_ = tableName;
    this.isShowColStats_ = isShowColStats;
  }

  @Override
  public String toSql() {
    return getSqlPrefix() + " " + tableName_.toString();
  }

  private String getSqlPrefix() {
    return "SHOW " + ((isShowColStats_) ? "COLUMN" : "TABLE") + " STATS";
  }

  @Override
  public void analyze(Analyzer analyzer) throws AnalysisException,
      AuthorizationException {
    table_ = analyzer.getTable(tableName_, Privilege.VIEW_METADATA);
    if (table_ instanceof View) {
      throw new AnalysisException(String.format(
          "%s not applicable to a view: %s", getSqlPrefix(), table_.getFullName()));
    }
  }

  public TShowStatsParams toThrift() {
    // Ensure the DB is set in the table_name field by using table and not tableName.
    return new TShowStatsParams(isShowColStats_,
        new TableName(table_.getDb().getName(), table_.getName()).toThrift());
  }
}
