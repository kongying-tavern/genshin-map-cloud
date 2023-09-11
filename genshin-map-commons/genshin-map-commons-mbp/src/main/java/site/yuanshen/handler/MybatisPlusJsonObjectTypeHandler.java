package site.yuanshen.handler;

import com.alibaba.fastjson2.JSON;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.postgresql.util.PGobject;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class MybatisPlusJsonObjectTypeHandler extends BaseTypeHandler<Map<String, Object>> {

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, Map<String, Object> stringObjectMap, JdbcType jdbcType) throws SQLException {
        PGobject jsonObject = new PGobject();
        jsonObject.setType("json");
        jsonObject.setValue(JSON.toJSONString(stringObjectMap));
        preparedStatement.setObject(i, jsonObject);
    }

    @Override
    public Map<String, Object> getNullableResult(ResultSet resultSet, String s) throws SQLException {
        String jsonValue = resultSet.getString(s);
        Map<String, Object> jsonMap = JSON.parseObject(jsonValue);
        return jsonMap;
    }

    @Override
    public Map<String, Object> getNullableResult(ResultSet resultSet, int i) throws SQLException {
        String jsonValue = resultSet.getString(i);
        Map<String, Object> jsonMap = JSON.parseObject(jsonValue);
        return jsonMap;
    }

    @Override
    public Map<String, Object> getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        String jsonValue = callableStatement.getString(i);
        Map<String, Object> jsonMap = JSON.parseObject(jsonValue);
        return jsonMap;
    }
}
