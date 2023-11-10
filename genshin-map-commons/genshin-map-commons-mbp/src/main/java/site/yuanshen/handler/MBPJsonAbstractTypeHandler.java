package site.yuanshen.handler;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.postgresql.util.PGobject;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class MBPJsonAbstractTypeHandler<U> extends BaseTypeHandler<U> {

    protected static final JSONReader.Feature[] readFeatures = new JSONReader.Feature[]{
            JSONReader.Feature.UseBigDecimalForFloats,
            JSONReader.Feature.UseBigDecimalForDoubles,
            JSONReader.Feature.UseNativeObject
    };

    protected static final JSONWriter.Feature[] writeFeatures = new JSONWriter.Feature[]{
            JSONWriter.Feature.BrowserCompatible,
            JSONWriter.Feature.WriteEnumUsingToString,
            JSONWriter.Feature.WriteBigDecimalAsPlain,
            JSONWriter.Feature.WriteEnumUsingToString
    };

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, U u, JdbcType jdbcType) throws SQLException {
        final PGobject jsonObject = new PGobject();
        jsonObject.setType("json");
        jsonObject.setValue(stringifier(u));
        preparedStatement.setObject(i, jsonObject);
    }

    @Override
    public U getNullableResult(ResultSet resultSet, String s) throws SQLException {
        final String jsonValue = resultSet.getString(s);
        return parser(jsonValue);
    }

    @Override
    public U getNullableResult(ResultSet resultSet, int i) throws SQLException {
        final String jsonValue = resultSet.getString(i);
        return parser(jsonValue);
    }

    @Override
    public U getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        String jsonValue = callableStatement.getString(i);
        return parser(jsonValue);
    }

    protected String stringifier(U u) {
        return JSON.toJSONString(u, writeFeatures);
    }

    protected abstract U parser(String jsonValue);

}
