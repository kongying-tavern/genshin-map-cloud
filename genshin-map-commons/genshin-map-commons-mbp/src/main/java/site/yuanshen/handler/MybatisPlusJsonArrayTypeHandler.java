package site.yuanshen.handler;

import com.alibaba.fastjson2.JSON;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.postgresql.util.PGobject;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class MybatisPlusJsonArrayTypeHandler extends BaseTypeHandler<List<Object>> {
    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, List<Object> objects, JdbcType jdbcType) throws SQLException {
        PGobject jsonObject = new PGobject();
        jsonObject.setType("json");
        jsonObject.setValue(JSON.toJSONString(objects));
        preparedStatement.setObject(i, jsonObject);
    }

    @Override
    public List<Object> getNullableResult(ResultSet resultSet, String s) throws SQLException {
        String jsonValue = resultSet.getString(s);
        List<Object> jsonList = JSON.parseArray(jsonValue);
        return jsonList;
    }

    @Override
    public List<Object> getNullableResult(ResultSet resultSet, int i) throws SQLException {
        String jsonValue = resultSet.getString(i);
        List<Object> jsonList = JSON.parseArray(jsonValue);
        return jsonList;
    }

    @Override
    public List<Object> getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        String jsonValue = callableStatement.getString(i);
        List<Object> jsonList = JSON.parseArray(jsonValue);
        return jsonList;
    }

}
