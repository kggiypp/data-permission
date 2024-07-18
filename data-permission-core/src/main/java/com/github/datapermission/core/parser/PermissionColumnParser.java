package com.github.datapermission.core.parser;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 数据权限字段解析类
 * 
 * @author keguang
 */
@Slf4j
public class PermissionColumnParser {

    private static final Set<String> ALL_COLUMNS_FLAG = Collections.unmodifiableSet(Collections.emptySet());
    
    private final List<String> candidateColumns;
    
    public PermissionColumnParser(String... candidateColumns) {
        this(Arrays.asList(candidateColumns));
    }

    public PermissionColumnParser(List<String> candidateColumns) {
        Assert.notEmpty(candidateColumns, "指定的候选权限字段不能为空");
        this.candidateColumns = candidateColumns;
    }

    /**
     * 解析提取查询sql里包含的权限字段
     * @param sql sql
     * @return 权限字段
     */
    public String extractPermissionColumn(String sql) {
        Statement statement;
        try {
            statement = CCJSqlParserUtil.parseStatement(CCJSqlParserUtil.newParser(sql));
        } catch (JSQLParserException e) {
            // 原始sql本身有问题，跳过插件处理
            log.error("sql解析异常，跳过数据权限处理，堆栈信息：", e);
            return null;
        }
        if (!(statement instanceof Select)) {
            // 只处理select类型
            return null;
        }
        Select select = (Select) statement;
        // Select可以看作一个root SubSelect，构建root SubSelect
        SubSelect root = new SubSelect();
        root.setWithItemsList(select.getWithItemsList());
        root.setSelectBody(select.getSelectBody());
        return lookupSubSelect(root);
    }

    /**
     * 处理子查询SubSelect
     * @param subSelect 子查询
     * @return 权限字段
     */
    private String lookupSubSelect(SubSelect subSelect) {
        String permissionColumn;
        // 处理SelectBody
        SelectBody selectBody = subSelect.getSelectBody();
        Set<String> fromTableNames = new HashSet<>();
        permissionColumn = lookupSelectBody(selectBody, fromTableNames);
        if (permissionColumn != null) {
            return permissionColumn;
        }
        // 处理WithItem
        List<WithItem> withItemList = subSelect.getWithItemsList();
        return lookupWithItemList(withItemList, fromTableNames);
    }

    /**
     * 处理SelectBody
     * @param selectBody sql body
     * @param fromTableNames 如果使用了 * 通配符查询，收集所有from里的表名
     * @return 权限字段
     */
    private String lookupSelectBody(SelectBody selectBody, Set<String> fromTableNames) {
        String permissionColumn;
        // 处理PlainSelect
        if (selectBody instanceof PlainSelect) {
            PlainSelect plainSelect = (PlainSelect) selectBody;
            List<SelectItem> selectItems = plainSelect.getSelectItems();
            // 如果使用了 t.* 这种表名+通配符的查询，收集这类指定的前缀表名，用来过滤没必要解析的from成员
            Set<String> specifiedTableNames = new HashSet<>();
            for (SelectItem selectItem : selectItems) {
                if (selectItem instanceof SelectExpressionItem) {
                    SelectExpressionItem expressionItem = (SelectExpressionItem) selectItem;
                    String aliasName = Optional.ofNullable(expressionItem.getAlias()).map(Alias::getName).orElse(null);
                    if (candidateColumns.contains(aliasName)) {
                        return aliasName;
                    }
                    Expression expression = expressionItem.getExpression();
                    if (expression instanceof Column) {
                        String columnName = ((Column) expression).getColumnName();
                        if (candidateColumns.contains(columnName)) {
                            return columnName;
                        }
                    }
                }
                // 处理 *
                if (selectItem instanceof AllColumns) {
                    FromItem fromItem = plainSelect.getFromItem();
                    permissionColumn = lookupFromItem(fromItem, fromTableNames, ALL_COLUMNS_FLAG);
                    if (permissionColumn != null) {
                        return permissionColumn;
                    }
                    return lookupJoinList(plainSelect.getJoins(), fromTableNames, ALL_COLUMNS_FLAG);
                }
                // 处理 t.*，这种类型暂时只先记录，优先处理明确的字段，最后再统一处理这种指定表前缀的通配符
                if (selectItem instanceof AllTableColumns) {
                    specifiedTableNames.add(((AllTableColumns) selectItem).getTable().getName());
                }
            }
            // 最后来处理 t.* 这种指定表前缀的通配符查询
            if (!specifiedTableNames.isEmpty()) {
                permissionColumn = lookupFromItem(plainSelect.getFromItem(), fromTableNames, specifiedTableNames);
                if (permissionColumn != null) {
                    return permissionColumn;
                }
                return lookupJoinList(plainSelect.getJoins(), fromTableNames, specifiedTableNames);
            }
        }
        // 处理SetOperationList
        if (selectBody instanceof SetOperationList) {
            SetOperationList setOperationList = (SetOperationList) selectBody;
            SelectBody oneSelectBody = setOperationList.getSelects().get(0);
            return lookupSelectBody(oneSelectBody, fromTableNames);
        }
        return null;
    }

