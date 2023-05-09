package site.yuanshen.handler;

import com.alibaba.fastjson2.JSON;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class MybatisPlusJsonArrayTypeHandler extends BaseTypeHandler<List<Object>> {
    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, List<Object> stringObjectMap, JdbcType jdbcType) throws SQLException {
        preparedStatement.setString(i, JSON.toJSONString(stringObjectMap));
    }

    @Override
    public List<Object> getNullableResult(ResultSet resultSet, String s) throws SQLException {
        String data = resultSet.getString(s);
        return StringUtils.isEmpty(data) ? null : JSON.parseArray(data);
    }

    @Override
    public List<Object> getNullableResult(ResultSet resultSet, int i) throws SQLException {
        String data = resultSet.getString(i);
        return StringUtils.isEmpty(data) ? null : JSON.parseArray(data);
    }

    @Override
    public List<Object> getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        String data = callableStatement.getString(i);
        return StringUtils.isEmpty(data) ? null : JSON.parseArray(data);
    }
}
