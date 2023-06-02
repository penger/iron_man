package cn.piesat.utils;

import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Data;
import org.apache.flink.api.java.utils.ParameterTool;

import java.io.*;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

/**
 * @author gongpeng
 */
public class JdbcComponent {
    
    private JdbcComponent() {
    }
    
    @Data
    @Builder
    public static class JdbcParam implements Serializable {
        private static final long serialVersionUID = 7395805666443429440L;
        private String driver;
        private String url;
        private String username;
        private String password;
    }
    
    @Data
    public static class MetaDataField {
        private String fieldName;
        private String fieldType;
        private int fieldSize;
    }

    //传参 命令行
    public static JdbcParam buildJdbcParam(ParameterTool parameterTool) {
        return JdbcParam.builder()
                .driver(parameterTool.get("driver"))
                .url(parameterTool.get("url"))
                .username(parameterTool.get("username"))
                .password(parameterTool.get("password"))
                .build();
    }

    //测试
    public static JdbcParam buildJdbcParam(Map<String,String> map) {
        return JdbcParam.builder()
                .driver(map.get("driver"))
                .url(map.get("url"))
                .username(map.get("username"))
                .password(map.get("password"))
                .build();
    }

    /**
     * 读取sql文件
     * @param path
     * @return
     * @throws IOException
     */
    public static String readSqlFile(String path) throws IOException {
        InputStream inputStream = JdbcComponent.class.getResourceAsStream(path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder(200);
        while (reader.ready()) {
            builder.append(reader.readLine());
            builder.append(System.getProperty("line.separator"));
        }
        reader.close();
        inputStream.close();
        return builder.toString();
    }
    
    /**
     * 创建数据库的连接
     * @param properties
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public static Connection getConnection(Properties properties) throws ClassNotFoundException, SQLException {
        Class.forName(properties.getProperty("driver"));
        return DriverManager.getConnection(properties.getProperty("url"), properties);
    }
    
    public static Connection getConnection(JdbcParam jdbcParam) throws ClassNotFoundException, SQLException {
        Class.forName(jdbcParam.getDriver());
        return DriverManager.getConnection(jdbcParam.getUrl(), jdbcParam.getUsername(), jdbcParam.getPassword());
    }
    
    /**
     * 关闭数据库的连接
     * @param rs
     * @param stmt
     * @param con
     * @throws SQLException
     */
    public static void close(ResultSet rs, Statement stmt, Connection con) throws SQLException {
        if (rs != null) {
            rs.close();
        }
        if (stmt != null) {
            stmt.close();
        }
        if (con != null) {
            con.close();
        }
    }
    
    /**
     * 将rs结果转换成对象列表
     * @param resultSet
     * @param clazz
     * @param <T>
     * @return
     * @throws SQLException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public static <T> List<T> convertResultSetToList(ResultSet resultSet, Class<T> clazz)
            throws SQLException, InstantiationException, IllegalAccessException {
        List<T> list = Lists.newArrayList();
        Field[] fields = clazz.getDeclaredFields();
        while (resultSet.next()) {
            T obj = clazz.newInstance();
            for (Field field : fields) {
                String fieldName = field.getName();
                if (isExistColumn(resultSet, fieldName)) {
                    Object value = resultSet.getObject(fieldName);
                    boolean flag = field.isAccessible();
                    field.setAccessible(true);
                    field.set(obj, value);
                    field.setAccessible(flag);
                }
            }
            list.add(obj);
        }
        return list;
    }
    
    private static boolean isExistColumn(ResultSet resultSet, String columnName) {
        try {
            if (resultSet.findColumn(columnName) > 0) {
                return true;
            }
        } catch (SQLException e) {
            return false;
        }
        return false;
    }
    
    /**
     * 获取List
     * @param properties
     * @param sql
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> List<T> selectList(Properties properties, String sql, Class<T> clazz, List<String> params) {
        try (
                Connection connection = getConnection(properties);
                PreparedStatement prepareStatement = connection.prepareStatement(sql);
        ) {
            if (null != params && !params.isEmpty()) {
                for (int i = 1, len = params.size(); i <= len; i++) {
                    prepareStatement.setString(i, params.get(i - 1));
                }
            }
            ResultSet resultSet = prepareStatement.executeQuery();
            return convertResultSetToList(resultSet, clazz);
        } catch (ClassNotFoundException
                | SQLException
                | InstantiationException
                | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static <T> List<T> selectList(JdbcParam jdbcParam, String sql, Class<T> clazz) {
        return selectList(jdbcParam, sql, clazz, null);
    }
    
    public static <T> List<T> selectList(JdbcParam jdbcParam, String sql, Class<T> clazz, List<String> params) {
        try (
                Connection connection = getConnection(jdbcParam);
                PreparedStatement prepareStatement = connection.prepareStatement(sql);
        ) {
            if (null != params && !params.isEmpty()) {
                for (int i = 1, len = params.size(); i <= len; i++) {
                    prepareStatement.setString(i, params.get(i - 1));
                }
            }
            ResultSet resultSet = prepareStatement.executeQuery();
            return convertResultSetToList(resultSet, clazz);
        } catch (ClassNotFoundException
                | SQLException
                | InstantiationException
                | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 读取sql文件
     * @param properties
     * @param sqlFilePath
     * @param clazz
     * @param params
     * @param <T>
     * @return
     */
    public static <T> List<T> selectListBySqlFile(Properties properties, String sqlFilePath, Class<T> clazz, List<String> params) {
        try {
            String sql = readSqlFile(sqlFilePath);
            return selectList(properties, sql, clazz, params);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 获取单个
     * @param properties
     * @param sql
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T selectOne(Properties properties, String sql, Class<T> clazz) {
        try (
                Connection connection = getConnection(properties);
                PreparedStatement prepareStatement = connection.prepareStatement(sql);
                ResultSet resultSet = prepareStatement.executeQuery();
        ) {
            return convertResultSetToList(resultSet, clazz).stream().findFirst().orElse(null);
        } catch (ClassNotFoundException
                | SQLException
                | InstantiationException
                | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static void executeSql(JdbcParam jdbcParam, String sql) {
        try (
                Connection connection = getConnection(jdbcParam);
                PreparedStatement prepareStatement = connection.prepareStatement(sql);
        ) {
            prepareStatement.executeUpdate();
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 执行sql
     * @param connection
     * @param sql
     */
    public static void executeSql(Connection connection, String sql) {
        try (
                PreparedStatement prepareStatement = connection.prepareStatement(sql);
        ) {
            prepareStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 获取List HashMap
     * @param connection
     * @param sql
     * @return
     */
    public static List<HashMap<String, Object>> selectList(Connection connection, String sql) {
        try (
                PreparedStatement prepareStatement = connection.prepareStatement(sql);
        ) {
            ResultSet resultSet = prepareStatement.executeQuery();
            ResultSetMetaData metaData = resultSet.getMetaData();
            List<HashMap<String, Object>> contents = new ArrayList<>();
            int columnCount = metaData.getColumnCount();
            HashMap<String, Object> content;
            while (resultSet.next()) {
                content = new HashMap<>(columnCount);
                for (int i = 1; i <= columnCount; i++) {
                    content.put(metaData.getColumnLabel(i), resultSet.getObject(i));
                }
                contents.add(content);
            }
            return contents;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 获取select字段
     * @param jdbcParam
     * @param sql
     * @return
     */
    public static List<MetaDataField> getSelectMetadataFields(JdbcParam jdbcParam, String sql) {
        try (
                Connection connection = getConnection(jdbcParam);
                PreparedStatement prepareStatement = connection.prepareStatement(sql);
        ) {
            ResultSet resultSet = prepareStatement.executeQuery();
            ResultSetMetaData metaData = resultSet.getMetaData();
            List<MetaDataField> fields = new ArrayList<>();
            int columnCount = metaData.getColumnCount();
            MetaDataField metaDataField;
            for (int i = 1; i <= columnCount; i++) {
                metaDataField = new MetaDataField();
                metaDataField.setFieldName(metaData.getColumnLabel(i));
                metaDataField.setFieldSize(metaData.getColumnDisplaySize(i));
                metaDataField.setFieldType(metaData.getColumnTypeName(i));
                fields.add(metaDataField);
            }
            return fields;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static List<String> selectListString(Connection connection, String sql) {
        try (
                PreparedStatement prepareStatement = connection.prepareStatement(sql);
        ) {
            ResultSet resultSet = prepareStatement.executeQuery();
            List<String> contents = new ArrayList<>();
            while (resultSet.next()) {
                contents.add(resultSet.getString(1));
            }
            return contents;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * map list转换为mysql insert sql
     * @param contents
     * @return
     */
    public static String mapListToMysqlInsertSql(List<HashMap<String, Object>> contents, String tableName) {
        StringBuilder builder = new StringBuilder();
        List<String> keys = Lists.newArrayList(contents.get(0).keySet());
        builder.append("INSERT INTO ").append(tableName).append(" (");
        for (int i = 0, len = keys.size(); i < len; i++) {
            if (i != 0) {
                builder.append(", ");
            }
            builder.append(keys.get(i));
        }
        builder.append(") VALUES ");
        HashMap<String, Object> content;
        Object value;
        for (int i = 0, len = contents.size(); i < len; i++) {
            content = contents.get(i);
            if (i != 0) {
                builder.append(", ");
            }
            builder.append("(");
            for (int j = 0, keyLen = keys.size(); j < keyLen; j++) {
                if (j != 0) {
                    builder.append(", ");
                }
                value = content.get(keys.get(j));
                if (null == value) {
                    builder.append("NULL");
                } else {
                    builder.append("'").append(value).append("'");
                }
            }
            builder.append(")");
        }
        return builder.toString();
    }
    
    public static String convertMysqlInsertSql(List<String> keys, String tableName) {
        StringBuilder builder = new StringBuilder();
        builder.append("INSERT INTO ").append(tableName).append(" (");
        for (int i = 0, len = keys.size(); i < len; i++) {
            if (i != 0) {
                builder.append(", ");
            }
            builder.append(keys.get(i));
        }
        builder.append(") VALUES ");
        builder.append("(");
        for (int j = 0, keyLen = keys.size(); j < keyLen; j++) {
            if (j != 0) {
                builder.append(", ");
            }
            builder.append("?");
        }
        builder.append(")");
        return builder.toString();
    }
}