    /**
     * 处理FromItem
     * @param fromItem from语句
     * @param fromTableNames 如果使用了 * 通配符查询，收集所有from里的表名
     * @param specifiedTableNames 如果使用了 t.* 这种表名+通配符的查询，该集合用来过滤没必要解析的from成员
     * @return 权限字段
     */
    private String lookupFromItem(FromItem fromItem, Set<String> fromTableNames, Set<String> specifiedTableNames) {
        String permissionColumn;
        if (fromItem instanceof ParenthesisFromItem) {
            return lookupFromItem(((ParenthesisFromItem) fromItem).getFromItem(), fromTableNames, specifiedTableNames);
        }
        if (fromItem instanceof Table) {
            String tableName = ((Table) fromItem).getName();
            if (specifiedTableNames == ALL_COLUMNS_FLAG || specifiedTableNames.contains(tableName)) {
                fromTableNames.add(tableName);
            }
            return null;
        }
        if (fromItem instanceof SubSelect) {
            SubSelect subSelect = (SubSelect) fromItem;
            String aliasName = Optional.ofNullable(subSelect.getAlias()).map(Alias::getName).orElse(null);
            if (specifiedTableNames == ALL_COLUMNS_FLAG || specifiedTableNames.contains(aliasName)) {
                return lookupSubSelect(subSelect);
            }
            return null;
        }
        if (fromItem instanceof SpecialSubSelect) {
            SpecialSubSelect specialSubSelect = (SpecialSubSelect) fromItem;
            String aliasName = Optional.ofNullable(specialSubSelect.getAlias()).map(Alias::getName).orElse(null);
            if (specifiedTableNames == ALL_COLUMNS_FLAG || specifiedTableNames.contains(aliasName)) {
                return lookupSubSelect(specialSubSelect.getSubSelect());
            }
            return null;
        }
        if (fromItem instanceof SubJoin) {
            SubJoin subJoin = (SubJoin) fromItem;
            permissionColumn = lookupFromItem(subJoin.getLeft(), fromTableNames, specifiedTableNames);
            if (permissionColumn != null) {
                return permissionColumn;
            }
            return lookupJoinList(subJoin.getJoinList(), fromTableNames, specifiedTableNames);
        }
        return null;
    }

    /**
     * 处理Join
     * @param joins join语句
     * @param fromTableNames 如果使用了 * 通配符查询，收集所有from里的表名
     * @param specifiedTableNames 如果使用了 t.* 这种表名+通配符的查询，该集合用来过滤没必要解析的from成员
     * @return 权限字段
     */
    private String lookupJoinList(List<Join> joinList, Set<String> fromTableNames, Set<String> specifiedTableNames) {
        String permissionColumn;
        if (!CollectionUtils.isEmpty(joinList)) {
            for (Join join : joinList) {
                permissionColumn = lookupFromItem(join.getRightItem(), fromTableNames, specifiedTableNames);
                if (permissionColumn != null) {
                    return permissionColumn;
                }
            }
        }
        return null;
    }

    /**
     * 处理WithItem
     * @param withItemList with语句
     * @param tableNames 如果使用了 * 通配符查询，收集所有from里的表名，用来匹配with生成的临时表
     * @return 权限字段
     */
    private String lookupWithItemList(List<WithItem> withItemList, Set<String> tableNames) {
        String permissionColumn;
        if (CollectionUtils.isEmpty(withItemList)) {
            return null;
        }
        for (WithItem withItem : withItemList) {
            if (tableNames.contains(withItem.getName())) {
                SubSelect subSelect = withItem.getSubSelect();
                permissionColumn = lookupSubSelect(subSelect);
                if (permissionColumn != null) {
                    return permissionColumn;
                }
            }
        }
        return null;
    }
    
}
